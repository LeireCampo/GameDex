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

        // Extraer desarrollador - aquí no tenemos información de desarrollador
        game.setDeveloper("Desconocido");

        // Convertir plataformas a JSON String
        JSONArray platformsArray = new JSONArray();
        if (response.getPlatforms() != null) {
            for (GameResponse.PlatformWrapper platform : response.getPlatforms()) {
                platformsArray.put(platform.getPlatform().getName());
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
            // Priorizar el video de alta calidad
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
                if (!screenshot.isDeleted() && screenshot.getImageUrl() != null) {
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

                    // Intentar capturar información adicional de la tienda
                    if (storeWrapper.getStore().getDomain() != null) {
                        storeObject.put("domain", storeWrapper.getStore().getDomain());
                    }

                    if (storeWrapper.getStore().getImageBackground() != null) {
                        storeObject.put("iconUrl", storeWrapper.getStore().getImageBackground());
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
        Game game = new Game(String.valueOf(response.getId()), response.getName());
        game.setDescription(response.getDescription());
        game.setCoverUrl(response.getBackgroundImage());
        game.setReleaseDate(response.getReleaseDate());
        game.setGlobalRating(response.getRating());

        // Extraer desarrollador (usamos el primero si hay varios)
        if (response.getDevelopers() != null && !response.getDevelopers().isEmpty()) {
            game.setDeveloper(response.getDevelopers().get(0).getName());
        } else {
            game.setDeveloper("Desconocido");
        }

        // Extraer editor (usamos el primero si hay varios)
        if (response.getPublishers() != null && !response.getPublishers().isEmpty()) {
            game.setPublisher(response.getPublishers().get(0).getName());
        } else {
            game.setPublisher("Desconocido");
        }

        // Convertir plataformas a JSON String
        JSONArray platformsArray = new JSONArray();
        if (response.getPlatforms() != null) {
            for (GameResponse.PlatformWrapper platform : response.getPlatforms()) {
                platformsArray.put(platform.getPlatform().getName());
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

        // Manejar trailers/movies (priorizar movies sobre clip)
        if (response.getMovies() != null && !response.getMovies().isEmpty()) {
            try {
                GameDetailResponse.Movie movie = response.getMovies().get(0);
                if (movie.getData() != null && movie.getData().getMax() != null) {
                    game.setTrailerUrl(movie.getData().getMax());
                } else if (movie.getPreviewUrl() != null) {
                    game.setTrailerUrl(movie.getPreviewUrl());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al procesar movie: " + e.getMessage());
            }
        } else if (response.getVideoClip() != null) {
            // Si no hay movies, intentar usar el videoclip
            String videoUrl = response.getVideoClip().getVideoUrl();
            if (videoUrl == null || videoUrl.isEmpty()) {
                videoUrl = response.getVideoClip().getClipUrl();
            }
            game.setTrailerUrl(videoUrl);
        }

        // Manejar capturas de pantalla (preferir las de alta calidad)
        if (response.getDetailedScreenshots() != null && !response.getDetailedScreenshots().isEmpty()) {
            JSONArray screenshotsArray = new JSONArray();
            for (GameResponse.Screenshot screenshot : response.getDetailedScreenshots()) {
                if (!screenshot.isDeleted() && screenshot.getImageUrl() != null) {
                    screenshotsArray.put(screenshot.getImageUrl());
                }
            }
            if (screenshotsArray.length() > 0) {
                game.setScreenshotsUrls(screenshotsArray.toString());
            }
        } else if (response.getDetailedScreenshots() == null && response.getVideoClip() != null && response.getVideoClip().getPreviewUrl() != null) {
            // Si no hay screenshots pero hay preview del video, usar eso como screenshot
            JSONArray screenshotsArray = new JSONArray();
            screenshotsArray.put(response.getVideoClip().getPreviewUrl());
            game.setScreenshotsUrls(screenshotsArray.toString());
        }

        // Manejar tiendas con información detallada
        if (response.getStores() != null && !response.getStores().isEmpty()) {
            JSONArray storesArray = new JSONArray();
            for (GameResponse.StoreWrapper storeWrapper : response.getStores()) {
                try {
                    if (storeWrapper.getStore() != null) {
                        JSONObject storeObject = new JSONObject();
                        storeObject.put("name", storeWrapper.getStore().getName());

                        // URL de la tienda
                        String storeUrl = storeWrapper.getUrl();
                        if (storeUrl == null || storeUrl.isEmpty()) {
                            // Construir una URL genérica basada en el dominio si no hay URL específica
                            if (storeWrapper.getStore().getDomain() != null) {
                                storeUrl = "https://" + storeWrapper.getStore().getDomain();
                            }
                        }
                        storeObject.put("url", storeUrl);

                        // Información adicional de la tienda
                        if (storeWrapper.getStore().getDomain() != null) {
                            storeObject.put("domain", storeWrapper.getStore().getDomain());
                        }

                        if (storeWrapper.getStore().getImageBackground() != null) {
                            storeObject.put("iconUrl", storeWrapper.getStore().getImageBackground());
                        }

                        // Añadir solo si tiene URL válida
                        if (storeUrl != null && !storeUrl.isEmpty()) {
                            storesArray.put(storeObject);
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error al crear JSON de tienda detallada: " + e.getMessage());
                }
            }
            if (storesArray.length() > 0) {
                game.setStoresInfo(storesArray.toString());
            }
        }

        // Información adicional desde Reddit si está disponible
        if (response.getRedditUrl() != null && !response.getRedditUrl().isEmpty()) {
            try {
                JSONObject redditInfo = new JSONObject();
                redditInfo.put("url", response.getRedditUrl());
                if (response.getRedditName() != null) {
                    redditInfo.put("name", response.getRedditName());
                }
                if (response.getRedditDescription() != null) {
                    redditInfo.put("description", response.getRedditDescription());
                }
                if (response.getRedditLogo() != null) {
                    redditInfo.put("logo", response.getRedditLogo());
                }
                if (response.getRedditCount() != null) {
                    redditInfo.put("count", response.getRedditCount());
                }

                // Podríamos almacenar esta información en un nuevo campo si se necesita
                // game.setRedditInfo(redditInfo.toString());
            } catch (JSONException e) {
                Log.e(TAG, "Error al crear JSON de Reddit: " + e.getMessage());
            }
        }

        return game;
    }

    public static List<Game> fromResponseListToEntityList(List<GameResponse> responses) {
        List<Game> games = new ArrayList<>();
        for (GameResponse response : responses) {
            games.add(fromResponseToEntity(response));
        }
        return games;
    }
}