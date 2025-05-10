package com.example.gamedex.data.mapper;

import com.example.gamedex.data.local.entity.Game;
import com.example.gamedex.data.remote.model.GameDetailResponse;
import com.example.gamedex.data.remote.model.GameResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class GameMapper {

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