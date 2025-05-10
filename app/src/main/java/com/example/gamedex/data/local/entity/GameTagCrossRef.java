package com.example.gamedex.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"gameId", "tagId"}, tableName = "game_tag_cross_ref")
public class GameTagCrossRef {
    @NonNull
    private String gameId;
    private int tagId;

    public GameTagCrossRef(@NonNull String gameId, int tagId) {
        this.gameId = gameId;
        this.tagId = tagId;
    }

    @NonNull
    public String getGameId() {
        return gameId;
    }

    public void setGameId(@NonNull String gameId) {
        this.gameId = gameId;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }
}