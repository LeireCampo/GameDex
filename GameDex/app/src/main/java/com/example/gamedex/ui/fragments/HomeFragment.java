package com.example.gamedex.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.adapters.GameAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements GameAdapter.OnGameClickListener {

    private RecyclerView recyclerPopularGames;
    private RecyclerView recyclerRecentGames;
    private RecyclerView recyclerRecommendedGames;
    private GameAdapter popularGamesAdapter;
    private GameAdapter recentGamesAdapter;
    private GameAdapter recommendedGamesAdapter;

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

        setupRecyclerViews();
        loadDummyData();
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

    private void loadDummyData() {
        // In a real app, this would come from a ViewModel and repository
        List<Game> popularGames = createDummyGames(5, "Popular");
        List<Game> recentGames = createDummyGames(5, "Recent");
        List<Game> recommendedGames = createDummyGames(5, "Recommended");

        popularGamesAdapter.updateGames(popularGames);
        recentGamesAdapter.updateGames(recentGames);
        recommendedGamesAdapter.updateGames(recommendedGames);
    }

    private List<Game> createDummyGames(int count, String prefix) {
        List<Game> games = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Game game = new Game("game_" + prefix.toLowerCase() + "_" + i, prefix + " Game " + (i + 1));
            game.setDeveloper("Developer " + (i + 1));
            game.setCoverUrl("https://via.placeholder.com/300x400?text=" + prefix + "+" + (i + 1));
            games.add(game);
        }

        return games;
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }
}