package com.example.gamedex.util;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.Window;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

/**
 * Clase de utilidad para transiciones y navegación con efectos neón
 */
public class NavigationUtils {

    /**
     * Inicia una actividad con una transición compartida
     * @param activity Actividad actual
     * @param intent Intent de la actividad a iniciar
     * @param sharedElement Elemento compartido para la transición
     * @param transitionName Nombre de la transición
     */
    public static void startActivityWithTransition(Activity activity, Intent intent,
                                                   View sharedElement, String transitionName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Habilitamos la transición en la ventana
            activity.getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

            // Creamos las opciones de transición
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                    activity,
                    sharedElement,
                    transitionName
            );

            // Iniciamos la actividad con las opciones
            activity.startActivity(intent, options.toBundle());
        } else {
            // En versiones anteriores, simplemente iniciamos la actividad
            activity.startActivity(intent);
        }
    }

    /**
     * Inicia una actividad con múltiples elementos compartidos
     * @param activity Actividad actual
     * @param intent Intent de la actividad a iniciar
     * @param pairs Pares de vistas y nombres de transición
     */
    @SafeVarargs
    public static void startActivityWithMultipleTransition(Activity activity, Intent intent,
                                                           Pair<View, String>... pairs) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);

            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity,
                    pairs
            );

            activity.startActivity(intent, options.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    /**
     * Aplica un efecto neón a la barra de estado
     * @param activity Actividad a la que aplicar el efecto
     * @param colorResId ID del recurso de color
     */
    public static void applyNeonStatusBar(Activity activity, int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.setStatusBarColor(activity.getResources().getColor(colorResId));
        }
    }
}