
package com.example.gamedex.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * GameListResponse actualizado para IGDB
 * Mantiene la misma estructura para compatibilidad con el código existente
 */
public class GameListResponse {
    // IGDB devuelve directamente un array, no un objeto con 'results'
    // Pero mantenemos la estructura para compatibilidad
    private int count;
    private String next;
    private String previous;
    private List<GameResponse> results;

    // Constructor para adaptar la respuesta de IGDB
    public GameListResponse(List<GameResponse> igdbGames) {
        this.results = igdbGames;
        this.count = igdbGames != null ? igdbGames.size() : 0;
        this.next = null; // IGDB maneja paginación diferente
        this.previous = null;
    }

    // Getters existentes
    public int getCount() { return count; }
    public String getNext() { return next; }
    public String getPrevious() { return previous; }
    public List<GameResponse> getResults() { return results; }
}