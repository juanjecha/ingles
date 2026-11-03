package com.example.ingles_app.repository;

import android.content.Context;

import com.example.ingles_app.api.ApiCliente;
import com.example.ingles_app.DB.AppDatabase;
import com.example.ingles_app.api.CategoryResponse;
import com.example.ingles_app.api.ProgressRequest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class VocabularyRepository {
    private final AppDatabase database;
    private final ExecutorService executorService;

    public VocabularyRepository(Context context) {
        database = AppDatabase.getINSTANCE(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public void synchonyzeCategories() {
        ApiCliente.getApiService().getCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CategoryResponse res = response.body();
                    executorService.execute(
                            () ->
                            {
                                database.vocabularyDAO().insertCategories(res.data);
                                Log.d("SYNC", "Categorias sincronizadas y guardadas localmente.");
                            });
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Log.e("SYNC", "Error al conectar con CI4: " + t.getMessage());
            }
        });
    }

    public void passedTest(int userID, int currentCategoryID, int nextCategoryID) {
        // Update local DB with Room
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getINSTANCE(context);
            //Mark current category as 'done' and unblock the next one
            db.vocabularyDAO().updateProgressCategory(nextCategoryID, true);
            db.vocabularyDAO().unblockCategory(nextCategoryID, false);
        });

        // 2. Send POST to CodeIgniter backend
        ProgressRequest request = new ProgressRequest(userID, currentCategoryID, 1);
        ApiCliente.getProgressService().saveProgress(request).enqueue(new Callback<GeneralResponse>())
        {
            @Override
            public void onResponse(Call<GeneralResponse> call, Response<GeneralReponse> response) {
                if(response.isSuccessful()) {
                    Log.d("SYNC", "Progress locally synchronized in Room and next unlocked category at the server");
                }
            }

            @Override
            public void onFailure(Call<GeneralResponse> call, Throwable t){
                Log.e("SYNC", "No connection. Progress storaged in Room to synchronize then")
            }
        }
    }
}