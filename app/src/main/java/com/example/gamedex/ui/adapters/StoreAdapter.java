package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.gamedex.R;
import com.example.gamedex.data.model.Store;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private final Context context;
    private List<Store> stores;

    public StoreAdapter(Context context, List<Store> stores) {
        this.context = context;
        this.stores = stores;
    }

    public void updateStores(List<Store> newStores) {
        this.stores = newStores;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = stores.get(position);

        holder.storeNameTextView.setText(store.getName());

        // Cargar icono de la tienda si está disponible
        if (store.getIconUrl() != null && !store.getIconUrl().isEmpty()) {
            Glide.with(context)
                    .load(store.getIconUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .circleCrop()
                    .placeholder(R.drawable.ic_store)
                    .error(R.drawable.ic_store)
                    .into(holder.storeIconImageView);
        } else {
            // Icono predeterminado
            holder.storeIconImageView.setImageResource(R.drawable.ic_store);
        }

        // Establecer colores según la tienda
        int storeColor = getColorForStore(store.getName().toLowerCase());
        holder.storeIconImageView.setColorFilter(ContextCompat.getColor(context, storeColor));

        // Configurar botón de compra
        holder.buyButton.setOnClickListener(v -> {
            if (store.getUrl() != null && !store.getUrl().isEmpty()) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(store.getUrl()));
                    context.startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(context, "No se pudo abrir la tienda", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "URL de tienda no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        // También hacer clickeable toda la tarjeta
        holder.itemView.setOnClickListener(v -> holder.buyButton.performClick());
    }

    private int getColorForStore(String storeName) {
        // Asignar colores según la tienda
        if (storeName.contains("steam")) {
            return R.color.steam_blue;
        } else if (storeName.contains("playstation") || storeName.contains("ps store")) {
            return R.color.playstation_blue;
        } else if (storeName.contains("xbox") || storeName.contains("microsoft")) {
            return R.color.xbox_green;
        } else if (storeName.contains("nintendo")) {
            return R.color.nintendo_red;
        } else if (storeName.contains("epic")) {
            return R.color.epic_purple;
        } else if (storeName.contains("gog")) {
            return R.color.gog_purple;
        } else if (storeName.contains("ubisoft")) {
            return R.color.ubisoft_blue;
        } else if (storeName.contains("origin")) {
            return R.color.origin_orange;
        } else if (storeName.contains("battle") || storeName.contains("blizzard")) {
            return R.color.battlenet_blue;
        } else if (storeName.contains("amazon")) {
            return R.color.amazon_orange;
        } else {
            return R.color.neon_blue;
        }
    }

    @Override
    public int getItemCount() {
        return stores != null ? stores.size() : 0;
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        ImageView storeIconImageView;
        TextView storeNameTextView;
        MaterialButton buyButton;

        StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            storeIconImageView = itemView.findViewById(R.id.image_store_icon);
            storeNameTextView = itemView.findViewById(R.id.text_store_name);
            buyButton = itemView.findViewById(R.id.button_buy);
        }
    }
}