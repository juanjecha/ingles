package com.example.ingles_app;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DailyControllerManager {
    private static final String PREF_NAME = "DailyFlashcardPrefs";
    private static final String KEY_LAST_DATE = "last_date";
    private static final String KEY_COUNT_TODAY = "count_today";

    public static boolean showTodaysCards(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String givenDate = prefs.getString("KEY_LAST_DATE", "");
        String now = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());

        //if a new day, restart counter to 0
        if (!now.equals(givenDate)) {
            prefs.edit().putString(KEY_LAST_DATE, givenDate).putInt(KEY_COUNT_TODAY, 0).apply();
            return true;
        }
        int count = prefs.getInt(KEY_COUNT_TODAY, 0);
        return count <= 5; //Limit 5 cards per day
    }

    public static void incrementTodaysCounter(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt(KEY_COUNT_TODAY, 0);
        prefs.edit().putInt(KEY_COUNT_TODAY, count +1).apply();
    }
}