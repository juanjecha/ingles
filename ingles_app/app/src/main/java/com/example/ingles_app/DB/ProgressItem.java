package com.example.ingles_app.DB;

import com.google.gson.annotations.SerializedName;

public class ProgressItem {
    @SerializedName("category_id")
    public int categoryID;
    public int passed;
    public Integer score;
}
