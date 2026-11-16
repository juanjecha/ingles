package com.example.ingles_app;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ingles_app.DB.AppDatabase;
import com.example.ingles_app.DB.VocabularyDAO;
import com.example.ingles_app.api.ApiCliente;
import com.example.ingles_app.entities.Category;
import com.example.ingles_app.entities.Flashcards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.ingles_app.repository.VocabularyRepository;
import com.example.ingles_app.api.GeneralResponse;

public class TestActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_CATEGORY_NAME = "category_name";

    public static final int MAX_QUESTIONS = 5;
    private static final double PASS_RATIO = 0.8;
    private static final long FEEDBACK_MS = 900;

    private static final int NEUTRAL = 0, RIGHT = 1, WRONG = 2;

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private TextView txtProgress, txtWordToTest;
    private Button[] optionButtons = new Button[4];

    private VocabularyRepository repository;
    private List<Flashcards> questions = new ArrayList<>();
    private List<String> translationPool = new ArrayList<>();
    private int categoryID;
    private int currentIndex = 0;
    private int correct = 0;
    private boolean answered = false; //Received from Intent
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        repository = new VocabularyRepository(getApplicationContext());
        categoryID = getIntent().getIntExtra(EXTRA_CATEGORY_ID, -1);

        txtProgress = findViewById(R.id.txtProgress);
        txtWordToTest = findViewById(R.id.txtWordToTest);
        optionButtons[0] = findViewById(R.id.btnOption1);
        optionButtons[1] = findViewById(R.id.btnOption2);
        optionButtons[2] = findViewById(R.id.btnOption3);
        optionButtons[3] = findViewById(R.id.btnOption4);

        TextView tvTitle = findViewById(R.id.tvTestTitle);
        tvTitle.setText("Test * " + capitalize(getIntent().getStringExtra(EXTRA_CATEGORY_NAME)));

        loadQuestions();
    }

    private void loadQuestions() {
        currentIndex = 0;
        correct = 0;

        io.execute(() -> {
            VocabularyDAO dao = AppDatabase.getINSTANCE(getApplicationContext()).vocabularyDAO();

            //without translation, it's not possible to ask
            List<Flashcards> words = new ArrayList<>();
            for (Flashcards f : dao.getFlashcardsByCat(categoryID)) {
                if (hasText(f.translation)) words.add(f);
            }
            Collections.shuffle(words);
            final List<Flashcards> chosen = words.size() >MAX_QUESTIONS ? new ArrayList<>(words.subList(0,MAX_QUESTIONS)) : words;

            //wrong options come from another words
            Set<String> pool= new LinkedHashSet<>();
            for (Flashcards f : dao.getAllFlashcards()) {
                if(hasText(f.translation)) pool.add(f.translation.trim());
            }

            mainHandler.post(() -> {
                if (chosen.isEmpty() || pool.size() < 2) {
                    Toast.makeText(this, "Not enough words to build a test yet",
                            Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                questions = chosen;
                translationPool = new ArrayList<>(pool);
                showQuestion();
            });
        });
    }

    private void showQuestion() {
        if (currentIndex >= questions.size()) {
            finishTest();
            return;
        }
        answered = false;

        Flashcards current = questions.get(currentIndex);
        txtProgress.setText((currentIndex + 1) + "/" + questions.size());
        txtWordToTest.setText(current.word);

        String right = current.translation.trim();
        // Generate options (1 right, 3 wrong ones)
        List<String> options = buildOptions(right);

        for(int i = 0; i < optionButtons.length; i++) {
            Button b = optionButtons[i];
            if (i < options.size()) {
                String option = options.get(i);
                b.setVisibility(View.VISIBLE);
                b.setText(option);
                paint(b, NEUTRAL);
                b.setOnClickListener(v -> answer(b, option, right));
            } else {
                b.setVisibility(View.GONE);
            }
        }
    }

    private List<String> buildOptions(String right) {
        List<String> distractors = new ArrayList<>();
        for (String t: translationPool) {
            if (!t.equalsIgnoreCase(right)) distractors.add(t);
        }
        Collections.shuffle(distractors);

        List<String> options = new ArrayList<>();
        options.add(right);

        for (String d : distractors) {
            if (options.size() >= optionButtons.length) break;
                options.add(d);
        }
        Collections.shuffle(options);
        return options;
    }

    private void answer(Button clicked, String selected, String right) {
        if (answered) return;
        answered = true;

        boolean isRight = selected.equalsIgnoreCase(right);
        if (isRight) correct++;

        for (Button b : optionButtons) {
            if (b.getVisibility() == View.VISIBLE && b.getText().toString().equalsIgnoreCase(right)) {
                paint(b, RIGHT);
            }
        }

        if (!isRight) paint(clicked, WRONG);

        mainHandler.postDelayed(() -> {
                currentIndex++;
                showQuestion();
            }, FEEDBACK_MS);
    }

    private void finishTest() {
        int total = questions.size();
        int needed = (int) Math.ceil(total * PASS_RATIO);
        boolean passed = correct >= needed;
        int percent = Math.round(100f * correct / total);

        // this saves the result in the device storage
        repository.saveTestResult(categoryID, passed, percent);

        io.execute(() -> {
            Category next = passed ? repository.getNextCategory(categoryID) : null;
            mainHandler.post(() -> showResult(passed, total, needed, percent, next));
        });
    }

    private void showResult(boolean passed, int total, int needed, int percent, Category next) {
        if (isFinishing() || isDestroyed()) return;

        StringBuilder message = new StringBuilder(
                "You got " + correct + " of " + total + " (" + percent + "%)");

        AlertDialog.Builder dialog = new AlertDialog.Builder(this).setCancelable(false);

        if (passed) {
            message.append("\n\n Greate Job! Your teacher can now assign you your new category");
            dialog.setTitle("Test passed!").setMessage(message).setPositiveButton(
                    "Back to home", (d,w) -> finish());
        } else {
            message.append("\n\n You need at least " + needed + "to pass.");
            dialog.setTitle("not yet").setMessage(message).setPositiveButton(
                    "Try again", (d,w) ->loadQuestions())
                    .setNegativeButton("Back to home", (d,w) ->finish());
        }
        dialog.show();
    }

    private void paint(Button b, int state) {
        int fill;
        int text;
        if (state == RIGHT) {
            fill = Color.parseColor("#2E7D32");
            text = Color.WHITE;
        } else if (state == WRONG) {
            fill = Color.parseColor("#C62828");
            text = Color.WHITE;
        } else {
            fill = Color.TRANSPARENT;
            text = ContextCompat.getColor(this, R.color.text_title);
        }
        b.setBackgroundTintList(ColorStateList.valueOf(fill));
        b.setTextColor(text);
    }

    private static boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0,1).toUpperCase() + text.substring(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(executorService != null) {
            executorService.shutdown();
        }
    }
}