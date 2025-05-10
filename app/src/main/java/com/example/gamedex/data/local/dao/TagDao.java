package com.example.gamedex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gamedex.data.local.entity.GameTagCrossRef;
import com.example.gamedex.data.local.entity.Tag;

import java.util.List;

@Dao
public interface TagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTag(Tag tag);

    @Update
    void updateTag(Tag tag);

    @Delete
    void deleteTag(Tag tag);

    @Query("SELECT * FROM tags")
    LiveData<List<Tag>> getAllTags();

    @Query("SELECT * FROM tags WHERE id = :tagId")
    Tag getTagById(int tagId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addTagToGame(GameTagCrossRef crossRef);

    @Delete
    void removeTagFromGame(GameTagCrossRef crossRef);

    @Query("DELETE FROM game_tag_cross_ref WHERE gameId = :gameId AND tagId = :tagId")
    void removeTagFromGameById(String gameId, int tagId);

    @Query("SELECT * FROM tags INNER JOIN game_tag_cross_ref ON tags.id = game_tag_cross_ref.tagId WHERE game_tag_cross_ref.gameId = :gameId")
    LiveData<List<Tag>> getTagsForGame(String gameId);
}