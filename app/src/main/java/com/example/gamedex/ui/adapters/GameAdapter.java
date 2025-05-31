package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final Context context;
    private List<Game> games;
    private final OnGameClickListener listener;
    private boolean useNeonStyle = true;

    public interface OnGameClickListener {
        void onGameClick(Game game);
    }

    public GameAdapter(Context context, List<Game> games, OnGameClickListener listener) {
        this.context = context;
        this.games = games;
        this.listener = listener;
    }

    public void updateGames(List<Game> newGames) {
        this.games = newGames;
        notifyDataSetChanged();
    }

    public void setUseNeonStyle(boolean useNeonStyle) {
        this.useNeonStyle = useNeonStyle;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    class GameViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView gameCoverImageView;
        private final TextView gameTitleTextView;
        private final TextView gameDeveloperTextView;
        private final TextView gamePlatformsTextView;
        private final RatingBar ratingBarMini;
        private final TextView textRating;
        private final View ratingContainer;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            gameCoverImageView = itemView.findViewById(R.id.image_game_cover);
            gameTitleTextView = itemView.findViewById(R.id.text_game_title);
            gameDeveloperTextView = itemView.findViewById(R.id.text_game_developer);
            gamePlatformsTextView = itemView.findViewById(R.id.text_game_platforms);
            ratingBarMini = itemView.findViewById(R.id.rating_bar_mini);
            textRating = itemView.findViewById(R.id.text_rating);
            ratingContainer = itemView.findViewById(R.id.rating_container);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onGameClick(games.get(position));
                }
            });
        }

        void bind(Game game) {
            // Título del juego
            gameTitleTextView.setText(game.getTitle());

            // Desarrollador
            if (gameDeveloperTextView != null) {
                gameDeveloperTextView.setText(game.getDeveloper() != null ?
                        game.getDeveloper() : context.getString(R.string.unknown_developer));
            }

            // Mostrar plataformas (solo la primera si hay varias)
            if (gamePlatformsTextView != null) {
                String platformText = getFirstPlatform(game.getPlatforms());
                if (platformText != null && !platformText.isEmpty()) {
                    gamePlatformsTextView.setText(platformText);
                    gamePlatformsTextView.setVisibility(View.VISIBLE);
                } else {
                    gamePlatformsTextView.setVisibility(View.GONE);
                }
            }

            // Mostrar valoración si existe
            if (ratingBarMini != null && textRating != null) {
                if (game.getUserRating() != null) {
                    ratingBarMini.setRating(game.getUserRating());
                    textRating.setText(String.format("%.1f", game.getUserRating()));
                    ratingContainer.setVisibility(View.VISIBLE);
                } else if (game.getGlobalRating() != null) {
                    ratingBarMini.setRating(game.getGlobalRating());
                    textRating.setText(String.format("%.1f", game.getGlobalRating()));
                    ratingContainer.setVisibility(View.VISIBLE);
                } else {
                    ratingContainer.setVisibility(View.GONE);
                }
            }

            // Aplicar estilo según el estado del juego
            if (useNeonStyle) {
                applyNeonStyle(game);
            } else {
                resetStyle();
            }

            // Cargar la imagen con Glide
            if (game.getCoverUrl() != null && !game.getCoverUrl().isEmpty()) {
                Glide.with(context)
                        .load(game.getCoverUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.ic_launcher_background)
                                .error(R.drawable.ic_launcher_background))
                        .centerCrop()
                        .into(gameCoverImageView);
            } else {
                gameCoverImageView.setImageResource(R.drawable.ic_launcher_background);
            }
        }

        private String getFirstPlatform(String platformsJson) {
            if (platformsJson == null || platformsJson.isEmpty()) {
                return null;
            }

            try {
                JSONArray platformsArray = new JSONArray(platformsJson);
                if (platformsArray.length() > 0) {
                    String platform = platformsArray.getString(0);
                    // Acortar nombres largos
                    if (platform.length() > 8) {
                        return platform.substring(0, 6) + "..";
                    }
                    return platform;
                }
            } catch (JSONException e) {
                // Si no es JSON válido, devolver como texto simple
                if (platformsJson.length() > 8) {
                    return platformsJson.substring(0, 6) + "..";
                }
                return platformsJson;
            }

            return null;
        }

        private void applyNeonStyle(Game game) {
            // Determinar el color basado en el estado del juego
            int strokeColor = R.color.primary_green; // Por defecto
            if (game.getStatus() != null) {
                switch (game.getStatus()) {
                    case "playing":
                        strokeColor = R.color.status_playing;
                        break;
                    case "completed":
                        strokeColor = R.color.status_completed;
                        break;
                    case "backlog":
                        strokeColor = R.color.status_backlog;
                        break;
                    case "wishlist":
                        strokeColor = R.color.status_wishlist;
                        break;
                }
            }

            // Aplicar color a la tarjeta
            cardView.setStrokeColor(ContextCompat.getColor(context, strokeColor));
            cardView.setStrokeWidth(3);
            cardView.setCardElevation(8);

            // Aplicar color neón al título
            gameTitleTextView.setTextColor(ContextCompat.getColor(context, strokeColor));
            gameTitleTextView.setShadowLayer(6, 0, 0, ContextCompat.getColor(context, strokeColor));

            // Aplicar color neón a la valoración
            if (textRating != null) {
                textRating.setTextColor(ContextCompat.getColor(context, strokeColor));
            }

            // Cambiar RatingBar a color correspondiente
            if (ratingBarMini != null) {
                ratingBarMini.setProgressTintList(ContextCompat.getColorStateList(context, strokeColor));
            }
        }

        private void resetStyle() {
            cardView.setStrokeColor(ContextCompat.getColor(context, R.color.primary_green));
            cardView.setStrokeWidth(2);
            cardView.setCardElevation(4);

            gameTitleTextView.setTextColor(ContextCompat.getColor(context, R.color.primary_green));
            gameTitleTextView.setShadowLayer(4, 0, 0, ContextCompat.getColor(context, R.color.primary_green));

            if (textRating != null) {
                textRating.setTextColor(ContextCompat.getColor(context, R.color.primary_green));
            }
        }
    }
}