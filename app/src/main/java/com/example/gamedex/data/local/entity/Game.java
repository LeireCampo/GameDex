package com.example.gamedex.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameResponse;

@Entity(
        tableName = "games",
        indices = {
                @Index(value = "title"),
                @Index(value = "isInLibrary"),
                @Index(value = "status"),
                @Index(value = "lastUpdated"),
                @Index(value = {"isInLibrary", "status"}),
                @Index(value = {"isInLibrary", "lastUpdated"})
        }
)
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
    private String platforms;        // Stored as JSON string
    private String genres;           // Stored as JSON string
    private Float userRating;
    private Float globalRating;
    private boolean isInLibrary;
    private String status;           // "Playing", "Completed", "Wishlist", etc.
    private long lastUpdated;

    // Nuevos campos para multimedia
    private String trailerUrl;       // URL del video/trailer
    private String screenshotsUrls;  // JSON de URLs de capturas
    private String storesInfo;       // JSON con información de tiendas
    private String suggestedTags;    // JSON de tags sugeridos de la API

    public Game(@NonNull String id, String title) {
        this.id = id;
        this.title = title;
        this.lastUpdated = System.currentTimeMillis();
    }

    // Getters y setters existentes
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

    // Getters y setters para los nuevos campos
    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public String getScreenshotsUrls() {
        return screenshotsUrls;
    }

    public void setScreenshotsUrls(String screenshotsUrls) {
        this.screenshotsUrls = screenshotsUrls;
    }

    public String getStoresInfo() {
        return storesInfo;
    }

    public void setStoresInfo(String storesInfo) {
        this.storesInfo = storesInfo;
    }

    public String getSuggestedTags() {
        return suggestedTags;
    }

    public void setSuggestedTags(String suggestedTags) {
        this.suggestedTags = suggestedTags;
    }

    public static Game fromResponseToEntity(GameResponse response) {
        Game game = new Game(String.valueOf(response.getId()), response.getName());
        game.setCoverUrl(response.getBackgroundImage());
        game.setReleaseDate(response.getReleaseDate());
        game.setGlobalRating(response.getRating());


        // ... resto del código existente ...

        return game;
    }

    public static Game fromDetailResponseToEntity(GameDetailResponse response) {
        if (response == null) {
            return new Game("0", "Juego no encontrado");
        }

        Game game = new Game(String.valueOf(response.getId()), response.getName());

        // ... código existente ...

        game.setGlobalRating(response.getRating());
        game.setDescription(response.getDescription());

        // ... resto del código existente ...

        return game;
    }
}