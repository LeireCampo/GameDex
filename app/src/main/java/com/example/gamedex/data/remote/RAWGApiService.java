package com.example.gamedex.data.remote;

import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameListResponse;
import com.example.gamedex.data.remote.model.GenreListResponse;
import com.example.gamedex.data.remote.model.PlatformListResponse;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;
import com.example.gamedex.data.remote.model.StoreListResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Servicio API para RAWG
 * Documentación: https://api.rawg.io/docs/
 */
public interface RAWGApiService {

    /**
     * Obtener lista de juegos
     * @param page Número de página (empezando desde 1)
     * @param pageSize Número de resultados por página (máximo 40)
     * @param ordering Ordenamiento (-added, -created, -updated, -rating, -metacritic)
     * @param search Término de búsqueda
     * @param platforms IDs de plataformas separados por coma
     * @param genres IDs de géneros separados por coma
     * @param dates Rango de fechas (YYYY-MM-DD,YYYY-MM-DD)
     * @param metacritic Rango de puntuación Metacritic (80,100)
     */
    @GET("games")
    Call<GameListResponse> getGames(
            @Query("page") Integer page,
            @Query("page_size") Integer pageSize,
            @Query("ordering") String ordering,
            @Query("search") String search,
            @Query("platforms") String platforms,
            @Query("genres") String genres,
            @Query("dates") String dates,
            @Query("metacritic") String metacritic
    );

    /**
     * Buscar juegos
     */
    @GET("games")
    Call<GameListResponse> searchGames(
            @Query("search") String search,
            @Query("page") Integer page,
            @Query("page_size") Integer pageSize
    );

    /**
     * Obtener detalles de un juego específico
     */
    @GET("games/{id}")
    Call<GameDetailResponse> getGameDetails(@Path("id") String gameId);

    /**
     * Obtener capturas de pantalla de un juego
     */
    @GET("games/{game_pk}/screenshots")
    Call<ScreenshotListResponse> getGameScreenshots(
            @Path("game_pk") String gamePk,
            @Query("page") Integer page,
            @Query("page_size") Integer pageSize
    );

    /**
     * Obtener tiendas donde se puede comprar un juego
     */
    @GET("games/{game_pk}/stores")
    Call<StoreListResponse> getGameStores(
            @Path("game_pk") String gamePk,
            @Query("page") Integer page,
            @Query("page_size") Integer pageSize
    );

    /**
     * Obtener lista de plataformas
     */
    @GET("platforms")
    Call<PlatformListResponse> getPlatforms(
            @Query("page") Integer page,
            @Query("page_size") Integer pageSize
    );

    /**
     * Obtener lista de géneros
     */
    @GET("genres")
    Call<GenreListResponse> getGenres(
            @Query("page") Integer page,
            @Query("page_size") Integer pageSize
    );

    /**
     * Juegos populares (ordenados por rating)
     */
    @GET("games")
    Call<GameListResponse> getPopularGames(
            @Query("ordering") String ordering, // "-rating"
            @Query("page_size") Integer pageSize,
            @Query("metacritic") String metacritic // "80,100" para filtrar buenos juegos
    );

    /**
     * Juegos recientes (ordenados por fecha de lanzamiento)
     */
    @GET("games")
    Call<GameListResponse> getRecentGames(
            @Query("ordering") String ordering, // "-released"
            @Query("page_size") Integer pageSize,
            @Query("dates") String dates // Últimos 6 meses
    );

    /**
     * Próximos lanzamientos
     */
    @GET("games")
    Call<GameListResponse> getUpcomingGames(
            @Query("ordering") String ordering, // "released"
            @Query("page_size") Integer pageSize,
            @Query("dates") String dates // Próximos meses
    );

    /**
     * Obtener juegos por plataforma específica
     */
    @GET("games")
    Call<GameListResponse> getGamesByPlatform(
            @Query("platforms") String platformIds,
            @Query("ordering") String ordering,
            @Query("page_size") Integer pageSize
    );

    /**
     * Obtener juegos por género específico
     */
    @GET("games")
    Call<GameListResponse> getGamesByGenre(
            @Query("genres") String genreIds,
            @Query("ordering") String ordering,
            @Query("page_size") Integer pageSize
    );

    /**
     * Búsqueda avanzada con múltiples filtros
     */
    @GET("games")
    Call<GameListResponse> searchGamesAdvanced(
            @Query("search") String search,
            @Query("platforms") String platforms,
            @Query("genres") String genres,
            @Query("dates") String dates,
            @Query("metacritic") String metacritic,
            @Query("ordering") String ordering,
            @Query("page") Integer page,
            @Query("page_size") Integer pageSize
    );
}