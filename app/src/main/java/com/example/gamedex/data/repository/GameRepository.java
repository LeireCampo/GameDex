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
import com.example.gamedex.data.remote.RAWGApiService;
import com.example.gamedex.data.remote.RAWGRetrofitClient;
import com.example.gamedex.data.remote.model.GameResponse;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;
import com.example.gamedex.data.remote.model.StoreListResponse;
import com.example.gamedex.data.remote.model.VideoListResponse;

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
    private final RAWGApiService apiService;
    private final FirestoreService firestoreService;
    private final FirebaseAuthService authService;

    public GameRepository(Application application) {
        try {
            GameDexDatabase db = GameDexDatabase.getDatabase(application);
            gameDao = db.gameDao();
            executorService = Executors.newSingleThreadExecutor();

            // Inicializar servicios con manejo de errores
            try {
                apiService = RAWGRetrofitClient.getClient().create(RAWGApiService.class);
            } catch (Exception e) {
                Log.e(TAG, "Error inicializando API service: " + e.getMessage());
                throw new RuntimeException("Error inicializando API service", e);
            }

            try {
                firestoreService = FirestoreService.getInstance();
                authService = FirebaseAuthService.getInstance();
            } catch (Exception e) {
                Log.e(TAG, "Error inicializando Firebase services: " + e.getMessage());
                throw new RuntimeException("Error inicializando Firebase services", e);
            }

            Log.d(TAG, "GameRepository inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error en constructor GameRepository: " + e.getMessage());
            throw new RuntimeException("Error inicializando GameRepository", e);
        }
    }

    // === MÉTODOS LOCALES CON FIREBASE ===

    public LiveData<List<Game>> getAllLibraryGames() {
        try {
            if (authService != null && authService.isUserSignedIn()) {
                return getCombinedLibraryGames();
            } else {
                return gameDao.getAllLibraryGames();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en getAllLibraryGames: " + e.getMessage());
            return new MutableLiveData<>(new ArrayList<>());
        }
    }

    private LiveData<List<Game>> getCombinedLibraryGames() {
        MediatorLiveData<List<Game>> mediatorLiveData = new MediatorLiveData<>();

        try {
            LiveData<List<Game>> localGames = gameDao.getAllLibraryGames();
            mediatorLiveData.addSource(localGames, mediatorLiveData::setValue);

            if (firestoreService != null) {
                LiveData<List<Game>> firebaseGames = firestoreService.getUserGamesFromFirestore();
                mediatorLiveData.addSource(firebaseGames, games -> {
                    if (games != null && !games.isEmpty()) {
                        executorService.execute(() -> {
                            try {
                                for (Game game : games) {
                                    Game existingGame = gameDao.getGameById(game.getId());
                                    if (existingGame == null || game.getLastUpdated() > existingGame.getLastUpdated()) {
                                        gameDao.insertGame(game);
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error sincronizando juegos de Firebase: " + e.getMessage());
                            }
                        });
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "Error en getCombinedLibraryGames: " + e.getMessage());
        }

        return mediatorLiveData;
    }

    public void insertGame(Game game) {
        executorService.execute(() -> {
            try {
                gameDao.insertGame(game);
                if (authService != null && authService.isUserSignedIn() && game.isInLibrary()) {
                    syncGameToFirebase(game);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error insertando juego: " + e.getMessage());
            }
        });
    }

    public void updateGame(Game game) {
        executorService.execute(() -> {
            try {
                game.setLastUpdated(System.currentTimeMillis());
                gameDao.updateGame(game);
                if (authService != null && authService.isUserSignedIn()) {
                    syncGameToFirebase(game);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error actualizando juego: " + e.getMessage());
            }
        });
    }

    public void deleteGame(Game game) {
        executorService.execute(() -> {
            try {
                gameDao.deleteGame(game);
                if (authService != null && authService.isUserSignedIn()) {
                    game.setInLibrary(false);
                    syncGameToFirebase(game);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error eliminando juego: " + e.getMessage());
            }
        });
    }

    private void syncGameToFirebase(Game game) {
        if (firestoreService != null) {
            firestoreService.syncGameToFirestore(game, new FirestoreService.FirestoreCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d(TAG, "Juego sincronizado con Firebase: " + game.getTitle());
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Error al sincronizar juego: " + error);
                }
            });
        }
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

        try {
            if (apiService == null) {
                Log.e(TAG, "API Service no disponible");
                games.setValue(new ArrayList<>());
                return games;
            }

            String query = RAWGApiService.RAWGQueryBuilder.getPopularGamesQuery(20, 0);

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
        } catch (Exception e) {
            Log.e(TAG, "Error en getPopularGames: " + e.getMessage());
            games.setValue(new ArrayList<>());
        }

        return games;
    }

    public LiveData<List<Game>> getRecentGames() {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        try {
            if (apiService == null) {
                Log.e(TAG, "API Service no disponible");
                games.setValue(new ArrayList<>());
                return games;
            }

            String query = RAWGApiService.RAWGQueryBuilder.getRecentGamesQuery(20, 0);

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
        } catch (Exception e) {
            Log.e(TAG, "Error en getRecentGames: " + e.getMessage());
            games.setValue(new ArrayList<>());
        }

        return games;
    }

    public LiveData<List<Game>> getUpcomingGames() {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        try {
            if (apiService == null) {
                Log.e(TAG, "API Service no disponible");
                games.setValue(new ArrayList<>());
                return games;
            }

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
        } catch (Exception e) {
            Log.e(TAG, "Error en getUpcomingGames: " + e.getMessage());
            games.setValue(new ArrayList<>());
        }

        return games;
    }

    // === MÉTODOS ADICIONALES ===

    public LiveData<Game> getGameDetailsOnline(String gameId) {
        MutableLiveData<Game> gameData = new MutableLiveData<>();

        try {
            if (apiService == null) {
                Log.e(TAG, "API Service no disponible para detalles");
                gameData.setValue(null);
                return gameData;
            }

            String query = GameApiService.IGDBQueryBuilder.getGameDetailsQuery(gameId);

            apiService.getGameDetails(query).enqueue(new Callback<List<GameResponse>>() {
                @Override
                public void onResponse(Call<List<GameResponse>> call, Response<List<GameResponse>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        GameResponse gameResponse = response.body().get(0);
                        Game gameEntity = GameMapper.fromResponseToEntity(gameResponse);
                        gameData.setValue(gameEntity);

                        // Guardar en base de datos local
                        executorService.execute(() -> {
                            try {
                                gameDao.insertGame(gameEntity);
                            } catch (Exception e) {
                                Log.e(TAG, "Error guardando detalles del juego: " + e.getMessage());
                            }
                        });

                        Log.d(TAG, "Detalles del juego cargados: " + gameEntity.getTitle());
                    } else {
                        gameData.setValue(null);
                        Log.w(TAG, "No se encontraron detalles para el juego ID: " + gameId);
                    }
                }

                @Override
                public void onFailure(Call<List<GameResponse>> call, Throwable t) {
                    gameData.setValue(null);
                    Log.e(TAG, "Error en llamada IGDB para detalles: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error en getGameDetailsOnline: " + e.getMessage());
            gameData.setValue(null);
        }

        return gameData;
    }

    public LiveData<List<ScreenshotListResponse.Screenshot>> getGameScreenshots(String gameId) {
        MutableLiveData<List<ScreenshotListResponse.Screenshot>> screenshots = new MutableLiveData<>();
        screenshots.setValue(new ArrayList<>());
        return screenshots;
    }

    public LiveData<List<VideoListResponse.Video>> getGameVideos(String gameId) {
        MutableLiveData<List<VideoListResponse.Video>> videos = new MutableLiveData<>();
        videos.setValue(new ArrayList<>());
        return videos;
    }

    public LiveData<List<StoreListResponse.GameStore>> getGameStores(String gameId) {
        MutableLiveData<List<StoreListResponse.GameStore>> stores = new MutableLiveData<>();
        stores.setValue(new ArrayList<>());
        return stores;
    }

    public LiveData<List<Game>> searchGamesOnline(String searchQuery) {
        MutableLiveData<List<Game>> searchResults = new MutableLiveData<>();

        try {
            if (apiService == null) {
                Log.e(TAG, "API Service no disponible para búsqueda");
                searchResults.setValue(new ArrayList<>());
                return searchResults;
            }

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
        } catch (Exception e) {
            Log.e(TAG, "Error en searchGamesOnline: " + e.getMessage());
            searchResults.setValue(new ArrayList<>());
        }

        return searchResults;
    }

    public LiveData<List<String>> getPlatforms() {
        MutableLiveData<List<String>> platforms = new MutableLiveData<>();
        List<String> defaultPlatforms = new ArrayList<>();
        defaultPlatforms.add("PC");
        defaultPlatforms.add("PlayStation 5");
        defaultPlatforms.add("Xbox Series X");
        defaultPlatforms.add("Nintendo Switch");
        platforms.setValue(defaultPlatforms);
        return platforms;
    }

    public LiveData<List<String>> getGenres() {
        MutableLiveData<List<String>> genres = new MutableLiveData<>();
        List<String> defaultGenres = new ArrayList<>();
        defaultGenres.add("Action");
        defaultGenres.add("Adventure");
        defaultGenres.add("RPG");
        defaultGenres.add("Strategy");
        defaultGenres.add("Sports");
        genres.setValue(defaultGenres);
        return genres;
    }

    public LiveData<List<Game>> searchGamesWithFilters(String searchQuery, String platform,
                                                       String genre, String yearFrom, String yearTo) {
        // Por ahora, usar búsqueda básica
        return searchGamesOnline(searchQuery);
    }

    public void syncLibraryWithFirebase() {
        if (authService == null || !authService.isUserSignedIn()) {
            Log.w(TAG, "Usuario no autenticado, no se puede sincronizar");
            return;
        }

        executorService.execute(() -> {
            try {
                List<Game> localLibraryGames = gameDao.getAllLibraryGamesSync();
                for (Game game : localLibraryGames) {
                    syncGameToFirebase(game);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sincronizando biblioteca: " + e.getMessage());
            }
        });
    }
}