package com.example.ingles_app.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "progress")
public class ProgressEntity {
    @PrimaryKey
    @ColumnInfo(name = "category_id")
    public int categoryID;

    public boolean passed;
    public Integer score;

    // true = progress saved locally without sending it to the server
    @ColumnInfo(name = "pending_sync")
    public boolean pendingSync;
}