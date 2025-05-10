package com.example.gamedex.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    private static final String BASE_URL = "https://api.rawg.io/api/";
    private static final String API_KEY = "467052ca912e4040bed8cfc0431c02d9"; // Tu clave API real

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
                    .addInterceptor(chain -> {
                        okhttp3.Request original = chain.request();
                        okhttp3.HttpUrl originalHttpUrl = original.url();

                        okhttp3.HttpUrl url = originalHttpUrl.newBuilder()
                                .addQueryParameter("key", API_KEY)
                                .build();

                        okhttp3.Request.Builder requestBuilder = original.newBuilder()
                                .url(url);

                        okhttp3.Request request = requestBuilder.build();
                        return chain.proceed(request);
                    });

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }
}