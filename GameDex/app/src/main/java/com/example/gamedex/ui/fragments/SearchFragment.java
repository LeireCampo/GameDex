package com.example.gamedex.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.adapters.GameAdapter;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment implements GameAdapter.OnGameClickListener {

    private RecyclerView recyclerSearchResults;
    private TextInputEditText searchEditText;
    private TextView textNoResults;
    private ProgressBar progressBar;
    private GameAdapter searchResultsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerSearchResults = view.findViewById(R.id.recycler_search_results);
        searchEditText = view.findViewById(R.id.search_edit_text);
        textNoResults = view.findViewById(R.id.text_no_results);
        progressBar = view.findViewById(R.id.progress_bar);

        setupRecyclerView();
        setupSearchInput();
    }

    private void setupRecyclerView() {
        searchResultsAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        recyclerSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerSearchResults.setAdapter(searchResultsAdapter);
    }

    private void setupSearchInput() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch(searchEditText.getText().toString());
                return true;
            }
            return false;
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            searchResultsAdapter.updateGames(new ArrayList<>());
            textNoResults.setVisibility(View.GONE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        textNoResults.setVisibility(View.GONE);

        // Simulate network delay
        requireView().postDelayed(() -> {
            progressBar.setVisibility(View.GONE);

            // In a real app, this would come from a ViewModel and repository
            List<Game> searchResults = searchDummyGames(query);
            searchResultsAdapter.updateGames(searchResults);

            if (searchResults.isEmpty()) {
                textNoResults.setVisibility(View.VISIBLE);
            } else {
                textNoResults.setVisibility(View.GONE);
            }
        }, 1000);
    }

    private List<Game> searchDummyGames(String query) {
        // Mock search functionality
        List<Game> results = new ArrayList<>();
        query = query.toLowerCase();

        // Generate some dummy search results
        for (int i = 0; i < 20; i++) {
            String title = "Game Title " + i;
            String developer = "Developer " + (i % 5);

            if (title.toLowerCase().contains(query) || developer.toLowerCase().contains(query)) {
                Game game = new Game("search_game_" + i, title);
                game.setDeveloper(developer);
                game.setCoverUrl("https://via.placeholder.com/300x400?text=Game+" + i);
                results.add(game);

                // Limit results
                if (results.size() >= 10) {
                    break;
                }
            }
        }

        return results;
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }
}