package com.example.ingles_app;

import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.ingles_app.DB.AppDatabase;
import com.example.ingles_app.entities.Flashcards;
import com.example.ingles_app.repository.ImageStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashcardActivity extends AppCompatActivity {
    public static final String EXTRA_CATEGORY_ID = "category_id";
    public static final String EXTRA_CATEGORY_NAME = "category_name";

    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private List<Flashcards> cards = new ArrayList<>();
    private int index = 0;
    private boolean showingTranslation = false;
    private ImageStore images;

    private ImageView ivCard;
    private TextView tvCounter, tvWord, tvTranslation, tvHint;
    private Button btnPrev, btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        images = new ImageStore(getApplicationContext());

        ivCard = findViewById(R.id.ivCard);
        tvCounter = findViewById(R.id.tvCounter);
        tvWord = findViewById(R.id.tvWord);
        tvTranslation = findViewById(R.id.tvTranslation);
        tvHint = findViewById(R.id.tvHint);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);

        TextView tvTitle = findViewById(R.id.tvTitle);
        String name = getIntent().getStringExtra(EXTRA_CATEGORY_NAME);
        tvTitle.setText(name == null || name.isEmpty()
                ? "Review" : name.substring(0, 1).toUpperCase() + name.substring(1));

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.cardFlash).setOnClickListener(v -> toggleTranslation());

        btnPrev.setOnClickListener(v -> {
            if (index > 0) {
                index--;
                show();
            }
        });
        btnNext.setOnClickListener(v -> {
            if (index < cards.size() -1) {
                index++;
                show();
            } else {
                finish();
            }
        });

        int categoryID = getIntent().getIntExtra(EXTRA_CATEGORY_ID, -1);
        io.execute(() -> {
            List<Flashcards> list = AppDatabase.getINSTANCE(getApplicationContext())
                    .vocabularyDAO().getFlashcardsByCat(categoryID);
            runOnUiThread(() -> {
                cards = list;
                index = 0;
                show();
            });
        });
    }

    public void show() {
        if (cards.isEmpty()) {
            tvWord.setText("No words yet");
            tvTranslation.setVisibility(View.INVISIBLE);
            tvHint.setVisibility(View.INVISIBLE);
            tvCounter.setText("");
            btnPrev.setEnabled(false);
            btnNext.setEnabled(false);
            return;
        }

        Flashcards card = cards.get(index);
        tvCounter.setText((index + 1) + " / " + cards.size());
        tvWord.setText(card.word);
        tvTranslation.setText(card.translation);

        showingTranslation = false;
        tvTranslation.setVisibility(View.INVISIBLE);
        tvHint.setVisibility(View.VISIBLE);

        File file = images.fileFor(card.img);
        if ( file.exists()) {
            Glide.with(this).load(file).into(ivCard);
        } else {
            ivCard.setImageResource(R.mipmap.ic_launcher);
        }

        btnPrev.setEnabled(index > 0);
        btnNext.setText(index == cards.size() -1 ? "Finish" : "Next");
    }

    private void toggleTranslation() {
        if (cards.isEmpty()) return;
        showingTranslation = !showingTranslation;
        tvTranslation.setVisibility(showingTranslation ? View.VISIBLE : View.INVISIBLE);
        tvHint.setVisibility(showingTranslation ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }
}