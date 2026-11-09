package com.example.ingles_app.api;

import com.google.gson.annotations.SerializedName;

public class GeneralResponse {
    public int status;
    public String message;

    @SerializedName("next_category_id")
    public Integer nextCategoryId;
}