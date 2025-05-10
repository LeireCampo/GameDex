package com.example.gamedex.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;

public class SearchFragment extends Fragment implements GameAdapter.OnGameClickListener {

    private RecyclerView recyclerSearchResults;
    private TextInputEditText searchEditText;
    private TextView textNoResults;
    private ProgressBar progressBar;
    private GameAdapter searchResultsAdapter;
    private SearchViewModel viewModel;

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

        setupViewModel();
        setupRecyclerView();
        setupSearchInput();
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

        viewModel.searchGames(query).observe(getViewLifecycleOwner(), searchResults -> {
            progressBar.setVisibility(View.GONE);

            if (searchResults != null) {
                searchResultsAdapter.updateGames(searchResults);

                if (searchResults.isEmpty()) {
                    textNoResults.setVisibility(View.VISIBLE);
                } else {
                    textNoResults.setVisibility(View.GONE);
                }
            } else {
                searchResultsAdapter.updateGames(new ArrayList<>());
                Toast.makeText(requireContext(), "Error al buscar juegos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }
}