package com.example.gamedex.ui.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gamedex.MainActivity;
import com.example.gamedex.R;
import com.example.gamedex.data.firebase.FirebaseAuthService;
import com.example.gamedex.data.firebase.FirestoreService;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class AuthActivity extends AppCompatActivity {

    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputEditText passwordEditText;
    private TextInputLayout usernameInputLayout;
    private TextInputEditText usernameEditText;
    private Button loginButton;
    private Button registerButton;
    private Button switchModeButton;
    private Button skipButton;
    private ProgressBar progressBar;
    private View rootView;

    private FirebaseAuthService authService;
    private FirestoreService firestoreService;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        initServices();
        initViews();
        setupClickListeners();
        updateUIMode();

        // Verificar si el usuario ya está autenticado
        if (authService.isUserSignedIn()) {
            navigateToMain();
        }
    }

    private void initServices() {
        authService = FirebaseAuthService.getInstance();
        firestoreService = FirestoreService.getInstance();
    }

    private void initViews() {
        rootView = findViewById(R.id.root_view);
        emailInputLayout = findViewById(R.id.email_input_layout);
        emailEditText = findViewById(R.id.email_edit_text);
        passwordInputLayout = findViewById(R.id.password_input_layout);
        passwordEditText = findViewById(R.id.password_edit_text);
        usernameInputLayout = findViewById(R.id.username_input_layout);
        usernameEditText = findViewById(R.id.username_edit_text);
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        switchModeButton = findViewById(R.id.switch_mode_button);
        skipButton = findViewById(R.id.skip_button);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        registerButton.setOnClickListener(v -> performRegister());
        switchModeButton.setOnClickListener(v -> switchMode());
        skipButton.setOnClickListener(v -> navigateToMain());
    }

    private void switchMode() {
        isLoginMode = !isLoginMode;
        updateUIMode();
        clearErrors();
    }

    private void updateUIMode() {
        if (isLoginMode) {
            // Modo inicio de sesión
            usernameInputLayout.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            registerButton.setVisibility(View.GONE);
            switchModeButton.setText(R.string.switch_to_register);
        } else {
            // Modo registro
            usernameInputLayout.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.GONE);
            registerButton.setVisibility(View.VISIBLE);
            switchModeButton.setText(R.string.switch_to_login);
        }
    }

    private void performLogin() {
        if (!validateInputs(true)) return;

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        showLoading(true);

        authService.signInUser(email, password, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Snackbar.make(rootView, R.string.login_successful, Snackbar.LENGTH_SHORT).show();
                navigateToMain();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void performRegister() {
        if (!validateInputs(false)) return;

        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();

        showLoading(true);

        authService.registerUser(email, password, username, new FirebaseAuthService.AuthCallback() {
            @Override
            public void onSuccess() {
                // Crear perfil en Firestore
                firestoreService.createUserProfile(username, new FirestoreService.FirestoreCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        showLoading(false);
                        Snackbar.make(rootView, R.string.registration_successful, Snackbar.LENGTH_SHORT).show();
                        navigateToMain();
                    }

                    @Override
                    public void onError(String error) {
                        showLoading(false);
                        Snackbar.make(rootView, R.string.registration_successful_profile_error, Snackbar.LENGTH_LONG).show();
                        navigateToMain();
                    }
                });
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Snackbar.make(rootView, error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private boolean validateInputs(boolean isLogin) {
        clearErrors();
        boolean isValid = true;

        // Validar email
        String email = emailEditText.getText().toString().trim();
        if (email.isEmpty()) {
            emailInputLayout.setError(getString(R.string.error_email_required));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }

        // Validar contraseña
        String password = passwordEditText.getText().toString().trim();
        if (password.isEmpty()) {
            passwordInputLayout.setError(getString(R.string.error_password_required));
            isValid = false;
        } else if (password.length() < 6) {
            passwordInputLayout.setError(getString(R.string.error_password_too_short));
            isValid = false;
        }

        // Validar nombre de usuario (solo en registro)
        if (!isLogin) {
            String username = usernameEditText.getText().toString().trim();
            if (username.isEmpty()) {
                usernameInputLayout.setError(getString(R.string.error_username_required));
                isValid = false;
            } else if (username.length() < 3) {
                usernameInputLayout.setError(getString(R.string.error_username_too_short));
                isValid = false;
            }
        }

        return isValid;
    }

    private void clearErrors() {
        emailInputLayout.setError(null);
        passwordInputLayout.setError(null);
        usernameInputLayout.setError(null);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        registerButton.setEnabled(!show);
        switchModeButton.setEnabled(!show);
        skipButton.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}