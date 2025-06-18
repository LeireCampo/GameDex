package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.Tag;
import com.example.gamedex.ui.activities.GamesByTagActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    private final Context context;
    private List<Tag> tags;
    private Map<Integer, Integer> tagGameCounts; // tagId -> game count
    private final OnTagClickListener listener;

    public interface OnTagClickListener {
        void onTagClick(Tag tag, int gameCount);
    }

    public TagAdapter(Context context, OnTagClickListener listener) {
        this.context = context;
        this.tags = new ArrayList<>();
        this.listener = listener;
    }

    public void updateTags(List<Tag> newTags, Map<Integer, Integer> gameCounts) {
        this.tags = newTags != null ? newTags : new ArrayList<>();
        this.tagGameCounts = gameCounts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = tags.get(position);
        int gameCount = tagGameCounts != null ? tagGameCounts.getOrDefault(tag.getId(), 0) : 0;
        holder.bind(tag, gameCount);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    class TagViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageTagIcon;
        private final TextView textTagName;
        private final TextView textGameCount;
        private final ImageView imageChevron;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            imageTagIcon = itemView.findViewById(R.id.image_tag_icon);
            textTagName = itemView.findViewById(R.id.text_tag_name);
            textGameCount = itemView.findViewById(R.id.text_game_count);
            imageChevron = itemView.findViewById(R.id.image_chevron);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < tags.size()) {
                    Tag tag = tags.get(position);
                    int gameCount = tagGameCounts != null ? tagGameCounts.getOrDefault(tag.getId(), 0) : 0;

                    // Llamar al listener
                    if (listener != null) {
                        listener.onTagClick(tag, gameCount);
                    }

                    // Navegar directamente a GamesByTagActivity
                    Intent intent = GamesByTagActivity.newIntent(context, tag.getName(), tag.getId());
                    context.startActivity(intent);
                }
            });
        }

        void bind(Tag tag, int gameCount) {
            textTagName.setText(tag.getName());
            textGameCount.setText(String.valueOf(gameCount));
        }
    }
}