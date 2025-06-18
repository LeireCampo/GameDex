package com.example.gamedex.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.ui.adapters.GameAdapter;
import com.example.gamedex.ui.viewmodels.GamesByTagViewModel;

import java.util.ArrayList;

public class GamesByTagActivity extends AppCompatActivity implements GameAdapter.OnGameClickListener {

    private static final String EXTRA_TAG_NAME = "tag_name";
    private static final String EXTRA_TAG_ID = "tag_id";
    private static final String EXTRA_TAG_COLOR = "tag_color";

    private RecyclerView recyclerGames;
    private TextView textEmptyState;
    private TextView textGameCount;
    private Toolbar toolbar;
    private GameAdapter gameAdapter;
    private GamesByTagViewModel viewModel;

    private String tagName;
    private int tagId;
    private String tagColor;

    public static Intent newIntent(android.content.Context context, String tagName, int tagId) {
        Intent intent = new Intent(context, GamesByTagActivity.class);
        intent.putExtra(EXTRA_TAG_NAME, tagName);
        intent.putExtra(EXTRA_TAG_ID, tagId);
        return intent;
    }

    public static Intent newIntent(android.content.Context context, String tagName, int tagId, String tagColor) {
        Intent intent = new Intent(context, GamesByTagActivity.class);
        intent.putExtra(EXTRA_TAG_NAME, tagName);
        intent.putExtra(EXTRA_TAG_ID, tagId);
        intent.putExtra(EXTRA_TAG_COLOR, tagColor);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_by_tag);

        // Obtener datos del intent
        tagName = getIntent().getStringExtra(EXTRA_TAG_NAME);
        tagId = getIntent().getIntExtra(EXTRA_TAG_ID, -1);
        tagColor = getIntent().getStringExtra(EXTRA_TAG_COLOR);

        Log.d("GamesByTagActivity", "Tag ID recibido: " + tagId + ", Tag Name: " + tagName);

        if (tagName == null || tagId == -1) {
            Log.e("GamesByTagActivity", "Datos del tag inválidos");
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModel();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerGames = findViewById(R.id.recycler_games);
        textEmptyState = findViewById(R.id.text_empty_state);
        textGameCount = findViewById(R.id.text_game_count);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Etiqueta: " + tagName);
        }
    }

    private void setupRecyclerView() {
        gameAdapter = new GameAdapter(this, new ArrayList<>(), this);
        gameAdapter.setUseNeonStyle(true); // Activar estilo neón
        recyclerGames.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerGames.setAdapter(gameAdapter);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(GamesByTagViewModel.class);
        viewModel.init(tagId);

        // Observar los juegos
        viewModel.getGamesByTag().observe(this, games -> {
            Log.d("GamesByTagActivity", "Juegos recibidos: " + (games != null ? games.size() : 0));

            if (games != null) {
                gameAdapter.updateGames(games);

                // Actualizar contador
                int count = games.size();
                textGameCount.setText(count + " " + (count == 1 ? "juego" : "juegos"));

                // Mostrar/ocultar estado vacío
                if (games.isEmpty()) {
                    textEmptyState.setVisibility(View.VISIBLE);
                    recyclerGames.setVisibility(View.GONE);
                    textEmptyState.setText("No hay juegos con la etiqueta \"" + tagName + "\"");
                    Log.d("GamesByTagActivity", "No se encontraron juegos para el tag: " + tagName);
                } else {
                    textEmptyState.setVisibility(View.GONE);
                    recyclerGames.setVisibility(View.VISIBLE);
                    Log.d("GamesByTagActivity", "Mostrando " + count + " juegos");
                }
            }
        });
    }

    @Override
    public void onGameClick(Game game) {
        Intent intent = new Intent(this, GameDetailActivity.class);
        intent.putExtra("game_id", game.getId());
        startActivity(intent);
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