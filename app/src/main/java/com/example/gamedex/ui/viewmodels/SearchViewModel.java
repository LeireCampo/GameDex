package com.example.gamedex.ui.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.mapper.GameMapper;
import com.example.gamedex.data.remote.GameApiService;
import com.example.gamedex.data.remote.model.GameListResponse;
import com.example.gamedex.data.remote.model.GenreListResponse;
import com.example.gamedex.data.remote.model.PlatformListResponse;
import com.example.gamedex.data.repository.GameRepository;
import com.example.gamedex.ui.fragments.SearchFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchViewModel extends AndroidViewModel {
    private static final String TAG = "SearchViewModel";

    private GameRepository gameRepository;
    private GameApiService apiService;

    // Cache para evitar múltiples llamadas a la API
    private MutableLiveData<List<String>> platformsLiveData;
    private MutableLiveData<List<String>> genresLiveData;
    private Map<String, String> platformIdMap; // nombre -> id
    private Map<String, String> genreIdMap; // nombre -> id

    public SearchViewModel(Application application) {
        super(application);
        gameRepository = new GameRepository(application);
        apiService = RetrofitClient.getClient().create(GameApiService.class);
        platformIdMap = new HashMap<>();
        genreIdMap = new HashMap<>();

        loadPlatforms();
        loadGenres();
    }

    // Búsqueda básica (método existente)
    public LiveData<List<Game>> searchGames(String query) {
        return gameRepository.searchGamesOnline(query);
    }

    // Nueva búsqueda con filtros avanzados
    public LiveData<List<Game>> searchGamesWithFilters(SearchFragment.SearchFilters filters) {
        MutableLiveData<List<Game>> searchResults = new MutableLiveData<>();

        // Convertir nombres a IDs para la API
        String platformIds = null;
        String genreIds = null;

        if (filters.platform != null && !filters.platform.isEmpty()) {
            platformIds = platformIdMap.get(filters.platform);
        }

        if (filters.genre != null && !filters.genre.isEmpty()) {
            genreIds = genreIdMap.get(filters.genre);
        }

        // Crear rango de fechas
        String dateRange = filters.getDateRange();

        Log.d(TAG, "Searching with filters - Query: " + filters.query +
                ", Platform: " + platformIds +
                ", Genre: " + genreIds +
                ", Dates: " + dateRange);

        apiService.advancedSearch(
                1, // page
                20, // page_size
                filters.query,
                platformIds, // platforms
                null, // stores
                genreIds, // genres
                null, // tags
                null, // developers
                null, // publishers
                dateRange, // dates
                "-rating", // ordering
                null // metacritic
        ).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> games = GameMapper.fromResponseListToEntityList(response.body().getResults());
                    searchResults.setValue(games);
                    Log.d(TAG, "Search successful, found " + games.size() + " games");
                } else {
                    searchResults.setValue(new ArrayList<>());
                    Log.e(TAG, "Search failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                searchResults.setValue(new ArrayList<>());
                Log.e(TAG, "Search failed", t);
            }
        });

        return searchResults;
    }

    // Obtener lista de plataformas
    public LiveData<List<String>> getPlatforms() {
        if (platformsLiveData == null) {
            platformsLiveData = new MutableLiveData<>();
            loadPlatforms();
        }
        return platformsLiveData;
    }

    // Obtener lista de géneros
    public LiveData<List<String>> getGenres() {
        if (genresLiveData == null) {
            genresLiveData = new MutableLiveData<>();
            loadGenres();
        }
        return genresLiveData;
    }

    private void loadPlatforms() {
        apiService.getPlatforms(1, 50).enqueue(new Callback<PlatformListResponse>() {
            @Override
            public void onResponse(Call<PlatformListResponse> call, Response<PlatformListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> platformNames = new ArrayList<>();
                    platformNames.add("Todas las plataformas"); // Opción por defecto

                    for (PlatformListResponse.Platform platform : response.body().getResults()) {
                        platformNames.add(platform.getName());
                        platformIdMap.put(platform.getName(), String.valueOf(platform.getId()));
                    }

                    if (platformsLiveData == null) {
                        platformsLiveData = new MutableLiveData<>();
                    }
                    platformsLiveData.setValue(platformNames);
                    Log.d(TAG, "Loaded " + platformNames.size() + " platforms");
                } else {
                    Log.e(TAG, "Failed to load platforms: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<PlatformListResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load platforms", t);
            }
        });
    }

    private void loadGenres() {
        apiService.getGenres(1, 50).enqueue(new Callback<GenreListResponse>() {
            @Override
            public void onResponse(Call<GenreListResponse> call, Response<GenreListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> genreNames = new ArrayList<>();
                    genreNames.add("Todos los géneros"); // Opción por defecto

                    for (GenreListResponse.Genre genre : response.body().getResults()) {
                        genreNames.add(genre.getName());
                        genreIdMap.put(genre.getName(), String.valueOf(genre.getId()));
                    }

                    if (genresLiveData == null) {
                        genresLiveData = new MutableLiveData<>();
                    }
                    genresLiveData.setValue(genreNames);
                    Log.d(TAG, "Loaded " + genreNames.size() + " genres");
                } else {
                    Log.e(TAG, "Failed to load genres: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GenreListResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load genres", t);
            }
        });
    }

    // Método para búsqueda rápida por categoría
    public LiveData<List<Game>> searchByCategory(String category) {
        MutableLiveData<List<Game>> categoryResults = new MutableLiveData<>();

        String ordering;
        switch (category.toLowerCase()) {
            case "popular":
                ordering = "-rating";
                break;
            case "recent":
                ordering = "-released";
                break;
            case "upcoming":
                ordering = "released";
                break;
            default:
                ordering = "-rating";
        }

        apiService.getGames(1, 20, ordering).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Game> games = GameMapper.fromResponseListToEntityList(response.body().getResults());
                    categoryResults.setValue(games);
                } else {
                    categoryResults.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                categoryResults.setValue(new ArrayList<>());
                Log.e(TAG, "Failed to load category: " + category, t);
            }
        });

        return categoryResults;
    }

    // Método para obtener sugerencias de búsqueda
    public LiveData<List<String>> getSearchSuggestions(String query) {
        MutableLiveData<List<String>> suggestions = new MutableLiveData<>();

        if (query.length() < 3) {
            suggestions.setValue(new ArrayList<>());
            return suggestions;
        }

        apiService.searchGames(1, 5, query, false).enqueue(new Callback<GameListResponse>() {
            @Override
            public void onResponse(Call<GameListResponse> call, Response<GameListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> suggestionsList = new ArrayList<>();
                    for (com.example.gamedex.data.remote.model.GameResponse game : response.body().getResults()) {
                        suggestionsList.add(game.getName());
                    }
                    suggestions.setValue(suggestionsList);
                } else {
                    suggestions.setValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<GameListResponse> call, Throwable t) {
                suggestions.setValue(new ArrayList<>());
            }
        });

        return suggestions;
    }
}