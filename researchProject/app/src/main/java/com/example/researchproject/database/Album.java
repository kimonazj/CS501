package com.example.researchproject.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Album {
    @PrimaryKey
    @NonNull
    private String songUri;

    @ColumnInfo(name = "album_nm")
    private String albumName;

    @ColumnInfo(name = "artist_nm")
    private String artistName;

    public Album(String songUri, String albumName, String artistName) {
        this.songUri = songUri;
        this.albumName = albumName;
        this.artistName = artistName;
    }

    public String getSongUri() {
        return songUri;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() { return artistName; }
}
