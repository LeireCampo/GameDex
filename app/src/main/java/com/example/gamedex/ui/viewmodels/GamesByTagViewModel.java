// Nuevo archivo: GamesByTagViewModel.java
package com.example.gamedex.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.repository.TagRepository;

import java.util.List;

public class GamesByTagViewModel extends AndroidViewModel {

    private TagRepository tagRepository;
    private LiveData<List<Game>> gamesByTag;

    public GamesByTagViewModel(Application application) {
        super(application);
        tagRepository = new TagRepository(application);
    }

    public void init(int tagId) {
        if (gamesByTag != null) {
            return; // Ya inicializado
        }
        gamesByTag = tagRepository.getGamesByTag(tagId);
    }

    public LiveData<List<Game>> getGamesByTag() {
        return gamesByTag;
    }
}