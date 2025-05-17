package com.example.gamedex.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.example.gamedex.R;

import java.util.List;

public class ScreenshotAdapter extends RecyclerView.Adapter<ScreenshotAdapter.ScreenshotViewHolder> {

    private final Context context;
    private List<String> screenshotUrls;
    private final OnScreenshotClickListener listener;

    public interface OnScreenshotClickListener {
        void onScreenshotClick(String url, int position);
    }

    public ScreenshotAdapter(Context context, List<String> screenshotUrls, OnScreenshotClickListener listener) {
        this.context = context;
        this.screenshotUrls = screenshotUrls;
        this.listener = listener;
    }

    public void updateScreenshots(List<String> newScreenshots) {
        this.screenshotUrls = newScreenshots;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ScreenshotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_screenshot, parent, false);
        return new ScreenshotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScreenshotViewHolder holder, int position) {
        String screenshotUrl = screenshotUrls.get(position);

        Glide.with(context)
                .load(screenshotUrl)
                .transition(DrawableTransitionOptions.withCrossFade())
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background))
                .centerCrop()
                .into(holder.screenshotImageView);

        // Establecer el listener de click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onScreenshotClick(screenshotUrl, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return screenshotUrls.size();
    }

    static class ScreenshotViewHolder extends RecyclerView.ViewHolder {
        ImageView screenshotImageView;

        ScreenshotViewHolder(@NonNull View itemView) {
            super(itemView);
            screenshotImageView = itemView.findViewById(R.id.image_screenshot);
        }
    }
}