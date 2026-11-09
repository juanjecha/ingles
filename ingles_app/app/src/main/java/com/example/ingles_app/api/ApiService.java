package com.example.ingles_app.api;

import com.example.ingles_app.DB.ApiRequest;
import com.example.ingles_app.DB.LoginRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/register/student")
    Call<ApiResponse>studentRegister(@Body ApiRequest request);

    @POST("api/login/student")
    Call<ApiResponse>studentLogin(@Body LoginRequest request);

    @GET("api/categories")
    Call<CategoryResponse> getCategories();
    @GET("api/flashcards")
    Call<FlashcardResponse> getFlashcards(@Query("category_id") int categoryID);

    @GET("api/progress")
    Call<ProgressResponse> getProgress();

    @POST("api/progress")
    Call<GeneralResponse> saveProgress(@Body ProgressRequest request);
}
