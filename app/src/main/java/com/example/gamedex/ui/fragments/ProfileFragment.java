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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.firebase.FirebaseAuthService;
import com.example.gamedex.data.firebase.FirestoreService;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.activities.AuthActivity;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.adapters.GameAdapter;
import com.example.gamedex.ui.viewmodels.LibraryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment implements GameAdapter.OnGameClickListener {

    // Variables para vistas
    private TextView usernameTextView;
    private TextView emailTextView;
    private TextView gamesCountTextView;
    private TextView completedCountTextView;
    private TextView playingCountTextView;
    private TextView backlogCountTextView;
    private TextView wishlistCountTextView;
    private RecyclerView favoriteGamesRecyclerView;
    private Button editProfileButton;
    private Button logoutButton;
    private Button loginButton;
    private View statsCard;
    private View favoriteGamesCard;

    private GameAdapter favoriteGamesAdapter;
    private LibraryViewModel libraryViewModel;
    private FirebaseAuthService authService;
    private FirestoreService firestoreService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initServices();
        initViews(view);
        setupRecyclerView();
        setupButtons();
        observeAuthState();
        setupViewModel();
    }

    private void initServices() {
        authService = FirebaseAuthService.getInstance();
        firestoreService = FirestoreService.getInstance();
    }

    private void initViews(View view) {
        usernameTextView = view.findViewById(R.id.text_username);
        emailTextView = view.findViewById(R.id.text_email);
        gamesCountTextView = view.findViewById(R.id.text_games_count);
        completedCountTextView = view.findViewById(R.id.text_completed_count);
        playingCountTextView = view.findViewById(R.id.text_playing_count);
        backlogCountTextView = view.findViewById(R.id.text_backlog_count);
        wishlistCountTextView = view.findViewById(R.id.text_wishlist_count);
        favoriteGamesRecyclerView = view.findViewById(R.id.recycler_favorite_games);
        editProfileButton = view.findViewById(R.id.button_edit_profile);
        logoutButton = view.findViewById(R.id.button_logout);
        loginButton = view.findViewById(R.id.button_login);
        statsCard = view.findViewById(R.id.stats_card);
        favoriteGamesCard = view.findViewById(R.id.favorite_games_card);
    }

    private void setupRecyclerView() {
        favoriteGamesAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        favoriteGamesRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        favoriteGamesRecyclerView.setAdapter(favoriteGamesAdapter);
    }

    private void setupButtons() {
        editProfileButton.setOnClickListener(v -> showEditProfileDialog());

        logoutButton.setOnClickListener(v -> showLogoutDialog());

        loginButton.setOnClickListener(v -> navigateToAuth());
    }

    private void observeAuthState() {
        authService.getCurrentUserLiveData().observe(getViewLifecycleOwner(), this::updateUIForUser);
    }

    private void setupViewModel() {
        libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        // Observar todos los juegos de la biblioteca para actualizar estadísticas
        libraryViewModel.getAllLibraryGames().observe(getViewLifecycleOwner(), games -> {
            updateLocalStats(games);

            // Filtrar juegos favoritos (los que tienen una valoración de 4 o más)
            List<Game> favoriteGames = games.stream()
                    .filter(game -> game.getUserRating() != null && game.getUserRating() >= 4.0f)
                    .limit(10)
                    .collect(Collectors.toList());

            favoriteGamesAdapter.updateGames(favoriteGames);
        });

        // Si el usuario está autenticado, observar también las estadísticas de Firebase
        if (authService.isUserSignedIn()) {
            observeFirebaseStats();
        }
    }

    private void observeFirebaseStats() {
        firestoreService.getUserStats().observe(getViewLifecycleOwner(), this::updateStatsFromFirebase);
    }

    private void updateUIForUser(FirebaseUser user) {
        if (user != null) {
            // Usuario autenticado
            showAuthenticatedUI(user);
        } else {
            // Usuario no autenticado
            showUnauthenticatedUI();
        }
    }

    private void showAuthenticatedUI(FirebaseUser user) {
        // Mostrar información del usuario
        usernameTextView.setText(user.getDisplayName() != null ? user.getDisplayName() : "Usuario");
        emailTextView.setText(user.getEmail());
        emailTextView.setVisibility(View.VISIBLE);

        // Mostrar botones para usuario autenticado
        editProfileButton.setVisibility(View.VISIBLE);
        logoutButton.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.GONE);

        // Mostrar estadísticas y juegos favoritos
        statsCard.setVisibility(View.VISIBLE);
        favoriteGamesCard.setVisibility(View.VISIBLE);

        // Observar estadísticas de Firebase
        observeFirebaseStats();
    }

    private void showUnauthenticatedUI() {
        // Mostrar información básica
        usernameTextView.setText(R.string.guest_user);
        emailTextView.setVisibility(View.GONE);

        // Mostrar botones para usuario no autenticado
        editProfileButton.setVisibility(View.GONE);
        logoutButton.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);

        // Mostrar estadísticas locales pero ocultar favoritos si no hay datos
        statsCard.setVisibility(View.VISIBLE);
        favoriteGamesCard.setVisibility(View.VISIBLE);
    }

    private void updateLocalStats(List<Game> games) {
        // Actualizar contador de juegos
        int totalGames = games.size();
        gamesCountTextView.setText(String.valueOf(totalGames));

        // Contar juegos por estado
        int completedGames = (int) games.stream().filter(game -> "completed".equals(game.getStatus())).count();
        int playingGames = (int) games.stream().filter(game -> "playing".equals(game.getStatus())).count();
        int backlogGames = (int) games.stream().filter(game -> "backlog".equals(game.getStatus())).count();
        int wishlistGames = (int) games.stream().filter(game -> "wishlist".equals(game.getStatus())).count();

        completedCountTextView.setText(String.valueOf(completedGames));
        if (playingCountTextView != null) playingCountTextView.setText(String.valueOf(playingGames));
        if (backlogCountTextView != null) backlogCountTextView.setText(String.valueOf(backlogGames));
        if (wishlistCountTextView != null) wishlistCountTextView.setText(String.valueOf(wishlistGames));
    }

    private void updateStatsFromFirebase(Map<String, Integer> stats) {
        if (stats != null) {
            gamesCountTextView.setText(String.valueOf(stats.getOrDefault("total", 0)));
            completedCountTextView.setText(String.valueOf(stats.getOrDefault("completed", 0)));
            if (playingCountTextView != null) playingCountTextView.setText(String.valueOf(stats.getOrDefault("playing", 0)));
            if (backlogCountTextView != null) backlogCountTextView.setText(String.valueOf(stats.getOrDefault("backlog", 0)));
            if (wishlistCountTextView != null) wishlistCountTextView.setText(String.valueOf(stats.getOrDefault("wishlist", 0)));
        }
    }

    private void showEditProfileDialog() {
        if (!authService.isUserSignedIn()) {
            Snackbar.make(getView(), R.string.login_required, Snackbar.LENGTH_SHORT).show();
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        final com.google.android.material.textfield.TextInputEditText editUsername = view.findViewById(R.id.edit_username);

        // Establecer username actual
        String currentUsername = authService.getCurrentUsername();
        editUsername.setText(currentUsername);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_profile)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String newUsername = editUsername.getText().toString().trim();
                    if (!newUsername.isEmpty() && !newUsername.equals(currentUsername)) {
                        updateUsername(newUsername);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void updateUsername(String newUsername) {
        authService.updateUserProfile(newUsername, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess() {
                usernameTextView.setText(newUsername);
                Snackbar.make(getView(), R.string.profile_updated, Snackbar.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Snackbar.make(getView(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void showLogoutDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.logout)
                .setMessage(R.string.confirm_logout)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    authService.signOut();
                    Snackbar.make(getView(), R.string.logged_out, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void navigateToAuth() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        startActivity(intent);
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }
}