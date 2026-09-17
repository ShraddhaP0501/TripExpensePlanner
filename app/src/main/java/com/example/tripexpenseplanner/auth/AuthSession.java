package com.example.tripexpenseplanner.auth;

import android.content.Context;
import android.content.SharedPreferences;

public final class AuthSession {
    private static final String PREFS = "auth_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";

    private AuthSession() {
    }

    public static void start(Context context, long userId, String name) {
        preferences(context).edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, name)
                .apply();
    }

    public static void clear(Context context) {
        preferences(context).edit().clear().apply();
    }

    public static boolean isAuthenticated(Context context) {
        return getUserId(context) > 0;
    }

    public static long getUserId(Context context) {
        return preferences(context).getLong(KEY_USER_ID, -1L);
    }

    public static String getUserName(Context context) {
        return preferences(context).getString(KEY_USER_NAME, "");
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
