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
import com.example.gamedex.data.remote.RetrofitClient;
import com.example.gamedex.data.remote.model.DlcListResponse;
import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameListResponse;
import com.example.gamedex.data.remote.model.GameSeriesResponse;
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
    private final GameApiService apiService;
    private final FirestoreService firestoreService;
    private final FirebaseAuthService authService;

    public GameRepository(Application application) {
        GameDexDatabase db = GameDexDatabase.getDatabase(application);
        gameDao = db.gameDao();
        executorService = Executors.newSingleThreadExecutor();
        apiService = RetrofitClient.getClient().create(GameApiService.class);
        firestoreService = FirestoreService.getInstance();
        authService = FirebaseAuthService.getInstance();
    }

    // Métodos locales existentes con sincronización Firebase

    public LiveData<List<Game>> getAllLibraryGames() {
        if (authService.isUserSignedIn()) {
            // Si el usuario está autenticado, combinar datos locales y de Firebase
            return getCombinedLibraryGames();
        } else {
            // Si no está autenticado, usar solo datos locales
            return gameDao.getAllLibraryGames();
        }
    }

    private LiveData<List<Game>> getCombinedLibraryGames() {
        MediatorLiveData<List<Game>> mediatorLiveData = new MediatorLiveData<>();
        LiveData<List<Game>> localGames = gameDao.getAllLibraryGames();
        LiveData<List<Game>> firebaseGames = firestoreService.getUserGamesFromFirestore();

        mediatorLiveData.addSource(localGames, games -> {
            // Cuando cambien los datos locales, emitir la lista local
            mediatorLiveData.setValue(games);
        });

        mediatorLiveData.addSource(firebaseGames, games -> {
            // Cuando lleguen datos de Firebase, sincronizar con la base local
            if (games != null && !games.isEmpty()) {
                executorService.execute(() -> {
                    for (Game game : games) {
                        Game existingGame = gameDao.getGameById(game.getId());
                        if (existingGame == null || game.getLastUpdated() > existingGame.getLastUpdated()) {
                            // Si el juego no existe localmente o la versión de Firebase es más reciente
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

            // Sincronizar con Firebase si el usuario está autenticado
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

            // Sincronizar con Firebase si el usuario está autenticado
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

            // Sincronizar eliminación con Firebase si el usuario está autenticado
            if (authService.isUserSignedIn()) {
                game.setInLibrary(false); // Marcar como no en biblioteca para que se elimine de Firebase
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

    // Método para sincronizar toda la biblioteca con Firebase
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

    // Métodos de la API existentes
    public LiveData<List<Game>> getPopularGames() {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        apiService.getGames(1, 10, "-rating").enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                    games.setValue(gamesList);
                } else {
                    games.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                games.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return games;
    }

    public LiveData<List<Game>> getRecentGames() {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        apiService.getGames(1, 10, "-released").enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                    games.setValue(gamesList);
                } else {
                    games.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                games.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return games;
    }

    public LiveData<Game> getGameDetailsOnline(String gameId) {
        MutableLiveData<Game> gameData = new MutableLiveData<>();

        apiService.getGameDetails(gameId).enqueue(new Callback<GameDetailResponse>() {
            @Override
            public void onResponse(Call<GameDetailResponse> call, Response<GameDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Game gameEntity = GameMapper.fromDetailResponseToEntity(response.body());
                    gameData.setValue(gameEntity);

                    // También guardar en la base de datos local para acceso offline
                    executorService.execute(() -> gameDao.insertGame(gameEntity));
                } else {
                    gameData.setValue(null);
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameDetailResponse> call, Throwable t) {
                gameData.setValue(null);
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return gameData;
    }

    // Obtener capturas de pantalla de un juego
    public LiveData<List<ScreenshotListResponse.Screenshot>> getGameScreenshots(String gameId) {
        MutableLiveData<List<ScreenshotListResponse.Screenshot>> screenshots = new MutableLiveData<>();

        apiService.getGameScreenshots(gameId).enqueue(new Callback<ScreenshotListResponse>() {
            @Override
            public void onResponse(Call<ScreenshotListResponse> call, Response<ScreenshotListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    screenshots.setValue(response.body().getResults());
                } else {
                    screenshots.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ScreenshotListResponse> call, Throwable t) {
                screenshots.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return screenshots;
    }

    // Obtener videos de un juego
    public LiveData<List<VideoListResponse.Video>> getGameVideos(String gameId) {
        MutableLiveData<List<VideoListResponse.Video>> videos = new MutableLiveData<>();

        apiService.getGameVideos(gameId).enqueue(new Callback<VideoListResponse>() {
            @Override
            public void onResponse(Call<VideoListResponse> call, Response<VideoListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    videos.setValue(response.body().getResults());
                } else {
                    videos.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<VideoListResponse> call, Throwable t) {
                videos.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return videos;
    }

    // Obtener DLCs de un juego
    public LiveData<List<Game>> getGameDlcs(String gameId) {
        MutableLiveData<List<Game>> dlcs = new MutableLiveData<>();

        apiService.getGameDlcs(gameId).enqueue(new Callback<DlcListResponse>() {
            @Override
            public void onResponse(Call<DlcListResponse> call, Response<DlcListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> dlcList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                    dlcs.setValue(dlcList);
                } else {
                    dlcs.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<DlcListResponse> call, Throwable t) {
                dlcs.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return dlcs;
    }

    // Obtener juegos de la misma serie
    public LiveData<List<Game>> getGameSeries(String gameId) {
        MutableLiveData<List<Game>> seriesGames = new MutableLiveData<>();

        apiService.getGameSeries(gameId).enqueue(new Callback<GameSeriesResponse>() {
            @Override
            public void onResponse(Call<GameSeriesResponse> call, Response<GameSeriesResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                    seriesGames.setValue(gamesList);
                } else {
                    seriesGames.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameSeriesResponse> call, Throwable t) {
                seriesGames.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return seriesGames;
    }

    // Obtener tiendas donde comprar el juego
    public LiveData<List<StoreListResponse.GameStore>> getGameStores(String gameId) {
        MutableLiveData<List<StoreListResponse.GameStore>> stores = new MutableLiveData<>();

        apiService.getGameStores(gameId).enqueue(new Callback<StoreListResponse>() {
            @Override
            public void onResponse(Call<StoreListResponse> call, Response<StoreListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    stores.setValue(response.body().getResults());
                } else {
                    stores.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<StoreListResponse> call, Throwable t) {
                stores.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return stores;
    }

    // Obtener juegos sugeridos basados en un juego
    public LiveData<List<Game>> getSuggestedGames(String gameId) {
        MutableLiveData<List<Game>> suggestedGames = new MutableLiveData<>();

        apiService.getSuggestedGames(gameId, 1, 10).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                    suggestedGames.setValue(gamesList);
                } else {
                    suggestedGames.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                suggestedGames.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return suggestedGames;
    }

    // Búsqueda avanzada de juegos
    public LiveData<List<Game>> advancedSearch(String query, String platforms, String genres,
                                               String tags, String dates, String ordering) {
        MutableLiveData<List<Game>> searchResults = new MutableLiveData<>();

        apiService.advancedSearch(1, 20, query, platforms, null, genres, tags,
                        null, null, dates, ordering, null)
                .enqueue(new Callback<GameListResponse>() {
                    @Override
                    public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                            searchResults.setValue(gamesList);
                        } else {
                            searchResults.setValue(new ArrayList<>());
                            Log.e(TAG, "Error en la respuesta: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<GameListResponse> call, Throwable t) {
                        searchResults.setValue(new ArrayList<>());
                        Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
                    }
                });

        return searchResults;
    }

    // Método para búsqueda en línea más simple
    public LiveData<List<Game>> searchGamesOnline(String query) {
        MutableLiveData<List<Game>> searchResults = new MutableLiveData<>();

        apiService.searchGames(1, 20, query, true).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> gamesList = GameMapper.fromResponseListToEntityList(response.body().getResults());
                    searchResults.setValue(gamesList);
                } else {
                    searchResults.setValue(new ArrayList<>());
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                searchResults.setValue(new ArrayList<>());
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return searchResults;
    }
}