package com.example.ingles_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.ingles_app.entities.Flashcards;
import com.example.ingles_app.repository.ImageStore;
import com.example.ingles_app.repository.VocabularyRepository;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FlashcardOverlayService extends Service {
    private static final String TAG = "CARDS";

    public static final String ACTION_PREVIEW = "com.example.ingles_app.PREVIEW_CARD";
    private static final String EXTRA_DELAY = "delay_ms";

    private static final String CHANNEL_ID = "flashcards_service";
    private static final int NOTIFICATION_ID = 1001;
    private static final long CARD_DURATION_MS = 4000;

    // it switches depending on the state of the screen
    //turned on -> true , turned off -> false
    private static final boolean TRIGGER_ON_UNLOCK = true;

    private static final String PREFS = "FlashcardServicePrefs";
    private static final String KEY_ENABLED = "enabled";

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private WindowManager windowManager;
    private View floatingView;
    private VocabularyRepository repository;
    private ImageStore images;

    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Event received: " + intent.getAction());
            showNextCard(false);
        }
    };


    public static void start(Context context) {
        setEnabled(context, true);
        ContextCompat.startForegroundService(context,
                new Intent(context, FlashcardOverlayService.class));
    }

    public static void stop(Context context) {
        setEnabled(context, false);
        context.stopService(new Intent(context, FlashcardOverlayService.class));
    }

    // flashcard to testing
    public static void preview(Context context, long delayMs) {
        Intent intent = new Intent(context, FlashcardOverlayService.class)
                .setAction(ACTION_PREVIEW)
                .putExtra(EXTRA_DELAY, delayMs);
        ContextCompat.startForegroundService(context, intent);
    }

    public static boolean isEnabled(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, false);
    }

    private static void setEnabled(Context context, boolean enabled) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();
    }

    // ---------- Life cycle ----------

    @Override
    public void onCreate() {
        super.onCreate();
        repository = new VocabularyRepository(getApplicationContext());
        images = new ImageStore(getApplicationContext());
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Este evento solo se puede recibir registrándolo en código, no en el manifest
        IntentFilter filter = new IntentFilter(
                TRIGGER_ON_UNLOCK ? Intent.ACTION_USER_PRESENT : Intent.ACTION_SCREEN_ON);
        ContextCompat.registerReceiver(this, screenReceiver, filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);

        Log.d(TAG, "Service created, listening for "
                + (TRIGGER_ON_UNLOCK ? "USER_PRESENT" : "SCREEN_ON"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification(),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);

        if (intent != null && ACTION_PREVIEW.equals(intent.getAction())) {
            long delay = intent.getLongExtra(EXTRA_DELAY, 0);
            mainHandler.postDelayed(() -> showNextCard(true), delay);
        }
        return START_STICKY;   // si el sistema lo detiene, lo vuelve a levantar
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(screenReceiver);
        } catch (IllegalArgumentException ignored) {
        }
        removeCard();
        io.shutdown();
    }

    // ---------- Flashcard ----------

    private void showNextCard(boolean preview) {
        if (floatingView != null) {
            Log.d(TAG, "A card is already on screen");
            return;
        }

        if (!Settings.canDrawOverlays(this)) {
            Log.w(TAG, "Missing 'Show over other apps' permission");
            return;
        }

        io.execute(() -> {
            Flashcards card = preview ? repository.pickPreviewCard() : repository.pickNextCard();
            if (card == null) {
                Log.d(TAG, "No card to show right now");
                return;
            }

            mainHandler.post(() -> {
                // Solo cuenta como "mostrada" si de verdad se dibujó
                if (drawCard(card) && !preview) {
                    io.execute(() -> repository.markShown(card));
                }
            });
        });
    }

    private boolean drawCard(Flashcards card) {
        if (floatingView != null) return false;

        Context themed = new ContextThemeWrapper(this, R.style.Theme_Ingles_app);
        View view = LayoutInflater.from(themed).inflate(R.layout.flashcard_overlay, null);

        ((TextView) view.findViewById(R.id.txtWord)).setText(card.word);

        ImageView image = view.findViewById(R.id.imgFlashcard);
        File file = images.fileFor(card.img);
        if (file.exists()) {
            Glide.with(getApplicationContext()).load(file).into(image);   // imagen guardada: sin conexión
        } else {
            image.setImageResource(R.mipmap.ic_launcher);
        }

        // NOT_TOUCHABLE
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        params.y = 150;

        try {
            windowManager.addView(view, params);
        } catch (Exception e) {
            Log.e(TAG, "Could not show the card", e);
            return false;
        }

        floatingView = view;
        Log.d(TAG, "Card shown: " + card.word);
        mainHandler.postDelayed(this::removeCard, CARD_DURATION_MS);
        return true;
    }

    private void removeCard() {
        if (floatingView != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception e) {
                Log.w(TAG, "Could not remove the card: " + e.getMessage());
            }
            floatingView = null;
        }
    }

    // ---------- Notifications ----------

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Flashcards", NotificationManager.IMPORTANCE_MIN);
        channel.setDescription("Keeps your daily flashcards ready");
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private Notification buildNotification() {
        PendingIntent open = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);

        String when = TRIGGER_ON_UNLOCK ? "unlock your phone" : "turn on your screen";

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Flashcards are active")
                .setContentText("Your daily words will appear when you " + when)
                .setContentIntent(open)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build();
    }
}