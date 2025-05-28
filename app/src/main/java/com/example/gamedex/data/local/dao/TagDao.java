package com.example.gamedex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
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

    // Opción 1: Usar @RewriteQueriesToDropUnusedColumns para optimizar automáticamente
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM tags INNER JOIN game_tag_cross_ref ON tags.id = game_tag_cross_ref.tagId WHERE game_tag_cross_ref.gameId = :gameId")
    LiveData<List<Tag>> getTagsForGame(String gameId);

    // Opción 2: Query más específica que solo selecciona las columnas necesarias
    // Puedes usar esta en lugar de la anterior si prefieres ser más explícito
    @Query("SELECT tags.id, tags.name, tags.color, tags.isSystemTag, tags.createdAt " +
            "FROM tags INNER JOIN game_tag_cross_ref ON tags.id = game_tag_cross_ref.tagId " +
            "WHERE game_tag_cross_ref.gameId = :gameId")
    LiveData<List<Tag>> getTagsForGameExplicit(String gameId);

    // Método adicional para obtener todos los tags disponibles para un juego específico
    // (todos los tags que NO están ya asociados al juego)
    @Query("SELECT * FROM tags WHERE id NOT IN " +
            "(SELECT tagId FROM game_tag_cross_ref WHERE gameId = :gameId)")
    LiveData<List<Tag>> getAvailableTagsForGame(String gameId);

    // Método para obtener tags del sistema
    @Query("SELECT * FROM tags WHERE isSystemTag = 1")
    LiveData<List<Tag>> getSystemTags();

    // Método para obtener tags personalizados del usuario
    @Query("SELECT * FROM tags WHERE isSystemTag = 0")
    LiveData<List<Tag>> getUserTags();

    // Método para buscar tags por nombre
    @Query("SELECT * FROM tags WHERE name LIKE '%' || :query || '%'")
    LiveData<List<Tag>> searchTags(String query);

    // Método para obtener el conteo de juegos por tag
    @Query("SELECT COUNT(*) FROM game_tag_cross_ref WHERE tagId = :tagId")
    int getGameCountForTag(int tagId);

    // Método para insertar tag y devolver el ID generado
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTagAndGetId(Tag tag);

    // Método para verificar si un tag ya existe por nombre
    @Query("SELECT COUNT(*) FROM tags WHERE name = :tagName")
    int checkTagExists(String tagName);

    // Método para obtener tag por nombre
    @Query("SELECT * FROM tags WHERE name = :tagName LIMIT 1")
    Tag getTagByName(String tagName);
}