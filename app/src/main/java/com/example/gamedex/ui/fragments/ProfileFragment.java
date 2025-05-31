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
import com.example.gamedex.data.repository.TagRepository;
import com.example.gamedex.ui.activities.AuthActivity;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.adapters.CustomTagAdapter;
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

        // CAMBIO PRINCIPAL: Logout directo sin confirmación
        logoutButton.setOnClickListener(v -> performLogout());

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
        if (emailTextView != null) {
            emailTextView.setText(user.getEmail());
            emailTextView.setVisibility(View.VISIBLE);
        }

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
        if (emailTextView != null) {
            emailTextView.setVisibility(View.GONE);
        }

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

    // MÉTODO PRINCIPAL MODIFICADO: Logout directo sin confirmación
    private void performLogout() {
        // Realizar logout
        authService.signOut();

        // Navegar inmediatamente a AuthActivity
        navigateToAuth();
    }

    private void navigateToAuth() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        // Limpiar la pila de actividades para que no se pueda volver atrás
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        // Finalizar la actividad actual
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

    // Agregar estas líneas en ProfileFragment.java

    // En las variables de clase, añadir:
    private RecyclerView recyclerUserTags;
    private CustomTagViewModel customTagViewModel;
    private CustomTagAdapter userTagsAdapter;

// En initViews(), añadir:
    recyclerUserTags = view.findViewById(R.id.recycler_user_tags);

    // En setupViewModel(), añadir al final:
    setupCustomTagsViewModel();

    // Agregar este método al ProfileFragment:
    private void setupCustomTagsViewModel() {
        customTagViewModel = new ViewModelProvider(this).get(CustomTagViewModel.class);
        customTagViewModel.init(requireActivity().getApplication());

        // Configurar RecyclerView para etiquetas del usuario
        userTagsAdapter = new CustomTagAdapter(requireContext(), new ArrayList<>(), null);
        recyclerUserTags.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerUserTags.setAdapter(userTagsAdapter);

        // Observar etiquetas del usuario
        customTagViewModel.getUserCustomTags().observe(getViewLifecycleOwner(), tags -> {
            if (tags != null) {
                userTagsAdapter.updateTags(tags);
            }
        });
    }

    private RecyclerView recyclerUserTags;
    private TagAdapter userTagsAdapter;
    private TagRepository tagRepository;
    private View tagsCard;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initServices();
        initViews(view);
        setupRecyclerView();
        setupTagsRecyclerView(); // Nuevo método
        setupButtons();
        observeAuthState();
        setupViewModel();
        setupTagsObserver(); // Nuevo método
    }

    private void initViews(View view) {
        // ... código existente ...

        // Nuevas vistas para tags
        recyclerUserTags = view.findViewById(R.id.recycler_user_tags);
        tagsCard = view.findViewById(R.id.tags_card);
    }

    private void setupTagsRecyclerView() {
        userTagsAdapter = new TagAdapter(requireContext(), new ArrayList<>(), this::onTagClick);
        recyclerUserTags.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerUserTags.setAdapter(userTagsAdapter);
    }

    private void setupTagsObserver() {
        if (tagRepository == null) {
            tagRepository = new TagRepository(getApplication());
        }

        // Observar tags del usuario con contador de juegos
        tagRepository.getTagStatistics().observe(getViewLifecycleOwner(), tagStats -> {
            if (tagStats != null && !tagStats.isEmpty()) {
                userTagsAdapter.updateTags(tagStats);
                tagsCard.setVisibility(View.VISIBLE);
            } else {
                tagsCard.setVisibility(View.GONE);
            }
        });
    }

    private void onTagClick(String tagName, int gameCount) {
        // Mostrar diálogo con opciones para el tag
        showTagOptionsDialog(tagName, gameCount);
    }

    private void showTagOptionsDialog(String tagName, int gameCount) {
        String[] options = {
                "Ver juegos con esta etiqueta (" + gameCount + ")",
                "Eliminar etiqueta"
        };

        androidx.appcompat.app.AlertDialog.Builder builder =
                new androidx.appcompat.app.AlertDialog.Builder(requireContext());

        builder.setTitle("Etiqueta: " + tagName)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showGamesWithTag(tagName);
                            break;
                        case 1:
                            confirmDeleteTag(tagName);
                            break;
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showGamesWithTag(String tagName) {
        // Obtener el ID del tag y navegar a GamesByTagActivity
        executorService.execute(() -> {
            try {
                Tag tag = tagRepository.getTagByName(tagName);
                if (tag != null) {
                    requireActivity().runOnUiThread(() -> {
                        Intent intent = GamesByTagActivity.newIntent(requireContext(), tagName, tag.getId());
                        startActivity(intent);
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Snackbar.make(getView(), "Error: No se encontró la etiqueta",
                                Snackbar.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Snackbar.make(getView(), "Error al abrir los juegos",
                            Snackbar.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmDeleteTag(String tagName) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Eliminar etiqueta")
                .setMessage("¿Estás seguro de que quieres eliminar la etiqueta '" + tagName + "'?\n\n" +
                        "Esta acción se aplicará a todos los juegos que tengan esta etiqueta.")
                .setPositiveButton("Eliminar", (dialog, which) -> deleteTag(tagName))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void deleteTag(String tagName) {
        executorService.execute(() -> {
            try {
                Tag tag = tagRepository.getTagByName(tagName);
                if (tag != null) {
                    tagRepository.deleteTagIfEmpty(tag, new TagRepository.TagDeleteCallback() {
                        @Override
                        public void onSuccess() {
                            requireActivity().runOnUiThread(() -> {
                                Snackbar.make(getView(), "Etiqueta eliminada correctamente",
                                        Snackbar.LENGTH_SHORT).show();
                            });
                        }

                        @Override
                        public void onError(String error) {
                            requireActivity().runOnUiThread(() -> {
                                Snackbar.make(getView(), error, Snackbar.LENGTH_LONG).show();
                            });
                        }
                    });
                }
            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    Snackbar.make(getView(), "Error al eliminar la etiqueta",
                            Snackbar.LENGTH_SHORT).show();
                });
            }
        });
    }

}