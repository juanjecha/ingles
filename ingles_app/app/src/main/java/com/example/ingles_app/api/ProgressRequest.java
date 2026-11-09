package com.example.ingles_app.api;

import com.google.gson.annotations.SerializedName;

public class ProgressRequest {
    //@SerializedName("user_id")
    //public int userID;

    @SerializedName("category_id")
    public int categoryID;

    @SerializedName("passed")
    public int passed;

    @SerializedName("score")
    public Integer score;

    public ProgressRequest(int userID, boolean passed, Integer score) {
        //this.userID = userID;
        this.categoryID = categoryID;
        this.passed = passed ? 1 : 0;
        this.score = score;
    }
}