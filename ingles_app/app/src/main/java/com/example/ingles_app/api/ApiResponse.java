package com.example.ingles_app;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {

    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("token")
    private String token;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
}
