package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PlatformListResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<Platform> results;

    public static class Platform {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        @SerializedName("games_count")
        private int gamesCount;

        @SerializedName("image_background")
        private String imageBackground;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }

        public int getGamesCount() {
            return gamesCount;
        }

        public String getImageBackground() {
            return imageBackground;
        }
    }

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<Platform> getResults() {
        return results;
    }
}