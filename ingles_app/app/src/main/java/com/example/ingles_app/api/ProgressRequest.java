package com.example.ingles_app.api;

import com.google.gson.annotations.SerializedName;

public class ProgressRequest {
    @SerializedName("user_id")
    public int userID;

    @SerializedName("passed_test")
    public int passedTest;

    public ProgressRequest(int userID, int categoryID, int passedTest) {
        this.userID = userID;
        this.categoryID = categoryID;
        this.passedTest = passedTest;
    }
}