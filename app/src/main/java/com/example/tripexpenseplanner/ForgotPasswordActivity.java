package com.example.tripexpenseplanner;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.auth.PasswordHasher;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.GeneralSecurityException;
import java.util.Locale;

public class ForgotPasswordActivity extends AppCompatActivity {
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private TextInputLayout confirmLayout;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private TextInputEditText confirmInput;
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        database = new DatabaseHelper(this);
        emailLayout = findViewById(R.id.layoutResetEmail);
        passwordLayout = findViewById(R.id.layoutResetPassword);
        confirmLayout = findViewById(R.id.layoutResetConfirmPassword);
        emailInput = findViewById(R.id.editResetEmail);
        passwordInput = findViewById(R.id.editResetPassword);
        confirmInput = findViewById(R.id.editResetConfirmPassword);
        findViewById(R.id.buttonResetPassword).setOnClickListener(v -> reset());
        findViewById(R.id.buttonBackFromReset).setOnClickListener(v -> finish());
    }

    private void reset() {
        emailLayout.setError(null); passwordLayout.setError(null); confirmLayout.setError(null);
        String email = textOf(emailInput).toLowerCase(Locale.US);
        String password = textOf(passwordInput);
        String confirm = textOf(confirmInput);
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { emailLayout.setError(getString(R.string.error_email_invalid)); return; }
        if (password.length() < 8) { passwordLayout.setError(getString(R.string.error_password_short)); return; }
        if (!password.equals(confirm)) { confirmLayout.setError(getString(R.string.error_password_mismatch)); return; }
        try (android.database.Cursor cursor = database.findUserByEmail(email)) {
            if (!cursor.moveToFirst()) { emailLayout.setError(getString(R.string.error_account_not_found)); return; }
            long userId = cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID));
            database.updateUserPassword(userId, PasswordHasher.hash(password));
            Toast.makeText(this, R.string.msg_password_reset, Toast.LENGTH_LONG).show();
            finish();
        } catch (GeneralSecurityException e) {
            Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_LONG).show();
        }
    }

    private String textOf(TextInputEditText input) { return input.getText() == null ? "" : input.getText().toString().trim(); }
}
