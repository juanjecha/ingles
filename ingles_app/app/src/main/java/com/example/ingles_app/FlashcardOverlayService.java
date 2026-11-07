package com.example.ingles_app;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.ingles_app.DB.AppDatabase;
import com.example.ingles_app.entities.Flashcards;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashcardOverlayService extends Service {
    private WindowManager windowManager;
    private View floatingView;
    private ExecutorService executorService;

    @Override
    public void onCreate() {
        super.onCreate();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        //Request Room in background to get the flashcards that users have left during the day
        AppDatabase db = AppDatabase.getINSTANCE(getApplicationContext());
        List<Flashcards> flashcards = db.vocabularyDAO().getFlashcardsByCat(1);

        if (flashcards != null && !flashcards.isEmpty()) {
            //Select a card (you can aplicate you day-to-day logic here)
            Flashcards card = flashcards.get(0);

            //Get back to the main thread to draw the window
            new Handler(Looper.getMainLooper()).post(() -> {
                showFloatingCard(card.word, card.imgUrl);
            });
        }

        return START_NOT_STICKY;
    }

    //@Override
    private void showFloatingCard(String word, String imageUrl) {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        //To go up the card design
        LayoutInflater inflater = LayoutInflater.from(this);
        floatingView = inflater.inflate(R.layout.flashcard_overlay, null);

        TextView txtWord = floatingView.findViewById(R.id.txtWord);
        ImageView imgFlashcard = floatingView.findViewById(R.id.imgFlashcard);

        txtWord.setText(word);
        Glide.with(this).load(imageUrl).into(imgFlashcard);

        //Set up parameters of the window on window
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
        );

        //Locate cards at the top of the screen
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 150; //Top margin

        try {
            windowManager.addView(floatingView, params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Handler to leave to show the card after 4s
        new Handler(Looper.getMainLooper()).postDelayed(this::removeOverlay, 4000);
    }

    private void removeOverlay() {
        if (floatingView != null && windowManager != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            floatingView = null;
        }
        stopSelf(); //Stop service at finishing

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeOverlay();
        if(executorService != null) {
            executorService.shutdown();
        }
    }
}
