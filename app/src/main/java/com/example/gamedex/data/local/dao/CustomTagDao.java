package com.example.gamedex.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.RewriteQueriesToDropUnusedColumns;
import androidx.room.Update;

import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.data.local.entity.Game;

import java.util.List;

@Dao
public interface CustomTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertCustomTag(CustomTag customTag);

    @Update
    void updateCustomTag(CustomTag customTag);

    @Delete
    void deleteCustomTag(CustomTag customTag);

    // Obtener todas las etiquetas del usuario actual
    @Query("SELECT * FROM custom_tags WHERE userId = :userId OR isDefault = 1 ORDER BY usageCount DESC, name ASC")
    LiveData<List<CustomTag>> getCustomTagsByUser(String userId);

    // Obtener etiquetas sin usuario (para modo offline)
    @Query("SELECT * FROM custom_tags WHERE userId IS NULL OR isDefault = 1 ORDER BY usageCount DESC, name ASC")
    LiveData<List<CustomTag>> getCustomTagsOffline();

    // Obtener etiqueta por ID
    @Query("SELECT * FROM custom_tags WHERE id = :tagId")
    CustomTag getCustomTagById(int tagId);

    // Buscar etiquetas por nombre
    @Query("SELECT * FROM custom_tags WHERE name LIKE '%' || :query || '%' AND (userId = :userId OR isDefault = 1)")
    LiveData<List<CustomTag>> searchCustomTags(String query, String userId);

    // Verificar si existe una etiqueta con el mismo nombre
    @Query("SELECT COUNT(*) FROM custom_tags WHERE name = :name AND (userId = :userId OR isDefault = 1)")
    int checkCustomTagExists(String name, String userId);

    // Obtener etiquetas más usadas
    @Query("SELECT * FROM custom_tags WHERE (userId = :userId OR isDefault = 1) AND usageCount > 0 ORDER BY usageCount DESC LIMIT :limit")
    LiveData<List<CustomTag>> getMostUsedTags(String userId, int limit);

    // Obtener etiquetas por color
    @Query("SELECT * FROM custom_tags WHERE color = :color AND (userId = :userId OR isDefault = 1)")
    LiveData<List<CustomTag>> getCustomTagsByColor(String color, String userId);

    // Incrementar contador de uso
    @Query("UPDATE custom_tags SET usageCount = usageCount + 1 WHERE id = :tagId")
    void incrementTagUsage(int tagId);

    // Decrementar contador de uso
    @Query("UPDATE custom_tags SET usageCount = usageCount - 1 WHERE id = :tagId AND usageCount > 0")
    void decrementTagUsage(int tagId);

    // Obtener etiquetas predefinidas
    @Query("SELECT * FROM custom_tags WHERE isDefault = 1 ORDER BY name ASC")
    LiveData<List<CustomTag>> getDefaultTags();

    // Obtener etiquetas del usuario (sin predefinidas)
    @Query("SELECT * FROM custom_tags WHERE userId = :userId AND isDefault = 0 ORDER BY usageCount DESC, name ASC")
    LiveData<List<CustomTag>> getUserCustomTags(String userId);

    // Limpiar etiquetas sin uso (para mantenimiento)
    @Query("DELETE FROM custom_tags WHERE usageCount = 0 AND isDefault = 0 AND userId = :userId")
    void cleanUnusedTags(String userId);

    // Métodos para relaciones juego-etiqueta
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addTagToGame(com.example.gamedex.data.local.entity.GameCustomTagCrossRef crossRef);

    @Delete
    void removeTagFromGame(com.example.gamedex.data.local.entity.GameCustomTagCrossRef crossRef);

    @Query("DELETE FROM game_custom_tag_cross_ref WHERE gameId = :gameId AND customTagId = :customTagId")
    void removeTagFromGameById(String gameId, int customTagId);

    // MÉTODO CORREGIDO: Añadida anotación @RewriteQueriesToDropUnusedColumns
    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM custom_tags INNER JOIN game_custom_tag_cross_ref ON custom_tags.id = game_custom_tag_cross_ref.customTagId WHERE game_custom_tag_cross_ref.gameId = :gameId")
    LiveData<List<CustomTag>> getTagsForGame(String gameId);

    @Query("SELECT games.* FROM games " +
            "INNER JOIN game_custom_tag_cross_ref ON games.id = game_custom_tag_cross_ref.gameId " +
            "WHERE game_custom_tag_cross_ref.customTagId = :customTagId")
    LiveData<List<Game>> getGamesByCustomTag(int customTagId);
}