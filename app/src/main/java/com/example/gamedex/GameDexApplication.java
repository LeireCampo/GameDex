package com.example.gamedex;

import android.app.Application;
import android.util.Log;

public class GameDexApplication extends Application {

    private static final String TAG = "GameDexApp";

    @Override
    public void onCreate() {
        super.onCreate();

        // Aquí puedes inicializar librerías globales, configuraciones, etc.
        Log.d(TAG, "GameDex Application iniciada");
    }
}