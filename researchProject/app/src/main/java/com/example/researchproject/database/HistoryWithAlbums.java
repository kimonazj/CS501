package com.example.researchproject.database;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.ArrayList;
import java.util.List;

public class HistoryWithAlbums {
    @Embedded public History history;
    @Relation(
            parentColumn = "historyId",
            entityColumn = "songUri",
            associateBy = @Junction(HistoryAlbumCrossRef.class)
    )
    public List<Album> albums;
}
