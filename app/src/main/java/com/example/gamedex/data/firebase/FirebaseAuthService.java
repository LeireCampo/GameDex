package com.example.gamedex.data.firebase;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseAuthService {
    private static final String TAG = "FirebaseAuthService";
    private static FirebaseAuthService instance;
    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<FirebaseUser> currentUserLiveData;

    private FirebaseAuthService() {
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserLiveData = new MutableLiveData<>();

        // Observar cambios en el estado de autenticación
        firebaseAuth.addAuthStateListener(auth -> {
            FirebaseUser user = auth.getCurrentUser();
            currentUserLiveData.setValue(user);
            Log.d(TAG, "Auth state changed: " + (user != null ? user.getEmail() : "null"));
        });
    }

    public static synchronized FirebaseAuthService getInstance() {
        if (instance == null) {
            instance = new FirebaseAuthService();
        }
        return instance;
    }

    // Registrar usuario con email y contraseña
    public void registerUser(String email, String password, String username, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            // Actualizar el perfil con el nombre de usuario
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(profileTask -> {
                                if (profileTask.isSuccessful()) {
                                    Log.d(TAG, "Usuario registrado y perfil actualizado");
                                    callback.onSuccess();
                                } else {
                                    Log.e(TAG, "Error al actualizar perfil", profileTask.getException());
                                    callback.onError("Error al actualizar perfil");
                                }
                            });
                        }
                    } else {
                        Log.e(TAG, "Error en registro", task.getException());
                        callback.onError(getErrorMessage(task.getException()));
                    }
                });
    }

    // Iniciar sesión con email y contraseña
    public void signInUser(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Inicio de sesión exitoso");
                        callback.onSuccess();
                    } else {
                        Log.e(TAG, "Error en inicio de sesión", task.getException());
                        callback.onError(getErrorMessage(task.getException()));
                    }
                });
    }

    // Cerrar sesión
    public void signOut() {
        firebaseAuth.signOut();
        Log.d(TAG, "Usuario cerró sesión");
    }

    // Obtener usuario actual
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    // LiveData para observar cambios en el usuario
    public LiveData<FirebaseUser> getCurrentUserLiveData() {
        return currentUserLiveData;
    }

    // Verificar si hay un usuario autenticado
    public boolean isUserSignedIn() {
        return getCurrentUser() != null;
    }

    // Obtener UID del usuario actual
    public String getCurrentUserId() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Obtener nombre de usuario actual
    public String getCurrentUsername() {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            return displayName != null ? displayName : user.getEmail();
        }
        return null;
    }

    // Actualizar perfil del usuario
    public void updateUserProfile(String newUsername, AuthCallback callback) {
        FirebaseUser user = getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newUsername)
                    .build();

            user.updateProfile(profileUpdates).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "Perfil actualizado exitosamente");
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "Error al actualizar perfil", task.getException());
                    callback.onError("Error al actualizar perfil");
                }
            });
        } else {
            callback.onError("No hay usuario autenticado");
        }
    }

    // Restablecer contraseña
    public void resetPassword(String email, AuthCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Email de restablecimiento enviado");
                        callback.onSuccess();
                    } else {
                        Log.e(TAG, "Error al enviar email de restablecimiento", task.getException());
                        callback.onError("Error al enviar email de restablecimiento");
                    }
                });
    }

    // Convertir errores de Firebase a mensajes legibles
    private String getErrorMessage(Exception exception) {
        if (exception == null) return "Error desconocido";

        String message = exception.getMessage();
        if (message == null) return "Error desconocido";

        // Personalizar mensajes de error más comunes
        if (message.contains("email address is already in use")) {
            return "Este email ya está registrado";
        } else if (message.contains("password is invalid")) {
            return "Contraseña incorrecta";
        } else if (message.contains("no user record")) {
            return "No existe una cuenta con este email";
        } else if (message.contains("network error")) {
            return "Error de conexión";
        } else if (message.contains("weak-password")) {
            return "La contraseña debe tener al menos 6 caracteres";
        } else if (message.contains("invalid-email")) {
            return "Email inválido";
        }

        return message;
    }

    // Interface para callbacks de autenticación
    public interface AuthCallback {
        void onSuccess();
        void onError(String error);
    }
}