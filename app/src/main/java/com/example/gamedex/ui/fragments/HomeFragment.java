package com.example.gamedex.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.adapters.GameAdapter;
import com.example.gamedex.ui.viewmodels.HomeViewModel;

import java.util.ArrayList;

public class HomeFragment extends Fragment implements GameAdapter.OnGameClickListener {

    private RecyclerView recyclerPopularGames;
    private RecyclerView recyclerRecentGames;
    private RecyclerView recyclerRecommendedGames;
    private ProgressBar progressBarPopular;
    private ProgressBar progressBarRecent;
    private ProgressBar progressBarRecommended;
    private GameAdapter popularGamesAdapter;
    private GameAdapter recentGamesAdapter;
    private GameAdapter recommendedGamesAdapter;
    private HomeViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize RecyclerViews
        recyclerPopularGames = view.findViewById(R.id.recycler_popular_games);
        recyclerRecentGames = view.findViewById(R.id.recycler_recent_games);
        recyclerRecommendedGames = view.findViewById(R.id.recycler_recommended_games);

        // Initialize ProgressBars (necesitarías añadirlos al layout XML)
        progressBarPopular = view.findViewById(R.id.progress_bar_popular);
        progressBarRecent = view.findViewById(R.id.progress_bar_recent);
        progressBarRecommended = view.findViewById(R.id.progress_bar_recommended);

        setupRecyclerViews();
        setupViewModel();
    }

    private void setupRecyclerViews() {
        // Popular games
        popularGamesAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        recyclerPopularGames.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerPopularGames.setAdapter(popularGamesAdapter);

        // Recent games
        recentGamesAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        recyclerRecentGames.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerRecentGames.setAdapter(recentGamesAdapter);

        // Recommended games
        recommendedGamesAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        recyclerRecommendedGames.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerRecommendedGames.setAdapter(recommendedGamesAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.init();

        // Mostrar ProgressBars
        progressBarPopular.setVisibility(View.VISIBLE);
        progressBarRecent.setVisibility(View.VISIBLE);
        progressBarRecommended.setVisibility(View.VISIBLE);

        // Observar cambios en popularGames
        viewModel.getPopularGames().observe(getViewLifecycleOwner(), games -> {
            progressBarPopular.setVisibility(View.GONE);
            if (games != null && !games.isEmpty()) {
                popularGamesAdapter.updateGames(games);
            } else {
                Toast.makeText(requireContext(), "No se pudieron cargar los juegos populares", Toast.LENGTH_SHORT).show();
            }
        });

        // Observar cambios en recentGames
        viewModel.getRecentGames().observe(getViewLifecycleOwner(), games -> {
            progressBarRecent.setVisibility(View.GONE);
            if (games != null && !games.isEmpty()) {
                recentGamesAdapter.updateGames(games);
            } else {
                Toast.makeText(requireContext(), "No se pudieron cargar los juegos recientes", Toast.LENGTH_SHORT).show();
            }
        });

        // Observar cambios en recommendedGames
        viewModel.getRecommendedGames().observe(getViewLifecycleOwner(), games -> {
            progressBarRecommended.setVisibility(View.GONE);
            if (games != null && !games.isEmpty()) {
                recommendedGamesAdapter.updateGames(games);
            } else {
                Toast.makeText(requireContext(), "No se pudieron cargar las recomendaciones", Toast.LENGTH_SHORT).show();
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