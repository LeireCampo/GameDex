package com.example.gamedex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.model.GameWithTags;

import java.util.List;

@Dao
public interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertGame(Game game);

    @Update
    void updateGame(Game game);

    @Delete
    void deleteGame(Game game);

    @Query("SELECT * FROM games WHERE id = :gameId")
    Game getGameById(String gameId);

    @Query("SELECT * FROM games WHERE isInLibrary = 1")
    LiveData<List<Game>> getAllLibraryGames();

    @Query("SELECT * FROM games WHERE title LIKE '%' || :searchQuery || '%'")
    LiveData<List<Game>> searchGames(String searchQuery);

    @Transaction
    @Query("SELECT * FROM games WHERE id = :gameId")
    LiveData<GameWithTags> getGameWithTags(String gameId);

    @Transaction
    @Query("SELECT * FROM games WHERE isInLibrary = 1")
    LiveData<List<GameWithTags>> getLibraryGamesWithTags();

    @Query("SELECT * FROM games WHERE status = :status")
    LiveData<List<Game>> getGamesByStatus(String status);
}