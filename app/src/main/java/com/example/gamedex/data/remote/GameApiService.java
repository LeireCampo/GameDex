package com.example.gamedex.data.remote;

import com.example.gamedex.data.remote.model.GameResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * GameApiService para IGDB API
 * IGDB devuelve directamente arrays, no objetos con 'results'
 */
public interface GameApiService {

    // Obtener lista de juegos
    @POST("games")
    Call<List<GameResponse>> getGames(@Body String query);

    // Buscar juegos
    @POST("games")
    Call<List<GameResponse>> searchGames(@Body String query);

    // Obtener detalles de un juego específico
    @POST("games")
    Call<List<GameResponse>> getGameDetails(@Body String query);

    // Obtener capturas de pantalla
    @POST("screenshots")
    Call<List<GameResponse.Screenshot>> getGameScreenshots(@Body String query);

    // Obtener plataformas
    @POST("platforms")
    Call<List<Platform>> getPlatforms(@Body String query);

    // Obtener géneros
    @POST("genres")
    Call<List<Genre>> getGenres(@Body String query);

    // Clase para Plataformas
    public static class Platform {
        public int id;
        public String name;
        public String abbreviation;
        public int category;
    }

    // Clase para Géneros
    public static class Genre {
        public int id;
        public String name;
        public String slug;
    }

    // Clase helper para construir queries IGDB
    public static class IGDBQueryBuilder {

        // Query para juegos populares
        public static String getPopularGamesQuery(int limit, int offset) {
            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher," +
                    "screenshots.url;" +
                    "where rating > 70 & platforms != null & cover != null;" +
                    "sort total_rating desc;" +
                    "limit " + limit + ";" +
                    (offset > 0 ? "offset " + offset + ";" : "");
        }

        // Query para juegos recientes
        public static String getRecentGamesQuery(int limit, int offset) {
            long currentTimestamp = System.currentTimeMillis() / 1000;
            long sixMonthsAgo = currentTimestamp - (180 * 24 * 60 * 60); // 6 meses atrás

            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher," +
                    "screenshots.url;" +
                    "where first_release_date > " + sixMonthsAgo + " & platforms != null & cover != null;" +
                    "sort first_release_date desc;" +
                    "limit " + limit + ";" +
                    (offset > 0 ? "offset " + offset + ";" : "");
        }

        // Query para buscar juegos
        public static String getSearchGamesQuery(String searchTerm, int limit, int offset) {
            // Escapar comillas en el término de búsqueda
            String escapedTerm = searchTerm.replace("\"", "\\\"");

            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher;" +
                    "search \"" + escapedTerm + "\";" +
                    "where platforms != null;" +
                    "limit " + limit + ";" +
                    (offset > 0 ? "offset " + offset + ";" : "");
        }

        // Query para detalles de juego
        public static String getGameDetailsQuery(String gameId) {
            return "fields name,summary,storyline,rating,total_rating,rating_count,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "screenshots.url,screenshots.width,screenshots.height," +
                    "videos.video_id,videos.name," +
                    "platforms.name,platforms.abbreviation,platforms.category," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher," +
                    "websites.url,websites.category;" +
                    "where id = " + gameId + ";";
        }

        // Query para capturas de pantalla
        public static String getScreenshotsQuery(String gameId) {
            return "fields url,width,height,image_id;" +
                    "where game = " + gameId + ";" +
                    "limit 20;";
        }

        // Query para próximos lanzamientos
        public static String getUpcomingGamesQuery(int limit, int offset) {
            long currentTimestamp = System.currentTimeMillis() / 1000;
            long futureLimit = currentTimestamp + (365 * 24 * 60 * 60); // 1 año adelante

            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher;" +
                    "where first_release_date > " + currentTimestamp +
                    " & first_release_date < " + futureLimit +
                    " & platforms != null & cover != null;" +
                    "sort first_release_date asc;" +
                    "limit " + limit + ";" +
                    (offset > 0 ? "offset " + offset + ";" : "");
        }

        // Query para plataformas
        public static String getPlatformsQuery(int limit) {
            return "fields name,abbreviation,category;" +
                    "where category = (1,6);" + // Solo consolas y PC
                    "sort name asc;" +
                    "limit " + limit + ";";
        }

        // Query para géneros
        public static String getGenresQuery(int limit) {
            return "fields name,slug;" +
                    "sort name asc;" +
                    "limit " + limit + ";";
        }

        // Query por plataforma específica
        public static String getGamesByPlatformQuery(int platformId, int limit, int offset) {
            return "fields name,summary,rating,total_rating,first_release_date," +
                    "cover.url,cover.width,cover.height," +
                    "platforms.name,platforms.abbreviation," +
                    "genres.name," +
                    "involved_companies.company.name,involved_companies.developer,involved_companies.publisher;" +
                    "where platforms = " + platformId + " & cover != null;" +
                    "sort total_rating desc;" +
                    "limit " + limit + ";" +
                    (offset > 0 ? "offset " + offset + ";" : "");
        }

        // Query con filtros avanzados
        public static String getAdvancedSearchQuery(String searchTerm, Integer platformId,
                                                    Integer genreId, Long dateFrom, Long dateTo,
                                                    int limit, int offset) {
            StringBuilder query = new StringBuilder();

            query.append("fields name,summary,rating,total_rating,first_release_date,")
                    .append("cover.url,cover.width,cover.height,")
                    .append("platforms.name,platforms.abbreviation,")
                    .append("genres.name,")
                    .append("involved_companies.company.name,involved_companies.developer,involved_companies.publisher;");

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String escapedTerm = searchTerm.replace("\"", "\\\"");
                query.append("search \"").append(escapedTerm).append("\";");
            }

            // Construir condiciones WHERE
            StringBuilder whereClause = new StringBuilder("where ");
            boolean hasCondition = false;

            if (platformId != null) {
                whereClause.append("platforms = ").append(platformId);
                hasCondition = true;
            }

            if (genreId != null) {
                if (hasCondition) whereClause.append(" & ");
                whereClause.append("genres = ").append(genreId);
                hasCondition = true;
            }

            if (dateFrom != null || dateTo != null) {
                if (hasCondition) whereClause.append(" & ");

                if (dateFrom != null && dateTo != null) {
                    whereClause.append("first_release_date >= ").append(dateFrom)
                            .append(" & first_release_date <= ").append(dateTo);
                } else if (dateFrom != null) {
                    whereClause.append("first_release_date >= ").append(dateFrom);
                } else {
                    whereClause.append("first_release_date <= ").append(dateTo);
                }
                hasCondition = true;
            }

            // Siempre requerir cover
            if (hasCondition) whereClause.append(" & ");
            whereClause.append("cover != null");

            query.append(whereClause).append(";");
            query.append("sort total_rating desc;");
            query.append("limit ").append(limit).append(";");

            if (offset > 0) {
                query.append("offset ").append(offset).append(";");
            }

            return query.toString();
        }
    }
}