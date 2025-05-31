package com.example.gamedex.ui.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.Tag;
import com.example.gamedex.data.model.Store;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;
import com.example.gamedex.data.remote.model.StoreListResponse;
import com.example.gamedex.data.repository.TagRepository;
import com.example.gamedex.ui.adapters.FullScreenImageAdapter;
import com.example.gamedex.ui.adapters.ScreenshotAdapter;
import com.example.gamedex.ui.adapters.StoreAdapter;
import com.example.gamedex.ui.dialogs.TagSelectionDialogFragment;
import com.example.gamedex.ui.viewmodels.GameDetailViewModel;
import com.example.gamedex.ui.viewmodels.ProfileTagsViewModel;
import com.example.gamedex.util.NetworkUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class GameDetailActivity extends AppCompatActivity implements ScreenshotAdapter.OnScreenshotClickListener {

    private static final String TAG = "GameDetailActivity";

    // Views principales
    private ImageView imageGameCover;
    private TextView textGameTitle;
    private TextView textGameDeveloper;
    private MaterialButton buttonAddToLibrary;
    private MaterialButton buttonChangeStatus;
    private ChipGroup chipGroupTags;
    private RatingBar ratingBar;
    private TextView textPlatforms;
    private TextView textGenres;
    private TextView textReleaseDate;
    private TextView textDescription;
    private TextView textPublisher;
    private RatingBar ratingBarGlobal;
    private TextView textGlobalRating;
    private TextView textRatingValue;
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private View contentLayout;

    // Views para trailer/video
    private VideoView videoTrailer;
    private ImageView imageTrailerPreview;
    private ImageButton buttonPlayTrailer;
    private Button buttonWatchTrailer;
    private View cardTrailer;

    // Views para screenshots
    private RecyclerView recyclerScreenshots;
    private ScreenshotAdapter screenshotAdapter;
    private List<String> allScreenshotUrls = new ArrayList<>();

    // Views para tiendas
    private RecyclerView recyclerStores;
    private StoreAdapter storeAdapter;

    // Views para pantalla completa
    private FrameLayout fullscreenContainer;
    private ViewPager2 viewPagerScreenshots;
    private ImageButton buttonCloseFullscreen;
    private TextView textScreenshotCounter;

    private GameDetailViewModel viewModel;
    private ProfileTagsViewModel profileTagsViewModel; // NUEVO
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
        profileTagsViewModel = new ViewModelProvider(this).get(ProfileTagsViewModel.class); // NUEVO
        viewModel.init(getApplication(), gameId);

        setupObservers();
        setupClickListeners();
        loadGameData();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        // Views principales
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
        textPublisher = findViewById(R.id.text_publisher);
        ratingBarGlobal = findViewById(R.id.rating_bar_global);
        textGlobalRating = findViewById(R.id.text_global_rating);
        textRatingValue = findViewById(R.id.text_rating_value);
        progressBar = findViewById(R.id.progress_bar);

        // Views para trailer
        videoTrailer = findViewById(R.id.video_trailer);
        imageTrailerPreview = findViewById(R.id.image_trailer_preview);
        buttonPlayTrailer = findViewById(R.id.button_play_trailer);
        buttonWatchTrailer = findViewById(R.id.button_watch_trailer);
        cardTrailer = findViewById(R.id.card_trailer);

        // Views para screenshots
        recyclerScreenshots = findViewById(R.id.recycler_screenshots);

        // Views para tiendas
        recyclerStores = findViewById(R.id.recycler_stores);

        // Views para pantalla completa
        fullscreenContainer = findViewById(R.id.fullscreen_container);
        viewPagerScreenshots = findViewById(R.id.view_pager_screenshots);
        buttonCloseFullscreen = findViewById(R.id.button_close_fullscreen);
        textScreenshotCounter = findViewById(R.id.text_screenshot_counter);

        // Verificación de seguridad
        if (textGameTitle == null || textGameDeveloper == null ||
                textPlatforms == null || textGenres == null ||
                textReleaseDate == null || textDescription == null) {
            Log.e(TAG, "Uno o más TextViews no se han podido inicializar");
            Toast.makeText(this, "Error al cargar la interfaz", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadGameData() {
        progressBar.setVisibility(View.VISIBLE);
        contentLayout.setVisibility(View.GONE);
    }

    private void setupObservers() {
        // Observar datos locales primero
        viewModel.getGameWithTags().observe(this, gameWithTags -> {
            if (gameWithTags != null && gameWithTags.game != null) {
                Game game = gameWithTags.game;
                isGameInLibrary = game.isInLibrary();
                updateUI(game);
                updateTagsChips(gameWithTags.tags);

                // CORRECCIÓN: Configurar multimedia desde datos locales primero
                setupTrailer(game);
                setupStoresFromGame(game);
                setupScreenshotsFromGame(game);
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

                // CORRECCIÓN: Actualizar multimedia desde datos de la API
                setupTrailer(game);
                setupStoresFromGame(game);
                setupScreenshotsFromGame(game);

                Log.d(TAG, "Datos del juego actualizados desde API: " + game.getTitle());
            } else {
                Log.e(TAG, "Error: No se pudieron cargar los datos del juego desde la API");
                Snackbar.make(contentLayout, R.string.error_loading_game_details, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> loadGameData())
                        .show();
            }
        });

        // Observar screenshots de la API (prioridad sobre datos locales)
        viewModel.getScreenshots().observe(this, screenshots -> {
            if (screenshots != null && !screenshots.isEmpty()) {
                List<String> screenshotUrls = screenshots.stream()
                        .map(ScreenshotListResponse.Screenshot::getImageUrl)
                        .filter(url -> url != null && !url.isEmpty())
                        .collect(Collectors.toList());

                if (!screenshotUrls.isEmpty()) {
                    setupScreenshots(screenshotUrls);
                    Log.d(TAG, "Screenshots cargadas desde API: " + screenshotUrls.size());
                }
            }
        });

        // Observar tiendas de la API (prioridad sobre datos locales)
        viewModel.getStores().observe(this, gameStores -> {
            if (gameStores != null && !gameStores.isEmpty()) {
                setupStores(gameStores);
                Log.d(TAG, "Tiendas cargadas desde API: " + gameStores.size());
            }
        });
    }

    private void setupClickListeners() {
        buttonAddToLibrary.setOnClickListener(v -> {
            viewModel.toggleInLibrary();
            isGameInLibrary = !isGameInLibrary;
            updateLibraryStatus(isGameInLibrary, null);
        });

        buttonChangeStatus.setOnClickListener(v -> showStatusSelectionDialog());

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                viewModel.updateUserRating(rating);
                textRatingValue.setText(String.format("%.1f", rating));
                Snackbar.make(contentLayout, getString(R.string.rating_updated), Snackbar.LENGTH_SHORT).show();
            }
        });

        // Configurar botón cerrar pantalla completa
        if (buttonCloseFullscreen != null) {
            buttonCloseFullscreen.setOnClickListener(v ->
                    fullscreenContainer.setVisibility(View.GONE));
        }
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
            Log.e(TAG, "game is null");
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
            updatePlatformsText(game);
        }

        // Actualizar géneros
        if (textGenres != null) {
            updateGenresText(game);
        }

        // Actualizar fecha de lanzamiento
        if (textReleaseDate != null) {
            textReleaseDate.setText(game.getReleaseDate() != null ?
                    game.getReleaseDate() : getString(R.string.not_available));
        }

        // Actualizar publisher
        if (textPublisher != null) {
            textPublisher.setText(game.getPublisher() != null ?
                    game.getPublisher() : getString(R.string.unknown_developer));
        }

        updateRatingsDisplay(game);
        // Actualizar estado de biblioteca
        updateLibraryStatus(game.isInLibrary(), game.getStatus());
    }

    private void updatePlatformsText(Game game) {
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

    private void updateGenresText(Game game) {
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

    private void updateLibraryStatus(boolean inLibrary, String status) {
        if (inLibrary) {
            buttonAddToLibrary.setText(R.string.remove_from_library);
            buttonChangeStatus.setVisibility(View.VISIBLE);

            if (status != null) {
                String displayStatus = getString(R.string.status) + ": ";
                switch (status) {
                    case "playing":
                        displayStatus += getString(R.string.playing);
                        buttonChangeStatus.setStrokeColor(getColorStateList(R.color.status_playing));
                        break;
                    case "completed":
                        displayStatus += getString(R.string.completed);
                        buttonChangeStatus.setStrokeColor(getColorStateList(R.color.status_completed));
                        break;
                    case "backlog":
                        displayStatus += getString(R.string.backlog);
                        buttonChangeStatus.setStrokeColor(getColorStateList(R.color.status_backlog));
                        break;
                    case "wishlist":
                        displayStatus += getString(R.string.wishlist);
                        buttonChangeStatus.setStrokeColor(getColorStateList(R.color.status_wishlist));
                        break;
                    default:
                        displayStatus += getString(R.string.none);
                        buttonChangeStatus.setStrokeColor(getColorStateList(R.color.neon_blue));
                }
                buttonChangeStatus.setText(displayStatus);
            } else {
                buttonChangeStatus.setText(R.string.change_status);
                buttonChangeStatus.setStrokeColor(getColorStateList(R.color.neon_blue));
            }
        } else {
            buttonAddToLibrary.setText(R.string.add_to_library);
            buttonChangeStatus.setVisibility(View.GONE);
        }
    }

    // MÉTODO ACTUALIZADO para gestionar tags personalizados
    private void updateTagsChips(List<Tag> tags) {
        chipGroupTags.removeAllViews();

        // Obtener y mostrar etiquetas personalizadas
        profileTagsViewModel.getTagsForGame(gameId).observe(this, customTags -> {
            chipGroupTags.removeAllViews(); // Limpiar antes de añadir

            // Añadir chips para etiquetas personalizadas existentes
            if (customTags != null && !customTags.isEmpty()) {
                for (CustomTag customTag : customTags) {
                    Chip chip = new Chip(this);
                    chip.setText(customTag.getName());
                    chip.setCloseIcon(getDrawable(R.drawable.ic_close));
                    chip.setCloseIconVisible(true);

                    // Configurar color de la etiqueta
                    try {
                        int color = Color.parseColor(customTag.getColor());
                        chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
                        chip.setTextColor(getContrastColor(color));
                    } catch (Exception e) {
                        chip.setChipBackgroundColorResource(R.color.primary_green);
                        chip.setTextColor(ContextCompat.getColor(this, R.color.background_black));
                    }

                    chip.setOnCloseIconClickListener(v -> {
                        profileTagsViewModel.removeTagFromGame(gameId, customTag.getId());
                        Snackbar.make(contentLayout, "Etiqueta '" + customTag.getName() + "' eliminada",
                                Snackbar.LENGTH_SHORT).show();
                    });

                    chipGroupTags.addView(chip);
                }
            }

            // Añadir chips para etiquetas del sistema (Tag) si las hay
            if (tags != null && !tags.isEmpty()) {
                for (Tag tag : tags) {
                    Chip chip = new Chip(this);
                    chip.setText(tag.getName());
                    chip.setCloseIcon(getDrawable(R.drawable.ic_close));
                    chip.setCloseIconVisible(true);
                    chip.setChipBackgroundColorResource(R.color.accent_blue);
                    chip.setTextColor(ContextCompat.getColor(this, R.color.white));
                    chip.setOnCloseIconClickListener(v -> {
                        viewModel.removeTagFromGame(tag.getId());
                        Snackbar.make(contentLayout, getString(R.string.tag_removed), Snackbar.LENGTH_SHORT).show();
                    });
                    chipGroupTags.addView(chip);
                }
            }

            // Si no hay etiquetas, mostrar mensaje
            if ((customTags == null || customTags.isEmpty()) && (tags == null || tags.isEmpty())) {
                Chip noTagsChip = new Chip(this);
                noTagsChip.setText(R.string.no_tags);
                noTagsChip.setClickable(false);
                noTagsChip.setChipBackgroundColorResource(android.R.color.transparent);
                noTagsChip.setChipStrokeColorResource(R.color.border_color);
                noTagsChip.setChipStrokeWidth(2f);
                noTagsChip.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                chipGroupTags.addView(noTagsChip);
            }

            // SIEMPRE añadir el chip "Añadir etiqueta" al final
            Chip addChip = new Chip(this);
            addChip.setText(getString(R.string.add_tag));
            addChip.setChipIcon(ContextCompat.getDrawable(this, R.drawable.ic_add));
            addChip.setChipBackgroundColorResource(android.R.color.transparent);
            addChip.setChipStrokeColorResource(R.color.neon_blue);
            addChip.setChipStrokeWidth(2f);
            addChip.setTextColor(ContextCompat.getColor(this, R.color.neon_blue));
            addChip.setChipIconTintResource(R.color.neon_blue);
            addChip.setOnClickListener(v -> showTagSelectionDialog());
            chipGroupTags.addView(addChip);
        });
    }

    // NUEVO método para mostrar el diálogo de selección de etiquetas
    private void showTagSelectionDialog() {
        TagSelectionDialogFragment dialog = TagSelectionDialogFragment.newInstance(gameId);
        dialog.setOnTagsSelectedListener(new TagSelectionDialogFragment.OnTagsSelectedListener() {
            @Override
            public void onTagsSelected(List<CustomTag> selectedTags, List<CustomTag> tagsToRemove) {
                // Mostrar mensaje de confirmación
                int addedCount = selectedTags.size();
                int removedCount = tagsToRemove.size();

                String message = "";
                if (addedCount > 0 && removedCount > 0) {
                    message = addedCount + " etiquetas añadidas, " + removedCount + " eliminadas";
                } else if (addedCount > 0) {
                    message = addedCount + " etiqueta" + (addedCount == 1 ? " añadida" : "s añadidas");
                } else if (removedCount > 0) {
                    message = removedCount + " etiqueta" + (removedCount == 1 ? " eliminada" : "s eliminadas");
                } else {
                    message = "Sin cambios";
                }

                Snackbar.make(contentLayout, message, Snackbar.LENGTH_SHORT).show();
            }
        });
        dialog.show(getSupportFragmentManager(), "tag_selection");
    }

    // NUEVO método auxiliar para calcular color de contraste
    private int getContrastColor(int color) {
        // Calcular luminancia para determinar si usar texto blanco o negro
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    // Métodos para trailer/video - CORREGIDO
    private void setupTrailer(Game game) {
        if (game.getTrailerUrl() != null && !game.getTrailerUrl().isEmpty()) {
            cardTrailer.setVisibility(View.VISIBLE);

            // Cargar imagen de preview
            if (game.getCoverUrl() != null) {
                Glide.with(this)
                        .load(game.getCoverUrl())
                        .centerCrop()
                        .into(imageTrailerPreview);
            }

            buttonPlayTrailer.setOnClickListener(v -> playTrailer(game.getTrailerUrl()));
            buttonWatchTrailer.setOnClickListener(v -> openTrailerInBrowser(game.getTrailerUrl()));
        } else {
            // OCULTAR COMPLETAMENTE la sección de trailer si no hay video
            cardTrailer.setVisibility(View.GONE);
        }
    }

    private void playTrailer(String trailerUrl) {
        try {
            Uri uri = Uri.parse(trailerUrl);
            videoTrailer.setVideoURI(uri);
            videoTrailer.setVisibility(View.VISIBLE);
            imageTrailerPreview.setVisibility(View.GONE);
            buttonPlayTrailer.setVisibility(View.GONE);

            videoTrailer.setOnPreparedListener(mp -> videoTrailer.start());

            videoTrailer.setOnErrorListener((mp, what, extra) -> {
                openTrailerInBrowser(trailerUrl);
                return true;
            });
        } catch (Exception e) {
            openTrailerInBrowser(trailerUrl);
        }
    }

    private void openTrailerInBrowser(String trailerUrl) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerUrl));
            startActivity(intent);
        } catch (Exception e) {
            Snackbar.make(contentLayout, "No se pudo abrir el trailer", Snackbar.LENGTH_SHORT).show();
        }
    }

    // Métodos para screenshots
    private void setupScreenshots(List<String> screenshotUrls) {
        allScreenshotUrls.clear();
        allScreenshotUrls.addAll(screenshotUrls);

        if (screenshotAdapter == null) {
            screenshotAdapter = new ScreenshotAdapter(this, screenshotUrls, this);
            recyclerScreenshots.setLayoutManager(
                    new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            recyclerScreenshots.setAdapter(screenshotAdapter);
        } else {
            screenshotAdapter.updateScreenshots(screenshotUrls);
        }
    }

    private void setupScreenshotsFromGame(Game game) {
        if (game.getScreenshotsUrls() != null) {
            try {
                JSONArray screenshotsArray = new JSONArray(game.getScreenshotsUrls());
                List<String> screenshotUrls = new ArrayList<>();

                for (int i = 0; i < screenshotsArray.length(); i++) {
                    screenshotUrls.add(screenshotsArray.getString(i));
                }

                if (!screenshotUrls.isEmpty()) {
                    setupScreenshots(screenshotUrls);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error al parsear screenshots: " + e.getMessage());
            }
        }
    }

    @Override
    public void onScreenshotClick(String url, int position) {
        showFullScreenImage(url, position);
    }

    private void showFullScreenImage(String imageUrl, int position) {
        if (fullscreenContainer == null || viewPagerScreenshots == null) return;

        // Configurar adaptador
        FullScreenImageAdapter fullScreenAdapter = new FullScreenImageAdapter(this, allScreenshotUrls);
        viewPagerScreenshots.setAdapter(fullScreenAdapter);
        viewPagerScreenshots.setCurrentItem(position, false);

        // Mostrar contador
        updateScreenshotCounter(position + 1, allScreenshotUrls.size());

        // Listener para cambio de página
        ViewPager2.OnPageChangeCallback pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateScreenshotCounter(position + 1, allScreenshotUrls.size());
            }
        };
        viewPagerScreenshots.registerOnPageChangeCallback(pageChangeCallback);

        // Mostrar pantalla completa
        fullscreenContainer.setVisibility(View.VISIBLE);

        // Actualizar botón cerrar para desregistrar callback
        buttonCloseFullscreen.setOnClickListener(v -> {
            fullscreenContainer.setVisibility(View.GONE);
            viewPagerScreenshots.unregisterOnPageChangeCallback(pageChangeCallback);
        });
    }

    private void updateScreenshotCounter(int current, int total) {
        if (textScreenshotCounter != null) {
            textScreenshotCounter.setText(current + " / " + total);
        }
    }

    // Métodos para tiendas
    private void setupStores(List<StoreListResponse.GameStore> gameStores) {
        if (recyclerStores == null) return;

        if (gameStores == null || gameStores.isEmpty()) {
            recyclerStores.setVisibility(View.GONE);
            Log.d(TAG, "No hay datos de tiendas de la API");
            return;
        }

        List<Store> stores = new ArrayList<>();

        for (StoreListResponse.GameStore gameStore : gameStores) {
            if (gameStore.getStore() != null) {
                String storeName = gameStore.getStore().getName();
                String storeUrl = gameStore.getStoreUrl();

                // Si no hay URL específica, generar una genérica
                if (storeUrl == null || storeUrl.isEmpty() || storeUrl.equals("null")) {
                    storeUrl = getGenericStoreUrl(storeName);
                }

                // CORRECCIÓN: Validar mejor los datos antes de añadir
                if (storeName != null && !storeName.isEmpty() &&
                        storeUrl != null && !storeUrl.isEmpty() && !storeUrl.equals("null")) {

                    Store store = new Store(
                            storeName,
                            storeUrl,
                            gameStore.getStore().getImageBackground()
                    );
                    stores.add(store);
                    Log.d(TAG, "Añadida tienda desde API: " + storeName + " -> " + storeUrl);
                }
            }
        }

        if (!stores.isEmpty()) {
            setupStoreAdapter(stores);
            recyclerStores.setVisibility(View.VISIBLE);
            Log.d(TAG, "Configuradas " + stores.size() + " tiendas desde API");
        } else {
            recyclerStores.setVisibility(View.GONE);
            Log.d(TAG, "No se encontraron tiendas válidas en la API");
        }
    }

    private void setupStoresFromGame(Game game) {
        if (recyclerStores == null) return;

        String storesInfo = game.getStoresInfo();
        if (storesInfo == null || storesInfo.isEmpty()) {
            recyclerStores.setVisibility(View.GONE);
            Log.d(TAG, "No hay información de tiendas para: " + game.getTitle());
            return;
        }

        try {
            JSONArray storesArray = new JSONArray(storesInfo);
            List<Store> stores = new ArrayList<>();

            for (int i = 0; i < storesArray.length(); i++) {
                JSONObject storeObj = storesArray.getJSONObject(i);
                String name = storeObj.optString("name", "");
                String url = storeObj.optString("url", "");
                String iconUrl = storeObj.optString("domain", "");

                // CORRECCIÓN: Validar mejor las URLs y nombres
                if (!name.isEmpty() && !url.isEmpty() && !url.equals("null")) {
                    stores.add(new Store(name, url, iconUrl));
                    Log.d(TAG, "Añadida tienda: " + name + " -> " + url);
                }
            }

            if (!stores.isEmpty()) {
                setupStoreAdapter(stores);
                recyclerStores.setVisibility(View.VISIBLE);
                Log.d(TAG, "Configuradas " + stores.size() + " tiendas para: " + game.getTitle());
            } else {
                recyclerStores.setVisibility(View.GONE);
                Log.d(TAG, "No se encontraron tiendas válidas para: " + game.getTitle());
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parseando tiendas del juego " + game.getTitle() + ": " + e.getMessage());
            recyclerStores.setVisibility(View.GONE);
        }
    }

    private void setupStoreAdapter(List<Store> stores) {
        if (recyclerStores == null || stores == null || stores.isEmpty()) return;

        if (storeAdapter == null) {
            storeAdapter = new StoreAdapter(this, stores);

            // CORRECCIÓN: Configurar LayoutManager correctamente
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerStores.setLayoutManager(layoutManager);
            recyclerStores.setAdapter(storeAdapter);

            // Deshabilitar scroll anidado para evitar problemas
            recyclerStores.setNestedScrollingEnabled(false);

            Log.d(TAG, "StoreAdapter inicializado con " + stores.size() + " tiendas");
        } else {
            storeAdapter.updateStores(stores);
            Log.d(TAG, "StoreAdapter actualizado con " + stores.size() + " tiendas");
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getGenericStoreUrl(String storeName) {
        if (storeName == null || storeName.isEmpty()) {
            return null;
        }

        String lowerStoreName = storeName.toLowerCase().trim();

        // Steam y variantes
        if (lowerStoreName.contains("steam")) {
            return "https://store.steampowered.com/";
        }
        // PlayStation
        else if (lowerStoreName.contains("playstation") || lowerStoreName.contains("ps store") ||
                lowerStoreName.contains("psn")) {
            return "https://store.playstation.com/";
        }
        // Xbox/Microsoft
        else if (lowerStoreName.contains("xbox") || lowerStoreName.contains("microsoft") ||
                lowerStoreName.contains("windows store")) {
            return "https://www.microsoft.com/store/games/xbox/";
        }
        // Nintendo
        else if (lowerStoreName.contains("nintendo") || lowerStoreName.contains("eshop") ||
                lowerStoreName.contains("switch")) {
            return "https://www.nintendo.com/us/store/";
        }
        // Epic Games
        else if (lowerStoreName.contains("epic") || lowerStoreName.contains("epic games")) {
            return "https://store.epicgames.com/";
        }
        // GOG
        else if (lowerStoreName.contains("gog")) {
            return "https://www.gog.com/";
        }
        // Origin/EA
        else if (lowerStoreName.contains("origin") || lowerStoreName.contains("ea app")) {
            return "https://www.ea.com/games";
        }
        // Ubisoft
        else if (lowerStoreName.contains("ubisoft") || lowerStoreName.contains("uplay")) {
            return "https://store.ubisoft.com/";
        }
        // Battle.net/Blizzard
        else if (lowerStoreName.contains("battle") || lowerStoreName.contains("blizzard")) {
            return "https://shop.battle.net/";
        }
        // Amazon
        else if (lowerStoreName.contains("amazon")) {
            return "https://www.amazon.com/videogames/";
        }
        // Google Play
        else if (lowerStoreName.contains("google") || lowerStoreName.contains("play store")) {
            return "https://play.google.com/store/games/";
        }
        // App Store
        else if (lowerStoreName.contains("app store") || lowerStoreName.contains("apple") ||
                lowerStoreName.contains("ios")) {
            return "https://apps.apple.com/us/genre/ios-games/id6014";
        }
        // Itch.io
        else if (lowerStoreName.contains("itch")) {
            return "https://itch.io/";
        }
        // Humble Store
        else if (lowerStoreName.contains("humble")) {
            return "https://www.humblebundle.com/store";
        }
        else {
            // Para tiendas desconocidas, crear una búsqueda genérica
            String searchTerm = storeName.replace(" ", "+").replace("store", "").trim();
            return "https://www.google.com/search?q=" + searchTerm + "+game+store";
        }
    }

    private void updateRatingsDisplay(Game game) {
        // Valoración del usuario
        if (ratingBar != null && textRatingValue != null) {
            if (game.getUserRating() != null && game.getUserRating() > 0) {
                ratingBar.setRating(game.getUserRating());
                textRatingValue.setText(String.format("%.1f", game.getUserRating()));
            } else {
                ratingBar.setRating(0f);
                textRatingValue.setText("Sin valorar");
            }
        }

        // Valoración global (sin mostrar número de valoraciones)
        if (ratingBarGlobal != null && textGlobalRating != null) {
            if (game.getGlobalRating() != null && game.getGlobalRating() > 0) {
                ratingBarGlobal.setRating(game.getGlobalRating());
                textGlobalRating.setText(String.format("%.1f", game.getGlobalRating()));
            } else {
                ratingBarGlobal.setRating(0f);
                textGlobalRating.setText("De momento no tiene valoraciones");
            }
        }
    }
}