package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GameListResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<GameResponse> results;

    // Constructor para adaptar respuesta de IGDB
    public GameListResponse(List<GameResponse> igdbGames) {
        this.results = igdbGames;
        this.count = igdbGames != null ? igdbGames.size() : 0;
        this.next = null;
        this.previous = null;
    }

    // Constructor vacío para Gson
    public GameListResponse() {}

    // Getters
    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }
    public List<GameResponse> getResults() { return results; }

    // Setters
    public void setCount(int count) { this.count = count; }
    public void setNext(String next) { this.next = next; }
    public void setPrevious(String previous) { this.previous = previous; }
    public void setResults(List<GameResponse> results) { this.results = results; }
}