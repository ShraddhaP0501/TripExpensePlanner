package com.example.tripexpenseplanner;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Trip;

/**
 * Shows full details for one trip and lets the user Edit or Delete it, and
 * navigate to its Itinerary, Expenses, Participants and Trip Summary sections.
 * Those sections are not implemented yet — they open a placeholder screen for now.
 */
public class TripDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final String TAG = "TripDetailsActivity";
    private static final long NO_TRIP_ID = -1L;

    private DatabaseHelper dbHelper;
    private long tripId = NO_TRIP_ID;

    private TextView textTripName;
    private TextView textDestination;
    private TextView textStartDate;
    private TextView textEndDate;
    private TextView labelNotes;
    private TextView textNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_TRIP_ID);

        textTripName = findViewById(R.id.textTripName);
        textDestination = findViewById(R.id.textDestination);
        textStartDate = findViewById(R.id.textStartDate);
        textEndDate = findViewById(R.id.textEndDate);
        labelNotes = findViewById(R.id.labelNotes);
        textNotes = findViewById(R.id.textNotes);

        findViewById(R.id.buttonEditTrip).setOnClickListener(v -> openEditTrip());
        findViewById(R.id.buttonDeleteTrip).setOnClickListener(v -> confirmDeleteTrip());

        findViewById(R.id.buttonItinerary).setOnClickListener(
                v -> openPlaceholder(getString(R.string.label_itinerary)));
        findViewById(R.id.buttonExpenses).setOnClickListener(
                v -> openPlaceholder(getString(R.string.label_trip_expenses)));
        findViewById(R.id.buttonParticipants).setOnClickListener(
                v -> openPlaceholder(getString(R.string.label_participants)));
        findViewById(R.id.buttonTripSummary).setOnClickListener(
                v -> openPlaceholder(getString(R.string.label_trip_summary)));

        if (tripId == NO_TRIP_ID) {
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tripId != NO_TRIP_ID) {
            // Reload every time, so returning from Edit Trip shows the latest values.
            loadTripDetails();
        }
    }

    private void loadTripDetails() {
        Trip trip = dbHelper.getTrip(tripId);
        if (trip == null) {
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textTripName.setText(trip.getTripName());
        textDestination.setText(trip.getDestination());
        textStartDate.setText(trip.getStartDate());
        textEndDate.setText(trip.getEndDate());

        String notes = trip.getNotes();
        boolean hasNotes = !TextUtils.isEmpty(notes);
        labelNotes.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
        textNotes.setVisibility(hasNotes ? View.VISIBLE : View.GONE);
        textNotes.setText(notes);
    }

    private void openEditTrip() {
        Intent intent = new Intent(this, AddTripActivity.class);
        intent.putExtra(AddTripActivity.EXTRA_TRIP_ID, tripId);
        startActivity(intent);
    }

    /**
     * Opens the shared "coming soon" screen for a trip section (Itinerary, Expenses,
     * Participants, Trip Summary), carrying this trip's id along with it.
     */
    private void openPlaceholder(String screenTitle) {
        Intent intent = new Intent(this, PlaceholderActivity.class);
        intent.putExtra(PlaceholderActivity.EXTRA_TRIP_ID, tripId);
        intent.putExtra(PlaceholderActivity.EXTRA_SCREEN_TITLE, screenTitle);
        startActivity(intent);
    }

    private void confirmDeleteTrip() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_trip_title)
                .setMessage(R.string.dialog_delete_trip_message)
                .setPositiveButton(R.string.label_delete, (dialog, which) -> deleteTrip())
                .setNegativeButton(R.string.label_cancel, null)
                .show();
    }

    private void deleteTrip() {
        try {
            int rowsDeleted = dbHelper.deleteTrip(tripId);
            if (rowsDeleted > 0) {
                Toast.makeText(this, R.string.msg_trip_deleted, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, R.string.error_deleting_trip, Toast.LENGTH_LONG).show();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting trip id=" + tripId, e);
            Toast.makeText(this, R.string.error_deleting_trip, Toast.LENGTH_LONG).show();
        }
    }
}
