package com.example.gamedex.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.model.GameWithTags;
import com.example.gamedex.data.repository.GameRepository;
import com.example.gamedex.data.repository.TagRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameDetailViewModel extends ViewModel {

    private GameRepository gameRepository;
    private TagRepository tagRepository;
    private LiveData<GameWithTags> gameWithTags;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private String gameId;

    public void init(Application application, String gameId) {
        if (this.gameRepository != null) {
            return; // ViewModel already initialized
        }

        this.gameId = gameId;
        this.gameRepository = new GameRepository(application);
        this.tagRepository = new TagRepository(application);
        this.gameWithTags = gameRepository.getGameWithTags(gameId);
    }

    public LiveData<GameWithTags> getGameWithTags() {
        return gameWithTags;
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