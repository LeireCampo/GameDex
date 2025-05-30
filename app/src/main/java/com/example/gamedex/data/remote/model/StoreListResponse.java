package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StoreListResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<GameStore> results;

    public static class GameStore {
        @SerializedName("id")
        private int id;

        @SerializedName("url")
        private String storeUrl;

        @SerializedName("store")
        private Store store;

        public static class Store {
            @SerializedName("id")
            private int id;

            @SerializedName("name")
            private String name;

            @SerializedName("slug")
            private String slug;

            @SerializedName("domain")
            private String domain;

            @SerializedName("image_background")
            private String imageBackground;

            public int getId() { return id; }
            public String getName() { return name; }
            public String getSlug() { return slug; }
            public String getDomain() { return domain; }
            public String getImageBackground() { return imageBackground; }
        }

        public int getId() { return id; }
        public String getStoreUrl() { return storeUrl; }
        public Store getStore() { return store; }
    }

    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }
    public List<GameStore> getResults() { return results; }
}