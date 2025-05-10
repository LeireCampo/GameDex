package com.example.gamedex.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.gamedex.data.local.dao.GameDao;
import com.example.gamedex.data.local.database.GameDexDatabase;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.model.GameWithTags;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameRepository {
    private final GameDao gameDao;
    private final ExecutorService executorService;

    public GameRepository(Application application) {
        GameDexDatabase db = GameDexDatabase.getDatabase(application);
        gameDao = db.gameDao();
        executorService = Executors.newSingleThreadExecutor();
    }

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
        // This would normally be done asynchronously, simplified for example
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
}