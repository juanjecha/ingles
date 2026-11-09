package com.example.ingles_app.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.ingles_app.App;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiCliente {
    private static final String BASE_URL = "http://192.168.1.10/ingles/backend/ingles_back/public/";
    private static Retrofit retrofit=null;
    private static OkHttpClient client = null;

    public static OkHttpClient getClient() {
        if (client == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            client = new OkHttpClient.Builder()
                    .addInterceptor(chain -> {
                        okhttp3.Request.Builder builder = chain.request().newBuilder();
                        SharedPreferences prefs = App.get()
                                .getSharedPreferences("preferencias", Context.MODE_PRIVATE);
                        String token = prefs.getString("JWT_TOKEN", null);
                        if (token != null) {
                            builder.header("Authorization", "Bearer " + token);
                        }
                        return chain.proceed(builder.build());
                    })
                    .addInterceptor(logging)
                    .build();
        }
        return client;
    }

    private static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    // Vocabulary
    public static ApiService getApiService() {
        return getRetrofitInstance().create(ApiService.class);
    }

    public static String imageUrl(String img) {
        return BASE_URL + "uploads/" + img;
    }

    // Progress
    public static ProgressApiService getProgressService() {
        return getRetrofitInstance().create(ProgressApiService.class);
    }

    //get flashcards list
    /*
    public static void new_card() {
        List<Flashcards>cards = new ArrayList<>();

        ApiCliente.getApiService().getFlashcards().enqueue(new Callback<List<FlashCards>>());
        @Override
        public void onResponse(Call<List<Flashcards>>call, Response(<List<Flashcards>>response)){
            if(response.isSuccesFull()) {
                cards = response.body();
            }
        }
    }

     */
}
