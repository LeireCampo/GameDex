package com.example.gamedex.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.model.GameWithTags;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;
import com.example.gamedex.data.remote.model.StoreListResponse;
import com.example.gamedex.data.remote.model.VideoListResponse;
import com.example.gamedex.data.repository.GameRepository;
import com.example.gamedex.data.repository.TagRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameDetailViewModel extends ViewModel {

    private GameRepository gameRepository;
    private TagRepository tagRepository;
    private LiveData<GameWithTags> gameWithTags;
    private LiveData<Game> gameDetails;
    private LiveData<List<ScreenshotListResponse.Screenshot>> screenshots;
    private LiveData<List<VideoListResponse.Video>> videos;
    private LiveData<List<Game>> dlcs;
    private LiveData<List<Game>> seriesGames;
    private LiveData<List<StoreListResponse.GameStore>> stores;
    private LiveData<List<Game>> suggestedGames;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String gameId;

    public void init(Application application, String gameId) {
        if (this.gameRepository != null) {
            return; // ViewModel already initialized
        }

        this.gameId = gameId;
        this.gameRepository = new GameRepository(application);
        this.tagRepository = new TagRepository(application);

        // Cargar datos básicos
        this.gameWithTags = gameRepository.getGameWithTags(gameId);
        this.gameDetails = gameRepository.getGameDetailsOnline(gameId);

        // Cargar datos adicionales
        this.screenshots = gameRepository.getGameScreenshots(gameId);
        this.videos = gameRepository.getGameVideos(gameId);
        this.dlcs = gameRepository.getGameDlcs(gameId);
        this.seriesGames = gameRepository.getGameSeries(gameId);
        this.stores = gameRepository.getGameStores(gameId);
        this.suggestedGames = gameRepository.getSuggestedGames(gameId);
    }

    public LiveData<GameWithTags> getGameWithTags() {
        return gameWithTags;
    }

    public LiveData<Game> getGameDetails() {
        return gameDetails;
    }

    public LiveData<List<ScreenshotListResponse.Screenshot>> getScreenshots() {
        return screenshots;
    }

    public LiveData<List<VideoListResponse.Video>> getVideos() {
        return videos;
    }

    public LiveData<List<Game>> getDlcs() {
        return dlcs;
    }

    public LiveData<List<Game>> getSeriesGames() {
        return seriesGames;
    }

    public LiveData<List<StoreListResponse.GameStore>> getStores() {
        return stores;
    }

    public LiveData<List<Game>> getSuggestedGames() {
        return suggestedGames;
    }

    public void toggleInLibrary() {
        executorService.execute(() -> {
            Game game = gameRepository.getGameById(gameId);
            if (game != null) {
                game.setInLibrary(!game.isInLibrary());
                // If adding to library for first time, set default status
                if (game.isInLibrary() && game.getStatus() == null) {
                    game.setStatus("backlog");
                }
                gameRepository.updateGame(game);
            }
        });
    }

    public void updateGameStatus(String status) {
        executorService.execute(() -> {
            Game game = gameRepository.getGameById(gameId);
            if (game != null) {
                game.setStatus(status);
                gameRepository.updateGame(game);
            }
        });
    }

    public void updateUserRating(float rating) {
        executorService.execute(() -> {
            Game game = gameRepository.getGameById(gameId);
            if (game != null) {
                game.setUserRating(rating);
                gameRepository.updateGame(game);
            }
        });
    }

    public void addTagToGame(int tagId) {
        executorService.execute(() -> {
            tagRepository.addTagToGame(gameId, tagId);
        });
    }

    public void removeTagFromGame(int tagId) {
        executorService.execute(() -> {
            tagRepository.removeTagFromGame(gameId, tagId);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}