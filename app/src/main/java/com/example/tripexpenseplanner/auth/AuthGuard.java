package com.example.tripexpenseplanner.auth;

import android.content.Context;
import android.content.Intent;

import com.example.tripexpenseplanner.LoginActivity;

public final class AuthGuard {
    private AuthGuard() {
    }

    public static boolean require(Context context) {
        if (AuthSession.isAuthenticated(context)) {
            return true;
        }
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        return false;
    }
}
