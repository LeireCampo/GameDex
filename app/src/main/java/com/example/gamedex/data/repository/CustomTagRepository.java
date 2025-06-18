package com.example.gamedex.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.gamedex.data.local.dao.CustomTagDao;
import com.example.gamedex.data.local.database.GameDexDatabase;
import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.GameCustomTagCrossRef;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CustomTagRepository {
    private final CustomTagDao customTagDao;
    private final ExecutorService executorService;

    public CustomTagRepository(Application application) {
        GameDexDatabase db = GameDexDatabase.getDatabase(application);
        customTagDao = db.customTagDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Métodos para obtener etiquetas
    public LiveData<List<CustomTag>> getCustomTagsByUser(String userId) {
        return customTagDao.getCustomTagsByUser(userId);
    }

    public LiveData<List<CustomTag>> getCustomTagsOffline() {
        return customTagDao.getCustomTagsOffline();
    }

    public LiveData<List<CustomTag>> getUserCustomTags(String userId) {
        return customTagDao.getUserCustomTags(userId);
    }

    public LiveData<List<CustomTag>> getMostUsedTags(String userId, int limit) {
        return customTagDao.getMostUsedTags(userId, limit);
    }

    public LiveData<List<CustomTag>> searchCustomTags(String query, String userId) {
        return customTagDao.searchCustomTags(query, userId);
    }

    public LiveData<List<CustomTag>> getDefaultTags() {
        return customTagDao.getDefaultTags();
    }

    // Métodos de inserción y actualización
    public void insertCustomTag(CustomTag customTag) {
        executorService.execute(() -> customTagDao.insertCustomTag(customTag));
    }

    public void insertCustomTagIfNotExists(CustomTag customTag) {
        executorService.execute(() -> {
            String userId = customTag.getUserId();
            int exists = customTagDao.checkCustomTagExists(customTag.getName(), userId);
            if (exists == 0) {
                customTagDao.insertCustomTag(customTag);
            }
        });
    }

    public void updateCustomTag(CustomTag customTag) {
        executorService.execute(() -> customTagDao.updateCustomTag(customTag));
    }

    public void deleteCustomTag(CustomTag customTag) {
        executorService.execute(() -> customTagDao.deleteCustomTag(customTag));
    }

    // Métodos para relaciones juego-etiqueta
    public void addTagToGame(String gameId, int customTagId) {
        executorService.execute(() -> {
            // Crear la relación
            GameCustomTagCrossRef crossRef = new GameCustomTagCrossRef(gameId, customTagId);
            customTagDao.addTagToGame(crossRef);

            // Incrementar contador de uso
            customTagDao.incrementTagUsage(customTagId);
        });
    }

    public void removeTagFromGame(String gameId, int customTagId) {
        executorService.execute(() -> {
            // Eliminar la relación
            customTagDao.removeTagFromGameById(gameId, customTagId);

            // Decrementar contador de uso
            customTagDao.decrementTagUsage(customTagId);
        });
    }

    public LiveData<List<CustomTag>> getTagsForGame(String gameId) {
        return customTagDao.getTagsForGame(gameId);
    }

    // Métodos de mantenimiento
    public void cleanUnusedTags(String userId) {
        executorService.execute(() -> customTagDao.cleanUnusedTags(userId));
    }

    public LiveData<List<Game>> getGamesByCustomTag(int customTagId) {
        return customTagDao.getGamesByCustomTag(customTagId);
    }
}