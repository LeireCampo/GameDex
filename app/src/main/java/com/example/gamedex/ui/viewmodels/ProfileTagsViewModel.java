package com.example.gamedex.ui.viewmodels;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamedex.data.firebase.FirebaseAuthService;
import com.example.gamedex.data.local.entity.CustomTag;
import com.example.gamedex.data.repository.CustomTagRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileTagsViewModel extends AndroidViewModel {
    private CustomTagRepository customTagRepository;
    private FirebaseAuthService authService;
    private ExecutorService executorService;

    public ProfileTagsViewModel(Application application) {
        super(application);
        customTagRepository = new CustomTagRepository(application);
        authService = FirebaseAuthService.getInstance();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Obtener todas las etiquetas del usuario
    public LiveData<List<CustomTag>> getUserTags() {
        if (authService != null && authService.isUserSignedIn()) {
            String userId = authService.getCurrentUserId();
            return customTagRepository.getCustomTagsByUser(userId);
        } else {
            return customTagRepository.getCustomTagsOffline();
        }
    }

    // Obtener etiquetas más usadas (para mostrar en el perfil)
    public LiveData<List<CustomTag>> getMostUsedTags(int limit) {
        if (authService != null && authService.isUserSignedIn()) {
            String userId = authService.getCurrentUserId();
            return customTagRepository.getMostUsedTags(userId, limit);
        } else {
            MutableLiveData<List<CustomTag>> emptyList = new MutableLiveData<>();
            emptyList.setValue(new ArrayList<>());
            return emptyList;
        }
    }

    // Crear nueva etiqueta
    public void createTag(String tagName, String color) {
        executorService.execute(() -> {
            try {
                CustomTag newTag = new CustomTag(tagName, color);

                // Establecer el ID del usuario si está autenticado
                if (authService != null && authService.isUserSignedIn()) {
                    newTag.setUserId(authService.getCurrentUserId());
                }

                customTagRepository.insertCustomTag(newTag);
            } catch (Exception e) {
                android.util.Log.e("ProfileTagsViewModel", "Error creando tag: " + e.getMessage());
            }
        });
    }

    // Eliminar etiqueta
    public void deleteTag(CustomTag tag) {
        executorService.execute(() -> {
            try {
                customTagRepository.deleteCustomTag(tag);
            } catch (Exception e) {
                android.util.Log.e("ProfileTagsViewModel", "Error eliminando tag: " + e.getMessage());
            }
        });
    }

    // Añadir etiqueta a un juego
    public void addTagToGame(String gameId, int tagId) {
        executorService.execute(() -> {
            try {
                customTagRepository.addTagToGame(gameId, tagId);
            } catch (Exception e) {
                android.util.Log.e("ProfileTagsViewModel", "Error añadiendo tag al juego: " + e.getMessage());
            }
        });
    }

    // Remover etiqueta de un juego
    public void removeTagFromGame(String gameId, int tagId) {
        executorService.execute(() -> {
            try {
                customTagRepository.removeTagFromGame(gameId, tagId);
            } catch (Exception e) {
                android.util.Log.e("ProfileTagsViewModel", "Error removiendo tag del juego: " + e.getMessage());
            }
        });
    }

    // Obtener etiquetas de un juego específico
    public LiveData<List<CustomTag>> getTagsForGame(String gameId) {
        return customTagRepository.getTagsForGame(gameId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}