package com.example.gamedex.data.remote;

import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameListResponse;
import com.example.gamedex.data.remote.model.ScreenshotListResponse;
import com.example.gamedex.data.remote.model.StoreListResponse;
import com.example.gamedex.data.remote.model.VideoListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * GameApiService actualizado para IGDB API
 * Mantiene la misma interfaz que antes pero usa IGDB en lugar de RAWG
 */
public interface GameApiService {

    // Obtener lista de juegos (equivalente al anterior getGames)
    @POST("games")
    Call<GameListResponse> getGames(
            @Body String query // IGDB usa queries en el body, no parámetros URL
    );

    // Buscar juegos (equivalente al anterior searchGames)
    @POST("games")
    Call<GameListResponse> searchGames(
            @Body String query
    );

    // Obtener detalles de un juego específico
    @POST("games")
    Call<GameDetailResponse> getGameDetails(
            @Body String query
    );

    // Obtener capturas de pantalla de un juego
    @POST("screenshots")
    Call<ScreenshotListResponse> getGameScreenshots(
            @Body String query
    );

    // Obtener videos/trailers de un juego
    @POST("games")
    Call<VideoListResponse> getGameVideos(
            @Body String query
    );

    // Obtener tiendas donde comprar el juego
    @POST("games")
    Call<StoreListResponse> getGameStores(
            @Body String query
    );

    // Clase helper para construir queries IGDB
    public static class IGDBQueryBuilder {

        // Query para juegos populares (ordenados por rating)
        public static String getPopularGamesQuery(int limit, int offset) {
            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher," +
                    "screenshots.url;" +
                    "where rating > 80 & platforms != null;" +
                    "sort total_rating desc;" +
                    "limit " + limit + ";" +
                    "offset " + offset + ";";
        }

        // Query para juegos recientes (ordenados por fecha de lanzamiento)
        public static String getRecentGamesQuery(int limit, int offset) {
            long currentTimestamp = System.currentTimeMillis() / 1000;
            long oneYearAgo = currentTimestamp - (365 * 24 * 60 * 60); // Un año atrás

            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher," +
                    "screenshots.url;" +
                    "where first_release_date > " + oneYearAgo + " & platforms != null;" +
                    "sort first_release_date desc;" +
                    "limit " + limit + ";" +
                    "offset " + offset + ";";
        }

        // Query para buscar juegos por nombre
        public static String getSearchGamesQuery(String searchTerm, int limit, int offset) {
            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher," +
                    "screenshots.url;" +
                    "search \"" + searchTerm + "\";" +
                    "where platforms != null;" +
                    "limit " + limit + ";" +
                    "offset " + offset + ";";
        }

        // Query para obtener detalles completos de un juego
        public static String getGameDetailsQuery(String gameId) {
            return "fields name,summary,storyline,rating,total_rating,rating_count,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "screenshots.url,screenshots.width,screenshots.height," +
                    "videos.video_id,videos.name," +
                    "platforms.name,platforms.abbreviation,platforms.category," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher," +
                    "release_dates.date,release_dates.platform.name,release_dates.human," +
                    "websites.url,websites.category;" +
                    "where id = " + gameId + ";";
        }

        // Query para obtener capturas de pantalla
        public static String getScreenshotsQuery(String gameId) {
            return "fields url,width,height;" +
                    "where game = " + gameId + ";" +
                    "limit 20;";
        }

        // Query para obtener videos/trailers
        public static String getGameVideosQuery(String gameId) {
            return "fields name,video_id,videos.video_id,videos.name;" +
                    "where id = " + gameId + ";";
        }

        // Query para obtener información de tiendas (websites)
        public static String getGameStoresQuery(String gameId) {
            return "fields websites.url,websites.category;" +
                    "where id = " + gameId + ";";
        }

        // Query para juegos por plataforma específica
        public static String getGamesByPlatformQuery(int platformId, int limit, int offset) {
            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher;" +
                    "where platforms = " + platformId + ";" +
                    "sort total_rating desc;" +
                    "limit " + limit + ";" +
                    "offset " + offset + ";";
        }

        // Query para próximos lanzamientos
        public static String getUpcomingGamesQuery(int limit, int offset) {
            long currentTimestamp = System.currentTimeMillis() / 1000;

            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher;" +
                    "where first_release_date > " + currentTimestamp + " & platforms != null;" +
                    "sort first_release_date asc;" +
                    "limit " + limit + ";" +
                    "offset " + offset + ";";
        }
    }
}