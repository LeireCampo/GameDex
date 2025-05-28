package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GameResponse {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("first_release_date")
    private Long firstReleaseDate;

    @SerializedName("summary")
    private String summary;

    @SerializedName("rating")
    private Double rating;

    @SerializedName("total_rating")
    private Double totalRating;

    @SerializedName("rating_count")
    private Integer ratingCount;

    @SerializedName("cover")
    private Cover cover;

    @SerializedName("screenshots")
    private List<Screenshot> screenshots;

    @SerializedName("videos")
    private List<Video> videos;

    @SerializedName("platforms")
    private List<PlatformWrapper> platforms;

    @SerializedName("genres")
    private List<Genre> genres;

    @SerializedName("involved_companies")
    private List<InvolvedCompany> involvedCompanies;

    @SerializedName("websites")
    private List<Website> websites;

    // Clases anidadas
    public static class Cover {
        @SerializedName("url")
        private String url;

        @SerializedName("width")
        private int width;

        @SerializedName("height")
        private int height;

        public String getUrl() {
            return url != null ? "https:" + url.replace("t_thumb", "t_cover_big") : null;
        }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
    }

    public static class Screenshot {
        @SerializedName("id")
        private int id;

        @SerializedName("url")
        private String url;

        @SerializedName("width")
        private int width;

        @SerializedName("height")
        private int height;

        public int getId() { return id; }
        public String getImageUrl() {
            return url != null ? "https:" + url.replace("t_thumb", "t_screenshot_big") : null;
        }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public boolean isDeleted() { return false; }
    }

    public static class Video {
        @SerializedName("video_id")
        private String videoId;

        @SerializedName("name")
        private String name;

        public String getVideoId() { return videoId; }
        public String getName() { return name; }

        public String getVideoUrl() {
            return videoId != null ? "https://www.youtube.com/watch?v=" + videoId : null;
        }

        public String getPreviewUrl() {
            return videoId != null ? "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg" : null;
        }
    }

    public static class PlatformWrapper {
        @SerializedName("name")
        private String name;

        @SerializedName("abbreviation")
        private String abbreviation;

        @SerializedName("category")
        private Integer category;

        public Platform getPlatform() {
            Platform platform = new Platform();
            platform.id = 0;
            platform.name = this.name;
            platform.slug = this.abbreviation != null ? this.abbreviation.toLowerCase() : "";
            return platform;
        }
    }

    public static class Platform {
        private int id;
        private String name;
        private String slug;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
    }

    public static class Genre {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSlug() {
            return name != null ? name.toLowerCase().replace(" ", "-") : "";
        }
    }

    public static class InvolvedCompany {
        @SerializedName("company")
        private Company company;

        @SerializedName("developer")
        private boolean developer;

        @SerializedName("publisher")
        private boolean publisher;

        public static class Company {
            @SerializedName("name")
            private String name;

            public String getName() { return name; }
        }

        public Company getCompany() { return company; }
        public boolean isDeveloper() { return developer; }
        public boolean isPublisher() { return publisher; }
    }

    public static class Website {
        @SerializedName("url")
        private String url;

        @SerializedName("category")
        private Integer category;

        public String getUrl() { return url; }
        public Integer getCategory() { return category; }

        public boolean isStore() {
            if (category == null) return false;
            return category == 13 || category == 16 || category == 17;
        }
    }

    public static class Tag {
        @SerializedName("id")
        private int id;

        @SerializedName("name")
        private String name;

        @SerializedName("slug")
        private String slug;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
    }

    public static class StoreWrapper {
        private String url;
        private Store store;

        public String getUrl() { return url; }
        public Store getStore() { return store; }
    }

    public static class Store {
        private int id;
        private String name;
        private String slug;
        private String domain;

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
        public String getDomain() { return domain; }
    }

    // SOLO UNA definición de VideoClip
    public static class VideoClip {
        private String videoUrl;
        private String previewUrl;

        public String getVideoUrl() { return videoUrl; }
        public String getPreviewUrl() { return previewUrl; }
        public String getClipUrl() { return videoUrl; }
    }

    // Getters principales
    public int getId() { return id; }
    public String getName() { return name; }
    public String getReleaseDate() {
        if (firstReleaseDate != null) {
            java.util.Date date = new java.util.Date(firstReleaseDate * 1000L);
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        }
        return null;
    }
    public String getBackgroundImage() {
        return cover != null ? cover.getUrl() : null;
    }
    public float getRating() {
        if (totalRating != null) {
            return (float) (totalRating / 20.0);
        }
        return 0f;
    }
    public int getRatingsCount() {
        return ratingCount != null ? ratingCount : 0;
    }
    public Integer getMetacritic() {
        return rating != null ? rating.intValue() : null;
    }
    public int getPlaytime() { return 0; }

    // Getters para listas
    public List<PlatformWrapper> getPlatforms() { return platforms; }
    public List<Genre> getGenres() { return genres; }
    public List<Screenshot> getScreenshots() { return screenshots; }
    public List<Video> getVideos() { return videos; }
    public List<InvolvedCompany> getInvolvedCompanies() { return involvedCompanies; }
    public List<Website> getWebsites() { return websites; }

    // Métodos auxiliares
    public String getDeveloper() {
        if (involvedCompanies != null) {
            for (InvolvedCompany ic : involvedCompanies) {
                if (ic.isDeveloper() && ic.getCompany() != null) {
                    return ic.getCompany().getName();
                }
            }
        }
        return "Desconocido";
    }

    public String getPublisher() {
        if (involvedCompanies != null) {
            for (InvolvedCompany ic : involvedCompanies) {
                if (ic.isPublisher() && ic.getCompany() != null) {
                    return ic.getCompany().getName();
                }
            }
        }
        return "Desconocido";
    }

    public VideoClip getVideoClip() {
        if (videos != null && !videos.isEmpty()) {
            Video firstVideo = videos.get(0);
            VideoClip clip = new VideoClip();
            clip.videoUrl = firstVideo.getVideoUrl();
            clip.previewUrl = firstVideo.getPreviewUrl();
            return clip;
        }
        return null;
    }

    public List<StoreWrapper> getStores() {
        List<StoreWrapper> stores = new java.util.ArrayList<>();
        if (websites != null) {
            for (Website website : websites) {
                if (website.isStore()) {
                    StoreWrapper wrapper = new StoreWrapper();
                    wrapper.url = website.getUrl();
                    wrapper.store = new Store();
                    wrapper.store.name = getStoreNameFromCategory(website.getCategory());
                    wrapper.store.id = website.getCategory();
                    stores.add(wrapper);
                }
            }
        }
        return stores;
    }

    private String getStoreNameFromCategory(Integer category) {
        if (category == null) return "Tienda";
        switch (category) {
            case 13: return "Steam";
            case 16: return "Epic Games Store";
            case 17: return "GOG";
            default: return "Tienda Digital";
        }
    }
}