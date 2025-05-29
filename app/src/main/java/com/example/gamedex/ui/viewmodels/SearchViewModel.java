package com.example.gamedex.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.repository.GameRepository;
import com.example.gamedex.ui.fragments.SearchFragment;

import java.util.List;

public class SearchViewModel extends AndroidViewModel {
    private static final String TAG = "SearchViewModel";

    private GameRepository gameRepository;

    public SearchViewModel(Application application) {
        super(application);
        gameRepository = new GameRepository(application);
    }

    // Búsqueda básica
    public LiveData<List<Game>> searchGames(String query) {
        return gameRepository.searchGamesOnline(query);
    }

    // Búsqueda con filtros avanzados
    public LiveData<List<Game>> searchGamesWithFilters(SearchFragment.SearchFilters filters) {
        return gameRepository.searchGamesWithFilters(
                filters.query,
                filters.platform,
                filters.genre,
                filters.yearFrom,
                filters.yearTo
        );
    }

    // Obtener lista de plataformas
    public LiveData<List<String>> getPlatforms() {
        return gameRepository.getPlatforms();
    }

    // Obtener lista de géneros
    public LiveData<List<String>> getGenres() {
        return gameRepository.getGenres();
    }

    // Búsqueda por categoría
    public LiveData<List<Game>> searchByCategory(String category) {
        switch (category.toLowerCase()) {
            case "popular":
                return gameRepository.getPopularGames();
            case "recent":
                return gameRepository.getRecentGames();
            case "upcoming":
                return gameRepository.getUpcomingGames();
            default:
                return gameRepository.getPopularGames();
        }
    }
}