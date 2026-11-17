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
import com.example.ingles_app.entities.DailyCard;
import com.example.ingles_app.entities.Flashcards;
import com.example.ingles_app.entities.ProgressEntity;
import com.example.ingles_app.entities.ShownCard;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Random;

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

    public static final int DAILY_LIMIT = 5;
    public static final long MIN_INTERVAL_MS = 0;

    //only one thread to everything
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    //private final ExecutorService executorService;
    private final AppDatabase database;
    private final ImageStore images;


    public interface SyncListener {
        void onFinished(boolean success);
    }

    public static class CategoryState {
        public Category category;
        public boolean unlocked;
        public boolean passed;
        public Integer score;
        public int words;
        public boolean testReady;
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
                    pullProgress();
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


    public List<CategoryState> getCategoryStates() {
        VocabularyDAO dao = database.vocabularyDAO();
        Map<Integer, ProgressEntity> progress = new HashMap<>();
        for (ProgressEntity p : dao.getProgress()) {
            progress.put(p.categoryID, p);
        }

        List<CategoryState> states = new ArrayList<>();
        for (Category c : dao.getCategories()) {
            ProgressEntity p = progress.get(c.id);

            CategoryState s = new CategoryState();
            s.category = c;
            s.unlocked = c.assigned;
            s.passed = p != null && p.passed;
            s.score = p != null ? p.score : null;
            s.words = dao.countFLashcards(c.id);
            s.testReady = s.unlocked && !s.passed && s.words > 0 && dao.countUnseen(c.id) == 0;
            states.add(s);
        }
        return states;
    }

    public List<Category> getUnlockedCategories() {
        List<Category> unlocked = new ArrayList<>();

        for (CategoryState s : getCategoryStates()) {
            if (s.unlocked) unlocked.add(s.category);
        }

        return unlocked;
    }

    public void clearLocalData() {
        EXECUTOR.execute(() -> {
            database.clearAllTables();
            images.clear();
        });
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
                } else {
                    Log.e(TAG, "Progress of category " + p.categoryID + " rejected: HTTP " + res.code());

                    if (res.code() == 403 || res.code() == 404) {
                        p.pendingSync = false;
                        dao.saveProgress(p);
                    }
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


    public Category getActiveCategory() {
        VocabularyDAO dao = database.vocabularyDAO();
        Map<Integer, ProgressEntity> progress = new HashMap<>();
        for (ProgressEntity p : dao.getProgress()) {
            progress.put(p.categoryID, p);
        }

        for (Category c : dao.getCategories()) {
            if(!c.assigned) continue;
            ProgressEntity p = progress.get(c.id);
            if (p == null || !p.passed) return c;
        }
        return null;   // todas aprobadas
    }

    //to know the category that has a test to the user (when the user saw the 5 words)
    public Category getTestReadyCategory() {
        Category active = getActiveCategory();
        if (active == null) return null;

        VocabularyDAO dao = database.vocabularyDAO();
        boolean ready = dao.countFLashcards(active.id) > 0 && dao.countUnseen(active.id) == 0;
        return ready ? active : null;
    }

    // It shows the next category with flashcards to be seen
    public Category getNextCategory(int categoryID) {
        boolean found = false;
        for (Category c : database.vocabularyDAO().getCategories()) {
            if (found) return c;
            if (c.id == categoryID) found = true;
        }
        return null;
    }

    // Next card of the day
    public Flashcards pickNextCard() {
        VocabularyDAO dao = database.vocabularyDAO();

        Category active = getActiveCategory();
        if (active == null) {
            Log.d("CARDS", "No active category (nothing synced, or all categories passed)");
            return null;
        }

        String day = today();
        if (dao.countDaily(day, active.id) == 0) {
            buildDailyset(dao, active, day);
        }

        Flashcards card = dao.getNextDaily(day);
        if (card == null) {
            Log.d("CARDS", "No words in '"+ active.name +"'");
        }
        return card;
    }

    private void buildDailyset(VocabularyDAO dao, Category active, String day) {
        List<Flashcards> pool = new ArrayList<>(dao.getFlashcardsByCat(active.id));
        Collections.shuffle(pool);

        int n = Math.min(DAILY_LIMIT, pool.size());
        List<DailyCard> set = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            DailyCard d = new DailyCard();
            d.flashcardID = pool.get(i).id;
            d.day = day;
            d.categoryID = active.id;
            d.pos = i;
            d.timesShown = 0;
            set.add(d);
        }

        dao.clearDaily();
        dao.insertDaily(set);
        Log.d("CARDS", "New daily set for " + day + ": " + n + " cards from '" + active.name + "'");
    }

    // Una palabra cualquiera de la categoría activa, sin contar para el límite (para probar)
    public Flashcards pickPreviewCard() {
        VocabularyDAO dao = database.vocabularyDAO();

        List<Flashcards> pool = new ArrayList<>();
        Category active = getActiveCategory();
        if (active != null) {
            pool = dao.getFlashcardsByCat(active.id);
        }

        if (pool.isEmpty()) {
            for (Category c : dao.getCategories()) {
                pool.addAll(dao.getFlashcardsByCat(c.id));
            }
        }

        if (pool.isEmpty()) return null;
        return pool.get(new Random().nextInt(pool.size()));
    }

    public void markShown(Flashcards card) {
        ShownCard shown = new ShownCard();
        shown.flashcardID = card.id;
        shown.shownDate = today();
        shown.shownAt = System.currentTimeMillis();
        database.vocabularyDAO().markShown(shown);
        database.vocabularyDAO().bumpDaily(card.id);
    }

    private static String today() {
        return LocalDate.now().toString();
    }
}