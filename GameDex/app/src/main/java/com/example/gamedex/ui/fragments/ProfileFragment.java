package com.example.gamedex.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

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

public class ProfileFragment extends Fragment implements GameAdapter.OnGameClickListener {

    // Variables para vistas
    private TextView usernameTextView;
    private TextView gamesCountTextView;
    private TextView completedCountTextView;
    private RecyclerView favoriteGamesRecyclerView;
    private Button editProfileButton;
    private Button logoutButton;

    private GameAdapter favoriteGamesAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views - usando los IDs exactos como están en el XML
        usernameTextView = view.findViewById(R.id.text_username);
        gamesCountTextView = view.findViewById(R.id.text_games_count);
        completedCountTextView = view.findViewById(R.id.text_completed_count);
        favoriteGamesRecyclerView = view.findViewById(R.id.recycler_favorite_games);
        editProfileButton = view.findViewById(R.id.button_edit_profile);
        logoutButton = view.findViewById(R.id.button_logout);

        setupRecyclerView();
        setupButtons();
        loadDummyData();
    }

    private void setupRecyclerView() {
        favoriteGamesAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        favoriteGamesRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        favoriteGamesRecyclerView.setAdapter(favoriteGamesAdapter);
    }

    private void setupButtons() {
        editProfileButton.setOnClickListener(v -> {
            // Launch edit profile activity
            // Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            // startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> {
            // Handle logout
        });
    }

    private void loadDummyData() {
        // Load user profile info
        usernameTextView.setText("GamerUsername");

        // Load user stats
        gamesCountTextView.setText("42");
        completedCountTextView.setText("18");

        // Load favorite games
        List<Game> favoriteGames = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Game game = new Game("favorite_game_" + i, "Favorite Game " + (i + 1));
            game.setDeveloper("Developer " + (i % 3 + 1));
            game.setCoverUrl("https://via.placeholder.com/300x400?text=Favorite+" + (i + 1));
            game.setUserRating(4.5f);
            favoriteGames.add(game);
        }

        favoriteGamesAdapter.updateGames(favoriteGames);
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }
}