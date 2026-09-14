package com.example.tripexpenseplanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.adapter.ReminderAdapter;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Reminder;
import com.example.tripexpenseplanner.model.Trip;
import com.example.tripexpenseplanner.notification.ReminderScheduler;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Lists every reminder created for one trip, lets the user add a new one, and
 * cancel (delete) an existing one. Reminders fire as local notifications via
 * {@link android.app.AlarmManager} — no Firebase or external server is used.
 */
public class RemindersActivity extends AppCompatActivity implements ReminderAdapter.OnReminderActionListener {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final String TAG = "RemindersActivity";
    private static final long NO_TRIP_ID = -1L;

    private DatabaseHelper dbHelper;
    private ReminderAdapter reminderAdapter;
    private long tripId = NO_TRIP_ID;

    private TextView textRemindersTitle;
    private RecyclerView recyclerReminders;
    private TextView textEmptyReminders;

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Must be registered unconditionally during onCreate, before the activity
        // reaches STARTED — even though it's only actually launched on Android 13+.
        notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (!granted) {
                        Toast.makeText(this, R.string.msg_notification_permission_denied, Toast.LENGTH_LONG).show();
                    }
                }
        );

        setContentView(R.layout.activity_reminders);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_TRIP_ID);

        if (tripId == NO_TRIP_ID) {
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textRemindersTitle = findViewById(R.id.textRemindersTitle);
        recyclerReminders = findViewById(R.id.recyclerReminders);
        textEmptyReminders = findViewById(R.id.textEmptyReminders);

        reminderAdapter = new ReminderAdapter(this);
        recyclerReminders.setLayoutManager(new LinearLayoutManager(this));
        recyclerReminders.setAdapter(reminderAdapter);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonAddReminder).setOnClickListener(v -> openAddReminder());

        updateTitle();
        requestNotificationPermissionIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time this screen becomes visible again, so a reminder
        // added or cancelled elsewhere is always reflected.
        loadReminders();
    }

    private void updateTitle() {
        Trip trip = dbHelper.getTrip(tripId);
        String label = getString(R.string.title_reminders);
        if (trip != null) {
            label = label + ": " + trip.getTripName();
        }
        textRemindersTitle.setText(label);
        setTitle(label);
    }

    /**
     * On Android 13 (API 33) and above, apps must be granted POST_NOTIFICATIONS
     * before they can show any notification — including the ones reminders rely
     * on. Below API 33 this permission doesn't exist and notifications work
     * automatically.
     */
    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return;
        }
        boolean alreadyGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
        if (!alreadyGranted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void loadReminders() {
        List<Reminder> reminders = dbHelper.getRemindersByTrip(tripId);
        reminderAdapter.setReminders(reminders);

        boolean isEmpty = reminders.isEmpty();
        textEmptyReminders.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerReminders.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openAddReminder() {
        Intent intent = new Intent(this, AddReminderActivity.class);
        intent.putExtra(AddReminderActivity.EXTRA_TRIP_ID, tripId);
        startActivity(intent);
    }

    @Override
    public void onCancelReminder(Reminder reminder) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_cancel_reminder_title)
                .setMessage(R.string.dialog_cancel_reminder_message)
                .setPositiveButton(R.string.label_cancel_reminder, (dialog, which) -> cancelReminder(reminder))
                .setNegativeButton(R.string.label_cancel, null)
                .show();
    }

    private void cancelReminder(Reminder reminder) {
        try {
            // Cancel the scheduled alarm first, then remove the database row.
            ReminderScheduler.cancelReminder(this, reminder.getId());
            int rowsDeleted = dbHelper.deleteReminder(reminder.getId());
            if (rowsDeleted > 0) {
                Toast.makeText(this, R.string.msg_reminder_cancelled, Toast.LENGTH_SHORT).show();
                loadReminders();
            } else {
                Toast.makeText(this, R.string.error_cancelling_reminder, Toast.LENGTH_LONG).show();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error cancelling reminder id=" + reminder.getId(), e);
            Toast.makeText(this, R.string.error_cancelling_reminder, Toast.LENGTH_LONG).show();
        }
    }
}
