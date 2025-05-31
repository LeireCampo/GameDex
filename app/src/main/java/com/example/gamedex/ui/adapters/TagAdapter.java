package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.google.android.material.chip.Chip;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private final Context context;
    private Map<String, Integer> tagStats;
    private final OnTagClickListener listener;

    public interface OnTagClickListener {
        void onTagClick(String tagName, int gameCount);
    }

    public TagAdapter(Context context, List<String> tags, OnTagClickListener listener) {
        this.context = context;
        this.tagStats = new HashMap<>();
        this.listener = listener;
    }

    public void updateTags(Map<String, Integer> newTagStats) {
        this.tagStats = newTagStats != null ? newTagStats : new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        String tagName = (String) tagStats.keySet().toArray()[position];
        int gameCount = tagStats.get(tagName);
        holder.bind(tagName, gameCount);
    }

    @Override
    public int getItemCount() {
        return tagStats.size();
    }

    class TagViewHolder extends RecyclerView.ViewHolder {
        private final Chip chipTag;
        private final TextView textGameCount;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            chipTag = itemView.findViewById(R.id.chip_tag);
            textGameCount = itemView.findViewById(R.id.text_game_count);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        String tagName = (String) tagStats.keySet().toArray()[position];
                        int gameCount = tagStats.get(tagName);
                        listener.onTagClick(tagName, gameCount);
                    }
                }
            });
        }

        void bind(String tagName, int gameCount) {
            chipTag.setText(tagName);
            textGameCount.setText(gameCount + " juegos");
        }
    }
}