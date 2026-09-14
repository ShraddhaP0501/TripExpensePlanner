package com.example.tripexpenseplanner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Reminder;
import com.example.tripexpenseplanner.notification.ReminderScheduler;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Screen for creating a new Reminder for a trip (Activity, Hotel Booking,
 * Transportation Booking, or Important Note). On save, it stores the reminder
 * in SQLite and schedules a local device alarm via {@link ReminderScheduler} —
 * there is no server involved.
 */
public class AddReminderActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final String TAG = "AddReminderActivity";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm";
    private static final long NO_TRIP_ID = -1L;
    /** Position 0 in reminder_types is the "Select Type" placeholder, not a real type. */
    private static final int TYPE_PLACEHOLDER_POSITION = 0;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.US);

    private DatabaseHelper dbHelper;
    private long tripId = NO_TRIP_ID;

    private TextInputLayout layoutReminderTitle;
    private TextInputLayout layoutReminderDate;
    private TextInputLayout layoutReminderTime;
    private Spinner spinnerReminderType;
    private TextView textErrorReminderType;

    private TextInputEditText editReminderTitle;
    private TextInputEditText editReminderDate;
    private TextInputEditText editReminderTime;
    private TextInputEditText editReminderNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reminder);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_TRIP_ID);

        if (tripId == NO_TRIP_ID) {
            // A reminder must always belong to a trip.
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        layoutReminderTitle = findViewById(R.id.layoutReminderTitle);
        layoutReminderDate = findViewById(R.id.layoutReminderDate);
        layoutReminderTime = findViewById(R.id.layoutReminderTime);
        spinnerReminderType = findViewById(R.id.spinnerReminderType);
        textErrorReminderType = findViewById(R.id.textErrorReminderType);

        editReminderTitle = findViewById(R.id.editReminderTitle);
        editReminderDate = findViewById(R.id.editReminderDate);
        editReminderTime = findViewById(R.id.editReminderTime);
        editReminderNote = findViewById(R.id.editReminderNote);

        editReminderDate.setOnClickListener(v -> showDatePicker());
        editReminderTime.setOnClickListener(v -> showTimePicker());

        findViewById(R.id.buttonCancelAddReminder).setOnClickListener(v -> finish());
        findViewById(R.id.buttonSaveReminder).setOnClickListener(v -> validateAndSaveReminder());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String existingValue = textOf(editReminderDate);
        if (!TextUtils.isEmpty(existingValue)) {
            try {
                calendar.setTime(dateFormat.parse(existingValue));
            } catch (ParseException e) {
                // Ignore and fall back to today's date.
            }
        }

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    editReminderDate.setText(dateFormat.format(selected.getTime()));
                    layoutReminderDate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /**
     * Uses a 24-hour picker so the stored time ("HH:mm") is unambiguous — this
     * is what gets combined with the date to schedule the exact alarm.
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        String existingValue = textOf(editReminderTime);
        if (!TextUtils.isEmpty(existingValue)) {
            try {
                calendar.setTime(timeFormat.parse(existingValue));
            } catch (ParseException e) {
                // Ignore and fall back to the current time.
            }
        }

        new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selected.set(Calendar.MINUTE, minute);
                    editReminderTime.setText(timeFormat.format(selected.getTime()));
                    layoutReminderTime.setError(null);
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true // 24-hour view
        ).show();
    }

    private void validateAndSaveReminder() {
        layoutReminderTitle.setError(null);
        layoutReminderDate.setError(null);
        layoutReminderTime.setError(null);
        textErrorReminderType.setVisibility(View.GONE);

        String title = textOf(editReminderTitle);
        String reminderDate = textOf(editReminderDate);
        String reminderTime = textOf(editReminderTime);
        String note = textOf(editReminderNote);

        boolean isValid = true;

        if (TextUtils.isEmpty(title)) {
            layoutReminderTitle.setError(getString(R.string.error_reminder_title_required));
            isValid = false;
        }
        if (spinnerReminderType.getSelectedItemPosition() == TYPE_PLACEHOLDER_POSITION) {
            textErrorReminderType.setText(R.string.error_reminder_type_required);
            textErrorReminderType.setVisibility(View.VISIBLE);
            isValid = false;
        }
        if (TextUtils.isEmpty(reminderDate)) {
            layoutReminderDate.setError(getString(R.string.error_reminder_date_required));
            isValid = false;
        }
        if (TextUtils.isEmpty(reminderTime)) {
            layoutReminderTime.setError(getString(R.string.error_reminder_time_required));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        String reminderType = (String) spinnerReminderType.getSelectedItem();
        Reminder reminder = new Reminder(tripId, title, reminderType, reminderDate, reminderTime,
                TextUtils.isEmpty(note) ? null : note);

        // A reminder scheduled in the past would either fire immediately or never
        // fire at all, depending on the OS — reject it here instead.
        Long triggerAtMillis = ReminderScheduler.computeTriggerAtMillis(reminder);
        if (triggerAtMillis == null || triggerAtMillis <= System.currentTimeMillis()) {
            layoutReminderTime.setError(getString(R.string.error_reminder_in_past));
            return;
        }

        saveReminder(reminder);
    }

    private void saveReminder(Reminder reminder) {
        try {
            long newReminderId = dbHelper.insertReminder(reminder);
            if (newReminderId == -1) {
                throw new SQLiteException("Insert returned -1 for reminder: " + reminder.getTitle());
            }
            reminder.setId(newReminderId);
            Log.d(TAG, "Reminder saved with id = " + newReminderId + " for trip id = " + tripId);

            boolean exact = ReminderScheduler.scheduleReminder(this, reminder);
            if (exact) {
                Toast.makeText(this, R.string.msg_reminder_saved, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.msg_reminder_saved_inexact, Toast.LENGTH_LONG).show();
            }
            finish();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving reminder for trip id=" + tripId, e);
            Toast.makeText(this, R.string.error_saving_reminder, Toast.LENGTH_LONG).show();
        }
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
