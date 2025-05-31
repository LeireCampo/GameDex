package com.example.gamedex.data.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamedex.data.local.dao.TagDao;
import com.example.gamedex.data.local.database.GameDexDatabase;
import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.local.entity.GameTagCrossRef;
import com.example.gamedex.data.local.entity.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TagRepository {
    private final TagDao tagDao;
    private final ExecutorService executorService;

    public TagRepository(Application application) {
        GameDexDatabase db = GameDexDatabase.getDatabase(application);
        tagDao = db.tagDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Tag>> getAllTags() {
        return tagDao.getAllTags();
    }

    public void insertTag(Tag tag) {
        executorService.execute(() -> tagDao.insertTag(tag));
    }

    public void updateTag(Tag tag) {
        executorService.execute(() -> tagDao.updateTag(tag));
    }

    public void deleteTag(Tag tag) {
        executorService.execute(() -> tagDao.deleteTag(tag));
    }

    public void addTagToGame(String gameId, int tagId) {
        executorService.execute(() -> tagDao.addTagToGame(new GameTagCrossRef(gameId, tagId)));
    }

    public void removeTagFromGame(String gameId, int tagId) {
        executorService.execute(() -> tagDao.removeTagFromGameById(gameId, tagId));
    }

    public LiveData<List<Tag>> getTagsForGame(String gameId) {
        return tagDao.getTagsForGame(gameId);
    }

    public Tag getTagById(int tagId) {
        // This would normally be done asynchronously, simplified for example
        return tagDao.getTagById(tagId);
    }

    // Nuevo método para verificar si existe un tag
    public int checkTagExists(String tagName) {
        try {
            return tagDao.checkTagExists(tagName);
        } catch (Exception e) {
            Log.e("TagRepository", "Error verificando existencia de tag: " + e.getMessage());
            return 0;
        }
    }

    // Nuevo método para insertar tag y obtener ID
    public long insertTagAndGetId(Tag tag) {
        try {
            return tagDao.insertTagAndGetId(tag);
        } catch (Exception e) {
            Log.e("TagRepository", "Error insertando tag: " + e.getMessage());
            return -1;
        }
    }

    // Método para obtener tags del usuario (no del sistema)
    public LiveData<List<Tag>> getUserTags() {
        return tagDao.getUserTags();
    }

    // Método para obtener tags por juego con detalles
    public LiveData<List<TagWithGameCount>> getTagsWithGameCount() {
        return tagDao.getTagsWithGameCount();
    }

    // Método para obtener juegos por tag específico
    public LiveData<List<Game>> getGamesByTag(int tagId) {
        return tagDao.getGamesByTag(tagId);
    }

    // Método para obtener estadísticas de tags del usuario
    public LiveData<Map<String, Integer>> getTagStatistics() {
        MutableLiveData<Map<String, Integer>> result = new MutableLiveData<>();

        executorService.execute(() -> {
            try {
                Map<String, Integer> stats = new HashMap<>();
                List<Tag> userTags = tagDao.getUserTagsSync();

                for (Tag tag : userTags) {
                    int gameCount = tagDao.getGameCountForTag(tag.getId());
                    stats.put(tag.getName(), gameCount);
                }

                result.postValue(stats);
            } catch (Exception e) {
                Log.e("TagRepository", "Error obteniendo estadísticas: " + e.getMessage());
                result.postValue(new HashMap<>());
            }
        });

        return result;
    }

    // Método para eliminar tag solo si no tiene juegos asociados
    public void deleteTagIfEmpty(Tag tag, TagDeleteCallback callback) {
        executorService.execute(() -> {
            try {
                int gameCount = tagDao.getGameCountForTag(tag.getId());
                if (gameCount == 0) {
                    tagDao.deleteTag(tag);
                    callback.onSuccess();
                } else {
                    callback.onError("No se puede eliminar: la etiqueta tiene " + gameCount + " juegos asociados");
                }
            } catch (Exception e) {
                callback.onError("Error al eliminar la etiqueta: " + e.getMessage());
            }
        });
    }

    // Interface para callback de eliminación
    public interface TagDeleteCallback {
        void onSuccess();
        void onError(String error);
    }

    // Nueva clase para tags con contador de juegos
    public static class TagWithGameCount {
        public Tag tag;
        public int gameCount;

        public TagWithGameCount(Tag tag, int gameCount) {
            this.tag = tag;
            this.gameCount = gameCount;
        }
    }
}


