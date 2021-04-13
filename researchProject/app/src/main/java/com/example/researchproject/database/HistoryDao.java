package com.example.researchproject.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface HistoryDao {

    @Query("SELECT * FROM history WHERE userId = :userId")
    History findByUserId(String userId);

    @Insert
    void insert(History history);
}
