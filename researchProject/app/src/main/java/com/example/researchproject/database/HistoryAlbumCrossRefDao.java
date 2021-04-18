package com.example.researchproject.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface HistoryAlbumCrossRefDao {
    @Query("SELECT * FROM historyAlbumCrossRef WHERE historyId = :historyId AND songUri = :songUri" )
    HistoryAlbumCrossRef findByHistoryIdAndSongUri(int historyId, String songUri);

    @Delete
    void delete(HistoryAlbumCrossRef historyAlbumCrossRef);

    @Insert
    void insert(HistoryAlbumCrossRef historyAlbumCrossRef);
}
