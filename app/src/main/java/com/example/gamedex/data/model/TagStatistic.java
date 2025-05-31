package com.example.gamedex.data.model;

/**
 * Clase auxiliar para las estadísticas de tags
 * Utilizada por Room para mapear resultados de consultas
 */
public class TagStatistic {
    public String tagName;
    public int gameCount;

    public TagStatistic() {
        // Constructor vacío requerido por Room
    }

    public TagStatistic(String tagName, int gameCount) {
        this.tagName = tagName;
        this.gameCount = gameCount;
    }

    // Getters y setters
    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public int getGameCount() {
        return gameCount;
    }

    public void setGameCount(int gameCount) {
        this.gameCount = gameCount;
    }
}