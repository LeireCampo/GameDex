package com.example.gamedex.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.adapters.GameAdapter;
import com.example.gamedex.ui.viewmodels.LibraryViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class LibraryFragment extends Fragment implements GameAdapter.OnGameClickListener {

    private RecyclerView recyclerLibrary;
    private TextView textEmptyLibrary;
    private ChipGroup filterChipGroup;
    private FloatingActionButton fabAddGame;
    private Toolbar toolbar;
    private GameAdapter gameAdapter;
    private LibraryViewModel viewModel;
    private View emptyStateCard;
    private View buttonAddFirstGame;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerLibrary = view.findViewById(R.id.recycler_library);
        textEmptyLibrary = view.findViewById(R.id.text_empty_library);
        filterChipGroup = view.findViewById(R.id.filter_chip_group);
        fabAddGame = view.findViewById(R.id.fab_add_game);
        toolbar = view.findViewById(R.id.toolbar);
        emptyStateCard = view.findViewById(R.id.empty_state_card);
        buttonAddFirstGame = view.findViewById(R.id.button_add_first_game);

        setupViewModel();
        setupRecyclerView();
        setupFilterChips();
        setupFab();

        if (buttonAddFirstGame != null) {
            buttonAddFirstGame.setOnClickListener(v -> navigateToSearch());
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        // Observar los juegos filtrados
        viewModel.getFilteredGames().observe(getViewLifecycleOwner(), games -> {
            if (games != null) {
                gameAdapter.updateGames(games);

                // Mostrar mensaje de biblioteca vacía si no hay juegos
                if (games.isEmpty()) {
                    if (emptyStateCard != null) emptyStateCard.setVisibility(View.VISIBLE);
                    recyclerLibrary.setVisibility(View.GONE);
                } else {
                    if (emptyStateCard != null) emptyStateCard.setVisibility(View.GONE);
                    recyclerLibrary.setVisibility(View.VISIBLE);
                }
            }
        });

        // Iniciar con "todos" los juegos
        viewModel.setStatusFilter("all");
    }

    private void setupRecyclerView() {
        gameAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        recyclerLibrary.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerLibrary.setAdapter(gameAdapter);
    }

    private void setupFilterChips() {
        String[] filters = {
                getString(R.string.all_games),
                getString(R.string.playing),
                getString(R.string.completed),
                getString(R.string.backlog),
                getString(R.string.wishlist)
        };

        String[] filterValues = {
                "all",
                "playing",
                "completed",
                "backlog",
                "wishlist"
        };

        int[] chipStyles = {
                R.style.Widget_GameDex_Chip_Neon,
                R.style.Widget_GameDex_Chip_Neon_Playing,
                R.style.Widget_GameDex_Chip_Neon_Completed,
                R.style.Widget_GameDex_Chip_Neon_Backlog,
                R.style.Widget_GameDex_Chip_Neon_Wishlist
        };

        for (int i = 0; i < filters.length; i++) {
            final String filter = filterValues[i];
            Chip chip = new Chip(requireContext(), null, chipStyles[i]);
            chip.setText(filters[i]);
            chip.setCheckable(true);
            chip.setClickable(true);
            chip.setTag(filter);

            // Añadir efecto neón al seleccionar
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Aumentar el efecto neón cuando está seleccionado
                    chip.setShadowLayer(8, 0, 0, ContextCompat.getColor(requireContext(),
                            getColorForStatus(filter)));
                } else {
                    // Quitar el efecto de sombra cuando no está seleccionado
                    chip.setShadowLayer(0, 0, 0, 0);
                }
            });

            // Establecer chip seleccionado por defecto
            if (filter.equals("all")) {
                chip.setChecked(true);
            }

            filterChipGroup.addView(chip);
        }

        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.size() > 0) {
                Chip selectedChip = group.findViewById(checkedIds.get(0));
                if (selectedChip != null) {
                    String filter = (String) selectedChip.getTag();
                    viewModel.setStatusFilter(filter);
                }
            }
        });
    }

    private int getColorForStatus(String status) {
        switch (status) {
            case "playing":
                return R.color.status_playing;
            case "completed":
                return R.color.status_completed;
            case "backlog":
                return R.color.status_backlog;
            case "wishlist":
                return R.color.status_wishlist;
            default:
                return R.color.neon_blue;
        }
    }

    private void setupFab() {
        fabAddGame.setOnClickListener(v -> {
            navigateToSearch();
        });
    }

    private void navigateToSearch() {
        // Cambiar directamente al fragmento de búsqueda
        if (getActivity() != null) {
            // Obtener el BottomNavigationView y seleccionar el ítem de búsqueda
            BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.navigation_search);
            }
        }
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }
}