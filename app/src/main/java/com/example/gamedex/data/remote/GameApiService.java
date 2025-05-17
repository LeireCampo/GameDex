package com.example.gamedex.data.remote;

import com.example.gamedex.data.remote.model.DlcListResponse;
import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameListResponse;
import com.example.gamedex.data.remote.model.GameSeriesResponse;
import com.example.gamedex.data.remote.model.GenreListResponse;
import com.example.gamedex.data.remote.model.PlatformListResponse;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;
import com.example.gamedex.data.remote.model.StoreListResponse;
import com.example.gamedex.data.remote.model.VideoListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GameApiService {
    // Endpoints existentes
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

    // Nuevos endpoints

    // Capturas de pantalla
    @GET("games/{id}/screenshots")
    Call<ScreenshotListResponse> getGameScreenshots(
            @Path("id") String gameId
    );

    // Videos del juego
    @GET("games/{id}/movies")
    Call<VideoListResponse> getGameVideos(
            @Path("id") String gameId
    );

    // DLCs y expansiones
    @GET("games/{id}/additions")
    Call<DlcListResponse> getGameDlcs(
            @Path("id") String gameId
    );

    // Juegos de la misma serie
    @GET("games/{id}/game-series")
    Call<GameSeriesResponse> getGameSeries(
            @Path("id") String gameId
    );

    // Tiendas donde comprar el juego
    @GET("games/{id}/stores")
    Call<StoreListResponse> getGameStores(
            @Path("id") String gameId
    );

    // Juegos recomendados basados en un juego
    @GET("games/{id}/suggested")
    Call<GameListResponse> getSuggestedGames(
            @Path("id") String gameId,
            @Query("page") int page,
            @Query("page_size") int pageSize
    );

    // Búsqueda avanzada con múltiples filtros
    @GET("games")
    Call<GameListResponse> advancedSearch(
            @Query("page") int page,
            @Query("page_size") int pageSize,
            @Query("search") String query,
            @Query("platforms") String platforms,
            @Query("stores") String stores,
            @Query("genres") String genres,
            @Query("tags") String tags,
            @Query("developers") String developers,
            @Query("publishers") String publishers,
            @Query("dates") String dates,
            @Query("ordering") String ordering,
            @Query("metacritic") String metacriticRange
    );
}