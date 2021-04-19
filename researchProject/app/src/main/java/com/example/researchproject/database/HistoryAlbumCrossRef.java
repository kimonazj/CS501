package com.example.researchproject.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

/**
 * Keeps track of albums in history
 */
@Entity(foreignKeys = {
        @ForeignKey(entity = History.class,
        parentColumns = "historyId",
        childColumns = "historyId",
        onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = Album.class,
                parentColumns = "songUri",
                childColumns = "songUri",
                onDelete = ForeignKey.CASCADE
)})
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
