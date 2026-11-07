package com.example.ingles_app.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ProgressApiService {
    @POST("progress/save")
    Call<GeneralResponse> saveProgress(@Body ProgressRequest request);
}