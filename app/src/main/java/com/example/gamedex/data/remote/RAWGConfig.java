package com.example.gamedex.data.remote;

/**
 * Configuración para RAWG API
 *
 * IMPORTANTE:
 * 1. Obtén tu API key gratuita en: https://rawg.io/apidocs
 * 2. Reemplaza "TU_API_KEY_AQUI" con tu clave real
 * 3. La API key es gratuita para hasta 20,000 requests por mes
 */
public class RAWGConfig {

    // ⚠️ REEMPLAZA ESTE VALOR CON TU API KEY REAL
    public static final String API_KEY = "e46e1c67bb2d4a29b302e830880d3baf";

    // URLs base
    public static final String BASE_URL = "https://api.rawg.io/api/";
    public static final String BASE_IMAGE_URL = "https://media.rawg.io/media/";

    // Límites de la API
    public static final int MAX_PAGE_SIZE = 40;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_REQUESTS_PER_SECOND = 5;

    // Configuraciones por defecto
    public static final String DEFAULT_ORDERING = "-rating";
    public static final String RECENT_ORDERING = "-released";
    public static final String UPCOMING_ORDERING = "released";

    // Filtros comunes
    public static final String HIGH_RATED_GAMES = "80,100"; // Metacritic score

    /**
     * Valida si la API key está configurada
     */
    public static boolean isApiKeyConfigured() {
        return API_KEY != null &&
                !API_KEY.isEmpty() &&
                !API_KEY.equals("TU_API_KEY_AQUI");
    }

    /**
     * Obtiene la API key configurada
     */
    public static String getApiKey() {
        if (!isApiKeyConfigured()) {
            throw new IllegalStateException(
                    "RAWG API Key no configurada. " +
                            "Obtén una gratis en https://rawg.io/apidocs y " +
                            "actualiza RAWGConfig.API_KEY"
            );
        }
        return API_KEY;
    }
}