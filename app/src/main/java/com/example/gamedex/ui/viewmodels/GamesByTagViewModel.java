package com.example.gamedex.ui.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.repository.CustomTagRepository;

import java.util.List;

public class GamesByTagViewModel extends AndroidViewModel {

    private CustomTagRepository customTagRepository;
    private MutableLiveData<Integer> tagIdLiveData = new MutableLiveData<>();
    private LiveData<List<Game>> gamesByTag;

    public GamesByTagViewModel(@NonNull Application application) {
        super(application);
        customTagRepository = new CustomTagRepository(application);

        // Configurar transformación para obtener juegos por custom tag
        gamesByTag = Transformations.switchMap(tagIdLiveData, tagId -> {
            if (tagId != null && tagId != -1) {
                return customTagRepository.getGamesByCustomTag(tagId);
            }
            return new MutableLiveData<>();
        });
    }

    public void init(int tagId) {
        tagIdLiveData.setValue(tagId);
    }

    public LiveData<List<Game>> getGamesByTag() {
        return gamesByTag;
    }

    public void refreshGames() {
        // Forzar actualización si es necesario
        Integer currentTagId = tagIdLiveData.getValue();
        if (currentTagId != null) {
            tagIdLiveData.setValue(currentTagId);
        }
    }
}