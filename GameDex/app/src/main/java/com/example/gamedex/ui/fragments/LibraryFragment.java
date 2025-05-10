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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.adapters.GameAdapter;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class LibraryFragment extends Fragment implements GameAdapter.OnGameClickListener {

    private RecyclerView recyclerLibrary;
    private TextView textEmptyLibrary;
    private ChipGroup filterChipGroup;
    private FloatingActionButton fabAddGame;
    private Toolbar toolbar;
    private GameAdapter gameAdapter;

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

        setupRecyclerView();
        setupFilterChips();
        setupFab();
        loadDummyData();
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

        for (String filter : filters) {
            Chip chip = new Chip(requireContext());
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setClickable(true);

            if (filter.equals(getString(R.string.all_games))) {
                chip.setChecked(true);
            }

            filterChipGroup.addView(chip);
        }

        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.size() > 0) {
                Chip chip = getView().findViewById(checkedIds.get(0));
                // Filter games based on selected chip
                // In a real app this would filter through ViewModel
            }
        });
    }

    private void setupFab() {
        fabAddGame.setOnClickListener(v -> {
            // Launch activity to add a new game
            // In a real app, we would start an AddGameActivity here
            // Intent intent = new Intent(requireContext(), AddGameActivity.class);
            // startActivity(intent);
        });
    }

    private void loadDummyData() {
        List<Game> libraryGames = new ArrayList<>();

        // Add some dummy games to the library
        for (int i = 0; i < 10; i++) {
            Game game = new Game("library_game_" + i, "Library Game " + (i + 1));
            game.setDeveloper("Developer " + (i % 5 + 1));
            game.setCoverUrl("https://via.placeholder.com/300x400?text=Game+" + (i + 1));
            game.setInLibrary(true);

            // Assign different statuses
            String status = null;
            if (i % 4 == 0) status = "playing";
            else if (i % 4 == 1) status = "completed";
            else if (i % 4 == 2) status = "backlog";
            else if (i % 4 == 3) status = "wishlist";
            game.setStatus(status);

            libraryGames.add(game);
        }

        gameAdapter.updateGames(libraryGames);

        // Show empty state if library is empty
        if (libraryGames.isEmpty()) {
            textEmptyLibrary.setVisibility(View.VISIBLE);
            recyclerLibrary.setVisibility(View.GONE);
        } else {
            textEmptyLibrary.setVisibility(View.GONE);
            recyclerLibrary.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }
}