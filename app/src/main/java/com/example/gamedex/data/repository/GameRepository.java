package com.example.gamedex.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamedex.data.local.dao.GameDao;
import com.example.gamedex.data.local.database.GameDexDatabase;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.mapper.GameMapper;
import com.example.gamedex.data.model.GameWithTags;
import com.example.gamedex.data.remote.GameApiService;
import com.example.gamedex.data.remote.RetrofitClient;
import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameListResponse;

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

    public GameRepository(Application application) {
        GameDexDatabase db = GameDexDatabase.getDatabase(application);
        gameDao = db.gameDao();
        executorService = Executors.newSingleThreadExecutor();
        apiService = RetrofitClient.getClient().create(GameApiService.class);
    }

    // Métodos locales existentes
    public LiveData<List<Game>> getAllLibraryGames() {
        return gameDao.getAllLibraryGames();
    }

    public void insertGame(Game game) {
        executorService.execute(() -> gameDao.insertGame(game));
    }

    public void updateGame(Game game) {
        executorService.execute(() -> gameDao.updateGame(game));
    }

    public void deleteGame(Game game) {
        executorService.execute(() -> gameDao.deleteGame(game));
    }

    public LiveData<GameWithTags> getGameWithTags(String gameId) {
        return gameDao.getGameWithTags(gameId);
    }

    public Game getGameById(String gameId) {
        return gameDao.getGameById(gameId);
    }

    public LiveData<List<Game>> searchGamesLocal(String query) {
        return gameDao.searchGames(query);
    }

    public LiveData<List<GameWithTags>> getLibraryGamesWithTags() {
        return gameDao.getLibraryGamesWithTags();
    }

    public LiveData<List<Game>> getGamesByStatus(String status) {
        return gameDao.getGamesByStatus(status);
    }

    // Nuevos métodos para la API
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

    public LiveData<List<Game>> searchGamesOnline(String query) {
        MutableLiveData<List<Game>> games = new MutableLiveData<>();

        apiService.searchGames(1, 20, query, true).enqueue(new Callback<GameListResponse>() {
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
        MutableLiveData<Game> game = new MutableLiveData<>();

        apiService.getGameDetails(gameId).enqueue(new Callback<GameDetailResponse>() {
            @Override
            public void onResponse(Call<GameDetailResponse> call, Response<GameDetailResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Game gameEntity = GameMapper.fromDetailResponseToEntity(response.body());
                    game.setValue(gameEntity);

                    // También guardar en la base de datos local para acceso offline
                    executorService.execute(() -> gameDao.insertGame(gameEntity));
                } else {
                    game.setValue(null);
                    Log.e(TAG, "Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameDetailResponse> call, Throwable t) {
                game.setValue(null);
                Log.e(TAG, "Error en la llamada a la API: " + t.getMessage());
            }
        });

        return game;
    }
}