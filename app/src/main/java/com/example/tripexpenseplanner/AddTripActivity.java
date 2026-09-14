package com.example.tripexpenseplanner;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Trip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Screen for creating a new Trip, and also for editing an existing one.
 * Pass {@link #EXTRA_TRIP_ID} in the launching Intent to open it in edit mode —
 * without it, the screen behaves as "Add New Trip".
 * Validates the input, then inserts/updates one row in the "trips" table via DatabaseHelper.
 */
public class AddTripActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final String TAG = "AddTripActivity";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final long NO_TRIP_ID = -1L;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    private DatabaseHelper dbHelper;

    /** -1 while creating a new trip; the real trip id while editing an existing one. */
    private long editingTripId = NO_TRIP_ID;

    private TextInputLayout layoutTripName;
    private TextInputLayout layoutDestination;
    private TextInputLayout layoutStartDate;
    private TextInputLayout layoutEndDate;

    private TextInputEditText editTripName;
    private TextInputEditText editDestination;
    private TextInputEditText editStartDate;
    private TextInputEditText editEndDate;
    private TextInputEditText editNotes;

    private Button buttonSaveTrip;
    private TextView textFormTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        dbHelper = new DatabaseHelper(this);
        editingTripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_TRIP_ID);

        layoutTripName = findViewById(R.id.layoutTripName);
        layoutDestination = findViewById(R.id.layoutDestination);
        layoutStartDate = findViewById(R.id.layoutStartDate);
        layoutEndDate = findViewById(R.id.layoutEndDate);

        editTripName = findViewById(R.id.editTripName);
        editDestination = findViewById(R.id.editDestination);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        editNotes = findViewById(R.id.editNotes);
        buttonSaveTrip = findViewById(R.id.buttonSaveTrip);
        textFormTitle = findViewById(R.id.textFormTitle);

        editStartDate.setOnClickListener(v -> showDatePicker(editStartDate, layoutStartDate));
        editEndDate.setOnClickListener(v -> showDatePicker(editEndDate, layoutEndDate));

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonCancel).setOnClickListener(v -> finish());
        buttonSaveTrip.setOnClickListener(v -> validateAndSaveTrip());

        if (isEditMode()) {
            setTitle(R.string.title_edit_trip);
            textFormTitle.setText(R.string.title_edit_trip);
            buttonSaveTrip.setText(R.string.label_update_trip);
            prefillFieldsForEdit();
        } else {
            setTitle(R.string.title_add_trip);
            textFormTitle.setText(R.string.title_add_trip);
        }
    }

    private boolean isEditMode() {
        return editingTripId != NO_TRIP_ID;
    }

    /**
     * Loads the existing trip and fills the form with its current values.
     */
    private void prefillFieldsForEdit() {
        Trip trip = dbHelper.getTrip(editingTripId);
        if (trip == null) {
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        editTripName.setText(trip.getTripName());
        editDestination.setText(trip.getDestination());
        editStartDate.setText(trip.getStartDate());
        editEndDate.setText(trip.getEndDate());
        editNotes.setText(trip.getNotes());
    }

    /**
     * Opens a DatePickerDialog pre-filled with the field's current date (or today,
     * if empty) and writes the chosen date back into the field as "yyyy-MM-dd".
     */
    private void showDatePicker(TextInputEditText targetField, TextInputLayout targetLayout) {
        Calendar calendar = Calendar.getInstance();

        String existingValue = targetField.getText() != null ? targetField.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(existingValue)) {
            try {
                calendar.setTime(dateFormat.parse(existingValue));
            } catch (ParseException e) {
                // Ignore and fall back to today's date.
            }
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    targetField.setText(dateFormat.format(selected.getTime()));
                    targetLayout.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    /**
     * Validates every required field, then inserts or updates the trip in SQLite.
     */
    private void validateAndSaveTrip() {
        layoutTripName.setError(null);
        layoutDestination.setError(null);
        layoutStartDate.setError(null);
        layoutEndDate.setError(null);

        String tripName = textOf(editTripName);
        String destination = textOf(editDestination);
        String startDate = textOf(editStartDate);
        String endDate = textOf(editEndDate);
        String notes = textOf(editNotes);

        boolean isValid = true;

        if (TextUtils.isEmpty(tripName)) {
            layoutTripName.setError(getString(R.string.error_trip_name_required));
            isValid = false;
        }
        if (TextUtils.isEmpty(destination)) {
            layoutDestination.setError(getString(R.string.error_destination_required));
            isValid = false;
        }
        if (TextUtils.isEmpty(startDate)) {
            layoutStartDate.setError(getString(R.string.error_start_date_required));
            isValid = false;
        }
        if (TextUtils.isEmpty(endDate)) {
            layoutEndDate.setError(getString(R.string.error_end_date_required));
            isValid = false;
        }

        // Only compare the dates once both are known to be present.
        if (isValid && startDate.compareTo(endDate) > 0) {
            layoutEndDate.setError(getString(R.string.error_start_after_end));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        Trip trip = new Trip(tripName, destination, startDate, endDate,
                TextUtils.isEmpty(notes) ? null : notes);
        if (isEditMode()) {
            trip.setId(editingTripId);
        }
        saveTrip(trip);
    }

    /**
     * Inserts (or updates, in edit mode) the trip and reports the result to the user.
     */
    private void saveTrip(Trip trip) {
        try {
            if (isEditMode()) {
                int rowsUpdated = dbHelper.updateTrip(trip);
                if (rowsUpdated <= 0) {
                    throw new SQLiteException("Update affected 0 rows for trip id: " + trip.getId());
                }
                Log.d(TAG, "Trip updated, id = " + trip.getId());
                Toast.makeText(this, R.string.msg_trip_updated, Toast.LENGTH_SHORT).show();
            } else {
                long newTripId = dbHelper.insertTrip(trip);
                if (newTripId == -1) {
                    // db.insert() returns -1 if the row could not be inserted.
                    throw new SQLiteException("Insert returned -1 for trip: " + trip.getTripName());
                }
                Log.d(TAG, "Trip saved with id = " + newTripId);
                Toast.makeText(this, R.string.msg_trip_saved, Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving trip", e);
            Toast.makeText(this, R.string.error_saving_trip, Toast.LENGTH_LONG).show();
        }
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
