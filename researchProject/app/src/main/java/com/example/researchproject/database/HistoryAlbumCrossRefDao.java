package com.example.researchproject.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface HistoryAlbumCrossRefDao {
    @Insert
    void insert(HistoryAlbumCrossRef historyAlbumCrossRef);
}
