package com.example.gamedex.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.Tag;
import com.example.gamedex.data.model.Store;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;
import com.example.gamedex.data.remote.model.StoreListResponse;
import com.example.gamedex.ui.adapters.FullScreenImageAdapter;
import com.example.gamedex.ui.adapters.ScreenshotAdapter;
import com.example.gamedex.ui.adapters.StoreAdapter;
import com.example.gamedex.ui.viewmodels.GameDetailViewModel;
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
                setupTrailer(game);
                setupScreenshotsFromGame(game);
                setupStoresFromGame(game);
            } else {
                // Error al cargar los datos del juego
                Snackbar.make(contentLayout, R.string.error_loading_game_details, Snackbar.LENGTH_LONG)
                        .setAction(R.string.retry, v -> loadGameData())
                        .show();
            }
        });

        // Observar screenshots de la API
        viewModel.getScreenshots().observe(this, screenshots -> {
            if (screenshots != null && !screenshots.isEmpty()) {
                List<String> screenshotUrls = screenshots.stream()
                        .map(ScreenshotListResponse.Screenshot::getImageUrl)
                        .collect(Collectors.toList());
                setupScreenshots(screenshotUrls);
            }
        });

        // Observar tiendas de la API
        viewModel.getStores().observe(this, gameStores -> {
            if (gameStores != null && !gameStores.isEmpty()) {
                setupStores(gameStores);
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

        // Actualizar calificación del usuario
        if (ratingBar != null) {
            if (game.getUserRating() != null) {
                ratingBar.setRating(game.getUserRating());
                textRatingValue.setText(String.format("%.1f", game.getUserRating()));
            } else {
                ratingBar.setRating(0f);
                textRatingValue.setText("0.0");
            }
        }

        // Actualizar calificación global
        if (ratingBarGlobal != null && textGlobalRating != null) {
            if (game.getGlobalRating() != null) {
                ratingBarGlobal.setRating(game.getGlobalRating());
                textGlobalRating.setText(String.format("%.1f", game.getGlobalRating()));
            } else {
                ratingBarGlobal.setRating(0f);
                textGlobalRating.setText("N/A");
            }
        }

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

    // Métodos para trailer/video
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
        List<Store> stores = new ArrayList<>();

        for (StoreListResponse.GameStore gameStore : gameStores) {
            if (gameStore.getStore() != null) {
                String storeUrl = gameStore.getStoreUrl();
                if (storeUrl == null || storeUrl.isEmpty()) {
                    if (gameStore.getStore().getDomain() != null) {
                        storeUrl = "https://" + gameStore.getStore().getDomain();
                    }
                }

                if (storeUrl != null && !storeUrl.isEmpty()) {
                    Store store = new Store(
                            gameStore.getStore().getName(),
                            storeUrl,
                            gameStore.getStore().getImageBackground()
                    );
                    stores.add(store);
                }
            }
        }

        setupStoreAdapter(stores);
    }

    private void setupStoresFromGame(Game game) {
        if (game.getStoresInfo() != null) {
            try {
                JSONArray storesArray = new JSONArray(game.getStoresInfo());
                List<Store> stores = new ArrayList<>();

                for (int i = 0; i < storesArray.length(); i++) {
                    JSONObject storeObj = storesArray.getJSONObject(i);
                    String name = storeObj.optString("name", "Tienda");
                    String url = storeObj.optString("url", "");
                    String iconUrl = storeObj.optString("iconUrl", "");

                    if (!url.isEmpty()) {
                        stores.add(new Store(name, url, iconUrl));
                    }
                }

                if (!stores.isEmpty()) {
                    setupStoreAdapter(stores);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error al parsear tiendas: " + e.getMessage());
            }
        }
    }

    private void setupStoreAdapter(List<Store> stores) {
        if (storeAdapter == null) {
            storeAdapter = new StoreAdapter(this, stores);
            recyclerStores.setLayoutManager(new LinearLayoutManager(this));
            recyclerStores.setAdapter(storeAdapter);
        } else {
            storeAdapter.updateStores(stores);
        }
    }

    // Métodos para tags
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
        addChip.setOnClickListener(v -> showAddTagDialog());
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