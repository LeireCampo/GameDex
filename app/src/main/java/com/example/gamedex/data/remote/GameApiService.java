package com.example.gamedex.data.remote;

import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameListResponse;
import com.example.gamedex.data.remote.model.GenreListResponse;
import com.example.gamedex.data.remote.model.PlatformListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GameApiService {
    @GET("games")
    Call<GameListResponse> getGames(
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("ordering") String ordering
    );

    @GET("games")
    Call<GameListResponse> searchGames(
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("search") String query,
            @Query("search_precise") boolean precise
    );

    @GET("games/{id}")
    Call<GameDetailResponse> getGameDetails(
            @Path("id") String gameId
    );

    @GET("platforms")
    Call<PlatformListResponse> getPlatforms(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    @GET("genres")
    Call<GenreListResponse> getGenres(
            @Query("page") int page,
            @Query("page_size") int pageSize
    );
}