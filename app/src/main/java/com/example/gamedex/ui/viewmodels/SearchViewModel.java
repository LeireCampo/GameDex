package com.example.gamedex.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.repository.GameRepository;

import java.util.List;

public class SearchViewModel extends AndroidViewModel {
    private GameRepository gameRepository;

    public SearchViewModel(Application application) {
        super(application);
        gameRepository = new GameRepository(application);
    }

    public LiveData<List<Game>> searchGames(String query) {
        return gameRepository.searchGamesOnline(query);
    }
}