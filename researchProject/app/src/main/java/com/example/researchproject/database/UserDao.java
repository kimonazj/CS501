package com.example.researchproject.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user WHERE userId = :userId")
    User findByUserId(String userId);

    @Insert
    void insert(User user);
}
