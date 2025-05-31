package com.example.gamedex.ui.fragments;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.activities.AuthActivity;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.activities.GamesByTagActivity;
import com.example.gamedex.ui.adapters.GameAdapter;
import com.example.gamedex.ui.adapters.ProfileTagsAdapter;
import com.example.gamedex.ui.viewmodels.LibraryViewModel;
import com.example.gamedex.ui.viewmodels.ProfileTagsViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment implements GameAdapter.OnGameClickListener, ProfileTagsAdapter.OnTagClickListener {

    // Variables para vistas existentes
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

    // NUEVAS variables para la sección de tags
    private RecyclerView recyclerUserTags;
    private TextView textTagsCount;
    private View emptyTagsLayout;
    private View tagsCard;

    private GameAdapter favoriteGamesAdapter;
    private ProfileTagsAdapter userTagsAdapter; // NUEVO adaptador para tags
    private LibraryViewModel libraryViewModel;
    private ProfileTagsViewModel profileTagsViewModel; // NUEVO ViewModel para tags
    private FirebaseAuthService authService;
    private FirestoreService firestoreService;
    private ExecutorService executorService;

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initServices();
        initViews(view);
        setupRecyclerViews();
        setupButtons();
        observeAuthState();
        setupViewModel();
    }

    private void initServices() {
        authService = FirebaseAuthService.getInstance();
        firestoreService = FirestoreService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void initViews(View view) {
        // Views existentes
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

        // NUEVAS views para tags
        recyclerUserTags = view.findViewById(R.id.recycler_user_tags);
        textTagsCount = view.findViewById(R.id.text_tags_count);
        emptyTagsLayout = view.findViewById(R.id.empty_tags_layout);
        tagsCard = view.findViewById(R.id.tags_card);
    }

    private void setupRecyclerViews() {
        // RecyclerView existente para juegos favoritos
        favoriteGamesAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        favoriteGamesRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        favoriteGamesRecyclerView.setAdapter(favoriteGamesAdapter);

        // NUEVO RecyclerView para etiquetas personalizadas
        userTagsAdapter = new ProfileTagsAdapter(requireContext(), new ArrayList<>(), this);
        recyclerUserTags.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerUserTags.setAdapter(userTagsAdapter);
        recyclerUserTags.setNestedScrollingEnabled(false);
    }

    private void setupButtons() {
        editProfileButton.setOnClickListener(v -> showEditProfileDialog());
        logoutButton.setOnClickListener(v -> performLogout());
        loginButton.setOnClickListener(v -> navigateToAuth());
    }

    private void observeAuthState() {
        authService.getCurrentUserLiveData().observe(getViewLifecycleOwner(), this::updateUIForUser);
    }

    private void setupViewModel() {
        libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);
        profileTagsViewModel = new ViewModelProvider(this).get(ProfileTagsViewModel.class); // NUEVO

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

        // NUEVO: Observar etiquetas del usuario
        setupTagsObserver();

        // Si el usuario está autenticado, observar también las estadísticas de Firebase
        if (authService.isUserSignedIn()) {
            observeFirebaseStats();
        }
    }

    // NUEVO método para configurar el observador de tags
    private void setupTagsObserver() {
        profileTagsViewModel.getUserTags().observe(getViewLifecycleOwner(), userTags -> {
            if (userTags != null && !userTags.isEmpty()) {
                // Filtrar solo las que tienen juegos asociados
                List<CustomTag> tagsWithGames = userTags.stream()
                        .filter(tag -> tag.getUsageCount() > 0)
                        .collect(Collectors.toList());

                userTagsAdapter.updateTags(tagsWithGames);

                // Actualizar contador
                int tagCount = tagsWithGames.size();
                textTagsCount.setText(tagCount + (tagCount == 1 ? " tag" : " tags"));

                // Mostrar/ocultar estado vacío
                if (tagCount > 0) {
                    recyclerUserTags.setVisibility(View.VISIBLE);
                    emptyTagsLayout.setVisibility(View.GONE);
                } else {
                    recyclerUserTags.setVisibility(View.GONE);
                    emptyTagsLayout.setVisibility(View.VISIBLE);
                }
            } else {
                // Sin tags - mostrar estado vacío
                recyclerUserTags.setVisibility(View.GONE);
                emptyTagsLayout.setVisibility(View.VISIBLE);
                textTagsCount.setText("0 tags");
            }
        });
    }

    private void observeFirebaseStats() {
        firestoreService.getUserStats().observe(getViewLifecycleOwner(), this::updateStatsFromFirebase);
    }

    private void updateUIForUser(FirebaseUser user) {
        if (user != null) {
            showAuthenticatedUI(user);
        } else {
            showUnauthenticatedUI();
        }
    }

    private void showAuthenticatedUI(FirebaseUser user) {
        usernameTextView.setText(user.getDisplayName() != null ? user.getDisplayName() : "Usuario");
        if (emailTextView != null) {
            emailTextView.setText(user.getEmail());
            emailTextView.setVisibility(View.VISIBLE);
        }

        editProfileButton.setVisibility(View.VISIBLE);
        logoutButton.setVisibility(View.VISIBLE);
        loginButton.setVisibility(View.GONE);

        statsCard.setVisibility(View.VISIBLE);
        favoriteGamesCard.setVisibility(View.VISIBLE);
        tagsCard.setVisibility(View.VISIBLE); // NUEVO

        observeFirebaseStats();
    }

    private void showUnauthenticatedUI() {
        usernameTextView.setText(R.string.guest_user);
        if (emailTextView != null) {
            emailTextView.setVisibility(View.GONE);
        }

        editProfileButton.setVisibility(View.GONE);
        logoutButton.setVisibility(View.GONE);
        loginButton.setVisibility(View.VISIBLE);

        statsCard.setVisibility(View.VISIBLE);
        favoriteGamesCard.setVisibility(View.VISIBLE);
        tagsCard.setVisibility(View.VISIBLE); // NUEVO - también mostrar para invitados
    }

    private void updateLocalStats(List<Game> games) {
        int totalGames = games.size();
        gamesCountTextView.setText(String.valueOf(totalGames));

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

    private void performLogout() {
        authService.signOut();
        navigateToAuth();
    }

    private void navigateToAuth() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }

    // NUEVOS métodos para manejar clicks en tags (implementa ProfileTagsAdapter.OnTagClickListener)
    @Override
    public void onTagClick(CustomTag tag) {
        navigateToGamesByTag(tag);
    }

    @Override
    public void onTagLongClick(CustomTag tag) {
        showTagOptionsDialog(tag);
    }

    // NUEVO método para navegar a la lista de juegos de una etiqueta
    private void navigateToGamesByTag(CustomTag tag) {
        Intent intent = new Intent(requireContext(), GamesByTagActivity.class);
        intent.putExtra("tag_name", tag.getName());
        intent.putExtra("tag_id", tag.getId());
        intent.putExtra("tag_color", tag.getColor());
        startActivity(intent);
    }

    // NUEVO método para mostrar opciones del tag (eliminar, editar)
    private void showTagOptionsDialog(CustomTag tag) {
        String[] options = {"Ver juegos", "Eliminar etiqueta"};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(tag.getName())
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            navigateToGamesByTag(tag);
                            break;
                        case 1:
                            confirmDeleteTag(tag);
                            break;
                    }
                })
                .show();
    }

    // NUEVO método para confirmar eliminación de etiqueta
    private void confirmDeleteTag(CustomTag tag) {
        String message = "¿Eliminar la etiqueta '" + tag.getName() + "'?";
        if (tag.getUsageCount() > 0) {
            message += "\n\nEsta etiqueta se eliminará de " + tag.getUsageCount() +
                    " juego" + (tag.getUsageCount() == 1 ? "" : "s") + ".";
        }

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmar eliminación")
                .setMessage(message)
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    profileTagsViewModel.deleteTag(tag);
                    Snackbar.make(getView(), "Etiqueta '" + tag.getName() + "' eliminada",
                            Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}