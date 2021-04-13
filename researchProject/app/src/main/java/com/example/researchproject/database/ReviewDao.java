package com.example.researchproject.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ReviewDao {
    @Query("SELECT * FROM review WHERE songUri = :songUri")
    List<Review> findBySongUri(String songUri);

    @Insert
    void insert(Review review);
}
