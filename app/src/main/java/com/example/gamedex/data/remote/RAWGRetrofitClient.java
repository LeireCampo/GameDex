package com.example.gamedex.data.remote;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RAWGRetrofitClient {
    private static final String BASE_URL = RAWGConfig.BASE_URL;

    // API Key se obtiene desde RAWGConfig

    private static Retrofit retrofit = null;


    /**
     * Interceptor para añadir automáticamente la API key a todas las requests
     */
    private static class RAWGApiInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();

            // Añadir API key como parámetro de query
            HttpUrl originalHttpUrl = originalRequest.url();
            HttpUrl url = originalHttpUrl.newBuilder()
                    .addQueryParameter("key", RAWGConfig.getApiKey())
                    .build();

            Request.Builder requestBuilder = originalRequest.newBuilder()
                    .url(url)
                    .header("User-Agent", "GameDex-Android-App");

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            try {
                // Configurar logging para debug
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)  // Aumentar timeout
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .retryOnConnectionFailure(true)  // Reintentar en fallos
                        .addInterceptor(logging)
                        .addInterceptor(new RAWGApiInterceptor());

                // Añadir configuración de DNS alternativa si es necesario
                java.util.List<java.net.InetAddress> customDns = new java.util.ArrayList<>();
                try {
                    customDns.add(java.net.InetAddress.getByName("8.8.8.8"));
                    customDns.add(java.net.InetAddress.getByName("8.8.4.4"));
                } catch (Exception e) {
                    android.util.Log.w("RAWGRetrofitClient", "No se pudieron configurar DNS alternativos");
                }

                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(httpClient.build())
                        .build();

                android.util.Log.d("RAWGRetrofitClient", "Cliente Retrofit creado exitosamente");
            } catch (Exception e) {
                android.util.Log.e("RAWGRetrofitClient", "Error creando cliente Retrofit", e);
                throw new RuntimeException("No se pudo inicializar el cliente RAWG", e);
            }
        }
        return retrofit;
    }
}