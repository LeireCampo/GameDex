package com.example.gamedex.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.model.GameWithTags;
import com.example.gamedex.data.repository.GameRepository;

import java.util.List;

public class LibraryViewModel extends AndroidViewModel {
    private GameRepository gameRepository;
    private LiveData<List<Game>> allLibraryGames;
    private LiveData<List<GameWithTags>> libraryGamesWithTags;
    private MutableLiveData<String> statusFilter = new MutableLiveData<>();
    private LiveData<List<Game>> filteredGames;

    public LibraryViewModel(Application application) {
        super(application);
        gameRepository = new GameRepository(application);
        allLibraryGames = gameRepository.getAllLibraryGames();
        libraryGamesWithTags = gameRepository.getLibraryGamesWithTags();

        filteredGames = Transformations.switchMap(statusFilter, status -> {
            if (status == null || status.equals("all")) {
                return allLibraryGames;
            } else {
                return gameRepository.getGamesByStatus(status);
            }
        });
    }

    public LiveData<List<Game>> getAllLibraryGames() {
        return allLibraryGames;
    }

    public LiveData<List<GameWithTags>> getLibraryGamesWithTags() {
        return libraryGamesWithTags;
    }

    public LiveData<List<Game>> getFilteredGames() {
        return filteredGames;
    }

    public void setStatusFilter(String status) {
        statusFilter.setValue(status);
    }

    public LiveData<List<Game>> getGamesByStatus(String status) {
        return gameRepository.getGamesByStatus(status);
    }
}