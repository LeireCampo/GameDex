package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.CustomTag;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CustomTagSelectionAdapter extends RecyclerView.Adapter<CustomTagSelectionAdapter.TagViewHolder> {

    private final Context context;
    private List<CustomTag> tags;
    private Set<Integer> selectedTagIds;

    public CustomTagSelectionAdapter(Context context, List<CustomTag> tags, List<Integer> selectedTagIds) {
        this.context = context;
        this.tags = tags;
        this.selectedTagIds = new HashSet<>(selectedTagIds);
    }

    public void updateTags(List<CustomTag> newTags, List<Integer> selectedIds) {
        this.tags = newTags;
        this.selectedTagIds = new HashSet<>(selectedIds);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_custom_tag_selection, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        CustomTag tag = tags.get(position);
        holder.bind(tag);
    }

    @Override
    public int getItemCount() {
        return tags.size();
    }

    public List<CustomTag> getSelectedTags() {
        List<CustomTag> selectedTags = new ArrayList<>();
        for (CustomTag tag : tags) {
            if (selectedTagIds.contains(tag.getId())) {
                selectedTags.add(tag);
            }
        }
        return selectedTags;
    }

    class TagViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final Chip chipTag;
        private final CheckBox checkBox;
        private final TextView textUsageCount;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_tag);
            chipTag = itemView.findViewById(R.id.chip_tag);
            checkBox = itemView.findViewById(R.id.checkbox_tag);
            textUsageCount = itemView.findViewById(R.id.text_usage_count);

            itemView.setOnClickListener(v -> {
                checkBox.setChecked(!checkBox.isChecked());
            });

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    CustomTag tag = tags.get(position);
                    if (isChecked) {
                        selectedTagIds.add(tag.getId());
                    } else {
                        selectedTagIds.remove(tag.getId());
                    }
                    updateCardAppearance(tag, isChecked);
                }
            });
        }

        void bind(CustomTag tag) {
            chipTag.setText(tag.getName());

            // Configurar color de la etiqueta
            try {
                int color = Color.parseColor(tag.getColor());
                chipTag.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
                chipTag.setTextColor(getContrastColor(color));
            } catch (Exception e) {
                chipTag.setChipBackgroundColor(ContextCompat.getColorStateList(context, R.color.primary_green));
                chipTag.setTextColor(ContextCompat.getColor(context, R.color.background_black));
            }

            // Mostrar contador de uso
            if (tag.getUsageCount() > 0) {
                textUsageCount.setText(context.getString(R.string.used_in_games, tag.getUsageCount()));
                textUsageCount.setVisibility(View.VISIBLE);
            } else {
                textUsageCount.setVisibility(View.GONE);
            }

            // Configurar estado del checkbox
            boolean isSelected = selectedTagIds.contains(tag.getId());
            checkBox.setChecked(isSelected);
            updateCardAppearance(tag, isSelected);

            // Indicar si es etiqueta predefinida
            if (tag.isDefault()) {
                chipTag.setChipIcon(ContextCompat.getDrawable(context, R.drawable.ic_star));
            } else {
                chipTag.setChipIcon(null);
            }
        }

        private void updateCardAppearance(CustomTag tag, boolean isSelected) {
            if (isSelected) {
                try {
                    int color = Color.parseColor(tag.getColor());
                    cardView.setStrokeColor(color);
                    cardView.setStrokeWidth(4);
                    cardView.setCardElevation(8);
                } catch (Exception e) {
                    cardView.setStrokeColor(ContextCompat.getColor(context, R.color.primary_green));
                    cardView.setStrokeWidth(4);
                }
            } else {
                cardView.setStrokeColor(ContextCompat.getColor(context, R.color.border_color));
                cardView.setStrokeWidth(2);
                cardView.setCardElevation(4);
            }
        }

        private int getContrastColor(int color) {
            // Calcular luminancia para determinar si usar texto blanco o negro
            double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
            return luminance > 0.5 ? Color.BLACK : Color.WHITE;
        }
    }
}