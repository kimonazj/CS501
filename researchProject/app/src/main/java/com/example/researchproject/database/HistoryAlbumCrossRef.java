package com.example.researchproject.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HistoryAlbumCrossRef {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int historyAlbumCrossRefId;

    @NonNull
    public int historyId;

    @NonNull
    public String songUri;

    public HistoryAlbumCrossRef(int historyId, String songUri) {
        this.historyId = historyId;
        this.songUri = songUri;
    }

    public int getHistoryAlbumCrossRefId() {
        return historyAlbumCrossRefId;
    }
}
