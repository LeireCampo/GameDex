package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.CustomTag;
import com.google.android.material.chip.Chip;

import java.util.List;

public class CustomTagAdapter extends RecyclerView.Adapter<CustomTagAdapter.TagViewHolder> {

    private final Context context;
    private List<CustomTag> tags;
    private final OnTagClickListener listener;

    public interface OnTagClickListener {
        void onTagClick(CustomTag tag);
        void onTagLongClick(CustomTag tag);
    }

    public CustomTagAdapter(Context context, List<CustomTag> tags, OnTagClickListener listener) {
        this.context = context;
        this.tags = tags;
        this.listener = listener;
    }

    public void updateTags(List<CustomTag> newTags) {
        this.tags = newTags;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_custom_tag_profile, parent, false);
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

    class TagViewHolder extends RecyclerView.ViewHolder {
        private final Chip chipTag;
        private final TextView textUsageCount;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            chipTag = itemView.findViewById(R.id.chip_tag);
            textUsageCount = itemView.findViewById(R.id.text_usage_count);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTagClick(tags.get(position));
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTagLongClick(tags.get(position));
                        return true;
                    }
                }
                return false;
            });
        }

        void bind(CustomTag tag) {
            chipTag.setText(tag.getName());

            // Configurar color de la etiqueta
            try {
                int color = Color.parseColor(tag.getColor());
                chipTag.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(color));
                chipTag.setTextColor(getContrastColor(color));

                // Agregar efecto de brillo sutil
                chipTag.setChipStrokeWidth(2);
                chipTag.setChipStrokeColor(android.content.res.ColorStateList.valueOf(lightenColor(color, 0.3f)));
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

            // Indicar si es etiqueta predefinida
            if (tag.isDefault()) {
                chipTag.setChipIcon(ContextCompat.getDrawable(context, R.drawable.ic_star));
            } else {
                chipTag.setChipIcon(null);
            }
        }

        private int getContrastColor(int color) {
            // Calcular luminancia para determinar si usar texto blanco o negro
            double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
            return luminance > 0.5 ? Color.BLACK : Color.WHITE;
        }

        private int lightenColor(int color, float factor) {
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);

            red = Math.min(255, (int) (red + (255 - red) * factor));
            green = Math.min(255, (int) (green + (255 - green) * factor));
            blue = Math.min(255, (int) (blue + (255 - blue) * factor));

            return Color.rgb(red, green, blue);
        }
    }
}