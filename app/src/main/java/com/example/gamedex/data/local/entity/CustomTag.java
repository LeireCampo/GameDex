package com.example.gamedex.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "custom_tags",
        indices = {
                @Index(value = "name"),
                @Index(value = "userId"),
                @Index(value = "createdAt")
        }
)
public class CustomTag {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String color;
    private String userId; // Para asociar con el usuario de Firebase
    private boolean isDefault; // Para etiquetas predefinidas
    private long createdAt;
    private int usageCount; // Cuántos juegos tienen esta etiqueta

    public CustomTag(String name, String color) {
        this.name = name;
        this.color = color;
        this.createdAt = System.currentTimeMillis();
        this.usageCount = 0;
        this.isDefault = false;
    }

    // Constructor para etiquetas predefinidas
    public CustomTag(String name, String color, boolean isDefault) {
        this.name = name;
        this.color = color;
        this.isDefault = isDefault;
        this.createdAt = System.currentTimeMillis();
        this.usageCount = 0;
    }

    // Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public int getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    // Métodos de utilidad
    public void incrementUsage() {
        this.usageCount++;
    }

    public void decrementUsage() {
        if (this.usageCount > 0) {
            this.usageCount--;
        }
    }
}