package com.example.ingles_app.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "daily_card")
public class DailyCard {
    @PrimaryKey
    @ColumnInfo(name = "flashcard_id")
    public int flashcardID;

    @ColumnInfo(name = "day")
    public String day;

    @ColumnInfo(name = "category_id")
    public int categoryID;

    public int pos;

    @ColumnInfo(name = "times_shown")
    public int timesShown;
}
