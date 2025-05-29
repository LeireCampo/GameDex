package com.example.gamedex.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.repository.GameRepository;

import java.util.List;

public class HomeViewModel extends AndroidViewModel {
    private GameRepository gameRepository;
    private LiveData<List<Game>> popularGames;
    private LiveData<List<Game>> recentGames;
    private LiveData<List<Game>> recommendedGames;

    public HomeViewModel(Application application) {
        super(application);
        gameRepository = new GameRepository(application);
    }

    public void init() {
        if (popularGames != null && recentGames != null) {
            return; // ViewModel ya inicializado
        }

        popularGames = gameRepository.getPopularGames();
        recentGames = gameRepository.getRecentGames();
        // Para recomendados usamos próximos lanzamientos
        recommendedGames = gameRepository.getUpcomingGames();
    }

    public LiveData<List<Game>> getPopularGames() {
        return popularGames;
    }

    public LiveData<List<Game>> getRecentGames() {
        return recentGames;
    }

    public LiveData<List<Game>> getRecommendedGames() {
        return recommendedGames;
    }
}