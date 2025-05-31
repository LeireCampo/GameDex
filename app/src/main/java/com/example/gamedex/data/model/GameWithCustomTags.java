package com.example.gamedex.data.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.GameCustomTagCrossRef;

import java.util.List;

public class GameWithCustomTags {
    @Embedded
    public Game game;

    @Relation(
            parentColumn = "id",
            entityColumn = "id",
            associateBy = @Junction(
                    value = GameCustomTagCrossRef.class,
                    parentColumn = "gameId",
                    entityColumn = "customTagId"
            )
    )
    public List<CustomTag> customTags;

    // Constructor
    public GameWithCustomTags() {}

    // Métodos de utilidad
    public boolean hasCustomTag(String tagName) {
        if (customTags == null) return false;
        return customTags.stream().anyMatch(tag -> tag.getName().equals(tagName));
    }

    public boolean hasCustomTag(int tagId) {
        if (customTags == null) return false;
        return customTags.stream().anyMatch(tag -> tag.getId() == tagId);
    }

    public int getCustomTagCount() {
        return customTags != null ? customTags.size() : 0;
    }

    public List<String> getCustomTagNames() {
        if (customTags == null) return null;
        return customTags.stream()
                .map(CustomTag::getName)
                .collect(java.util.stream.Collectors.toList());
    }
}