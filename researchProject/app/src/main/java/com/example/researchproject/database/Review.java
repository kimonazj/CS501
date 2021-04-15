package com.example.researchproject.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = Album.class,
        parentColumns = "songUri",
        childColumns = "songUri",
        onDelete = ForeignKey.CASCADE))

public class Review {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int reviewId;

    @ColumnInfo(name = "songUri", index = true)
    public String songUri;

    @ColumnInfo(name = "author")
    public String author;

    @ColumnInfo(name = "reviewDetails")
    public String reviewDetails;

    public Review(String songUri, String author, String reviewDetails) {
        this.songUri = songUri;
        this.author = author;
        this.reviewDetails = reviewDetails;
    }

    public int getReviewId() { return reviewId; }

    public String getSongUri() {
        return songUri;
    }

    public String getAuthor() {
        return author;
    }

    public String getReviewDetails() {return reviewDetails;}


}
