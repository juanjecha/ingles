package com.example.ingles_app.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "resource")
public class Flashcards {
    @PrimaryKey
    public int id;
    @ColumnInfo (name = "category_id")
    public int categoryID;
    @ColumnInfo (name = "word")
    public String word;
    public String translation;
    @ColumnInfo(name = "img")
    public String imgUrl;
}