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
import com.example.gamedex.R;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;

import java.util.List;

public class ScreenshotAdapter extends RecyclerView.Adapter<ScreenshotAdapter.ScreenshotViewHolder> {

    private final Context context;
    private List<ScreenshotListResponse.Screenshot> screenshots;
    private final OnScreenshotClickListener listener;

    public interface OnScreenshotClickListener {
        void onScreenshotClick(ScreenshotListResponse.Screenshot screenshot);
    }

    public ScreenshotAdapter(Context context, List<ScreenshotListResponse.Screenshot> screenshots, OnScreenshotClickListener listener) {
        this.context = context;
        this.screenshots = screenshots;
        this.listener = listener;
    }

    public void updateScreenshots(List<ScreenshotListResponse.Screenshot> newScreenshots) {
        this.screenshots = newScreenshots;
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
        ScreenshotListResponse.Screenshot screenshot = screenshots.get(position);
        holder.bind(screenshot);
    }

    @Override
    public int getItemCount() {
        return screenshots != null ? screenshots.size() : 0;
    }

    class ScreenshotViewHolder extends RecyclerView.ViewHolder {
        private final ImageView screenshotImageView;

        public ScreenshotViewHolder(@NonNull View itemView) {
            super(itemView);
            screenshotImageView = itemView.findViewById(R.id.image_screenshot);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onScreenshotClick(screenshots.get(position));
                }
            });
        }

        void bind(ScreenshotListResponse.Screenshot screenshot) {
            Glide.with(context)
                    .load(screenshot.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(screenshotImageView);
        }
    }
}