package com.example.gamedex.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamedex.data.firebase.FirebaseAuthService;
import com.example.gamedex.data.firebase.FirestoreService;
import com.example.gamedex.data.local.dao.GameDao;
import com.example.gamedex.data.local.database.GameDexDatabase;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.mapper.GameMapper;
import com.example.gamedex.data.model.GameWithTags;
import com.example.gamedex.data.remote.GameApiService;
import com.example.gamedex.data.remote.IGDBRetrofitClient;
import com.example.gamedex.data.remote.model.GameResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GameRepository {
    private static final String TAG = "GameRepository";

    private final GameDao gameDao;
    private final ExecutorService executorService;
    private final GameApiService apiService;
    private final FirestoreService firestoreService;
    private final FirebaseAuthService authService;

    public GameRepository(Application application) {
        GameDexDatabase db = GameDexDatabase.getDatabase(application);
        gameDao = db.gameDao();
        executorService = Executors.newSingleThreadExecutor();
        apiService = IGDBRetrofitClient.getClient().create(GameApiService.class);
        firestoreService = FirestoreService.getInstance();
        authService = FirebaseAuthService.getInstance();
    }

    // === MÉTODOS LOCALES CON FIREBASE ===

    public LiveData<List<Game>> getAllLibraryGames() {
        if (authService.isUserSignedIn()) {
            return getCombinedLibraryGames();
        } else {
            return gameDao.getAllLibraryGames();
        }
    }

    private LiveData<List<Game>> getCombinedLibraryGames() {
        MediatorLiveData<List<Game>> mediatorLiveData = new MediatorLiveData<>();
        LiveData<List<Game>> localGames = gameDao.getAllLibraryGames();
        LiveData<List<Game>> firebaseGames = firestoreService.getUserGamesFromFirestore();

        mediatorLiveData.addSource(localGames, mediatorLiveData::setValue);
        mediatorLiveData.addSource(firebaseGames, games -> {
            if (games != null && !games.isEmpty()) {
                executorService.execute(() -> {
                    for (Game game : games) {
                        Game existingGame = gameDao.getGameById(game.getId());
                        if (existingGame == null || game.getLastUpdated() > existingGame.getLastUpdated()) {
                            gameDao.insertGame(game);
                        }
                    }
                });
            }
        });

        return mediatorLiveData;
    }

    public void insertGame(Game game) {
        executorService.execute(() -> {
            gameDao.insertGame(game);
            if (authService.isUserSignedIn() && game.isInLibrary()) {
                syncGameToFirebase(game);
            }
        });
    }

    public void updateGame(Game game) {
        executorService.execute(() -> {
            game.setLastUpdated(System.currentTimeMillis());
            gameDao.updateGame(game);
            if (authService.isUserSignedIn()) {
                syncGameToFirebase(game);
            }
        });
    }

    public void deleteGame(Game game) {
        executorService.execute(() -> {
            gameDao.deleteGame(game);
            if (authService.isUserSignedIn()) {
                game.setInLibrary(false);
                syncGameToFirebase(game);
            }
        });
    }

    private void syncGameToFirebase(Game game) {
        firestoreService.syncGameToFirestore(game, new FirestoreService.FirestoreCallback<Void>() {
            @Override
            public void onFailure(Call<List<GameResponse>> call, Throwable t) {
                searchResults.setValue(new ArrayList<>());
                Log.e(TAG, "Error en búsqueda con filtros: " + t.getMessage());
            }
        });

        return searchResults;
    }

    // === MÉTODOS PARA CAPTURAS DE PANTALLA ===

    public LiveData<List<String>> getGameScreenshots(String gameId) {
        MutableLiveData<List<String>> screenshots = new MutableLiveData<>();

        String query = GameApiService.IGDBQueryBuilder.getScreenshotsQuery(gameId);

        apiService.getGameScreenshots(query).enqueue(new Callback<List<GameResponse.Screenshot>>() {
            @Override
            public void onResponse(Call<List<GameResponse.Screenshot>> call, Response<List<GameResponse.Screenshot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> screenshotUrls = new ArrayList<>();
                    for (GameResponse.Screenshot screenshot : response.body()) {
                        if (screenshot.getImageUrl() != null) {
                            screenshotUrls.add(screenshot.getImageUrl());
                        }
                    }
                    screenshots.setValue(screenshotUrls);
                    Log.d(TAG, "Capturas cargadas: " + screenshotUrls.size());
                } else {
                    screenshots.setValue(new ArrayList<>());
                    Log.e(TAG, "Error cargando capturas: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GameResponse.Screenshot>> call, Throwable t) {
                screenshots.setValue(new ArrayList<>());
                Log.e(TAG, "Error en capturas: " + t.getMessage());
            }
        });

        return screenshots;
    }

    // === MÉTODOS POR PLATAFORMA ===

    public LiveData<List<Game>> getGamesByPlatform(String platformName) {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        int platformId = getPlatformId(platformName);
        if (platformId == -1) {
            games.setValue(new ArrayList<>());
            return games;
        }

        String query = GameApiService.IGDBQueryBuilder.getGamesByPlatformQuery(platformId, 20, 0);

        apiService.getGames(query).enqueue(new Callback<List<GameResponse>>() {
            @Override
            public void onResponse(Call<List<GameResponse>> call, Response<List<GameResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body());
                    games.setValue(gamesList);
                    Log.d(TAG, "Juegos de " + platformName + " cargados: " + gamesList.size());
                } else {
                    games.setValue(new ArrayList<>());
                    Log.e(TAG, "Error cargando juegos de " + platformName + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GameResponse>> call, Throwable t) {
                games.setValue(new ArrayList<>());
                Log.e(TAG, "Error en llamada para " + platformName + ": " + t.getMessage());
            }
        });

        return games;
    }

    // === MÉTODOS AUXILIARES ===

    private int getPlatformId(String platformName) {
        switch (platformName.toLowerCase()) {
            case "pc":
            case "windows":
                return 6; // PC (Microsoft Windows)
            case "playstation":
            case "ps4":
                return 48; // PlayStation 4
            case "ps5":
            case "playstation 5":
                return 167; // PlayStation 5
            case "xbox one":
                return 49; // Xbox One
            case "xbox series x":
            case "xbox series s":
                return 169; // Xbox Series X|S
            case "nintendo switch":
            case "switch":
                return 130; // Nintendo Switch
            case "ios":
                return 39; // iOS
            case "android":
                return 34; // Android
            case "mac":
            case "macos":
                return 14; // Mac
            case "linux":
                return 3; // Linux
            default:
                Log.w(TAG, "Plataforma no reconocida: " + platformName);
                return -1;
        }
    }

    public void syncLibraryWithFirebase() {
        if (!authService.isUserSignedIn()) {
            Log.w(TAG, "Usuario no autenticado, no se puede sincronizar");
            return;
        }

        executorService.execute(() -> {
            List<Game> localLibraryGames = gameDao.getAllLibraryGamesSync();
            for (Game game : localLibraryGames) {
                syncGameToFirebase(game);
            }
        });
    }
}
public void onSuccess(Void result) {
    Log.d(TAG, "Juego sincronizado con Firebase: " + game.getTitle());
}

@Override
public void onError(String error) {
    Log.e(TAG, "Error al sincronizar juego: " + error);
}
        });
                }

// === MÉTODOS LOCALES EXISTENTES ===

public LiveData<GameWithTags> getGameWithTags(String gameId) {
    return gameDao.getGameWithTags(gameId);
}

public Game getGameById(String gameId) {
    return gameDao.getGameById(gameId);
}

public LiveData<List<Game>> searchGames(String query) {
    return gameDao.searchGames(query);
}

public LiveData<List<GameWithTags>> getLibraryGamesWithTags() {
    return gameDao.getLibraryGamesWithTags();
}

public LiveData<List<Game>> getGamesByStatus(String status) {
    return gameDao.getGamesByStatus(status);
}

// === MÉTODOS API IGDB ===

public LiveData<List<Game>> getPopularGames() {
    MutableLiveData<List<Game>> games = new MutableLiveData<>();

    String query = GameApiService.IGDBQueryBuilder.getPopularGamesQuery(20, 0);

    apiService.getGames(query).enqueue(new Callback<List<GameResponse>>() {
        @Override
        public void onResponse(Call<List<GameResponse>> call, Response<List<GameResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body());
                games.setValue(gamesList);
                Log.d(TAG, "Juegos populares cargados: " + gamesList.size());
            } else {
                games.setValue(new ArrayList<>());
                Log.e(TAG, "Error en respuesta IGDB: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<List<GameResponse>> call, Throwable t) {
            games.setValue(new ArrayList<>());
            Log.e(TAG, "Error en llamada IGDB: " + t.getMessage());
        }
    });

    return games;
}

public LiveData<List<Game>> getRecentGames() {
    MutableLiveData<List<Game>> games = new MutableLiveData<>();

    String query = GameApiService.IGDBQueryBuilder.getRecentGamesQuery(20, 0);

    apiService.getGames(query).enqueue(new Callback<List<GameResponse>>() {
        @Override
        public void onResponse(Call<List<GameResponse>> call, Response<List<GameResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body());
                games.setValue(gamesList);
                Log.d(TAG, "Juegos recientes cargados: " + gamesList.size());
            } else {
                games.setValue(new ArrayList<>());
                Log.e(TAG, "Error en respuesta IGDB: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<List<GameResponse>> call, Throwable t) {
            games.setValue(new ArrayList<>());
            Log.e(TAG, "Error en llamada IGDB: " + t.getMessage());
        }
    });

    return games;
}

public LiveData<List<Game>> getUpcomingGames() {
    MutableLiveData<List<Game>> games = new MutableLiveData<>();

    String query = GameApiService.IGDBQueryBuilder.getUpcomingGamesQuery(15, 0);

    apiService.getGames(query).enqueue(new Callback<List<GameResponse>>() {
        @Override
        public void onResponse(Call<List<GameResponse>> call, Response<List<GameResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body());
                games.setValue(gamesList);
                Log.d(TAG, "Próximos lanzamientos cargados: " + gamesList.size());
            } else {
                games.setValue(new ArrayList<>());
                Log.e(TAG, "Error cargando próximos lanzamientos: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<List<GameResponse>> call, Throwable t) {
            games.setValue(new ArrayList<>());
            Log.e(TAG, "Error en próximos lanzamientos: " + t.getMessage());
        }
    });

    return games;
}

public LiveData<Game> getGameDetailsOnline(String gameId) {
    MutableLiveData<Game> gameData = new MutableLiveData<>();

    String query = GameApiService.IGDBQueryBuilder.getGameDetailsQuery(gameId);

    apiService.getGameDetails(query).enqueue(new Callback<List<GameResponse>>() {
        @Override
        public void onResponse(Call<List<GameResponse>> call, Response<List<GameResponse>> response) {
            if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                GameResponse gameResponse = response.body().get(0);
                Game gameEntity = GameMapper.fromResponseToEntity(gameResponse);
                gameData.setValue(gameEntity);

                // Guardar en base de datos local
                executorService.execute(() -> gameDao.insertGame(gameEntity));

                Log.d(TAG, "Detalles del juego cargados: " + gameEntity.getTitle());
            } else {
                gameData.setValue(null);
                Log.w(TAG, "No se encontraron detalles para el juego ID: " + gameId);
            }
        }

        @Override
        public void onFailure(Call<List<GameResponse>> call, Throwable t) {
            gameData.setValue(null);
            Log.e(TAG, "Error en llamada IGDB: " + t.getMessage());
        }
    });

    return gameData;
}

public LiveData<List<Game>> searchGamesOnline(String searchQuery) {
    MutableLiveData<List<Game>> searchResults = new MutableLiveData<>();

    String query = GameApiService.IGDBQueryBuilder.getSearchGamesQuery(searchQuery, 20, 0);

    apiService.searchGames(query).enqueue(new Callback<List<GameResponse>>() {
        @Override
        public void onResponse(Call<List<GameResponse>> call, Response<List<GameResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body());
                searchResults.setValue(gamesList);
                Log.d(TAG, "Búsqueda completada: " + gamesList.size() + " juegos encontrados");
            } else {
                searchResults.setValue(new ArrayList<>());
                Log.e(TAG, "Error en búsqueda IGDB: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<List<GameResponse>> call, Throwable t) {
            searchResults.setValue(new ArrayList<>());
            Log.e(TAG, "Error en búsqueda IGDB: " + t.getMessage());
        }
    });

    return searchResults;
}

// === MÉTODOS PARA FILTROS AVANZADOS ===

public LiveData<List<String>> getPlatforms() {
    MutableLiveData<List<String>> platforms = new MutableLiveData<>();

    String query = GameApiService.IGDBQueryBuilder.getPlatformsQuery(50);

    apiService.getPlatforms(query).enqueue(new Callback<List<GameApiService.Platform>>() {
        @Override
        public void onResponse(Call<List<GameApiService.Platform>> call, Response<List<GameApiService.Platform>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<String> platformNames = new ArrayList<>();
                platformNames.add("Todas las plataformas");

                for (GameApiService.Platform platform : response.body()) {
                    platformNames.add(platform.name);
                }

                platforms.setValue(platformNames);
                Log.d(TAG, "Plataformas cargadas: " + platformNames.size());
            } else {
                platforms.setValue(new ArrayList<>());
                Log.e(TAG, "Error cargando plataformas: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<List<GameApiService.Platform>> call, Throwable t) {
            platforms.setValue(new ArrayList<>());
            Log.e(TAG, "Error en plataformas: " + t.getMessage());
        }
    });

    return platforms;
}

public LiveData<List<String>> getGenres() {
    MutableLiveData<List<String>> genres = new MutableLiveData<>();

    String query = GameApiService.IGDBQueryBuilder.getGenresQuery(50);

    apiService.getGenres(query).enqueue(new Callback<List<GameApiService.Genre>>() {
        @Override
        public void onResponse(Call<List<GameApiService.Genre>> call, Response<List<GameApiService.Genre>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<String> genreNames = new ArrayList<>();
                genreNames.add("Todos los géneros");

                for (GameApiService.Genre genre : response.body()) {
                    genreNames.add(genre.name);
                }

                genres.setValue(genreNames);
                Log.d(TAG, "Géneros cargados: " + genreNames.size());
            } else {
                genres.setValue(new ArrayList<>());
                Log.e(TAG, "Error cargando géneros: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<List<GameApiService.Genre>> call, Throwable t) {
            genres.setValue(new ArrayList<>());
            Log.e(TAG, "Error en géneros: " + t.getMessage());
        }
    });

    return genres;
}

public LiveData<List<Game>> searchGamesWithFilters(String searchQuery, String platform,
                                                   String genre, String yearFrom, String yearTo) {
    MutableLiveData<List<Game>> searchResults = new MutableLiveData<>();

    // Convertir años a timestamps Unix si se proporcionan
    Long dateFrom = null;
    Long dateTo = null;

    if (yearFrom != null && !yearFrom.isEmpty()) {
        try {
            dateFrom = java.time.LocalDate.of(Integer.parseInt(yearFrom), 1, 1)
                    .atStartOfDay(java.time.ZoneOffset.UTC).toEpochSecond();
        } catch (Exception e) {
            Log.e(TAG, "Error parseando año desde: " + yearFrom);
        }
    }

    if (yearTo != null && !yearTo.isEmpty()) {
        try {
            dateTo = java.time.LocalDate.of(Integer.parseInt(yearTo), 12, 31)
                    .atTime(23, 59, 59).toEpochSecond(java.time.ZoneOffset.UTC);
        } catch (Exception e) {
            Log.e(TAG, "Error parseando año hasta: " + yearTo);
        }
    }

    // Por simplicidad, usar búsqueda normal por ahora
    // En una implementación completa, necesitarías mapear nombres a IDs
    String query = GameApiService.IGDBQueryBuilder.getSearchGamesQuery(
            searchQuery != null ? searchQuery : "", 20, 0);

    apiService.searchGames(query).enqueue(new Callback<List<GameResponse>>() {
        @Override
        public void onResponse(Call<List<GameResponse>> call, Response<List<GameResponse>> response) {
            if (response.isSuccessful() && response.body() != null) {
                List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body());
                searchResults.setValue(gamesList);
                Log.d(TAG, "Búsqueda con filtros completada: " + gamesList.size());
            } else {
                searchResults.setValue(new ArrayList<>());
                Log.e(TAG, "Error en búsqueda con filtros: " + response.code());
            }
        }

        @Override