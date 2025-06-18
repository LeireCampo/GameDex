package com.example.gamedex.data.model;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.GameCustomTagCrossRef;
import com.example.gamedex.data.local.entity.GameTagCrossRef;
import com.example.gamedex.data.local.entity.Tag;

import java.util.List;

public class GameWithTags {
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
    public List<CustomTag> tags;

}