package com.example.gamedex.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "games")
public class Game {
    @PrimaryKey
    @NonNull
    private String id;
    private String title;
    private String description;
    private String coverUrl;
    private String releaseDate;
    private String publisher;
    private String developer;
    private String platforms;    // Stored as JSON string
    private String genres;       // Stored as JSON string
    private Float userRating;
    private Float globalRating;
    private boolean isInLibrary;
    private String status;       // "Playing", "Completed", "Wishlist", etc.
    private long lastUpdated;

    public Game(@NonNull String id, String title) {
        this.id = id;
        this.title = title;
        this.lastUpdated = System.currentTimeMillis();
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getPlatforms() {
        return platforms;
    }

    public void setPlatforms(String platforms) {
        this.platforms = platforms;
    }

    public String getGenres() {
        return genres;
    }

    public void setGenres(String genres) {
        this.genres = genres;
    }

    public Float getUserRating() {
        return userRating;
    }

    public void setUserRating(Float userRating) {
        this.userRating = userRating;
    }

    public Float getGlobalRating() {
        return globalRating;
    }

    public void setGlobalRating(Float globalRating) {
        this.globalRating = globalRating;
    }

    public boolean isInLibrary() {
        return isInLibrary;
    }

    public void setInLibrary(boolean inLibrary) {
        isInLibrary = inLibrary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}