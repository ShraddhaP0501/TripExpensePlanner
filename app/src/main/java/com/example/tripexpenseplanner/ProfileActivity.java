package com.example.tripexpenseplanner;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.auth.AuthGuard;
import com.example.tripexpenseplanner.auth.AuthSession;
import com.example.tripexpenseplanner.auth.PasswordHasher;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.security.GeneralSecurityException;

public class ProfileActivity extends AppCompatActivity {
    private DatabaseHelper database;
    private TextInputLayout currentPasswordLayout;
    private TextInputLayout newPasswordLayout;
    private TextInputLayout confirmPasswordLayout;
    private TextInputEditText currentPasswordInput;
    private TextInputEditText newPasswordInput;
    private TextInputEditText confirmPasswordInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.require(this)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_profile);
        database = new DatabaseHelper(this);
        ((android.widget.TextView) findViewById(R.id.textProfileName)).setText(AuthSession.getUserName(this));
        loadEmail();
        currentPasswordLayout = findViewById(R.id.layoutCurrentPassword);
        newPasswordLayout = findViewById(R.id.layoutNewPassword);
        confirmPasswordLayout = findViewById(R.id.layoutConfirmPassword);
        currentPasswordInput = findViewById(R.id.editCurrentPassword);
        newPasswordInput = findViewById(R.id.editNewPassword);
        confirmPasswordInput = findViewById(R.id.editConfirmPassword);
        findViewById(R.id.buttonChangePassword).setOnClickListener(v -> changePassword());
        findViewById(R.id.buttonProfileLogout).setOnClickListener(v -> confirmLogout());
        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
    }

    private void loadEmail() {
        try (android.database.Cursor cursor = database.findUserById(AuthSession.getUserId(this))) {
            if (cursor.moveToFirst()) {
                ((android.widget.TextView) findViewById(R.id.textProfileEmail)).setText(
                        cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
            }
        }
    }

    private void changePassword() {
        currentPasswordLayout.setError(null);
        newPasswordLayout.setError(null);
        confirmPasswordLayout.setError(null);
        String current = textOf(currentPasswordInput);
        String next = textOf(newPasswordInput);
        String confirm = textOf(confirmPasswordInput);
        if (TextUtils.isEmpty(current)) { currentPasswordLayout.setError(getString(R.string.error_password_required)); return; }
        if (next.length() < 8) { newPasswordLayout.setError(getString(R.string.error_password_short)); return; }
        if (!next.equals(confirm)) { confirmPasswordLayout.setError(getString(R.string.error_password_mismatch)); return; }
        try (android.database.Cursor cursor = database.findUserById(AuthSession.getUserId(this))) {
            if (!cursor.moveToFirst() || !PasswordHasher.matches(current,
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD_HASH)))) {
                currentPasswordLayout.setError(getString(R.string.error_current_password));
                return;
            }
            database.updateUserPassword(AuthSession.getUserId(this), PasswordHasher.hash(next));
            currentPasswordInput.setText("");
            newPasswordInput.setText("");
            confirmPasswordInput.setText("");
            Toast.makeText(this, R.string.msg_password_changed, Toast.LENGTH_SHORT).show();
        } catch (GeneralSecurityException e) {
            Toast.makeText(this, R.string.error_unexpected, Toast.LENGTH_LONG).show();
        }
    }

    private void confirmLogout() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.label_logout, (dialog, which) -> {
                    AuthSession.clear(this);
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }).show();
    }

    private String textOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }
}
