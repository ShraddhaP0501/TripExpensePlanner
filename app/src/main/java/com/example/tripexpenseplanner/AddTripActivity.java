package com.example.tripexpenseplanner;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
 * Screen for creating a new Trip.
 * Validates the input, then inserts one row into the "trips" table via DatabaseHelper.
 * Trip listing/editing is not implemented yet — this screen only creates trips.
 */
public class AddTripActivity extends AppCompatActivity {

    private static final String TAG = "AddTripActivity";
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    private DatabaseHelper dbHelper;

    private TextInputLayout layoutTripName;
    private TextInputLayout layoutDestination;
    private TextInputLayout layoutStartDate;
    private TextInputLayout layoutEndDate;

    private TextInputEditText editTripName;
    private TextInputEditText editDestination;
    private TextInputEditText editStartDate;
    private TextInputEditText editEndDate;
    private TextInputEditText editNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip);

        dbHelper = new DatabaseHelper(this);

        layoutTripName = findViewById(R.id.layoutTripName);
        layoutDestination = findViewById(R.id.layoutDestination);
        layoutStartDate = findViewById(R.id.layoutStartDate);
        layoutEndDate = findViewById(R.id.layoutEndDate);

        editTripName = findViewById(R.id.editTripName);
        editDestination = findViewById(R.id.editDestination);
        editStartDate = findViewById(R.id.editStartDate);
        editEndDate = findViewById(R.id.editEndDate);
        editNotes = findViewById(R.id.editNotes);

        editStartDate.setOnClickListener(v -> showDatePicker(editStartDate, layoutStartDate));
        editEndDate.setOnClickListener(v -> showDatePicker(editEndDate, layoutEndDate));

        findViewById(R.id.buttonCancel).setOnClickListener(v -> finish());
        findViewById(R.id.buttonSaveTrip).setOnClickListener(v -> validateAndSaveTrip());
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
     * Validates every required field, then inserts the trip into SQLite.
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
        saveTrip(trip);
    }

    /**
     * Inserts the trip into the database and reports the result to the user.
     */
    private void saveTrip(Trip trip) {
        try {
            long newTripId = dbHelper.insertTrip(trip);
            if (newTripId == -1) {
                // db.insert() returns -1 if the row could not be inserted.
                throw new SQLiteException("Insert returned -1 for trip: " + trip.getTripName());
            }
            Log.d(TAG, "Trip saved with id = " + newTripId);
            Toast.makeText(this, R.string.msg_trip_saved, Toast.LENGTH_SHORT).show();
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
