package com.example.ingles_app.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

@Entity(tableName = "resource", indices = {@Index("category_id")})
public class Flashcards {
    @PrimaryKey
    public int id;

    @SerializedName("category_id")
    @ColumnInfo (name = "category_id")
    public int categoryID;

    public String word;
    public String translation;
    public String img;
}