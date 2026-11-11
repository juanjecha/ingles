package com.example.ingles_app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class SessionManager {
    private static final String PREFS = "preferencias";
    private static final String KEY_TOKEN = "JWT_TOKEN";
    private static final String KEY_USER_ID = "USER_ID";

    private static final String KEY_USER_NAME = "USER_NAME";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    //Returns true if a different student from the last session signed in
    public boolean save(String token, int userId, String name) {
        int previousId = prefs.getInt(KEY_USER_ID, -1);
        prefs.edit().putString(KEY_TOKEN, token).putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME).apply();
        return previousId != userId;
    }

    public String getKeyToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public void logout() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    public void isLoggedIn() {
        return getTokenExpiry(getToken()) > System.currentTimeMillis();
    }

    private static long getTokenExpiry(String token) {
        if (token == null) return 0;
        try {
            String[] parts = token.split("\\.");
            byte[] json = Base64.decode(parts[1], Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_WRAP);
            return new JSONObject(new String(json, StandardCharsets.UTF_8)).getLong("exp") * 1000L;
        } catch (Exception e) {
            return 0;
        }
    }
}