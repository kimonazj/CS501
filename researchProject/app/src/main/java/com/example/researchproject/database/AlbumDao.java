package com.example.researchproject.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlbumDao {
    @Query("SELECT * FROM album WHERE songUri = :songUri")
    Album findBySongUri(String songUri);

    @Insert
    void insert(Album album);
}
