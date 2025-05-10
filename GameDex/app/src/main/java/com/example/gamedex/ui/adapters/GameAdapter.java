package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Game;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private final Context context;
    private List<Game> games;
    private final OnGameClickListener listener;

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
        private final ImageView gameCoverImageView;
        private final TextView gameTitleTextView;
        private final TextView gameDeveloperTextView;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameCoverImageView = itemView.findViewById(R.id.image_game_cover);
            gameTitleTextView = itemView.findViewById(R.id.text_game_title);
            gameDeveloperTextView = itemView.findViewById(R.id.text_game_developer);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onGameClick(games.get(position));
                }
            });
        }

        void bind(Game game) {
            gameTitleTextView.setText(game.getTitle());
            gameDeveloperTextView.setText(game.getDeveloper());

            if (game.getCoverUrl() != null && !game.getCoverUrl().isEmpty()) {
                Glide.with(context)
                        .load(game.getCoverUrl())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(gameCoverImageView);
            } else {
                gameCoverImageView.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}