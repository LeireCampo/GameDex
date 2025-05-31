package com.example.gamedex.data.mapper;

import android.util.Log;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GameMapper {
    private static final String TAG = "GameMapper";

    public static Game fromResponseToEntity(GameResponse response) {
        Game game = new Game(String.valueOf(response.getId()), response.getName());
        game.setCoverUrl(response.getBackgroundImage());
        game.setReleaseDate(response.getReleaseDate());
        game.setGlobalRating(response.getRating());
        game.setRatingsCount(response.getRatingsCount());

        // CORRECCIÓN: Usar el método getDeveloper() que ya existe en GameResponse
        game.setDeveloper(response.getDeveloper());

        // También establecer el publisher si está disponible
        game.setPublisher(response.getPublisher());

        // Convertir plataformas a JSON String
        JSONArray platformsArray = new JSONArray();
        if (response.getPlatforms() != null) {
            for (GameResponse.PlatformWrapper platformWrapper : response.getPlatforms()) {
                if (platformWrapper.getPlatform() != null) {
                    platformsArray.put(platformWrapper.getPlatform().getName());
                }
            }
        }
        game.setPlatforms(platformsArray.toString());

        // Convertir géneros a JSON String
        JSONArray genresArray = new JSONArray();
        if (response.getGenres() != null) {
            for (GameResponse.Genre genre : response.getGenres()) {
                genresArray.put(genre.getName());
            }
        }
        game.setGenres(genresArray.toString());

        // CORRECCIÓN: Capturar video clip con validación mejorada
        if (response.getVideoClip() != null) {
            String videoUrl = null;

            // Intentar obtener la mejor URL disponible
            if (response.getVideoClip().getVideoUrl() != null &&
                    !response.getVideoClip().getVideoUrl().isEmpty() &&
                    !response.getVideoClip().getVideoUrl().equals("null")) {
                videoUrl = response.getVideoClip().getVideoUrl();
            } else if (response.getVideoClip().getClipUrl() != null &&
                    !response.getVideoClip().getClipUrl().isEmpty() &&
                    !response.getVideoClip().getClipUrl().equals("null")) {
                videoUrl = response.getVideoClip().getClipUrl();
            }

            // Solo establecer si hay una URL válida
            if (videoUrl != null) {
                game.setTrailerUrl(videoUrl);
                Log.d(TAG, "Trailer URL encontrada para " + response.getName() + ": " + videoUrl);
            }
        }

        // Capturar capturas de pantalla
        if (response.getScreenshots() != null && !response.getScreenshots().isEmpty()) {
            JSONArray screenshotsArray = new JSONArray();
            for (GameResponse.Screenshot screenshot : response.getScreenshots()) {
                if (screenshot.getImageUrl() != null && !screenshot.getImageUrl().isEmpty()) {
                    screenshotsArray.put(screenshot.getImageUrl());
                }
            }
            if (screenshotsArray.length() > 0) {
                game.setScreenshotsUrls(screenshotsArray.toString());
            }
        }

        // CORRECCIÓN: Capturar tiendas con validación mejorada
        if (response.getStores() != null && !response.getStores().isEmpty()) {
            JSONArray storesArray = new JSONArray();
            for (GameResponse.StoreWrapper storeWrapper : response.getStores()) {
                try {
                    if (storeWrapper.getStore() != null) {
                        JSONObject storeObject = new JSONObject();
                        String storeName = storeWrapper.getStore().getName();
                        String storeUrl = storeWrapper.getUrl();

                        // Si no hay URL específica, generar una genérica
                        if (storeUrl == null || storeUrl.isEmpty()) {
                            storeUrl = generateGenericStoreUrl(storeName);
                        }

                        // Solo añadir si tenemos nombre y URL válidos
                        if (storeName != null && !storeName.isEmpty() &&
                                storeUrl != null && !storeUrl.isEmpty()) {

                            storeObject.put("name", storeName);
                            storeObject.put("url", storeUrl);

                            if (storeWrapper.getStore().getDomain() != null) {
                                storeObject.put("domain", storeWrapper.getStore().getDomain());
                            }

                            storesArray.put(storeObject);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error al crear JSON de tienda: " + e.getMessage());
                }
            }
            if (storesArray.length() > 0) {
                game.setStoresInfo(storesArray.toString());
                Log.d(TAG, "Tiendas encontradas para " + response.getName() + ": " + storesArray.length());
            }
        }

        return game;
    }

    public static Game fromDetailResponseToEntity(GameDetailResponse response) {
        if (response == null) {
            return new Game("0", "Juego no encontrado");
        }

        Game game = new Game(String.valueOf(response.getId()), response.getName());

        if (response.getBackgroundImage() != null) {
            game.setCoverUrl(response.getBackgroundImage());
        }

        if (response.getReleaseDate() != null) {
            game.setReleaseDate(response.getReleaseDate());
        }

        game.setGlobalRating(response.getRating());
        game.setDescription(response.getDescription());

        // Desarrollador y publisher
        if (response.getDevelopers() != null && !response.getDevelopers().isEmpty()) {
            game.setDeveloper(response.getDevelopers().get(0).getName());
        } else {
            game.setDeveloper("Desconocido");
        }

        if (response.getPublishers() != null && !response.getPublishers().isEmpty()) {
            game.setPublisher(response.getPublishers().get(0).getName());
        } else {
            game.setPublisher("Desconocido");
        }

        // Convertir plataformas a JSON String
        if (response.getPlatforms() != null) {
            JSONArray platformsArray = new JSONArray();
            for (GameResponse.PlatformWrapper platform : response.getPlatforms()) {
                if (platform.getPlatform() != null) {
                    platformsArray.put(platform.getPlatform().getName());
                }
            }
            game.setPlatforms(platformsArray.toString());
        }

        // Convertir géneros a JSON String
        if (response.getGenres() != null) {
            JSONArray genresArray = new JSONArray();
            for (GameResponse.Genre genre : response.getGenres()) {
                genresArray.put(genre.getName());
            }
            game.setGenres(genresArray.toString());
        }

        // CORRECCIÓN: Capturar trailer del GameDetailResponse si está disponible
        if (response.getVideoClip() != null) {
            String videoUrl = null;

            if (response.getVideoClip().getVideoUrl() != null &&
                    !response.getVideoClip().getVideoUrl().isEmpty() &&
                    !response.getVideoClip().getVideoUrl().equals("null")) {
                videoUrl = response.getVideoClip().getVideoUrl();
            }

            if (videoUrl != null) {
                game.setTrailerUrl(videoUrl);
            }
        }

        // CORRECCIÓN: Capturar tiendas del GameDetailResponse
        if (response.getStores() != null && !response.getStores().isEmpty()) {
            JSONArray storesArray = new JSONArray();
            for (GameResponse.StoreWrapper storeWrapper : response.getStores()) {
                try {
                    if (storeWrapper.getStore() != null) {
                        JSONObject storeObject = new JSONObject();
                        String storeName = storeWrapper.getStore().getName();
                        String storeUrl = storeWrapper.getUrl();

                        if (storeUrl == null || storeUrl.isEmpty()) {
                            storeUrl = generateGenericStoreUrl(storeName);
                        }

                        if (storeName != null && !storeName.isEmpty() &&
                                storeUrl != null && !storeUrl.isEmpty()) {

                            storeObject.put("name", storeName);
                            storeObject.put("url", storeUrl);

                            if (storeWrapper.getStore().getDomain() != null) {
                                storeObject.put("domain", storeWrapper.getStore().getDomain());
                            }

                            storesArray.put(storeObject);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error al crear JSON de tienda en detalles: " + e.getMessage());
                }
            }
            if (storesArray.length() > 0) {
                game.setStoresInfo(storesArray.toString());
            }
        }

        return game;
    }

    /**
     * Genera URLs genéricas para tiendas conocidas
     */
    private static String generateGenericStoreUrl(String storeName) {
        if (storeName == null) return null;

        String lowerStoreName = storeName.toLowerCase();

        if (lowerStoreName.contains("steam")) {
            return "https://store.steampowered.com/";
        } else if (lowerStoreName.contains("playstation") || lowerStoreName.contains("ps store")) {
            return "https://store.playstation.com/";
        } else if (lowerStoreName.contains("xbox") || lowerStoreName.contains("microsoft")) {
            return "https://www.microsoft.com/store/games/xbox/";
        } else if (lowerStoreName.contains("nintendo") || lowerStoreName.contains("eshop")) {
            return "https://www.nintendo.com/us/store/";
        } else if (lowerStoreName.contains("epic") || lowerStoreName.contains("epic games")) {
            return "https://store.epicgames.com/";
        } else if (lowerStoreName.contains("gog")) {
            return "https://www.gog.com/";
        } else if (lowerStoreName.contains("origin")) {
            return "https://www.origin.com/";
        } else if (lowerStoreName.contains("ubisoft")) {
            return "https://store.ubisoft.com/";
        } else if (lowerStoreName.contains("battle") || lowerStoreName.contains("blizzard")) {
            return "https://shop.battle.net/";
        } else if (lowerStoreName.contains("amazon")) {
            return "https://www.amazon.com/videogames/";
        } else if (lowerStoreName.contains("google") || lowerStoreName.contains("play")) {
            return "https://play.google.com/store/games/";
        } else if (lowerStoreName.contains("app store") || lowerStoreName.contains("apple")) {
            return "https://apps.apple.com/us/genre/ios-games/id6014";
        }

        // Para tiendas desconocidas, devolver una búsqueda genérica
        return "https://www.google.com/search?q=" + storeName.replace(" ", "+") + "+game+store";
    }

    public static List<Game> fromResponseListToEntityList(List<GameResponse> responses) {
        List<Game> games = new ArrayList<>();
        if (responses != null) {
            for (GameResponse response : responses) {
                try {
                    Game game = fromResponseToEntity(response);
                    games.add(game);
                } catch (Exception e) {
                    Log.e(TAG, "Error mapeando juego: " + e.getMessage());
                    // Continúa con el siguiente juego en lugar de fallar completamente
                }
            }
        }
        return games;
    }

    // Método auxiliar para mapear desde respuesta IGDB
    public static List<Game> fromIGDBResponseToEntityList(Object igdbResponse) {
        List<Game> games = new ArrayList<>();

        try {
            if (igdbResponse instanceof List) {
                List<?> responseList = (List<?>) igdbResponse;
                for (Object item : responseList) {
                    if (item instanceof GameResponse) {
                        Game game = fromResponseToEntity((GameResponse) item);
                        games.add(game);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error mapeando respuesta IGDB: " + e.getMessage());
        }

        return games;
    }
}