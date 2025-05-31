package com.example.gamedex.ui.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.gamedex.MainActivity;
import com.example.gamedex.R;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 3000; // 3 segundos
    private static final long ANIMATION_DURATION = 800;

    private ImageView logoImageView;
    private TextView appNameTextView;
    private TextView taglineTextView;
    private TextView loadingTextView;
    private TextView versionTextView;
    private ProgressBar progressBar;

    private Handler handler;
    private Runnable loadingRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configurar pantalla completa moderna
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_splash);

        initViews();
        startAnimations();
        startProgressAnimation();

        // Iniciar MainActivity después del delay
        handler = new Handler(Looper.getMainLooper());
        loadingRunnable = this::navigateToMain;
        handler.postDelayed(loadingRunnable, SPLASH_DELAY);
    }

    private void initViews() {
        logoImageView = findViewById(R.id.image_logo);
        appNameTextView = findViewById(R.id.text_app_name);
        taglineTextView = findViewById(R.id.text_tagline);
        loadingTextView = findViewById(R.id.text_loading);
        versionTextView = findViewById(R.id.text_version);
        progressBar = findViewById(R.id.progress_bar);

        // Establecer tu logo personalizado
        logoImageView.setImageResource(R.drawable.logosinfondo);

        // Configurar versión de la app
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            versionTextView.setText("v" + versionName);
        } catch (Exception e) {
            versionTextView.setText("v1.0.0");
        }

        // Inicialmente ocultar elementos para animación
        logoImageView.setAlpha(0f);
        logoImageView.setScaleX(0.3f);
        logoImageView.setScaleY(0.3f);
        logoImageView.setRotation(-15f); // Rotación inicial para efecto dinámico

        appNameTextView.setAlpha(0f);
        appNameTextView.setTranslationY(50f);

        taglineTextView.setAlpha(0f);
        taglineTextView.setTranslationY(30f);

        loadingTextView.setAlpha(0f);
        versionTextView.setAlpha(0f);
        progressBar.setAlpha(0f);
    }

    private void startAnimations() {
        // Animación del logo con efecto de entrada más dramático
        AnimatorSet logoAnimSet = new AnimatorSet();
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logoImageView, "alpha", 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoImageView, "scaleX", 0.3f, 1.1f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoImageView, "scaleY", 0.3f, 1.1f, 1f);
        ObjectAnimator logoRotation = ObjectAnimator.ofFloat(logoImageView, "rotation", -15f, 5f, 0f);

        logoAnimSet.playTogether(logoAlpha, logoScaleX, logoScaleY, logoRotation);
        logoAnimSet.setDuration(ANIMATION_DURATION + 200);
        logoAnimSet.setInterpolator(new OvershootInterpolator(1.2f));

        // Efecto de resplandor en el logo (opcional - usando alpha pulsante)
        ObjectAnimator logoGlow = ObjectAnimator.ofFloat(logoImageView, "alpha", 1f, 0.8f, 1f);
        logoGlow.setDuration(1500);
        logoGlow.setRepeatCount(ValueAnimator.INFINITE);
        logoGlow.setRepeatMode(ValueAnimator.REVERSE);
        logoGlow.setStartDelay(ANIMATION_DURATION + 500);

        // Animación del título con efecto neón
        AnimatorSet titleAnimSet = new AnimatorSet();
        ObjectAnimator titleAlpha = ObjectAnimator.ofFloat(appNameTextView, "alpha", 0f, 1f);
        ObjectAnimator titleTransY = ObjectAnimator.ofFloat(appNameTextView, "translationY", 50f, 0f);
        ObjectAnimator titleScaleX = ObjectAnimator.ofFloat(appNameTextView, "scaleX", 0.8f, 1f);
        ObjectAnimator titleScaleY = ObjectAnimator.ofFloat(appNameTextView, "scaleY", 0.8f, 1f);

        titleAnimSet.playTogether(titleAlpha, titleTransY, titleScaleX, titleScaleY);
        titleAnimSet.setDuration(ANIMATION_DURATION);
        titleAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());
        titleAnimSet.setStartDelay(400);

        // Animación del tagline
        AnimatorSet taglineAnimSet = new AnimatorSet();
        ObjectAnimator taglineAlpha = ObjectAnimator.ofFloat(taglineTextView, "alpha", 0f, 1f);
        ObjectAnimator taglineTransY = ObjectAnimator.ofFloat(taglineTextView, "translationY", 30f, 0f);

        taglineAnimSet.playTogether(taglineAlpha, taglineTransY);
        taglineAnimSet.setDuration(ANIMATION_DURATION);
        taglineAnimSet.setInterpolator(new AccelerateDecelerateInterpolator());
        taglineAnimSet.setStartDelay(700);

        // Animación de elementos inferiores
        ObjectAnimator progressAlpha = ObjectAnimator.ofFloat(progressBar, "alpha", 0f, 1f);
        progressAlpha.setDuration(ANIMATION_DURATION);
        progressAlpha.setStartDelay(1000);

        ObjectAnimator loadingAlpha = ObjectAnimator.ofFloat(loadingTextView, "alpha", 0f, 1f);
        loadingAlpha.setDuration(ANIMATION_DURATION);
        loadingAlpha.setStartDelay(1100);

        ObjectAnimator versionAlpha = ObjectAnimator.ofFloat(versionTextView, "alpha", 0f, 1f);
        versionAlpha.setDuration(ANIMATION_DURATION);
        versionAlpha.setStartDelay(1200);

        // Iniciar todas las animaciones
        logoAnimSet.start();
        logoGlow.start(); // Efecto de resplandor continuo
        titleAnimSet.start();
        taglineAnimSet.start();
        progressAlpha.start();
        loadingAlpha.start();
        versionAlpha.start();
    }

    private void startProgressAnimation() {
        // Animar barra de progreso con colores neón
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(SPLASH_DELAY - 500);
        progressAnimator.setStartDelay(1200);
        progressAnimator.addUpdateListener(animation -> {
            int progress = (int) animation.getAnimatedValue();
            progressBar.setProgress(progress);

            // Actualizar texto de loading con mensajes temáticos de gaming
            if (progress < 25) {
                loadingTextView.setText("Conectando con el servidor...");
            } else if (progress < 50) {
                loadingTextView.setText("Cargando tu biblioteca...");
            } else if (progress < 75) {
                loadingTextView.setText("Sincronizando datos...");
            } else if (progress < 90) {
                loadingTextView.setText("Preparando la experiencia...");
            } else {
                loadingTextView.setText("¡Listo para jugar!");
            }
        });

        progressAnimator.start();
    }

    private void navigateToMain() {
        // Animación de salida suave con efecto de escala
        AnimatorSet exitAnimSet = new AnimatorSet();

        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(
                findViewById(android.R.id.content), "alpha", 1f, 0f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(
                logoImageView, "scaleX", 1f, 0.8f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(
                logoImageView, "scaleY", 1f, 0.8f);

        exitAnimSet.playTogether(fadeOut, scaleDownX, scaleDownY);
        exitAnimSet.setDuration(400);

        // CORRECCIÓN: Usar addListener en lugar de setAnimatorListener
        exitAnimSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                // Transición suave entre actividades
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        exitAnimSet.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && loadingRunnable != null) {
            handler.removeCallbacks(loadingRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        // Deshabilitar botón back en splash
        // No llamar a super.onBackPressed()
    }
}