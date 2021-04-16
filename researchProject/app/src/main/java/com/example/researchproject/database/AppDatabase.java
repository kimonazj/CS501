package com.example.researchproject.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Album.class, History.class, HistoryAlbumCrossRef.class, Review.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract HistoryDao historyDao();
    public abstract AlbumDao albumDao();
    public abstract ReviewDao reviewDao();
    public abstract HistoryAlbumCrossRefDao historyAlbumCrossRefDao();
    public abstract HistoryWithAlbumsDao historyWithAlbumsDao();
}
