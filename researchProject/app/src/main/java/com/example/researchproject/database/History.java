package com.example.researchproject.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(foreignKeys = @ForeignKey(entity = User.class,
        parentColumns = "userId",
        childColumns = "userId",
        onDelete = ForeignKey.CASCADE))

public class History {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public int historyId;

    @ColumnInfo(name = "userId", index = true)
    public String userId;

    public History(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
