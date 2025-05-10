package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GameDetailResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("name")
    private String name;

    @SerializedName("description_raw")
    private String description;

    @SerializedName("background_image")
    private String backgroundImage;

    @SerializedName("released")
    private String releaseDate;

    @SerializedName("rating")
    private float rating;

    @SerializedName("ratings_count")
    private int ratingsCount;

    @SerializedName("metacritic")
    private Integer metacritic;

    @SerializedName("playtime")
    private int playtime;

    @SerializedName("website")
    private String website;

    @SerializedName("platforms")
    private List<GameResponse.PlatformWrapper> platforms;

    @SerializedName("genres")
    private List<GameResponse.Genre> genres;

    @SerializedName("stores")
    private List<GameResponse.StoreWrapper> stores;

    @SerializedName("tags")
    private List<GameResponse.Tag> tags;

    @SerializedName("developers")
    private List<Developer> developers;

    @SerializedName("publishers")
    private List<Publisher> publishers;

    @SerializedName("esrb_rating")
    private EsrbRating esrbRating;

    @SerializedName("achievements_count")
    private int achievementsCount;

    public static class Developer {
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

    public static class Publisher {
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

    public static class EsrbRating {
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

    public String getDescription() {
        return description;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public String getReleaseDate() {
        return releaseDate;
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

    public String getWebsite() {
        return website;
    }

    public List<GameResponse.PlatformWrapper> getPlatforms() {
        return platforms;
    }

    public List<GameResponse.Genre> getGenres() {
        return genres;
    }

    public List<GameResponse.StoreWrapper> getStores() {
        return stores;
    }

    public List<GameResponse.Tag> getTags() {
        return tags;
    }

    public List<Developer> getDevelopers() {
        return developers;
    }

    public List<Publisher> getPublishers() {
        return publishers;
    }

    public EsrbRating getEsrbRating() {
        return esrbRating;
    }

    public int getAchievementsCount() {
        return achievementsCount;
    }
}