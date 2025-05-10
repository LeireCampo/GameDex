package com.example.gamedex.ui.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.Tag;
import com.example.gamedex.ui.viewmodels.GameDetailViewModel;
import com.example.gamedex.util.NetworkUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class GameDetailActivity extends AppCompatActivity {

    private ImageView imageGameCover;
    private TextView textGameTitle;
    private TextView textGameDeveloper;
    private Button buttonAddToLibrary;
    private Button buttonChangeStatus;
    private ChipGroup chipGroupTags;
    private RatingBar ratingBar;
    private TextView textPlatforms;
    private TextView textGenres;
    private TextView textReleaseDate;
    private TextView textDescription;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private View contentLayout;

    private GameDetailViewModel viewModel;
    private String gameId;
    private boolean isGameInLibrary = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        initViews();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        gameId = getIntent().getStringExtra("game_id");
        if (gameId == null) {
            Toast.makeText(this, "Error: Game ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Verificar conexión a Internet
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Snackbar.make(contentLayout, R.string.no_internet_connection, Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, v -> loadGameData())
                    .show();
        }

        viewModel = new ViewModelProvider(this).get(GameDetailViewModel.class);
        viewModel.init(getApplication(), gameId);

        setupObservers();
        setupClickListeners();
        loadGameData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        contentLayout = findViewById(R.id.content_layout);
        imageGameCover = findViewById(R.id.image_game_cover);
        textGameTitle = findViewById(R.id.text_game_title);
        textGameDeveloper = findViewById(R.id.text_game_developer);
        buttonAddToLibrary = findViewById(R.id.button_add_to_library);
        buttonChangeStatus = findViewById(R.id.button_change_status);
        chipGroupTags = findViewById(R.id.chip_group_tags);
        ratingBar = findViewById(R.id.rating_bar);
        textPlatforms = findViewById(R.id.text_platforms);
        textGenres = findViewById(R.id.text_genres);
        textReleaseDate = findViewById(R.id.text_release_date);
        textDescription = findViewById(R.id.text_description);
        progressBar = findViewById(R.id.progress_bar);

        // Verificación de seguridad
        if (textGameTitle == null || textGameDeveloper == null ||
                textPlatforms == null || textGenres == null ||
                textReleaseDate == null || textDescription == null) {
            Log.e("GameDetailActivity", "Uno o más TextViews no se han podido inicializar");
            // Podemos añadir aquí un Toast para mostrar un error al usuario
            Toast.makeText(this, "Error al cargar la interfaz", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGameData() {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
    }

    private void setupObservers() {
        // Observar datos locales
        viewModel.getGameWithTags().observe(this, gameWithTags -> {
            if (gameWithTags != null && gameWithTags.game != null) {
                Game game = gameWithTags.game;
                isGameInLibrary = game.isInLibrary();
                updateUI(game);
                updateTagsChips(gameWithTags.tags);
            }
        });

        // Observar datos actualizados de la API
        viewModel.getGameDetails().observe(this, game -> {
            progressBar.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);

            if (game != null) {
                // Mantener el estado de biblioteca y tags
                game.setInLibrary(isGameInLibrary);
                updateUI(game);
            } else {
                // Error al cargar los datos del juego
                Snackbar.make(contentLayout, R.string.error_loading_game_details, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> loadGameData())
                        .show();
            }
        });
    }

    private void setupClickListeners() {
        buttonAddToLibrary.setOnClickListener(v -> {
            viewModel.toggleInLibrary();
            isGameInLibrary = !isGameInLibrary;
            updateLibraryStatus(isGameInLibrary, null);
        });

        buttonChangeStatus.setOnClickListener(v -> {
            showStatusSelectionDialog();
        });

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                viewModel.updateUserRating(rating);
                Snackbar.make(contentLayout, getString(R.string.rating_updated), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void showStatusSelectionDialog() {
        String[] statuses = {"playing", "completed", "backlog", "wishlist"};
        String[] statusDisplay = {
                getString(R.string.playing),
                getString(R.string.completed),
                getString(R.string.backlog),
                getString(R.string.wishlist)
        };

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle(R.string.change_status)
                .setItems(statusDisplay, (dialog, which) -> {
                    viewModel.updateGameStatus(statuses[which]);
                    updateLibraryStatus(true, statuses[which]);
                    Snackbar.make(contentLayout, getString(R.string.status_updated), Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    private void updateUI(Game game) {
        // Comprobación de seguridad para evitar NPE
        if (game == null) {
            Log.e("GameDetailActivity", "game is null");
            return;
        }

        // Usar operador de seguridad para evitar NPE
        if (textGameTitle != null) {
            textGameTitle.setText(game.getTitle());
        }

        if (textGameDeveloper != null) {
            textGameDeveloper.setText(game.getDeveloper() != null ?
                    game.getDeveloper() : getString(R.string.unknown_developer));
        }

        // Cargar la imagen con verificación null
        if (imageGameCover != null && game.getCoverUrl() != null && !game.getCoverUrl().isEmpty()) {
            Glide.with(this)
                    .load(game.getCoverUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(imageGameCover);
        } else if (imageGameCover != null) {
            imageGameCover.setImageResource(R.drawable.ic_launcher_background);
        }

        // Actualizar descripción
        if (textDescription != null) {
            textDescription.setText(game.getDescription() != null ?
                    game.getDescription() : getString(R.string.no_description_available));
        }

        // Actualizar plataformas
        if (textPlatforms != null) {
            try {
                if (game.getPlatforms() != null) {
                    JSONArray platforms = new JSONArray(game.getPlatforms());
                    StringBuilder platformsText = new StringBuilder();
                    for (int i = 0; i < platforms.length(); i++) {
                        if (i > 0) platformsText.append(", ");
                        platformsText.append(platforms.getString(i));
                    }
                    textPlatforms.setText(platformsText.toString());
                } else {
                    textPlatforms.setText(R.string.not_available);
                }
            } catch (JSONException e) {
                textPlatforms.setText(R.string.not_available);
            }
        }

        // Actualizar géneros
        if (textGenres != null) {
            try {
                if (game.getGenres() != null) {
                    JSONArray genres = new JSONArray(game.getGenres());
                    StringBuilder genresText = new StringBuilder();
                    for (int i = 0; i < genres.length(); i++) {
                        if (i > 0) genresText.append(", ");
                        genresText.append(genres.getString(i));
                    }
                    textGenres.setText(genresText.toString());
                } else {
                    textGenres.setText(R.string.not_available);
                }
            } catch (JSONException e) {
                textGenres.setText(R.string.not_available);
            }
        }

        // Actualizar fecha de lanzamiento
        if (textReleaseDate != null) {
            textReleaseDate.setText(game.getReleaseDate() != null ?
                    game.getReleaseDate() : getString(R.string.not_available));
        }

        // Actualizar calificación
        if (ratingBar != null) {
            if (game.getUserRating() != null) {
                ratingBar.setRating(game.getUserRating());
            } else {
                ratingBar.setRating(0f);
            }
        }

        // Actualizar estado de biblioteca
        updateLibraryStatus(game.isInLibrary(), game.getStatus());
    }

    private void updateLibraryStatus(boolean inLibrary, String status) {
        if (inLibrary) {
            buttonAddToLibrary.setText(R.string.remove_from_library);
            buttonChangeStatus.setVisibility(View.VISIBLE);

            if (status != null) {
                String displayStatus = getString(R.string.status) + ": ";
                switch (status) {
                    case "playing":
                        displayStatus += getString(R.string.playing);
                        break;
                    case "completed":
                        displayStatus += getString(R.string.completed);
                        break;
                    case "backlog":
                        displayStatus += getString(R.string.backlog);
                        break;
                    case "wishlist":
                        displayStatus += getString(R.string.wishlist);
                        break;
                    default:
                        displayStatus += getString(R.string.none);
                }
                buttonChangeStatus.setText(displayStatus);
            } else {
                buttonChangeStatus.setText(R.string.change_status);
            }
        } else {
            buttonAddToLibrary.setText(R.string.add_to_library);
            buttonChangeStatus.setVisibility(View.GONE);
        }
    }

    private void updateTagsChips(List<Tag> tags) {
        chipGroupTags.removeAllViews();

        if (tags == null || tags.isEmpty()) {
            Chip chip = new Chip(this);
            chip.setText(R.string.no_tags);
            chip.setClickable(false);
            chipGroupTags.addView(chip);
            return;
        }

        for (Tag tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag.getName());
            chip.setCloseIcon(getDrawable(R.drawable.ic_close));
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                viewModel.removeTagFromGame(tag.getId());
                Snackbar.make(contentLayout, getString(R.string.tag_removed), Snackbar.LENGTH_SHORT).show();
            });
            chipGroupTags.addView(chip);
        }

        // Añadir chip "Añadir etiqueta"
        Chip addChip = new Chip(this);
        addChip.setText(getString(R.string.add_tag));
        addChip.setChipBackgroundColorResource(android.R.color.transparent);
        addChip.setChipStrokeWidth(1f);
        addChip.setOnClickListener(v -> {
            showAddTagDialog();
        });
        chipGroupTags.addView(addChip);
    }

    private void showAddTagDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_tag, null);
        final com.google.android.material.textfield.TextInputEditText editTagName = view.findViewById(R.id.edit_tag_name);

        builder.setTitle(R.string.add_tag)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    String tagName = editTagName.getText().toString().trim();
                    if (!tagName.isEmpty()) {
                        // En una app real, deberías usar un ViewModel para Tags
                        // Aquí, simplemente simulamos la creación de una etiqueta
                        Tag newTag = new Tag(tagName, "#FF5722");
                        newTag.setId((int) (System.currentTimeMillis() % 1000));
                        viewModel.addTagToGame(newTag.getId());
                        Snackbar.make(contentLayout, getString(R.string.tag_added), Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}