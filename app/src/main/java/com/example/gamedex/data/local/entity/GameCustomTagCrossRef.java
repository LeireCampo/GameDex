package com.example.gamedex.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

@Entity(
        primaryKeys = {"gameId", "customTagId"},
        tableName = "game_custom_tag_cross_ref",
        indices = {
                @Index(value = "gameId"),
                @Index(value = "customTagId"),
                @Index(value = {"gameId", "customTagId"}, unique = true)
        }
)
public class GameCustomTagCrossRef {
    @NonNull
    private String gameId;
    private int customTagId;
    private long assignedAt; // Cuándo se asignó la etiqueta

    public GameCustomTagCrossRef(@NonNull String gameId, int customTagId) {
        this.gameId = gameId;
        this.customTagId = customTagId;
        this.assignedAt = System.currentTimeMillis();
    }

    @NonNull
    public String getGameId() {
        return gameId;
    }

    public void setGameId(@NonNull String gameId) {
        this.gameId = gameId;
    }

    public int getCustomTagId() {
        return customTagId;
    }

    public void setCustomTagId(int customTagId) {
        this.customTagId = customTagId;
    }

    public long getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(long assignedAt) {
        this.assignedAt = assignedAt;
    }
}