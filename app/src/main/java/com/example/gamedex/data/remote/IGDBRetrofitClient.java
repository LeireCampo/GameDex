package com.example.gamedex.data.remote;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class IGDBRetrofitClient {
    private static final String BASE_URL = "https://api.igdb.com/v4/";

    // Credenciales IGDB - Necesitas registrarte en https://api.igdb.com/
    private static final String CLIENT_ID = "h3j6x53uw34cezxih565ngti2zwq9y";
    private static final String ACCESS_TOKEN = "EL_ACCESS_TOKEN_QUE_RECIBAS";

    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .addInterceptor(logging)
                    .addInterceptor(new IGDBAuthInterceptor());

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    // Interceptor para añadir headers de autenticación automáticamente
    private static class IGDBAuthInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request originalRequest = chain.request();

            Request.Builder requestBuilder = originalRequest.newBuilder()
                    .header("Client-ID", CLIENT_ID)
                    .header("Authorization", "Bearer " + ACCESS_TOKEN)
                    .header("Accept", "application/json");

            Request request = requestBuilder.build();
            return chain.proceed(request);
        }
    }
}