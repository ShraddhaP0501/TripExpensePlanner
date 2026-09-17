package com.example.tripexpenseplanner;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.auth.AuthSession;
import com.example.tripexpenseplanner.auth.PasswordHasher;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.GeneralSecurityException;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        database = new DatabaseHelper(this);
        emailLayout = findViewById(R.id.layoutLoginEmail);
        passwordLayout = findViewById(R.id.layoutLoginPassword);
        emailInput = findViewById(R.id.editLoginEmail);
        passwordInput = findViewById(R.id.editLoginPassword);

        findViewById(R.id.buttonLogin).setOnClickListener(v -> login());
        findViewById(R.id.buttonRegister).setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.buttonForgotPassword).setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void login() {
        emailLayout.setError(null);
        passwordLayout.setError(null);
        String email = textOf(emailInput).toLowerCase(Locale.US);
        String password = textOf(passwordInput);
        boolean valid = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_email_invalid));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_password_required));
            valid = false;
        }
        if (!valid) return;

        try (android.database.Cursor cursor = database.findUserByEmail(email)) {
            if (!cursor.moveToFirst() || !PasswordHasher.matches(password,
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD_HASH)))) {
                passwordLayout.setError(getString(R.string.error_invalid_login));
                return;
            }
            long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
            AuthSession.start(this, userId, name);
            database.claimUnownedTrips(userId);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (GeneralSecurityException e) {
            Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_LONG).show();
        }
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
