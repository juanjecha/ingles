package com.example.ingles_app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PermissionsActivity extends AppCompatActivity {
    private static final int REQ_NOTIFICATIONS = 200;

    private static class Row {
        TextView status;
        Button button;
        String missingLabel;
    }

    private Row overlayRow, notificationsRow, batteryRow;

    // Obligatorios para que la tarjeta se vea
    public static boolean allRequiredGranted(Context context) {
        return hasOverlay(context) && hasNotifications(context);
    }

    public static boolean hasOverlay(Context context) {
        return Settings.canDrawOverlays(context);
    }

    public static boolean hasNotifications(Context context) {
        return NotificationManagerCompat.from(context).areNotificationsEnabled();
    }

    public static boolean hasBatteryExemption(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_permissions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout list = findViewById(R.id.permissionList);

        overlayRow = addRow(list, "Show over other apps",
                "Needed to display the flashcard on top of any app.",
                "Required",
                v -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()))));

        notificationsRow = addRow(list, "Notifications",
                "Android shows a small silent notification while the flashcards are active.",
                "Required",
                v -> requestNotifications());

        batteryRow = addRow(list, "Unrestricted battery",
                "Recommended: keeps the flashcards working after your phone has been idle for a long time.",
                "Recommended",
                v -> startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)));

        findViewById(R.id.btnPreview).setOnClickListener(v -> {
            if (!hasOverlay(this)) {
                Toast.makeText(this, "Allow \"Show over other apps\" first", Toast.LENGTH_SHORT).show();
                return;
            }
            FlashcardOverlayService.preview(getApplicationContext(), 5000);
            Toast.makeText(this, "A card will appear in 5 seconds. Go to the home screen or open any app.",
                    Toast.LENGTH_LONG).show();
        });

        findViewById(R.id.btnDone).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        update(overlayRow, hasOverlay(this));
        update(notificationsRow, hasNotifications(this));
        update(batteryRow, hasBatteryExemption(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_NOTIFICATIONS
                && (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            // Si ya lo negó antes, el diálogo no vuelve a salir: se abre la configuración
            openNotificationSettings();
        }
    }

    private void requestNotifications() {
        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIFICATIONS);
        } else {
            openNotificationSettings();
        }
    }

    private void openNotificationSettings() {
        startActivity(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName()));
    }

    private Row addRow(LinearLayout parent, String title, String description,
                       String missingLabel, View.OnClickListener action) {
        View item = getLayoutInflater().inflate(R.layout.item_permission, parent, false);
        ((TextView) item.findViewById(R.id.tvPermTitle)).setText(title);
        ((TextView) item.findViewById(R.id.tvPermDesc)).setText(description);

        Row row = new Row();
        row.status = item.findViewById(R.id.tvPermStatus);
        row.button = item.findViewById(R.id.btnPermAction);
        row.missingLabel = missingLabel;
        row.button.setOnClickListener(action);

        parent.addView(item);
        return row;
    }

    private void update(Row row, boolean granted) {
        row.status.setText(granted ? "✓ Allowed" : row.missingLabel);
        row.status.setTextColor(ContextCompat.getColor(this,
                granted ? R.color.primary_green : R.color.accent_orange));
        row.button.setVisibility(granted ? View.GONE : View.VISIBLE);
    }
}