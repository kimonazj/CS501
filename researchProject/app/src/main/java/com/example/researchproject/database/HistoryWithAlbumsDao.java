package com.example.researchproject.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface HistoryWithAlbumsDao {
    @Transaction
    @Query("SELECT * FROM history WHERE userId = :userId")
    HistoryWithAlbums getHistoryWithAlbums(String userId);

    @Insert
    void insert(HistoryAlbumCrossRef albums);
}
