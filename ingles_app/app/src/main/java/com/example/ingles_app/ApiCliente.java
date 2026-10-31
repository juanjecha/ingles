package com.example.ingles_app;
import com.example.ingles_app.entities.Flashcards;

import java.util.ArrayList;
import java.util.List;

import kotlin.reflect.KFunction;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiCliente {
    private static final String BASE_URL = "http://192.168.1.10/ingles/backend/ingles_back/public/";
    private static Retrofit retrofit=null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);

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
