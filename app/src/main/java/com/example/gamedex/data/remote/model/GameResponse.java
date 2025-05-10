package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GameResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("name")
    private String name;

    @SerializedName("released")
    private String releaseDate;

    @SerializedName("background_image")
    private String backgroundImage;

    @SerializedName("rating")
    private float rating;

    @SerializedName("ratings_count")
    private int ratingsCount;

    @SerializedName("metacritic")
    private Integer metacritic;

    @SerializedName("playtime")
    private int playtime;

    @SerializedName("platforms")
    private List<PlatformWrapper> platforms;

    @SerializedName("genres")
    private List<Genre> genres;

    @SerializedName("stores")
    private List<StoreWrapper> stores;

    @SerializedName("tags")
    private List<Tag> tags;

    public static class PlatformWrapper {
        @SerializedName("platform")
        private Platform platform;

        public Platform getPlatform() {
            return platform;
        }
    }

    public static class Platform {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
    }

    public static class Genre {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
    }

    public static class StoreWrapper {
        @SerializedName("store")
        private Store store;

        public Store getStore() {
            return store;
        }
    }

    public static class Store {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
    }

    public static class Tag {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public float getRating() {
        return rating;
    }

    public int getRatingsCount() {
        return ratingsCount;
    }

    public Integer getMetacritic() {
        return metacritic;
    }

    public int getPlaytime() {
        return playtime;
    }

    public List<PlatformWrapper> getPlatforms() {
        return platforms;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public List<StoreWrapper> getStores() {
        return stores;
    }

    public List<Tag> getTags() {
        return tags;
    }
}