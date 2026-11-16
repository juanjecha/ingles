package com.example.ingles_app.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "category")
public class Category {
    @PrimaryKey
    public int id;
    public String name;
    public String difficulty;
    public int ord;
    public boolean assigned;
    //@ColumnInfo(name = "blocked_by_default")
    //public boolean blockedByDefault;
}
