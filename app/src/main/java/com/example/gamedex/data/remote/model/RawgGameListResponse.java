package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RawgGameListResponse {
    @SerializedName("count")
    private int count;

    @SerializedName("next")
    private String next;

    @SerializedName("previous")
    private String previous;

    @SerializedName("results")
    private List<RawgGameResponse> results;

    public int getCount() {
        return count;
    }

    public String getNext() {
        return next;
    }

    public String getPrevious() {
        return previous;
    }

    public List<RawgGameResponse> getResults() {
        return results;
    }
}