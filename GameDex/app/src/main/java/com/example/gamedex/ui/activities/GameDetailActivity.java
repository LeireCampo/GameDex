package com.example.gamedex.ui.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.Tag;
import com.example.gamedex.ui.viewmodels.GameDetailViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

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

    private GameDetailViewModel viewModel;
    private String gameId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_detail);

        initViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        gameId = getIntent().getStringExtra("game_id");
        if (gameId == null) {
            Toast.makeText(this, "Error: Game ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(GameDetailViewModel.class);
        viewModel.init(getApplication(), gameId);

        setupObservers();
        setupClickListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
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
    }

    private void setupObservers() {
        viewModel.getGameWithTags().observe(this, gameWithTags -> {
            if (gameWithTags != null && gameWithTags.game != null) {
                Game game = gameWithTags.game;
                updateUI(game);
                updateTagsChips(gameWithTags.tags);
            }
        });
    }

    private void setupClickListeners() {
        buttonAddToLibrary.setOnClickListener(v -> {
            viewModel.toggleInLibrary();
        });

        buttonChangeStatus.setOnClickListener(v -> {
            // Show status selection dialog
            // In a real app, we would show a dialog with status options
            String[] statuses = {"playing", "completed", "backlog", "wishlist"};
            int randomIndex = (int) (Math.random() * statuses.length);
            viewModel.updateGameStatus(statuses[randomIndex]);
        });

        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                viewModel.updateUserRating(rating);
            }
        });
    }

    private void updateUI(Game game) {
        textGameTitle.setText(game.getTitle());
        textGameDeveloper.setText(game.getDeveloper());

        // Load cover image
        if (game.getCoverUrl() != null && !game.getCoverUrl().isEmpty()) {
            Glide.with(this)
                    .load(game.getCoverUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(imageGameCover);
        }

        // Update description
        textDescription.setText(game.getDescription() != null ?
                game.getDescription() : "No description available.");

        // Update platforms
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
                textPlatforms.setText("N/A");
            }
        } catch (JSONException e) {
            textPlatforms.setText("N/A");
        }

        // Update genres
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
                textGenres.setText("N/A");
            }
        } catch (JSONException e) {
            textGenres.setText("N/A");
        }

        // Update release date
        textReleaseDate.setText(game.getReleaseDate() != null ?
                game.getReleaseDate() : "N/A");

        // Update rating
        if (game.getUserRating() != null) {
            ratingBar.setRating(game.getUserRating());
        } else {
            ratingBar.setRating(0f);
        }

        // Update library status
        updateLibraryStatus(game.isInLibrary(), game.getStatus());
    }

    private void updateLibraryStatus(boolean inLibrary, String status) {
        if (inLibrary) {
            buttonAddToLibrary.setText(R.string.remove_from_library);
            buttonChangeStatus.setVisibility(View.VISIBLE);

            if (status != null) {
                String displayStatus = "Status: ";
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
                        displayStatus += "None";
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

        if (tags.isEmpty()) {
            Chip chip = new Chip(this);
            chip.setText("No tags");
            chip.setClickable(false);
            chipGroupTags.addView(chip);
            return;
        }

        for (Tag tag : tags) {
            Chip chip = new Chip(this);
            chip.setText(tag.getName());
            chip.setCloseIcon(getDrawable(android.R.drawable.ic_menu_close_clear_cancel));
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                viewModel.removeTagFromGame(tag.getId());
            });
            chipGroupTags.addView(chip);
        }

        // Add "Add Tag" chip
        Chip addChip = new Chip(this);
        addChip.setText("+ Add Tag");
        addChip.setChipBackgroundColorResource(android.R.color.transparent);
        addChip.setChipStrokeWidth(1f);
        addChip.setOnClickListener(v -> {
            // Show tag selection dialog
            // In a real app, we would show a dialog with available tags
            Tag newTag = new Tag("New Tag " + System.currentTimeMillis() % 100, "#FF5722");
            newTag.setId((int) (System.currentTimeMillis() % 100));
            viewModel.addTagToGame(newTag.getId());
        });
        chipGroupTags.addView(addChip);
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