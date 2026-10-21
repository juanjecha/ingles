package com.example.ingles_app;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/alumnos")
    Call<ApiResponse> registrarAlumnos(@Header("Authorization") String token,
                                       @Body ApiRequest apiRequest);
}