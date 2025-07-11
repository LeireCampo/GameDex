package com.example.gamedex.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.model.GameWithTags;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;
import com.example.gamedex.data.remote.model.StoreListResponse;
import com.example.gamedex.data.remote.model.VideoListResponse;
import com.example.gamedex.data.repository.GameRepository;
import com.example.gamedex.data.repository.CustomTagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameDetailViewModel extends ViewModel {

    private GameRepository gameRepository;
    private CustomTagRepository customTagRepository;
    private LiveData<GameWithTags> gameWithTags;
    private LiveData<Game> gameDetails;
    private LiveData<List<ScreenshotListResponse.Screenshot>> screenshots;
    private LiveData<List<VideoListResponse.Video>> videos;
    private LiveData<List<StoreListResponse.GameStore>> stores;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String gameId;

    public void init(Application application, String gameId) {
        if (this.gameRepository != null) {
            return; // ViewModel already initialized
        }

        this.gameId = gameId;
        this.gameRepository = new GameRepository(application);
        this.customTagRepository = new CustomTagRepository(application);

        // Cargar datos básicos del juego local
        this.gameWithTags = gameRepository.getGameWithTags(gameId);

        // Cargar detalles actualizados de la API
        this.gameDetails = gameRepository.getGameDetailsOnline(gameId);

        // Cargar datos multimedia de la API
        this.screenshots = gameRepository.getGameScreenshots(gameId);
        this.videos = gameRepository.getGameVideos(gameId);
        this.stores = gameRepository.getGameStores(gameId);
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

    // Simplificar los observables para videos ya que RAWG no los tiene
    public LiveData<List<VideoListResponse.Video>> getVideos() {
        MutableLiveData<List<VideoListResponse.Video>> videos = new MutableLiveData<>();
        videos.setValue(new ArrayList<>()); // Lista vacía por defecto
        return videos;
    }

    public LiveData<List<StoreListResponse.GameStore>> getStores() {
        return stores;
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

    public void addTagToGame(int customTagId) {
        executorService.execute(() -> {
            customTagRepository.addTagToGame(gameId, customTagId);
        });
    }

    public void removeTagFromGame(int customTagId) {
        executorService.execute(() -> {
            customTagRepository.removeTagFromGame(gameId, customTagId);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}