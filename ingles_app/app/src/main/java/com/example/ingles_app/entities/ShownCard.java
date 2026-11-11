package com.example.ingles_app.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shown_card")
public class ShownCard {
    @PrimaryKey
    @ColumnInfo(name = "flashcard_id")
    public int flashcardID;

    @ColumnInfo(name = "shown_date")
    public String shownDate;

    @ColumnInfo(name = "shown_at")
    public long shownAt;
}