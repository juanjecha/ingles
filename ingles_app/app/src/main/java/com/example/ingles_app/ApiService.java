package com.example.ingles_app;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @POST("api/register/student")
    Call<ApiResponse>studentRegister(@Body ApiRequest request);

    @POST("api/login/student")
    Call<ApiResponse>studentLogin(@Body LoginRequest request);
}
