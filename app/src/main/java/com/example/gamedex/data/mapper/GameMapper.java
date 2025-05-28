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

        // Extraer desarrollador
        game.setDeveloper(response.getDeveloper());

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

        // Capturar video clip si está disponible
        if (response.getVideoClip() != null) {
            String videoUrl = response.getVideoClip().getVideoUrl();
            if (videoUrl == null || videoUrl.isEmpty()) {
                videoUrl = response.getVideoClip().getClipUrl();
            }
            game.setTrailerUrl(videoUrl);
        }

        // Capturar capturas de pantalla
        if (response.getScreenshots() != null && !response.getScreenshots().isEmpty()) {
            JSONArray screenshotsArray = new JSONArray();
            for (GameResponse.Screenshot screenshot : response.getScreenshots()) {
                if (screenshot.getImageUrl() != null) {
                    screenshotsArray.put(screenshot.getImageUrl());
                }
            }
            if (screenshotsArray.length() > 0) {
                game.setScreenshotsUrls(screenshotsArray.toString());
            }
        }

        // Capturar tiendas
        if (response.getStores() != null && !response.getStores().isEmpty()) {
            JSONArray storesArray = new JSONArray();
            for (GameResponse.StoreWrapper storeWrapper : response.getStores()) {
                try {
                    JSONObject storeObject = new JSONObject();
                    storeObject.put("name", storeWrapper.getStore().getName());
                    storeObject.put("url", storeWrapper.getUrl());

                    if (storeWrapper.getStore().getDomain() != null) {
                        storeObject.put("domain", storeWrapper.getStore().getDomain());
                    }

                    storesArray.put(storeObject);
                } catch (JSONException e) {
                    Log.e(TAG, "Error al crear JSON de tienda: " + e.getMessage());
                }
            }
            if (storesArray.length() > 0) {
                game.setStoresInfo(storesArray.toString());
            }
        }

        return game;
    }

    public static Game fromDetailResponseToEntity(GameDetailResponse response) {
        // Para IGDB, tanto GameDetailResponse como GameResponse tienen estructura similar
        // Mapeamos los campos básicos que están disponibles

        if (response == null) {
            return new Game("0", "Juego no encontrado");
        }

        // Crear el juego usando los métodos disponibles en GameDetailResponse
        Game game = new Game(String.valueOf(response.getId()), response.getName());

        // Mapear campos básicos si existen en GameDetailResponse
        if (response.getBackgroundImage() != null) {
            game.setCoverUrl(response.getBackgroundImage());
        }

        if (response.getReleaseDate() != null) {
            game.setReleaseDate(response.getReleaseDate());
        }

        game.setGlobalRating(response.getRating());
        game.setDescription(response.getDescription());

        // Desarrollador y publisher si están disponibles
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

        // Convertir plataformas a JSON String si están disponibles
        if (response.getPlatforms() != null) {
            JSONArray platformsArray = new JSONArray();
            for (GameResponse.PlatformWrapper platform : response.getPlatforms()) {
                if (platform.getPlatform() != null) {
                    platformsArray.put(platform.getPlatform().getName());
                }
            }
            game.setPlatforms(platformsArray.toString());
        }

        // Convertir géneros a JSON String si están disponibles
        if (response.getGenres() != null) {
            JSONArray genresArray = new JSONArray();
            for (GameResponse.Genre genre : response.getGenres()) {
                genresArray.put(genre.getName());
            }
            game.setGenres(genresArray.toString());
        }

        return game;
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

    // Método auxiliar para mapear desde respuesta IGDB (que es un array directo)
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