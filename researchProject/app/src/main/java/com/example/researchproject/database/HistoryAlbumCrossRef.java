package com.example.researchproject.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(primaryKeys = {"historyId", "songUri"})
public class HistoryAlbumCrossRef {
    @NonNull
    public int historyId;

    @NonNull
    public String songUri;

    public HistoryAlbumCrossRef(int historyId, String songUri) {
        this.historyId = historyId;
        this.songUri = songUri;
    }

}
