package com.example.tripexpenseplanner;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.adapter.UpcomingActivityAdapter;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.TripActivity;

import java.util.Collections;
import java.util.List;

/**
 * Entry point of the Trip Expense and Planner app — also acts as the main Dashboard.
 * Step 1: Displayed a simple welcome screen.
 * Step 2: Opened the SQLite database on launch so its creation could be verified.
 * Step 3: Shows the Dashboard UI (Add Trip / My Trips / Expenses / Upcoming Activities /
 *         Total Expenses). Trip, expense and activity CRUD screens are not implemented
 *         yet, so the related buttons just show a "coming soon" message for now.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TripExpensePlanner";

    private DatabaseHelper dbHelper;
    private UpcomingActivityAdapter upcomingActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Touching the database here forces SQLiteOpenHelper to create the
        // .db file (and run onCreate/onUpgrade) the first time the app runs.
        dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d(TAG, "Database opened successfully. Version = " + db.getVersion());

        setupActionButtons();
        setupUpcomingActivitiesList();
        NavigationHelper.setup(this, R.id.navHome);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationHelper.resetSelection(this, R.id.navHome);
    }

    /**
     * "Add New Trip" opens the Add Trip screen and "My Trips" opens the trip list.
     * "Expenses" depends on a feature not built yet, so it still just shows a
     * "coming soon" message.
     */
    private void setupActionButtons() {
        Button buttonAddTrip = findViewById(R.id.buttonAddTrip);
        Button buttonMyTrips = findViewById(R.id.buttonMyTrips);
        Button buttonExpenses = findViewById(R.id.buttonExpenses);

        View.OnClickListener comingSoonListener = v ->
                Toast.makeText(MainActivity.this, R.string.msg_feature_coming_soon, Toast.LENGTH_SHORT).show();

        buttonAddTrip.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddTripActivity.class)));
        buttonMyTrips.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MyTripsActivity.class)));
        buttonExpenses.setOnClickListener(comingSoonListener);
    }

    /**
     * Sets up the (currently empty) Upcoming Activities preview list.
     * No activities can exist yet since Trip/Activity creation isn't built,
     * so the empty-state message is shown instead of the RecyclerView.
     */
    private void setupUpcomingActivitiesList() {
        RecyclerView recyclerView = findViewById(R.id.recyclerUpcomingActivities);
        TextView textEmptyActivities = findViewById(R.id.textEmptyActivities);

        upcomingActivityAdapter = new UpcomingActivityAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(upcomingActivityAdapter);

        // No trip exists yet, so there is nothing to query. This starts out empty
        // and will be filled once Trip/Activity features are implemented.
        List<TripActivity> upcomingActivities = Collections.emptyList();
        upcomingActivityAdapter.setActivities(upcomingActivities);

        boolean isEmpty = upcomingActivities.isEmpty();
        textEmptyActivities.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }
}
