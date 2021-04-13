package com.example.researchproject.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey
    @NonNull
    private String userId;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "ifArtist")
    private Boolean ifArtist;

    public User(String userId, String name, Boolean ifArtist) {
        this.userId = userId;
        this.name = name;
        this.ifArtist = ifArtist;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public Boolean getIfArtist() {
        return ifArtist;
    }
}
