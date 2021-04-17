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

    @ColumnInfo(name = "song_nm")
    private String songName;

    @ColumnInfo(name = "artist_nm")
    private String artistName;

    public Album(String songUri, String albumName, String songName, String artistName) {
        this.songUri = songUri;
        this.albumName = albumName;
        this.songName = songName;
        this.artistName = artistName;
    }

    public String getSongUri() {
        return songUri;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getSongName() { return songName; }

    public String getArtistName() { return artistName; }
}
