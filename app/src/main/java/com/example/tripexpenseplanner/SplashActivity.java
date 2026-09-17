package com.example.tripexpenseplanner;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.auth.AuthSession;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Class<?> destination = AuthSession.isAuthenticated(this) ? MainActivity.class : LoginActivity.class;
            startActivity(new Intent(this, destination));
            finish();
        }, 350);
    }
}
