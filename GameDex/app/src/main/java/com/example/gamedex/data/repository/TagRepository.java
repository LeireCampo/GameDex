package com.example.gamedex.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.gamedex.data.local.dao.TagDao;
import com.example.gamedex.data.local.database.GameDexDatabase;
import com.example.gamedex.data.local.entity.GameTagCrossRef;
import com.example.gamedex.data.local.entity.Tag;

import java.util.List;
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
}