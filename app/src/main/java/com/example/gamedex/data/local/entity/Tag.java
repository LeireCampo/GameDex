package com.example.gamedex.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "tags",
        indices = {
                @Index(value = "name"),
                @Index(value = "isSystemTag"),
                @Index(value = "createdAt")
        }
)
public class Tag {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String color;
    private boolean isSystemTag;
    private long createdAt;

    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
        this.createdAt = System.currentTimeMillis();
    }

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

    public boolean isSystemTag() {
        return isSystemTag;
    }

    public void setSystemTag(boolean systemTag) {
        isSystemTag = systemTag;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}