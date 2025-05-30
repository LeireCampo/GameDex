package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RawgGameResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("slug")
    private String slug;

    @SerializedName("name")
    private String name;

    @SerializedName("released")
    private String released;

    @SerializedName("background_image")
    private String backgroundImage;

    @SerializedName("rating")
    private float rating;

    @SerializedName("rating_top")
    private int ratingTop;

    @SerializedName("ratings_count")
    private int ratingsCount;

    @SerializedName("metacritic")
    private Integer metacritic;

    @SerializedName("playtime")
    private int playtime;

    @SerializedName("platforms")
    private List<PlatformInfo> platforms;

    @SerializedName("genres")
    private List<Genre> genres;

    @SerializedName("stores")
    private List<StoreInfo> stores;

    @SerializedName("tags")
    private List<Tag> tags;

    @SerializedName("esrb_rating")
    private EsrbRating esrbRating;

    public static class PlatformInfo {
        @SerializedName("platform")
        private Platform platform;

        @SerializedName("released_at")
        private String releasedAt;

        public Platform getPlatform() {
            return platform;
        }

        public String getReleasedAt() {
            return releasedAt;
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

    public static class StoreInfo {
        @SerializedName("id")
        private int id;

        @SerializedName("store")
        private Store store;

        public int getId() {
            return id;
        }

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

        @SerializedName("domain")
        private String domain;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getSlug() {
            return slug;
        }

        public String getDomain() {
            return domain;
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

    // Getters principales
    public int getId() {
        return id;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getReleased() {
        return released;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public float getRating() {
        return rating;
    }

    public int getRatingTop() {
        return ratingTop;
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

    public List<PlatformInfo> getPlatforms() {
        return platforms;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public List<StoreInfo> getStores() {
        return stores;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public EsrbRating getEsrbRating() {
        return esrbRating;
    }

    // Métodos auxiliares
    public String getDeveloper() {
        // RAWG no siempre incluye desarrollador en la respuesta básica
        // Necesitarías hacer una llamada adicional a getGameDetails
        return "Desconocido";
    }

    public String getPublisher() {
        // Similar al desarrollador
        return "Desconocido";
    }
}