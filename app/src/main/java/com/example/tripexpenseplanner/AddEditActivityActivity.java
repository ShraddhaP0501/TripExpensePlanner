package com.example.tripexpenseplanner;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.TripActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Screen for creating a new itinerary Activity for a trip, and also for editing
 * an existing one. Pass {@link #EXTRA_TRIP_ID} (required) and, in edit mode,
 * {@link #EXTRA_ACTIVITY_ID} in the launching Intent.
 * Validates the input, then inserts/updates one row in the "activities" table.
 */
public class AddEditActivityActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    public static final String EXTRA_ACTIVITY_ID = "extra_activity_id";
    private static final String TAG = "AddEditActivity";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "hh:mm a";
    private static final long NO_ID = -1L;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_PATTERN, Locale.US);

    private DatabaseHelper dbHelper;
    private long tripId = NO_ID;
    /** -1 while creating a new activity; the real activity id while editing one. */
    private long editingActivityId = NO_ID;

    private TextView textFormTitle;
    private TextInputLayout layoutActivityName;
    private TextInputLayout layoutActivityDate;

    private TextInputEditText editActivityName;
    private TextInputEditText editActivityDate;
    private TextInputEditText editActivityTime;
    private TextInputEditText editActivityDescription;

    private Button buttonSaveActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_activity);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_ID);
        editingActivityId = getIntent().getLongExtra(EXTRA_ACTIVITY_ID, NO_ID);

        if (tripId == NO_ID) {
            // An activity must always belong to a trip.
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textFormTitle = findViewById(R.id.textFormTitle);
        layoutActivityName = findViewById(R.id.layoutActivityName);
        layoutActivityDate = findViewById(R.id.layoutActivityDate);

        editActivityName = findViewById(R.id.editActivityName);
        editActivityDate = findViewById(R.id.editActivityDate);
        editActivityTime = findViewById(R.id.editActivityTime);
        editActivityDescription = findViewById(R.id.editActivityDescription);
        buttonSaveActivity = findViewById(R.id.buttonSaveActivity);

        editActivityDate.setOnClickListener(v -> showDatePicker());
        editActivityTime.setOnClickListener(v -> showTimePicker());

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonCancelActivity).setOnClickListener(v -> finish());
        buttonSaveActivity.setOnClickListener(v -> validateAndSaveActivity());

        if (isEditMode()) {
            setTitle(R.string.title_edit_activity);
            textFormTitle.setText(R.string.title_edit_activity);
            buttonSaveActivity.setText(R.string.label_update_activity);
            prefillFieldsForEdit();
        } else {
            setTitle(R.string.title_add_activity);
            textFormTitle.setText(R.string.title_add_activity);
        }
    }

    private boolean isEditMode() {
        return editingActivityId != NO_ID;
    }

    /**
     * Loads the existing activity and fills the form with its current values.
     */
    private void prefillFieldsForEdit() {
        TripActivity activity = dbHelper.getActivity(editingActivityId);
        if (activity == null) {
            Toast.makeText(this, R.string.error_activity_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        editActivityName.setText(activity.getActivityName());
        editActivityDate.setText(activity.getActivityDate());
        editActivityTime.setText(activity.getActivityTime());
        editActivityDescription.setText(activity.getDescription());
    }

    /**
     * Opens a DatePickerDialog pre-filled with the field's current date (or today,
     * if empty) and writes the chosen date back into the field as "yyyy-MM-dd".
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String existingValue = textOf(editActivityDate);
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
                    editActivityDate.setText(dateFormat.format(selected.getTime()));
                    layoutActivityDate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /**
     * Opens a TimePickerDialog pre-filled with the field's current time (or now,
     * if empty) and writes the chosen time back into the field as "hh:mm a".
     */
    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();

        String existingValue = textOf(editActivityTime);
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
                    editActivityTime.setText(timeFormat.format(selected.getTime()));
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        ).show();
    }

    /**
     * Validates the required fields, then inserts or updates the activity in SQLite.
     */
    private void validateAndSaveActivity() {
        layoutActivityName.setError(null);
        layoutActivityDate.setError(null);

        String activityName = textOf(editActivityName);
        String activityDate = textOf(editActivityDate);
        String activityTime = textOf(editActivityTime);
        String description = textOf(editActivityDescription);

        boolean isValid = true;

        if (TextUtils.isEmpty(activityName)) {
            layoutActivityName.setError(getString(R.string.error_activity_name_required));
            isValid = false;
        }
        if (TextUtils.isEmpty(activityDate)) {
            layoutActivityDate.setError(getString(R.string.error_activity_date_required));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        TripActivity activity = new TripActivity(
                tripId,
                activityName,
                activityDate,
                TextUtils.isEmpty(activityTime) ? null : activityTime,
                TextUtils.isEmpty(description) ? null : description
        );
        if (isEditMode()) {
            activity.setId(editingActivityId);
        }
        saveActivity(activity);
    }

    /**
     * Inserts (or updates, in edit mode) the activity and reports the result to the user.
     */
    private void saveActivity(TripActivity activity) {
        try {
            if (isEditMode()) {
                int rowsUpdated = dbHelper.updateActivity(activity);
                if (rowsUpdated <= 0) {
                    throw new SQLiteException("Update affected 0 rows for activity id: " + activity.getId());
                }
                Log.d(TAG, "Activity updated, id = " + activity.getId());
                Toast.makeText(this, R.string.msg_activity_updated, Toast.LENGTH_SHORT).show();
            } else {
                long newActivityId = dbHelper.insertActivity(activity);
                if (newActivityId == -1) {
                    throw new SQLiteException("Insert returned -1 for activity: " + activity.getActivityName());
                }
                Log.d(TAG, "Activity saved with id = " + newActivityId + " for trip id = " + tripId);
                Toast.makeText(this, R.string.msg_activity_saved, Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving activity for trip id=" + tripId, e);
            Toast.makeText(this, R.string.error_saving_activity, Toast.LENGTH_LONG).show();
        }
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
