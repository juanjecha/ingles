package com.example.ingles_app;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("enrollment")
    private String enrollment;
    @SerializedName("password")
    private String password;

    public LoginRequest(String enrollment, String password) {
        this.enrollment = enrollment;
        this.password = password;
    }
}
