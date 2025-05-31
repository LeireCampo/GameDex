package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gamedex.R;
import com.example.gamedex.data.local.entity.CustomTag;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ProfileTagsAdapter extends RecyclerView.Adapter<ProfileTagsAdapter.TagViewHolder> {

    private final Context context;
    private List<CustomTag> tags;
    private final OnTagClickListener listener;

    public interface OnTagClickListener {
        void onTagClick(CustomTag tag);
        void onTagLongClick(CustomTag tag); // Para opciones adicionales como eliminar
    }

    public ProfileTagsAdapter(Context context, List<CustomTag> tags, OnTagClickListener listener) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.item_profile_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        CustomTag tag = tags.get(position);
        holder.bind(tag);
    }

    @Override
    public int getItemCount() {
        return tags != null ? tags.size() : 0;
    }

    class TagViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView imageTagIcon;
        private final TextView textTagName;
        private final TextView textGameCount;
        private final ImageView imageChevron;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (MaterialCardView) itemView;
            imageTagIcon = itemView.findViewById(R.id.image_tag_icon);
            textTagName = itemView.findViewById(R.id.text_tag_name);
            textGameCount = itemView.findViewById(R.id.text_game_count);
            imageChevron = itemView.findViewById(R.id.image_chevron);

            // Click normal - navegar a la lista de juegos
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onTagClick(tags.get(position));
                    }
                }
            });

            // Long click - mostrar opciones (editar, eliminar)
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
            textTagName.setText(tag.getName());

            // Mostrar contador de juegos
            int gameCount = tag.getUsageCount();
            textGameCount.setText(String.valueOf(gameCount));

            // Configurar colores basados en el color de la etiqueta
            try {
                int tagColor = Color.parseColor(tag.getColor());

                // Color del icono y texto principal
                imageTagIcon.setColorFilter(tagColor);
                textTagName.setTextColor(tagColor);

                // Color del contador
                textGameCount.setTextColor(tagColor);

                // Borde de la tarjeta con el color de la etiqueta
                cardView.setStrokeColor(tagColor);
                cardView.setStrokeWidth(2);

                // Efecto de glow sutil
                cardView.setCardElevation(4);

            } catch (Exception e) {
                // Color por defecto si hay error
                int defaultColor = ContextCompat.getColor(context, R.color.primary_green);
                imageTagIcon.setColorFilter(defaultColor);
                textTagName.setTextColor(defaultColor);
                textGameCount.setTextColor(defaultColor);
                cardView.setStrokeColor(defaultColor);
            }

            // Indicar si es etiqueta predefinida (opcional)
            if (tag.isDefault()) {
                // Añadir un indicador visual para etiquetas del sistema
                imageTagIcon.setAlpha(0.8f);
            } else {
                imageTagIcon.setAlpha(1.0f);
            }

            // Color del chevron
            imageChevron.setColorFilter(ContextCompat.getColor(context, R.color.text_secondary));
        }
    }
}