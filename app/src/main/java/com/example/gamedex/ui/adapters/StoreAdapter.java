package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
                    .circleCrop()
                    .into(holder.storeIconImageView);
        } else {
            // Icono predeterminado
            holder.storeIconImageView.setImageResource(R.drawable.ic_store);
        }

        // Establecer colores según la tienda (esto es opcional, puedes personalizarlo)
        int storeColor = getColorForStore(store.getName().toLowerCase());
        holder.storeIconImageView.setColorFilter(ContextCompat.getColor(context, storeColor));

        // Configurar botón de compra
        holder.buyButton.setOnClickListener(v -> {
            if (store.getUrl() != null && !store.getUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(store.getUrl()));
                context.startActivity(intent);
            }
        });
    }

    private int getColorForStore(String storeName) {
        // Asignar colores según la tienda
        switch (storeName) {
            case "steam":
                return R.color.steam_blue;
            case "playstation store":
            case "ps store":
                return R.color.playstation_blue;
            case "xbox store":
            case "microsoft store":
                return R.color.xbox_green;
            case "nintendo eshop":
            case "nintendo":
                return R.color.nintendo_red;
            case "epic games":
            case "epic games store":
                return R.color.epic_purple;
            case "gog":
            case "gog.com":
                return R.color.gog_purple;
            default:
                return R.color.neon_blue;
        }
    }

    @Override
    public int getItemCount() {
        return stores.size();
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