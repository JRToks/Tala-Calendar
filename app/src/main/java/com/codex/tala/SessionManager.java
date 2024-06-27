package com.codex.tala;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public class SessionManager {
    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private static final String PREF_NAME = "session";
    private static final String SESSION_KEY = "session_user";
    private static final String DEFAULT_USER_ID = "";

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void saveSession(String userId) {
        editor.putString(SESSION_KEY, userId);
        editor.apply();
    }

    public String getSession() {
        return pref.getString(SESSION_KEY, DEFAULT_USER_ID);
    }

    public boolean isLoggedIn() {
        return !Objects.equals(getSession(), DEFAULT_USER_ID);
    }

    public void removeSession() {
        editor.putString(SESSION_KEY, DEFAULT_USER_ID);
        editor.apply();
    }
}
