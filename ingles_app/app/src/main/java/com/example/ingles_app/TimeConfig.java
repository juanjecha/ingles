package com.example.ingles_app;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public class TimeConfig {
    private static final String PREFS = "schedule";
    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_START = "start_time";
    private static final String KEY_END = "end_time";

    private final SharedPreferences prefs;

    public TimeConfig(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isEnabled() {return prefs.getBoolean(KEY_ENABLED,false);}
    public int getStart() {return prefs.getInt(KEY_START, 7 * 60);}
    public int getEnd() { return prefs.getInt(KEY_END, 19 * 60);}

    public void setEnabled(boolean enabled) {prefs.edit().putBoolean(KEY_ENABLED, enabled).apply();}
    public void setStart(int minutes) {prefs.edit().putInt(KEY_START, minutes).apply();}
    public void setEnd(int minutes) {prefs.edit().putInt(KEY_END, minutes).apply();}

    public boolean isAllowedNow() {
        if (!isEnabled()) return true;
        Calendar now = Calendar.getInstance();
        int current = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int start = getStart();
        int end = getEnd();
        if (start == end) return true;
        if (start < end) return current >= start && current < end;
        return current >= start || current <end;
    }

    public static String format(int minutes) {
        return String.format("%02d:%02d", minutes / 60, minutes % 60);
    }
}
