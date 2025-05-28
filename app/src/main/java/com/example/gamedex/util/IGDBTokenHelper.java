package com.example.gamedex.util;

import android.util.Log;
import okhttp3.*;
import java.io.IOException;

/**
 * Clase temporal para obtener el Access Token de IGDB
 * SOLO USAR PARA DESARROLLO - NO INCLUIR EN PRODUCCIÓN
 */
public class IGDBTokenHelper {
    private static final String TAG = "IGDBTokenHelper";
    private static final String TOKEN_URL = "https://id.twitch.tv/oauth2/token";

    // Reemplazar con tus credenciales reales
    private static final String CLIENT_ID = "TU_CLIENT_ID_AQUI";
    private static final String CLIENT_SECRET = "TU_CLIENT_SECRET_AQUI";

    public static void getAccessToken() {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("grant_type", "client_credentials")
                .build();

        Request request = new Request.Builder()
                .url(TOKEN_URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error obteniendo token: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "Respuesta del token: " + responseBody);

                    // La respuesta será algo como:
                    // {"access_token":"abc123xyz789","expires_in":5184000,"token_type":"bearer"}

                } else {
                    Log.e(TAG, "Error en respuesta: " + response.code() + " - " + response.message());
                }
            }
        });
    }

    // Método para llamar desde tu Activity (solo para testing)
    public static void testTokenFromActivity() {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                RequestBody formBody = new FormBody.Builder()
                        .add("client_id", CLIENT_ID)
                        .add("client_secret", CLIENT_SECRET)
                        .add("grant_type", "client_credentials")
                        .build();

                Request request = new Request.Builder()
                        .url(TOKEN_URL)
                        .post(formBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Log.d(TAG, "🎉 TOKEN OBTENIDO: " + responseBody);

                    // Extraer solo el access_token si quieres
                    if (responseBody.contains("access_token")) {
                        String token = responseBody.split("\"access_token\":\"")[1].split("\"")[0];
                        Log.d(TAG, "🔑 ACCESS TOKEN: " + token);
                    }
                } else {
                    Log.e(TAG, "❌ Error: " + response.code() + " - " + response.message());
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ Excepción: " + e.getMessage());
            }
        }).start();
    }
}