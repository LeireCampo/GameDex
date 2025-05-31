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

public class CustomTagViewModel extends AndroidViewModel {
    private CustomTagRepository customTagRepository;
    private FirebaseAuthService authService;
    private ExecutorService executorService;

    public CustomTagViewModel(Application application) {
        super(application);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void init(Application application) {
        if (customTagRepository != null) {
            return; // Ya inicializado
        }

        customTagRepository = new CustomTagRepository(application);
        authService = FirebaseAuthService.getInstance();
    }

    // Obtener todas las etiquetas personalizadas
    public LiveData<List<CustomTag>> getAllCustomTags() {
        if (authService != null && authService.isUserSignedIn()) {
            String userId = authService.getCurrentUserId();
            return customTagRepository.getCustomTagsByUser(userId);
        } else {
            return customTagRepository.getCustomTagsOffline();
        }
    }

    // Obtener etiquetas del usuario (sin predefinidas)
    public LiveData<List<CustomTag>> getUserCustomTags() {
        if (authService != null && authService.isUserSignedIn()) {
            String userId = authService.getCurrentUserId();
            return customTagRepository.getUserCustomTags(userId);
        } else {
            // Retornar lista vacía para usuarios no autenticados
            MutableLiveData<List<CustomTag>> emptyList = new MutableLiveData<>();
            emptyList.setValue(new ArrayList<>());
            return emptyList;
        }
    }

    // Obtener etiquetas más usadas
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

    // Añadir nueva etiqueta personalizada
    public void addCustomTag(CustomTag customTag) {
        if (customTagRepository == null) return;

        executorService.execute(() -> {
            try {
                // Establecer el ID del usuario si está autenticado
                if (authService != null && authService.isUserSignedIn()) {
                    customTag.setUserId(authService.getCurrentUserId());
                }

                customTagRepository.insertCustomTagIfNotExists(customTag);
            } catch (Exception e) {
                android.util.Log.e("CustomTagViewModel", "Error añadiendo etiqueta: " + e.getMessage());
            }
        });
    }

    // Actualizar etiqueta
    public void updateCustomTag(CustomTag customTag) {
        if (customTagRepository == null) return;

        executorService.execute(() -> {
            try {
                customTagRepository.updateCustomTag(customTag);
            } catch (Exception e) {
                android.util.Log.e("CustomTagViewModel", "Error actualizando etiqueta: " + e.getMessage());
            }
        });
    }

    // Eliminar etiqueta
    public void deleteCustomTag(CustomTag customTag) {
        if (customTagRepository == null) return;

        executorService.execute(() -> {
            try {
                customTagRepository.deleteCustomTag(customTag);
            } catch (Exception e) {
                android.util.Log.e("CustomTagViewModel", "Error eliminando etiqueta: " + e.getMessage());
            }
        });
    }

    // Añadir etiqueta a un juego
    public void addTagToGame(String gameId, int customTagId) {
        if (customTagRepository == null) return;

        executorService.execute(() -> {
            try {
                customTagRepository.addTagToGame(gameId, customTagId);
            } catch (Exception e) {
                android.util.Log.e("CustomTagViewModel", "Error añadiendo etiqueta al juego: " + e.getMessage());
            }
        });
    }

    // Remover etiqueta de un juego
    public void removeTagFromGame(String gameId, int customTagId) {
        if (customTagRepository == null) return;

        executorService.execute(() -> {
            try {
                customTagRepository.removeTagFromGame(gameId, customTagId);
            } catch (Exception e) {
                android.util.Log.e("CustomTagViewModel", "Error removiendo etiqueta del juego: " + e.getMessage());
            }
        });
    }

    // Obtener etiquetas de un juego específico
    public LiveData<List<CustomTag>> getTagsForGame(String gameId) {
        if (customTagRepository == null) {
            MutableLiveData<List<CustomTag>> emptyList = new MutableLiveData<>();
            emptyList.setValue(new ArrayList<>());
            return emptyList;
        }

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