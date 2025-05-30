package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class VideoListResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Video> results;

    public static class Video {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("preview")
        private String previewImageUrl;

        @SerializedName("video_id")
        private String videoId;

        @SerializedName("data")
        private VideoData data;

        public static class VideoData {
            @SerializedName("480")
            private String url480p;

            @SerializedName("max")
            private String urlMax;

            public String getUrl480p() { return url480p; }
            public String getUrlMax() { return urlMax; }
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getPreviewImageUrl() {
            // Para IGDB, construir URL de YouTube
            if (videoId != null) {
                return "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
            }
            return previewImageUrl;
        }
        public VideoData getData() { return data; }

        // Métodos adicionales para IGDB
        public String getVideoUrl() {
            return videoId != null ? "https://www.youtube.com/watch?v=" + videoId : null;
        }
    }

    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }
    public List<Video> getResults() { return results; }
}