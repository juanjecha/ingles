package com.example.ingles_app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ingles_app.entities.Flashcards;
import com.example.ingles_app.repository.VocabularyRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class HomeActivity extends AppCompatActivity {
    private static final String LOCAL_NETWORK = "android.permisssion.ACCESS_LOCAL_NETWORK";

    private final ExecutorService io = ExecutorService.newSingleThreadExecutor();

    private SessionManager session;
    private VocabularyRepository repository;

    private LinearLayout categoryList;
    private TextView tvEmpty, btnRefresh, tvPermBanner;
    private ProgressBar progressSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            goToWelcome("Your session expired. Please log in again");
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvGreeting = findViewById(R.id.tvGreeting);
        categoryList = findViewById(R.id.categoryList);
        tvEmpty = findViewById(R.id.tvEmpty);
        progressSync = findViewById(R.id.progressSync);
        btnRefresh = findViewById(R.id.btnRefresh);

        tvPermBanner = findViewById(R.id.tvPermBanner);
        tvPermBanner.setOnClickListener(v ->
                startActivity(new Intent(this, PermissionsActivity.class)));

        // It keeps alive the service which shows the flashcards as long as a session is active
        FlashcardOverlayService.start(getApplicationContext());

        tvGreeting.setText("Hello, " + firstName(session.getName()) + "!");
        btnRefresh.setOnClickListener(v -> sync());
        findViewById(R.id.btnLogout).setOnClickListener( v -> {
            session.logout();
            FlashcardOverlayService.stop(getApplicationContext());
            goToWelcome(null);
        });

        if (needsLocalNetworkPermission()) {
            requestPermissions(new String[]{LOCAL_NETWORK}, 100);
        } else {
            sync();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (session != null && session.isLoggedIn() && repository != null) {
            loadCategories();
            tvPermBanner.setVisibility(
                    PermissionsActivity.allRequiredGranted(this) ? View.GONE: View.VISIBLE;
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            sync();
        }
    }

    // Try to update from the data on the server
    //without connection keeps the local information
    private void sync() {
        progressSync.setVisibility(View.VISIBLE);
        btnRefresh.setEnabled(false);

        repository.syncAll(success -> {
            progressSync.setVisibility(View.GONE);
            btnRefresh.setEnabled(true);
            if(!success) {
                Toast.makeText(this, "Offline: showing your saved words", Toast.LENGTH_SHORT).show();
            }
            loadCategories();
        });
    }

    private void loadCategories() {
        io.execute(() -> {
            List<VocabularyRepository.CategoryState> states = repository.getCategoryStates();
            runOnUiThread(() -> render(states));
        });
    }

    private void render(List<VocabularyRepository.CategoryState> states) {
        categoryList.removeAllViews();
        tvEmpty.setVisibility(states.isEmpty() ? View.VISIBLE : View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (VocabularyRepository.CategoryState s : states) {
            View item = inflater.inflate(R.layout.item_category, categoryList, false);

            TextView name = item.findViewById(R.id.tvCategoryName);
            TextView info = item.findViewById(R.id.tvCategoryName);
            TextView status = item.findViewById(R.id.tvCategoryStatus);

            name.setText(capitalize(s.category.name));
            info.setText(s.category.difficulty + " . " + s.words + (s.words == 1 ? " word" : " words"));

            if(!s.unlocked) {
                status.setText("Locked");
                status.setTextColor(ContextCompat.getColor(this, R.color.text_secondary));
                item.setAlpha(0.55f);
            } else if (s.passed) {
                status.setText("Completed");
                status.setTextColor(ContextCompat.getColor(this, R.color.primary_green));
            } else {
                status.setText("Available");
                status.setTextColor(ContextCompat.getColor(this, R.color.accent_orange));
            }

            item.setOnClickListener(v -> openCategory(s));
            categoryList.addView(item);
        }
    }

    private void openCategory(VocabularyRepository.CategoryState s) {
        if (!s.unlocked) {
            Toast.makeText(this, "Pass the previous category's test to unlock this one",
            Toast.LENGTH_SHORT).show();
            return;
        }
        if(s.words == 0) {
            Toast.makeText(this."No words yet.Tap refresh when you're online",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, FlashcardActivity.class);
        intent.putExtra(FlashcardActivity.EXTRA_CATEGORY_ID, s.category.id);
        intent.putExtra(FlashcardActivity.EXTRA_CATEGORY_NAME, s.category.name);
        startActivity(intent);
    }

    private void goToWelcome(String message) {
        if (message != null) {
            Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean needsLocalNetworkPermission() {
        return Build.VERSION.SDK_INT >= 37
                && checkSelfPermission(LOCAL_NETWORK) != PackageManager.PERMISSION_GRANTED;
    }

    private static String firstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) return "student";
        return fullName.trim().split("\\s+")[0];
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return "";
        return text.substring(0,1).toUpperCase() + text.substring(1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdown();
    }
}
