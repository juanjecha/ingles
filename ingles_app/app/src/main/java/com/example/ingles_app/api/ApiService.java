package com.example.ingles_app;
import com.example.ingles_app.api.CategoryResponse;
import com.example.ingles_app.api.FlashcardResponse;
import com.example.ingles_app.entities.Category;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {
    @POST("api/register/student")
    Call<ApiResponse>studentRegister(@Body ApiRequest request);

    @POST("api/login/student")
    Call<ApiResponse>studentLogin(@Body LoginRequest request);

    @GET("categories") Call<CategoryResponse> getCategories();
    @GET("flashcards") Call<FlashcardResponse> getFlashcards(@Query("category_id") int categoryID);
}
