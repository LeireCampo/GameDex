package com.example.gamedex.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.adapters.GameAdapter;
import com.example.gamedex.ui.viewmodels.SearchViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;

public class SearchFragment extends Fragment implements GameAdapter.OnGameClickListener {

    // Views
    private RecyclerView recyclerSearchResults;
    private TextInputEditText searchEditText;
    private TextView textNoResults;
    private ProgressBar progressBar;
    private Button buttonFilters;
    private Button buttonClearFilters;
    private Button buttonApplyFilters;
    private View filtersPanel;
    private View emptyStateCard;
    private View activeFiltersScroll;
    private ChipGroup activeFiltersChipGroup;

    // Filter views
    private AutoCompleteTextView platformDropdown;
    private AutoCompleteTextView genreDropdown;
    private TextInputEditText yearFromEditText;
    private TextInputEditText yearToEditText;

    // Adapters and data
    private GameAdapter searchResultsAdapter;
    private SearchViewModel viewModel;
    private boolean filtersVisible = false;

    // Filter data
    private String selectedPlatform = null;
    private String selectedGenre = null;
    private String yearFrom = null;
    private String yearTo = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSearchInput();
        setupFilters();
        loadFilterData();
    }

    private void initViews(View view) {
        recyclerSearchResults = view.findViewById(R.id.recycler_search_results);
        searchEditText = view.findViewById(R.id.search_edit_text);
        textNoResults = view.findViewById(R.id.text_no_results);
        progressBar = view.findViewById(R.id.progress_bar);
        buttonFilters = view.findViewById(R.id.button_filters);
        buttonClearFilters = view.findViewById(R.id.button_clear_filters);
        buttonApplyFilters = view.findViewById(R.id.button_apply_filters);
        filtersPanel = view.findViewById(R.id.filters_panel);
        emptyStateCard = view.findViewById(R.id.empty_state_card);
        activeFiltersScroll = view.findViewById(R.id.active_filters_scroll);
        activeFiltersChipGroup = view.findViewById(R.id.active_filters_chip_group);

        // Filter views
        platformDropdown = view.findViewById(R.id.platform_dropdown);
        genreDropdown = view.findViewById(R.id.genre_dropdown);
        yearFromEditText = view.findViewById(R.id.year_from_edit_text);
        yearToEditText = view.findViewById(R.id.year_to_edit_text);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);
    }

    private void setupRecyclerView() {
        searchResultsAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        recyclerSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerSearchResults.setAdapter(searchResultsAdapter);
    }

    private void setupSearchInput() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void setupFilters() {
        buttonFilters.setOnClickListener(v -> toggleFilters());
        buttonClearFilters.setOnClickListener(v -> clearAllFilters());
        buttonApplyFilters.setOnClickListener(v -> performSearch());
    }

    private void loadFilterData() {
        // Cargar plataformas
        viewModel.getPlatforms().observe(getViewLifecycleOwner(), platforms -> {
            if (platforms != null) {
                ArrayAdapter<String> platformAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        platforms
                );
                platformDropdown.setAdapter(platformAdapter);
                platformDropdown.setOnItemClickListener((parent, view, position, id) -> {
                    selectedPlatform = platforms.get(position);
                    updateActiveFilters();
                });
            }
        });

        // Cargar géneros
        viewModel.getGenres().observe(getViewLifecycleOwner(), genres -> {
            if (genres != null) {
                ArrayAdapter<String> genreAdapter = new ArrayAdapter<>(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        genres
                );
                genreDropdown.setAdapter(genreAdapter);
                genreDropdown.setOnItemClickListener((parent, view, position, id) -> {
                    selectedGenre = genres.get(position);
                    updateActiveFilters();
                });
            }
        });
    }

    private void toggleFilters() {
        filtersVisible = !filtersVisible;
        filtersPanel.setVisibility(filtersVisible ? View.VISIBLE : View.GONE);

        if (filtersVisible) {
            buttonFilters.setText(R.string.hide_filters);
            buttonFilters.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_expand_less, 0, 0, 0);
        } else {
            buttonFilters.setText(R.string.filters);
            buttonFilters.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_filter, 0, 0, 0);
        }
    }

    private void performSearch() {
        String query = searchEditText.getText().toString().trim();

        // Obtener valores de filtros de año
        yearFrom = yearFromEditText.getText().toString().trim();
        yearTo = yearToEditText.getText().toString().trim();

        // Validar años
        if (!yearFrom.isEmpty() && !isValidYear(yearFrom)) {
            yearFromEditText.setError(getString(R.string.invalid_year));
            return;
        }
        if (!yearTo.isEmpty() && !isValidYear(yearTo)) {
            yearToEditText.setError(getString(R.string.invalid_year));
            return;
        }

        showLoading(true);
        hideEmptyState();

        // Crear filtros para la búsqueda
        SearchFilters filters = new SearchFilters(
                query,
                selectedPlatform,
                selectedGenre,
                yearFrom,
                yearTo
        );

        viewModel.searchGamesWithFilters(filters).observe(getViewLifecycleOwner(), games -> {
            showLoading(false);

            if (games != null && !games.isEmpty()) {
                searchResultsAdapter.updateGames(games);
                showResults();
            } else {
                showEmptyState();
            }

            updateActiveFilters();
        });
    }

    private void updateActiveFilters() {
        activeFiltersChipGroup.removeAllViews();
        boolean hasActiveFilters = false;

        // Agregar chip para plataforma
        if (selectedPlatform != null && !selectedPlatform.isEmpty()) {
            addFilterChip(getString(R.string.platform) + ": " + selectedPlatform, () -> {
                selectedPlatform = null;
                platformDropdown.setText("");
                updateActiveFilters();
            });
            hasActiveFilters = true;
        }

        // Agregar chip para género
        if (selectedGenre != null && !selectedGenre.isEmpty()) {
            addFilterChip(getString(R.string.genre) + ": " + selectedGenre, () -> {
                selectedGenre = null;
                genreDropdown.setText("");
                updateActiveFilters();
            });
            hasActiveFilters = true;
        }

        // Agregar chip para rango de años
        if ((yearFrom != null && !yearFrom.isEmpty()) || (yearTo != null && !yearTo.isEmpty())) {
            String yearRange = getString(R.string.year) + ": ";
            if (yearFrom != null && !yearFrom.isEmpty()) {
                yearRange += yearFrom;
            }
            yearRange += " - ";
            if (yearTo != null && !yearTo.isEmpty()) {
                yearRange += yearTo;
            }

            addFilterChip(yearRange, () -> {
                yearFrom = null;
                yearTo = null;
                yearFromEditText.setText("");
                yearToEditText.setText("");
                updateActiveFilters();
            });
            hasActiveFilters = true;
        }

        // Mostrar/ocultar panel de filtros activos y botón limpiar
        activeFiltersScroll.setVisibility(hasActiveFilters ? View.VISIBLE : View.GONE);
        buttonClearFilters.setVisibility(hasActiveFilters ? View.VISIBLE : View.GONE);
    }

    private void addFilterChip(String text, Runnable onRemove) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);
        chip.setChipBackgroundColorResource(R.color.dark_surface);
        chip.setTextColor(getResources().getColor(R.color.neon_blue, null));
        chip.setCloseIconTint(getResources().getColorStateList(R.color.neon_blue, null));
        chip.setOnCloseIconClickListener(v -> onRemove.run());

        activeFiltersChipGroup.addView(chip);
    }

    private void clearAllFilters() {
        selectedPlatform = null;
        selectedGenre = null;
        yearFrom = null;
        yearTo = null;

        platformDropdown.setText("");
        genreDropdown.setText("");
        yearFromEditText.setText("");
        yearToEditText.setText("");

        updateActiveFilters();

        // Si hay una búsqueda activa, repetirla sin filtros
        if (!searchEditText.getText().toString().trim().isEmpty()) {
            performSearch();
        }
    }

    private boolean isValidYear(String year) {
        try {
            int yearInt = Integer.parseInt(year);
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            return yearInt >= 1970 && yearInt <= currentYear + 5; // Permitir hasta 5 años en el futuro
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerSearchResults.setVisibility(show ? View.GONE : View.VISIBLE);
        emptyStateCard.setVisibility(View.GONE);
    }

    private void showResults() {
        recyclerSearchResults.setVisibility(View.VISIBLE);
        emptyStateCard.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerSearchResults.setVisibility(View.GONE);
        emptyStateCard.setVisibility(View.VISIBLE);
    }

    private void hideEmptyState() {
        emptyStateCard.setVisibility(View.GONE);
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }

    // Clase para encapsular filtros de búsqueda
    public static class SearchFilters {
        public final String query;
        public final String platform;
        public final String genre;
        public final String yearFrom;
        public final String yearTo;

        public SearchFilters(String query, String platform, String genre, String yearFrom, String yearTo) {
            this.query = query;
            this.platform = platform;
            this.genre = genre;
            this.yearFrom = yearFrom;
            this.yearTo = yearTo;
        }

        public boolean hasFilters() {
            return (platform != null && !platform.isEmpty()) ||
                    (genre != null && !genre.isEmpty()) ||
                    (yearFrom != null && !yearFrom.isEmpty()) ||
                    (yearTo != null && !yearTo.isEmpty());
        }

        public String getDateRange() {
            if ((yearFrom == null || yearFrom.isEmpty()) && (yearTo == null || yearTo.isEmpty())) {
                return null;
            }

            String from = (yearFrom != null && !yearFrom.isEmpty()) ? yearFrom + "-01-01" : "1970-01-01";
            String to = (yearTo != null && !yearTo.isEmpty()) ? yearTo + "-12-31" : Calendar.getInstance().get(Calendar.YEAR) + "-12-31";

            return from + "," + to;
        }
    }
}