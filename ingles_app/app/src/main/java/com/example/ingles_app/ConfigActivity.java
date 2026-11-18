package com.example.ingles_app;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ConfigActivity extends AppCompatActivity {
    private TimeConfig config;
    private SwitchCompat swSchedule;
    private Button btnStartTime, btnEndTime;
    private TextView tvHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.time), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), bars.top + 24 , v.getPaddingRight(), bars.bottom +24);
            return insets;
        });

        config = new TimeConfig(this);
        swSchedule = findViewById(R.id.swSchedule);
        btnStartTime = findViewById(R.id.btnSartTime);
        btnEndTime = findViewById(R.id.btnEndTime);
        tvHint = findViewById(R.id.tvScheduleHint);

        swSchedule.setChecked(config.isEnabled());
        swSchedule.setOnCheckedChangeListener((b, checked) ->{
            config.setEnabled(checked);
            refresh();
        });

        btnStartTime.setOnClickListener(v -> pickTime(config.getStart(), minutes -> config.setStart(minutes)));
        btnEndTime.setOnClickListener(v -> pickTime(config.getEnd(), minutes -> config.setEnd(minutes)));

        refresh();
    }

    private interface OnTime { void set(int minutes);}

    private void pickTime(int current, OnTime callback) {
        new TimePickerDialog(this, (view, hour, minute) -> {
            callback.set(hour * 60 + minute);
            refresh();
        }, current / 60, current % 60, true).show();
    }

    private void refresh() {
        btnStartTime.setText(TimeConfig.format(config.getStart()));
        btnEndTime.setText(TimeConfig.format(config.getEnd()));
        boolean on = config.isEnabled();
        btnStartTime.setEnabled(on);
        btnEndTime.setEnabled(on);
        tvHint.setText(on
            ? "Cards appear between " + TimeConfig.format(config.getStart()) + " and" + TimeConfig.format(config.getEnd())
            : "Cards can appear at any time"
                );
    }
}
