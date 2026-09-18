package com.example.tripexpenseplanner;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.auth.AuthSession;
import com.example.tripexpenseplanner.auth.PasswordHasher;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.GeneralSecurityException;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {
    private TextInputLayout nameLayout;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmLayout;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        database = new DatabaseHelper(this);
        nameLayout = findViewById(R.id.layoutRegisterName);
        emailLayout = findViewById(R.id.layoutRegisterEmail);
        passwordLayout = findViewById(R.id.layoutRegisterPassword);
        confirmLayout = findViewById(R.id.layoutRegisterConfirmPassword);
        nameInput = findViewById(R.id.editRegisterName);
        emailInput = findViewById(R.id.editRegisterEmail);
        passwordInput = findViewById(R.id.editRegisterPassword);
        confirmInput = findViewById(R.id.editRegisterConfirmPassword);
        findViewById(R.id.buttonCreateAccount).setOnClickListener(v -> register());
        findViewById(R.id.buttonBackToLogin).setOnClickListener(v -> finish());
    }

    private void register() {
        clearErrors();
        String name = textOf(nameInput);
        String email = textOf(emailInput).toLowerCase(Locale.US);
        String password = textOf(passwordInput);
        String confirm = textOf(confirmInput);
        boolean valid = true;
        if (TextUtils.isEmpty(name)) { nameLayout.setError(getString(R.string.error_name_required)); valid = false; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailLayout.setError(getString(R.string.error_email_invalid)); valid = false; }
        if (password.length() < 8) { passwordLayout.setError(getString(R.string.error_password_short)); valid = false; }
        if (!password.equals(confirm)) { confirmLayout.setError(getString(R.string.error_password_mismatch)); valid = false; }
        if (!valid) return;

        try (android.database.Cursor cursor = database.findUserByEmail(email)) {
            if (cursor.moveToFirst()) { emailLayout.setError(getString(R.string.error_duplicate_email)); return; }
        }
        try {
            long userId = database.createUser(name, email, PasswordHasher.hash(password));
            if (userId <= 0) throw new GeneralSecurityException();
            AuthSession.start(this, userId, name);
            database.claimUnownedTrips(userId);
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } catch (GeneralSecurityException e) {
            Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_LONG).show();
        }
    }

    private void clearErrors() {
        nameLayout.setError(null); emailLayout.setError(null); passwordLayout.setError(null); confirmLayout.setError(null);
    }

    private String textOf(TextInputEditText input) { return input.getText() == null ? "" : input.getText().toString().trim(); }
}
