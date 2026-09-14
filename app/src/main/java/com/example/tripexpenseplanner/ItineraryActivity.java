package com.example.tripexpenseplanner;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.adapter.TripActivityAdapter;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Trip;
import com.example.tripexpenseplanner.model.TripActivity;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Lists every activity (itinerary item) that belongs to one trip, ordered by
 * date and time. Lets the user add, edit, and delete activities for that trip.
 * Expenses for the trip are not shown here yet.
 */
public class ItineraryActivity extends AppCompatActivity implements TripActivityAdapter.OnActivityActionListener {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final String TAG = "ItineraryActivity";
    private static final long NO_TRIP_ID = -1L;

    private DatabaseHelper dbHelper;
    private TripActivityAdapter activityAdapter;
    private long tripId = NO_TRIP_ID;

    private TextView textItineraryTitle;
    private RecyclerView recyclerActivities;
    private TextView textEmptyActivities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_TRIP_ID);

        if (tripId == NO_TRIP_ID) {
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textItineraryTitle = findViewById(R.id.textItineraryTitle);
        recyclerActivities = findViewById(R.id.recyclerActivities);
        textEmptyActivities = findViewById(R.id.textEmptyActivities);

        activityAdapter = new TripActivityAdapter(this);
        recyclerActivities.setLayoutManager(new LinearLayoutManager(this));
        recyclerActivities.setAdapter(activityAdapter);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonAddActivity).setOnClickListener(v -> openAddActivity());

        updateTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time this screen becomes visible again, so activities
        // added/edited/deleted via Add/Edit Activity are always reflected.
        loadActivities();
    }

    /**
     * Shows the trip's name in the header, e.g. "Itinerary: Goa Trip", so it's
     * clear which trip this screen belongs to.
     */
    private void updateTitle() {
        Trip trip = dbHelper.getTrip(tripId);
        String label = getString(R.string.label_itinerary);
        if (trip != null) {
            label = label + ": " + trip.getTripName();
        }
        textItineraryTitle.setText(label);
        setTitle(label);
    }

    private void loadActivities() {
        List<TripActivity> activities = dbHelper.getActivitiesByTrip(tripId);
        activityAdapter.setActivities(activities);

        boolean isEmpty = activities.isEmpty();
        textEmptyActivities.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerActivities.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openAddActivity() {
        Intent intent = new Intent(this, AddEditActivityActivity.class);
        intent.putExtra(AddEditActivityActivity.EXTRA_TRIP_ID, tripId);
        startActivity(intent);
    }

    @Override
    public void onEditActivity(TripActivity activity) {
        Intent intent = new Intent(this, AddEditActivityActivity.class);
        intent.putExtra(AddEditActivityActivity.EXTRA_TRIP_ID, tripId);
        intent.putExtra(AddEditActivityActivity.EXTRA_ACTIVITY_ID, activity.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteActivity(TripActivity activity) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_activity_title)
                .setMessage(R.string.dialog_delete_activity_message)
                .setPositiveButton(R.string.label_delete, (dialog, which) -> deleteActivity(activity))
                .setNegativeButton(R.string.label_cancel, null)
                .show();
    }

    private void deleteActivity(TripActivity activity) {
        try {
            int rowsDeleted = dbHelper.deleteActivity(activity.getId());
            if (rowsDeleted > 0) {
                Toast.makeText(this, R.string.msg_activity_deleted, Toast.LENGTH_SHORT).show();
                loadActivities();
            } else {
                Toast.makeText(this, R.string.error_deleting_activity, Toast.LENGTH_LONG).show();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting activity id=" + activity.getId(), e);
            Toast.makeText(this, R.string.error_deleting_activity, Toast.LENGTH_LONG).show();
        }
    }
}
