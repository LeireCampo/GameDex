package com.example.gamedex.data.firebase;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.gamedex.data.local.entity.Game;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreService {
    private static final String TAG = "FirestoreService";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_USER_GAMES = "userGames";

    private static FirestoreService instance;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth auth;

    private FirestoreService() {
        try {
            firestore = FirebaseFirestore.getInstance();
            auth = FirebaseAuth.getInstance();
            Log.d(TAG, "FirestoreService inicializado correctamente");
        } catch (Exception e) {
            Log.e(TAG, "Error inicializando FirestoreService: " + e.getMessage());
            throw new IllegalStateException("Firebase no está disponible: " + e.getMessage());
        }
    }

    public static synchronized FirestoreService getInstance() {
        if (instance == null) {
            instance = new FirestoreService();
        }
        return instance;
    }

    // Crear perfil de usuario
    public void createUserProfile(String username, FirestoreCallback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("uid", user.getUid());
        userProfile.put("username", username);
        userProfile.put("email", user.getEmail());
        userProfile.put("createdAt", System.currentTimeMillis());
        userProfile.put("lastUpdated", System.currentTimeMillis());

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Perfil de usuario creado exitosamente");
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creando perfil de usuario", e);
                    callback.onError("Error creando perfil: " + e.getMessage());
                });
    }

    // Sincronizar juego con Firestore
    public void syncGameToFirestore(Game game, FirestoreCallback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        if (!game.isInLibrary()) {
            // Si el juego no está en la biblioteca, eliminarlo de Firestore
            removeGameFromFirestore(game.getId(), callback);
            return;
        }

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("id", game.getId());
        gameData.put("title", game.getTitle());
        gameData.put("developer", game.getDeveloper());
        gameData.put("coverUrl", game.getCoverUrl());
        gameData.put("status", game.getStatus());
        gameData.put("userRating", game.getUserRating());
        gameData.put("isInLibrary", game.isInLibrary());
        gameData.put("lastUpdated", game.getLastUpdated());

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .collection(COLLECTION_USER_GAMES)
                .document(game.getId())
                .set(gameData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Juego sincronizado: " + game.getTitle());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error sincronizando juego", e);
                    callback.onError("Error sincronizando: " + e.getMessage());
                });
    }

    // Eliminar juego de Firestore
    private void removeGameFromFirestore(String gameId, FirestoreCallback<Void> callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onError("Usuario no autenticado");
            return;
        }

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .collection(COLLECTION_USER_GAMES)
                .document(gameId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Juego eliminado de Firestore: " + gameId);
                    callback.onSuccess(null);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error eliminando juego de Firestore", e);
                    callback.onError("Error eliminando: " + e.getMessage());
                });
    }

    // Obtener juegos del usuario desde Firestore
    public LiveData<List<Game>> getUserGamesFromFirestore() {
        MutableLiveData<List<Game>> gamesLiveData = new MutableLiveData<>();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            gamesLiveData.setValue(new ArrayList<>());
            return gamesLiveData;
        }

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .collection(COLLECTION_USER_GAMES)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Error obteniendo juegos", error);
                        gamesLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Game> games = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            try {
                                Game game = documentToGame(doc);
                                if (game != null) {
                                    games.add(game);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error convirtiendo documento a juego", e);
                            }
                        }
                    }

                    Log.d(TAG, "Juegos cargados desde Firestore: " + games.size());
                    gamesLiveData.setValue(games);
                });

        return gamesLiveData;
    }

    // Obtener estadísticas del usuario
    public LiveData<Map<String, Integer>> getUserStats() {
        MutableLiveData<Map<String, Integer>> statsLiveData = new MutableLiveData<>();

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            statsLiveData.setValue(new HashMap<>());
            return statsLiveData;
        }

        firestore.collection(COLLECTION_USERS)
                .document(user.getUid())
                .collection(COLLECTION_USER_GAMES)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.w(TAG, "Error obteniendo estadísticas", error);
                        statsLiveData.setValue(new HashMap<>());
                        return;
                    }

                    Map<String, Integer> stats = new HashMap<>();
                    stats.put("total", 0);
                    stats.put("completed", 0);
                    stats.put("playing", 0);
                    stats.put("backlog", 0);
                    stats.put("wishlist", 0);

                    if (value != null) {
                        stats.put("total", value.size());

                        for (QueryDocumentSnapshot doc : value) {
                            String status = doc.getString("status");
                            if (status != null) {
                                Integer currentCount = stats.get(status);
                                if (currentCount != null) {
                                    stats.put(status, currentCount + 1);
                                }
                            }
                        }
                    }

                    Log.d(TAG, "Estadísticas calculadas: " + stats);
                    statsLiveData.setValue(stats);
                });

        return statsLiveData;
    }

    // Convertir documento de Firestore a objeto Game
    private Game documentToGame(QueryDocumentSnapshot doc) {
        try {
            String id = doc.getString("id");
            String title = doc.getString("title");

            if (id == null || title == null) {
                Log.w(TAG, "Documento incompleto, saltando: " + doc.getId());
                return null;
            }

            Game game = new Game(id, title);
            game.setDeveloper(doc.getString("developer"));
            game.setCoverUrl(doc.getString("coverUrl"));
            game.setStatus(doc.getString("status"));
            game.setInLibrary(Boolean.TRUE.equals(doc.getBoolean("isInLibrary")));

            // Manejar userRating que puede ser Double o null
            Double userRating = doc.getDouble("userRating");
            if (userRating != null) {
                game.setUserRating(userRating.floatValue());
            }

            // Manejar lastUpdated
            Long lastUpdated = doc.getLong("lastUpdated");
            if (lastUpdated != null) {
                game.setLastUpdated(lastUpdated);
            }

            return game;
        } catch (Exception e) {
            Log.e(TAG, "Error convirtiendo documento: " + doc.getId(), e);
            return null;
        }
    }

    // Interface para callbacks
    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }
}