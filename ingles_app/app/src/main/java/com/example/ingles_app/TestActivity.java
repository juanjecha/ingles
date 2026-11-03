package com.example.ingles_app;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ingles_app.DB.AppDatabase;
import com.example.ingles_app.api.ApiCliente;
import com.example.ingles_app.entities.Flashcards;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TestActivity extends AppCompatActivity {
    private TextView txtProgress, txtWordToTest;
    private Button[] optionButtons = new Button[4];

    private List<Flashcards> flashcardsList;
    private List<Flashcards> allFlashcardsInDB; //wrong answers
    private int currentIndex = 0;
    private int score = 0;
    private int categoryID = 1; //Received from Intent
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        categoryID = getIntent().getIntExtra("category_id", 1);
        executorService = Executors.newSingleThreadExecutor();

        txtProgress = findViewById(R.id.txtProgress);
        txtWordToTest = findViewById(R.id.txtWordToTest);
        optionButtons[0] = findViewById(R.id.btnOption1);
        optionButtons[1] = findViewById(R.id.btnOption2);
        optionButtons[2] = findViewById(R.id.btnOption3);
        optionButtons[3] = findViewById(R.id.btnOption4);

        chargeQuestions();
    }

    private void chargeQuestions() {
        executorService.execute(() -> {
            AppDatabase db = AppDatabase.getINSTANCE(getApplicationContext());
            flashcardsList = db.vocabularyDAO().getFlashcardsByCat(categoryID);
            allFlashcardsInDB = db.vocabularyDAO().getFlashcardsByCat(categoryID);

            //To go through the questions
            Collections.shuffle(flashcardsList);

            //Limit the test to 5 questions
            if (flashcardsList.size() > 5) {
                flashcardsList = flashcardsList.subList(0, 5);
            }

            new Handler(Looper.getMainLooper()).post(this::showCurrentQuestion);
        });
    }

    private void showCurrentQuestion() {
        if (currentIndex >= flashcardsList.size()) {
            finishTest();
            return;
        }

        txtProgress.setText("Question " + (currentIndex + 1) + " out of " + flashcardsList.size());
        Flashcards current = flashcardsList.get(currentIndex);
        txtWordToTest.setText(current.word);

        // Generate options (1 right, 3 wrong ones)
        List<String> options = getRandomOptions(current);

        for(int i = 0; i < 4; i++) {
            String optionTxt = options.get(i);
            optionButtons[i].setText(optionTxt);

            //clear last listeners and assign new click
            optionButtons[i].setOnClickListener(v -> checkAnswer(optionTxt, current.translation));
        }
    }

    private List<String> getRandomOptions(Flashcards rightOne) {
        List<String> options = new java.util.ArrayList<>();
        options.add(rightOne.translation);

        List<Flashcards> distractors = new java.util.ArrayList<>(allFlashcardsInDB);
        Collections.shuffle(distractors);

        for (Flashcards f : distractors) {
            if (!f.translation.equals(rightOne.translation) && options.size() < 4) {
                options.add(f.translation);
            }
        }
        Collections.shuffle(options);
        return options;
    }

    private void checkAnswer(String selected, String rightOne) {
        if (selected.equals(rightOne)) {
            score++;
            Toast.makeText(this, "You're right!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "You're wrong, the right one was" + rightOne, Toast.LENGTH_SHORT).show();
        }
        currentIndex++;
        showCurrentQuestion();
    }

    private void finishTest() {
        //Evaluate at least 4/5
        boolean checked = score >= (flashcardsList.size() * 0.8);

        if (checked) {
            Toast.makeText(this, "Congratulations. New category available", Toast.LENGTH_LONG).show();
            ProgressApiService service = ApiCliente.getProgressService();
            Call<GeneralResponse> call = service.saveProgress(service);

            call.enqueue(new Callback<GeneralResponse>() {
                @Override
                public void onResponse(Call<GeneralResponse> call, Response<GeneralResponse> response) {
                    //Manejo de respuesta exitosa
                }

                @Override
                public void onFailure (Call<GeneralResponse> call, Throwable t) {
                    //Manejar error de conexión
                }
            });
        } else {
            Toast.makeText(this, "You didn't reach an enough score. Please,try again", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(executorService != null) {
            executorService.shutdown();
        }
    }
}