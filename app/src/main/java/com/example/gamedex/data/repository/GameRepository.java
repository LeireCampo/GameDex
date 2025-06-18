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
import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameListResponse;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;
import com.example.gamedex.data.remote.model.StoreListResponse;
import com.example.gamedex.data.remote.model.VideoListResponse;
import com.example.gamedex.data.model.GameWithTags;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    public LiveData<GameWithTags> getGameWithTags(String gameId) {
        return gameDao.getGameWithTags(gameId);
    }

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

    // === MÉTODOS API RAWG ===

    public LiveData<List<Game>> getPopularGames() {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        try {
            if (apiService == null) {
                Log.e(TAG, "API Service no disponible");
                games.setValue(new ArrayList<>());
                return games;
            }

            // Usar directamente el método del servicio RAWG
            apiService.getPopularGames("-rating", 20, "80,100")
                    .enqueue(new Callback<GameListResponse>() {
                        @Override
                        public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                                games.setValue(gamesList);
                                Log.d(TAG, "Juegos populares cargados: " + gamesList.size());
                            } else {
                                games.setValue(new ArrayList<>());
                                Log.e(TAG, "Error en respuesta RAWG: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<GameListResponse> call, Throwable t) {
                            games.setValue(new ArrayList<>());
                            Log.e(TAG, "Error en llamada RAWG: " + t.getMessage());
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

            // Calcular fechas para juegos recientes (últimos 3 meses)
            Calendar cal = Calendar.getInstance();
            String endDate = formatDate(cal.getTime());
            cal.add(Calendar.MONTH, -3);
            String startDate = formatDate(cal.getTime());
            String dateRange = startDate + "," + endDate;

            apiService.getRecentGames("-released", 20, dateRange)
                    .enqueue(new Callback<GameListResponse>() {
                        @Override
                        public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                                games.setValue(gamesList);
                                Log.d(TAG, "Juegos recientes cargados: " + gamesList.size());
                            } else {
                                games.setValue(new ArrayList<>());
                                Log.e(TAG, "Error en respuesta RAWG: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<GameListResponse> call, Throwable t) {
                            games.setValue(new ArrayList<>());
                            Log.e(TAG, "Error en llamada RAWG: " + t.getMessage());
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

            // Calcular fechas para próximos lanzamientos (próximos 6 meses)
            Calendar cal = Calendar.getInstance();
            String startDate = formatDate(cal.getTime());
            cal.add(Calendar.MONTH, 6);
            String endDate = formatDate(cal.getTime());
            String dateRange = startDate + "," + endDate;

            apiService.getUpcomingGames("released", 15, dateRange)
                    .enqueue(new Callback<GameListResponse>() {
                        @Override
                        public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                                games.setValue(gamesList);
                                Log.d(TAG, "Próximos lanzamientos cargados: " + gamesList.size());
                            } else {
                                games.setValue(new ArrayList<>());
                                Log.e(TAG, "Error cargando próximos lanzamientos: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<GameListResponse> call, Throwable t) {
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

            apiService.getGameDetails(gameId).enqueue(new Callback<GameDetailResponse>() {
                @Override
                public void onResponse(Call<GameDetailResponse> call, Response<GameDetailResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        GameDetailResponse gameResponse = response.body();
                        Game gameEntity = GameMapper.fromDetailResponseToEntity(gameResponse);
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
                public void onFailure(Call<GameDetailResponse> call, Throwable t) {
                    gameData.setValue(null);
                    Log.e(TAG, "Error en llamada RAWG para detalles: " + t.getMessage());
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

        try {
            if (apiService == null) {
                screenshots.setValue(new ArrayList<>());
                return screenshots;
            }

            apiService.getGameScreenshots(gameId, 1, 10)
                    .enqueue(new Callback<ScreenshotListResponse>() {
                        @Override
                        public void onResponse(Call<ScreenshotListResponse> call, Response<ScreenshotListResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                screenshots.setValue(response.body().getResults());
                                Log.d(TAG, "Screenshots cargadas: " + response.body().getResults().size());
                            } else {
                                screenshots.setValue(new ArrayList<>());
                                Log.e(TAG, "Error cargando screenshots: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<ScreenshotListResponse> call, Throwable t) {
                            screenshots.setValue(new ArrayList<>());
                            Log.e(TAG, "Error en screenshots: " + t.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error en getGameScreenshots: " + e.getMessage());
            screenshots.setValue(new ArrayList<>());
        }

        return screenshots;
    }

    public LiveData<List<VideoListResponse.Video>> getGameVideos(String gameId) {
        MutableLiveData<List<VideoListResponse.Video>> videos = new MutableLiveData<>();
        // RAWG no tiene endpoint de videos, devolver lista vacía
        videos.setValue(new ArrayList<>());
        return videos;
    }

    public LiveData<List<StoreListResponse.GameStore>> getGameStores(String gameId) {
        MutableLiveData<List<StoreListResponse.GameStore>> stores = new MutableLiveData<>();

        try {
            if (apiService == null) {
                stores.setValue(new ArrayList<>());
                return stores;
            }

            apiService.getGameStores(gameId, 1, 10)
                    .enqueue(new Callback<StoreListResponse>() {
                        @Override
                        public void onResponse(Call<StoreListResponse> call, Response<StoreListResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                stores.setValue(response.body().getResults());
                                Log.d(TAG, "Tiendas cargadas: " + response.body().getResults().size());
                            } else {
                                stores.setValue(new ArrayList<>());
                                Log.e(TAG, "Error cargando tiendas: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<StoreListResponse> call, Throwable t) {
                            stores.setValue(new ArrayList<>());
                            Log.e(TAG, "Error en tiendas: " + t.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error en getGameStores: " + e.getMessage());
            stores.setValue(new ArrayList<>());
        }

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

            apiService.searchGames(searchQuery, 1, 20)
                    .enqueue(new Callback<GameListResponse>() {
                        @Override
                        public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                                searchResults.setValue(gamesList);
                                Log.d(TAG, "Búsqueda completada: " + gamesList.size() + " juegos encontrados");
                            } else {
                                searchResults.setValue(new ArrayList<>());
                                Log.e(TAG, "Error en búsqueda RAWG: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<GameListResponse> call, Throwable t) {
                            searchResults.setValue(new ArrayList<>());
                            Log.e(TAG, "Error en búsqueda RAWG: " + t.getMessage());
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
        defaultPlatforms.add("PlayStation 4");
        defaultPlatforms.add("Xbox One");
        defaultPlatforms.add("iOS");
        defaultPlatforms.add("Android");
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
        defaultGenres.add("Racing");
        defaultGenres.add("Shooter");
        defaultGenres.add("Simulation");
        defaultGenres.add("Puzzle");
        defaultGenres.add("Fighting");
        genres.setValue(defaultGenres);
        return genres;
    }

    public LiveData<List<Game>> searchGamesWithFilters(String searchQuery, String platform,
                                                       String genre, String yearFrom, String yearTo) {
        MutableLiveData<List<Game>> searchResults = new MutableLiveData<>();

        try {
            if (apiService == null) {
                searchResults.setValue(new ArrayList<>());
                return searchResults;
            }

            // Convertir plataforma a ID de RAWG
            String platformIds = getPlatformId(platform);
            // Convertir género a ID de RAWG
            String genreIds = getGenreId(genre);

            // Construir rango de fechas
            String dateRange = null;
            if (yearFrom != null && !yearFrom.isEmpty() || yearTo != null && !yearTo.isEmpty()) {
                String fromDate = (yearFrom != null && !yearFrom.isEmpty()) ? yearFrom + "-01-01" : "1970-01-01";
                String toDate = (yearTo != null && !yearTo.isEmpty()) ? yearTo + "-12-31" : "2030-12-31";
                dateRange = fromDate + "," + toDate;
            }

            apiService.searchGamesAdvanced(
                    searchQuery,
                    platformIds,
                    genreIds,
                    dateRange,
                    null, // metacritic
                    "-rating",
                    1,
                    20
            ).enqueue(new Callback<GameListResponse>() {
                @Override
                public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                        searchResults.setValue(gamesList);
                        Log.d(TAG, "Búsqueda con filtros completada: " + gamesList.size() + " juegos encontrados");
                    } else {
                        searchResults.setValue(new ArrayList<>());
                        Log.e(TAG, "Error en búsqueda con filtros: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<GameListResponse> call, Throwable t) {
                    searchResults.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en búsqueda con filtros: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error en searchGamesWithFilters: " + e.getMessage());
            searchResults.setValue(new ArrayList<>());
        }

        return searchResults;
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

    // === MÉTODOS AUXILIARES ===

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    // Mapeo básico de plataformas a IDs de RAWG
    private String getPlatformId(String platformName) {
        if (platformName == null || platformName.isEmpty()) {
            return null;
        }

        switch (platformName.toLowerCase()) {
            case "pc": return "4";
            case "playstation 5": return "187";
            case "xbox series x": return "186";
            case "nintendo switch": return "7";
            case "playstation 4": return "18";
            case "xbox one": return "1";
            case "ios": return "3";
            case "android": return "21";
            default: return null;
        }
    }

    // Mapeo básico de géneros a IDs de RAWG
    private String getGenreId(String genreName) {
        if (genreName == null || genreName.isEmpty()) {
            return null;
        }

        switch (genreName.toLowerCase()) {
            case "action": return "4";
            case "adventure": return "3";
            case "rpg": return "5";
            case "strategy": return "10";
            case "sports": return "15";
            case "racing": return "1";
            case "shooter": return "2";
            case "simulation": return "14";
            case "puzzle": return "7";
            case "fighting": return "6";
            default: return null;
        }
    }
}