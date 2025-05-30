package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ScreenshotListResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Screenshot> results;

    public static class Screenshot {
        @SerializedName("id")
        private int id;

        @SerializedName("image")
        private String imageUrl;

        @SerializedName("url")
        private String url;

        @SerializedName("width")
        private int width;

        @SerializedName("height")
        private int height;

        @SerializedName("is_deleted")
        private boolean isDeleted;

        public int getId() { return id; }
        public String getImageUrl() {
            // Para IGDB, usar el campo url y añadir https:
            if (url != null && !url.startsWith("http")) {
                return "https:" + url.replace("t_thumb", "t_screenshot_big");
            }
            return imageUrl != null ? imageUrl : url;
        }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public boolean isDeleted() { return isDeleted; }
    }

    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }
    public List<Screenshot> getResults() { return results; }
}
