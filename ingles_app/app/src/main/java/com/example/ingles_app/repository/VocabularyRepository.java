package com.example.ingles_app.repository;

import android.content.Context;
import android.os.Handler;

import com.example.ingles_app.DB.ProgressItem;
import com.example.ingles_app.DB.VocabularyDAO;
import com.example.ingles_app.api.ApiCliente;
import com.example.ingles_app.DB.AppDatabase;
import com.example.ingles_app.api.CategoryResponse;
import com.example.ingles_app.api.FlashcardResponse;
import com.example.ingles_app.api.GeneralResponse;
import com.example.ingles_app.api.ProgressRequest;
import com.example.ingles_app.api.ProgressResponse;
import com.example.ingles_app.entities.Category;
import com.example.ingles_app.entities.Flashcards;
import com.example.ingles_app.entities.ProgressEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Looper;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class VocabularyRepository {
    private static final String TAG = "SYNC";

    //only one thread to everything
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    //private final ExecutorService executorService;
    private final AppDatabase database;
    private final ImageStore images;


    public interface SyncListener {
        void onFinished(boolean success);
    }

    public VocabularyRepository(Context context) {
        Context app = context.getApplicationContext();
        database = AppDatabase.getINSTANCE(app);
        images = new ImageStore(app);
    }


    //To download categories,progress,flashcards and images. If there's no connection, it keeps what's still en Room
    public void syncAll(SyncListener listener) {
        EXECUTOR.execute(() -> {
            boolean ok = false;
            try {
                ok = syncCategories();
                if (ok) {
                    pushPendingProgress();
                    syncFlashcards();
                }
            } catch (Exception e) {
                Log.e(TAG, "synchronization failed: " + e.getMessage(), e);
            }
            final boolean result = ok;
            if (listener != null) {
                new Handler(Looper.getMainLooper()).post(() -> listener.onFinished(result));
            }
        });
    }

    //Save the result of a test in the local storage to send to the server.
    //If there's no connection it is kept until the next synchronization
    public void saveTestResult(int categoryID, boolean passed, int score) {
        EXECUTOR.execute(() -> {
            VocabularyDAO dao = database.vocabularyDAO();
            ProgressEntity current = dao.getProgressByCat(categoryID);

            ProgressEntity p = new ProgressEntity();
            p.categoryID = categoryID;
            p.passed = passed || (current != null && current.passed);
            p.score = score;
            p.pendingSync = true;
            dao.saveProgress(p);

            pushPendingProgress();
        });
    }

    //Categories available to students
    // Background thread
    public List<Category> getUnlockedCategories() {
        VocabularyDAO dao = database.vocabularyDAO();
        Map<Integer, ProgressEntity> progress = new HashMap<>();
        for (ProgressEntity p : dao.getProgress()) {
            progress.put(p.categoryID, p);
        }

        List<Category> unlocked = new ArrayList<>();
        boolean previousPassed = true;
        for (Category c : dao.getCategories()) {
            if (!previousPassed) break;
            unlocked.add(c);
            ProgressEntity p = progress.get(c.id);
            previousPassed = p != null && p.passed;
        }
        return unlocked;
    }

    public boolean syncCategories() throws IOException {
        Response<CategoryResponse> res = ApiCliente.getApiService().getCategories().execute();
        if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
            Log.e(TAG, "Categorías: HTTP " + res.code());
            return false;
        }

        List<Category> list = res.body().data;
        List<Integer> ids = new ArrayList<>();
        for (Category c : list) {
            ids.add(c.id);
        }

        VocabularyDAO dao = database.vocabularyDAO();
        dao.insertCategories(list);
        dao.deleteCategoriesNotIN(ids);
        dao.deleteOrphanFlashcards();
        Log.d(TAG, "Categories succesfully synchronized: " + list.size());
        return true;
    }

    private void syncFlashcards() {
        VocabularyDAO dao = database.vocabularyDAO();

        for (Category category : getUnlockedCategories()) {
            try {
                Response<FlashcardResponse> res = ApiCliente.getApiService()
                        .getFlashcards(category.id).execute();
                if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                    Log.e(TAG,  category.name + "flashcards" + ": HTTP " + res.code());
                    continue;
                }

                List<Flashcards> cards = res.body().data;
                List<Integer> ids = new ArrayList<>();
                for (Flashcards f : cards) {
                    ids.add(f.id);
                }

                dao.insertFlashcards(cards);
                dao.deleteFlashcardsNotIn(category.id, ids);

                int downloaded = 0;
                for (Flashcards f: cards) {
                    if (images.download(f.img)) downloaded++;
                }
                Log.d(TAG, category.name + "flashcards" + ": " + cards.size() +
                        " (images ready: " + downloaded + ")");
            } catch (IOException e) {
                Log.e(TAG, category.name + "flashcards" + ": " + e.getMessage());
            }
        }
    }

    private void pushPendingProgress() {
        VocabularyDAO dao = database.vocabularyDAO();

        for (ProgressEntity p : dao.getPendingProgress()){
            try {
                Response<GeneralResponse> res = ApiCliente.getApiService()
                        .saveProgress(new ProgressRequest(p.categoryID, p.passed, p.score))
                        .execute();
                if (res.isSuccessful()) {
                    p.pendingSync = false;
                    dao.saveProgress(p);
                    Log.d(TAG, "Progressed on  category number " + p.categoryID + ": HTTP " + res.code());
                }
            } catch (IOException e) {
                Log.e(TAG, "No internet");
            }
        }
    }

    private void pullProgress() {
        try {
            Response<ProgressResponse> res = ApiCliente.getApiService().getProgress().execute();
            if (!res.isSuccessful() || res.body() == null || res.body().data == null) {
                Log.e(TAG, "Progress: HTPP " + res.code());
                return;
            }

            VocabularyDAO dao = database.vocabularyDAO();
            for (ProgressItem item : res.body().data) {
                ProgressEntity local = dao.getProgressByCat(item.categoryID);
                if (local != null && local.pendingSync) continue;

                ProgressEntity p = new ProgressEntity();
                p.categoryID = item.categoryID;
                p.passed = item.passed == 1;
                p.score = item.score;
                p.pendingSync = false;
                dao.saveProgress(p);
            }
            Log.d(TAG, "Progress storaged: " + res.body().data.size());
        } catch (IOException e) {
            Log.e(TAG, "Progress: " + e.getMessage());
        }
    }
}