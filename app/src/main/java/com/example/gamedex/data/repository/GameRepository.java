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
import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameListResponse;
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

/**
 * GameRepository actualizado para usar IGDB API
 * Mantiene toda la funcionalidad existente pero con mejor cobertura de plataformas
 */
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
        // Cambiamos a IGDB API
        apiService = IGDBRetrofitClient.getClient().create(GameApiService.class);
        firestoreService = FirestoreService.getInstance();
        authService = FirebaseAuthService.getInstance();
    }

    // === MÉTODOS LOCALES CON FIREBASE (SIN CAMBIOS) ===

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

        mediatorLiveData.addSource(localGames, games -> {
            mediatorLiveData.setValue(games);
        });

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
                firestoreService.syncGameToFirestore(game, new FirestoreService.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Juego sincronizado con Firebase: " + game.getTitle());
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error al sincronizar juego con Firebase: " + error);
                    }
                });
            }
        });
    }

    public void updateGame(Game game) {
        executorService.execute(() -> {
            game.setLastUpdated(System.currentTimeMillis());
            gameDao.updateGame(game);

            if (authService.isUserSignedIn()) {
                firestoreService.syncGameToFirestore(game, new FirestoreService.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Juego actualizado en Firebase: " + game.getTitle());
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error al actualizar juego en Firebase: " + error);
                    }
                });
            }
        });
    }

    public void deleteGame(Game game) {
        executorService.execute(() -> {
            gameDao.deleteGame(game);

            if (authService.isUserSignedIn()) {
                game.setInLibrary(false);
                firestoreService.syncGameToFirestore(game, new FirestoreService.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Juego eliminado de Firebase: " + game.getTitle());
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error al eliminar juego de Firebase: " + error);
                    }
                });
            }
        });
    }

    public void syncLibraryWithFirebase() {
        if (!authService.isUserSignedIn()) {
            Log.w(TAG, "Usuario no autenticado, no se puede sincronizar");
            return;
        }

        executorService.execute(() -> {
            List<Game> localLibraryGames = gameDao.getAllLibraryGamesSync();
            for (Game game : localLibraryGames) {
                firestoreService.syncGameToFirestore(game, new FirestoreService.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.d(TAG, "Biblioteca sincronizada con Firebase");
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Error al sincronizar biblioteca: " + error);
                    }
                });
            }
        });
    }

    // === MÉTODOS LOCALES EXISTENTES (SIN CAMBIOS) ===

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

    // === MÉTODOS API ACTUALIZADOS PARA IGDB ===

    // Juegos populares con mejor cobertura de plataformas
    public LiveData<List<Game>> getPopularGames() {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        String query = GameApiService.IGDBQueryBuilder.getPopularGamesQuery(10, 0);

        apiService.getGames(query).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // IGDB devuelve directamente una lista, no un objeto con results
                    List<GameResponse> igdbGames = (List<GameResponse>) response.body();
                    GameListResponse gameListResponse = new GameListResponse(igdbGames);

                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(gameListResponse.getResults());
                    games.setValue(gamesList);

                    Log.d(TAG, "Juegos populares cargados: " + gamesList.size() + " juegos de múltiples plataformas");
                } else {
                    games.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta IGDB: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                games.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a IGDB API: " + t.getMessage());
            }
        });

        return games;
    }

    // Juegos recientes de todas las plataformas
    public LiveData<List<Game>> getRecentGames() {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        String query = GameApiService.IGDBQueryBuilder.getRecentGamesQuery(10, 0);

        apiService.getGames(query).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GameResponse> igdbGames = (List<GameResponse>) response.body();
                    GameListResponse gameListResponse = new GameListResponse(igdbGames);

                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(gameListResponse.getResults());
                    games.setValue(gamesList);

                    Log.d(TAG, "Juegos recientes cargados: " + gamesList.size() + " juegos multiplataforma");
                } else {
                    games.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta IGDB: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                games.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a IGDB API: " + t.getMessage());
            }
        });

        return games;
    }

    // Detalles completos de un juego con todas las plataformas
    public LiveData<Game> getGameDetailsOnline(String gameId) {
        MutableLiveData<Game> gameData = new MutableLiveData<>();

        String query = GameApiService.IGDBQueryBuilder.getGameDetailsQuery(gameId);

        apiService.getGameDetails(query).enqueue(new Callback<GameDetailResponse>() {
            @Override
            public void onResponse(Call<GameDetailResponse> call, Response<GameDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // IGDB devuelve un array, tomamos el primer elemento
                    List<GameResponse> igdbGames = (List<GameResponse>) response.body();
                    if (!igdbGames.isEmpty()) {
                        GameResponse gameResponse = igdbGames.get(0);
                        Game gameEntity = GameMapper.fromResponseToEntity(gameResponse);
                        gameData.setValue(gameEntity);

                        // Guardar en la base de datos local para acceso offline
                        executorService.execute(() -> gameDao.insertGame(gameEntity));

                        Log.d(TAG, "Detalles del juego cargados: " + gameEntity.getTitle() +
                                " (" + (gameResponse.getPlatforms() != null ? gameResponse.getPlatforms().size() : 0) + " plataformas)");
                    } else {
                        gameData.setValue(null);
                        Log.w(TAG, "No se encontraron detalles para el juego ID: " + gameId);
                    }
                } else {
                    gameData.setValue(null);
                    Log.e(TAG, "Error en la respuesta IGDB: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameDetailResponse> call, Throwable t) {
                gameData.setValue(null);
                Log.e(TAG, "Error en la llamada a IGDB API: " + t.getMessage());
            }
        });

        return gameData;
    }

    // Búsqueda de juegos en múltiples plataformas
    public LiveData<List<Game>> searchGamesOnline(String searchQuery) {
        MutableLiveData<List<Game>> searchResults = new MutableLiveData<>();

        String query = GameApiService.IGDBQueryBuilder.getSearchGamesQuery(searchQuery, 20, 0);

        apiService.searchGames(query).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GameResponse> igdbGames = (List<GameResponse>) response.body();
                    GameListResponse gameListResponse = new GameListResponse(igdbGames);

                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(gameListResponse.getResults());
                    searchResults.setValue(gamesList);

                    Log.d(TAG, "Búsqueda completada: " + gamesList.size() + " juegos encontrados para '" + searchQuery + "'");
                } else {
                    searchResults.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en búsqueda IGDB: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                searchResults.setValue(new ArrayList<>());
                Log.e(TAG, "Error en búsqueda IGDB API: " + t.getMessage());
            }
        });

        return searchResults;
    }

    // === MÉTODOS ESPECÍFICOS DE PLATAFORMAS ===

    // Obtener juegos por plataforma específica
    public LiveData<List<Game>> getGamesByPlatform(String platformName) {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        // Mapear nombres de plataformas a IDs de IGDB
        int platformId = getPlatformId(platformName);
        if (platformId == -1) {
            games.setValue(new ArrayList<>());
            return games;
        }

        String query = GameApiService.IGDBQueryBuilder.getGamesByPlatformQuery(platformId, 20, 0);

        apiService.getGames(query).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GameResponse> igdbGames = (List<GameResponse>) response.body();
                    GameListResponse gameListResponse = new GameListResponse(igdbGames);

                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(gameListResponse.getResults());
                    games.setValue(gamesList);

                    Log.d(TAG, "Juegos de " + platformName + " cargados: " + gamesList.size());
                } else {
                    games.setValue(new ArrayList<>());
                    Log.e(TAG, "Error cargando juegos de " + platformName + ": " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                games.setValue(new ArrayList<>());
                Log.e(TAG, "Error en llamada para " + platformName + ": " + t.getMessage());
            }
        });

        return games;
    }

    // Próximos lanzamientos de todas las plataformas
    public LiveData<List<Game>> getUpcomingGames() {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        String query = GameApiService.IGDBQueryBuilder.getUpcomingGamesQuery(15, 0);

        apiService.getGames(query).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GameResponse> igdbGames = (List<GameResponse>) response.body();
                    GameListResponse gameListResponse = new GameListResponse(igdbGames);

                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(gameListResponse.getResults());
                    games.setValue(gamesList);

                    Log.d(TAG, "Próximos lanzamientos cargados: " + gamesList.size() + " juegos");
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

        return games;
    }

    // === MÉTODOS DE MULTIMEDIA (ACTUALIZADOS PARA IGDB) ===

    public LiveData<List<ScreenshotListResponse.Screenshot>> getGameScreenshots(String gameId) {
        MutableLiveData<List<ScreenshotListResponse.Screenshot>> screenshots = new MutableLiveData<>();

        String query = GameApiService.IGDBQueryBuilder.getScreenshotsQuery(gameId);

        apiService.getGameScreenshots(query).enqueue(new Callback<ScreenshotListResponse>() {
            @Override
            public void onResponse(Call<ScreenshotListResponse> call, Response<ScreenshotListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    screenshots.setValue(response.body().getResults());
                } else {
                    screenshots.setValue(new ArrayList<>());
                    Log.e(TAG, "Error cargando capturas: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ScreenshotListResponse> call, Throwable t) {
                screenshots.setValue(new ArrayList<>());
                Log.e(TAG, "Error en capturas: " + t.getMessage());
            }
        });

        return screenshots;
    }

    public LiveData<List<VideoListResponse.Video>> getGameVideos(String gameId) {
        MutableLiveData<List<VideoListResponse.Video>> videos = new MutableLiveData<>();

        String query = GameApiService.IGDBQueryBuilder.getGameVideosQuery(gameId);

        apiService.getGameVideos(query).enqueue(new Callback<VideoListResponse>() {
            @Override
            public void onResponse(Call<VideoListResponse> call, Response<VideoListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    videos.setValue(response.body().getResults());
                } else {
                    videos.setValue(new ArrayList<>());
                    Log.e(TAG, "Error cargando videos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<VideoListResponse> call, Throwable t) {
                videos.setValue(new ArrayList<>());
                Log.e(TAG, "Error en videos: " + t.getMessage());
            }
        });

        return videos;
    }

    public LiveData<List<StoreListResponse.GameStore>> getGameStores(String gameId) {
        MutableLiveData<List<StoreListResponse.GameStore>> stores = new MutableLiveData<>();

        String query = GameApiService.IGDBQueryBuilder.getGameStoresQuery(gameId);

        apiService.getGameStores(query).enqueue(new Callback<StoreListResponse>() {
            @Override
            public void onResponse(Call<StoreListResponse> call, Response<StoreListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    stores.setValue(response.body().getResults());
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

        return stores;
    }

    // === MÉTODOS AUXILIARES ===

    // Mapear nombres de plataformas a IDs de IGDB
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
}