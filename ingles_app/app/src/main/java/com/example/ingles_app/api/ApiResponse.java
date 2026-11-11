package com.example.ingles_app.api;

import com.google.gson.annotations.SerializedName;

public class ApiResponse {

    @SerializedName("status")
    private int status;
    @SerializedName("message")
    private String message;
    @SerializedName("token")
    private String token;
    @SerializedName("user")
    private User user;

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public String getToken() { return token; }
    public User getUser() { return user; }

    public static class User {
        @SerializedName("id")
        public int id;
        @SerializedName("name")
        public String name;
    }
}