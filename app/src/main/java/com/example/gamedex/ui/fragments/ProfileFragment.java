package com.example.gamedex.ui.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.activities.GameDetailActivity;
import com.example.gamedex.ui.adapters.GameAdapter;
import com.example.gamedex.ui.viewmodels.LibraryViewModel;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileFragment extends Fragment implements GameAdapter.OnGameClickListener {

    // Variables para vistas
    private TextView usernameTextView;
    private TextView gamesCountTextView;
    private TextView completedCountTextView;
    private RecyclerView favoriteGamesRecyclerView;
    private Button editProfileButton;
    private Button logoutButton;
    private ImageView profileBackgroundImageView;
    private ShapeableImageView profileImageView;
    private ImageButton changeBackgroundButton;

    private GameAdapter favoriteGamesAdapter;
    private LibraryViewModel libraryViewModel;

    // Para manejar los permisos de almacenamiento
    private static final int PERMISSION_REQUEST_CODE = 100;

    // Para manejar el resultado de seleccionar imagen de la galería
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar el launcher para seleccionar imagen
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Cargar la imagen seleccionada en el fondo
                            loadBackgroundImage(selectedImageUri);
                            // Guardar la URI para futuro uso
                            saveBackgroundImageUri(selectedImageUri.toString());
                        }
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        usernameTextView = view.findViewById(R.id.text_username);
        gamesCountTextView = view.findViewById(R.id.text_games_count);
        completedCountTextView = view.findViewById(R.id.text_completed_count);
        favoriteGamesRecyclerView = view.findViewById(R.id.recycler_favorite_games);
        editProfileButton = view.findViewById(R.id.button_add_to_library);
        logoutButton = view.findViewById(R.id.button_logout);
        profileBackgroundImageView = view.findViewById(R.id.image_profile_background);
        profileImageView = view.findViewById(R.id.image_profile);
        changeBackgroundButton = view.findViewById(R.id.button_change_background);

        // Cargar nombre de usuario guardado
        if (getActivity() != null) {
            String savedUsername = getActivity().getSharedPreferences("profile_prefs",
                            getActivity().MODE_PRIVATE)
                    .getString("username", getString(R.string.username_placeholder));

            if (savedUsername != null) {
                usernameTextView.setText(savedUsername);
            }
        }

        setupViewModel();
        setupRecyclerView();
        setupButtons();
        loadSavedBackgroundImage();
    }

    private void setupViewModel() {
        libraryViewModel = new ViewModelProvider(this).get(LibraryViewModel.class);

        // Observar todos los juegos de la biblioteca para actualizar estadísticas
        libraryViewModel.getAllLibraryGames().observe(getViewLifecycleOwner(), games -> {
            updateStats(games);

            // Filtrar juegos favoritos (los que tienen una valoración de 4 o más)
            List<Game> favoriteGames = games.stream()
                    .filter(game -> game.getUserRating() != null && game.getUserRating() >= 4.0f)
                    .limit(10)
                    .collect(Collectors.toList());

            favoriteGamesAdapter.updateGames(favoriteGames);
        });
    }

    private void updateStats(List<Game> games) {
        // Actualizar contador de juegos
        int totalGames = games.size();
        gamesCountTextView.setText(String.valueOf(totalGames));

        // Contar juegos completados
        int completedGames = (int) games.stream()
                .filter(game -> "completed".equals(game.getStatus()))
                .count();
        completedCountTextView.setText(String.valueOf(completedGames));
    }

    private void setupRecyclerView() {
        favoriteGamesAdapter = new GameAdapter(requireContext(), new ArrayList<>(), this);
        favoriteGamesRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        favoriteGamesRecyclerView.setAdapter(favoriteGamesAdapter);
    }

    private void setupButtons() {
        editProfileButton.setOnClickListener(v -> {
            showEditProfileDialog();
        });

        logoutButton.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.logout)
                    .setMessage(R.string.confirm_logout)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        // En una app real, implementarías la lógica de cierre de sesión
                        Snackbar.make(v, R.string.logged_out, Snackbar.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });

        changeBackgroundButton.setOnClickListener(v -> {
            // Verificar y solicitar permisos si es necesario
            if (checkAndRequestPermissions()) {
                openImagePicker();
            }
        });

        profileImageView.setOnClickListener(v -> {
            // Aquí podrías implementar la selección de imagen de perfil
            // Similar a la selección de fondo
            Snackbar.make(v, "Selección de imagen de perfil no implementada", Snackbar.LENGTH_SHORT).show();
        });
    }

    private boolean checkAndRequestPermissions() {
        // En Android 13+ se usa un permiso específico para imágenes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        // En versiones anteriores se usa el permiso de almacenamiento
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PERMISSION_REQUEST_CODE);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.permission_required)
                        .setMessage(R.string.storage_permission_message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadBackgroundImage(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .centerCrop()
                .error(R.color.md_theme_primary)
                .into(profileBackgroundImageView);
    }

    private void saveBackgroundImageUri(String uriString) {
        // Guardar en SharedPreferences
        if (getActivity() != null) {
            getActivity().getSharedPreferences("profile_prefs", getActivity().MODE_PRIVATE)
                    .edit()
                    .putString("background_image_uri", uriString)
                    .apply();
        }
    }

    private void loadSavedBackgroundImage() {
        if (getActivity() != null) {
            String savedUriString = getActivity().getSharedPreferences("profile_prefs",
                            getActivity().MODE_PRIVATE)
                    .getString("background_image_uri", null);

            if (savedUriString != null) {
                try {
                    Uri savedUri = Uri.parse(savedUriString);
                    loadBackgroundImage(savedUri);
                } catch (Exception e) {
                    // Si hay algún problema al cargar la imagen guardada
                    Toast.makeText(getContext(), R.string.error_loading_image, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void showEditProfileDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        final com.google.android.material.textfield.TextInputEditText editUsername =
                view.findViewById(R.id.edit_username);

        // Establecer username actual
        editUsername.setText(usernameTextView.getText());

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_profile)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String newUsername = editUsername.getText().toString().trim();
                    if (!newUsername.isEmpty()) {
                        usernameTextView.setText(newUsername);

                        // Guardar en SharedPreferences
                        if (getActivity() != null) {
                            getActivity().getSharedPreferences("profile_prefs", getActivity().MODE_PRIVATE)
                                    .edit()
                                    .putString("username", newUsername)
                                    .apply();
                        }

                        Snackbar.make(getView(), R.string.profile_updated, Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(requireContext(), GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
    }
}