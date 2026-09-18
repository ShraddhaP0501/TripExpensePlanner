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
import com.example.tripexpenseplanner.auth.AuthGuard;
import com.example.tripexpenseplanner.auth.AuthSession;
import com.example.tripexpenseplanner.model.TripActivity;

import java.util.Collections;
import java.util.List;

/**
 * Entry point of the Trip Expense and Planner app — also acts as the main Dashboard.
 * Step 1: Displayed a simple welcome screen.
 * Step 2: Opened the SQLite database on launch so its creation could be verified.
 * Step 3: Shows the Dashboard UI (Add Trip / My Trips / Expenses / Upcoming Activities /
 *         Total Expenses).
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TripExpensePlanner";

    private DatabaseHelper dbHelper;
    private UpcomingActivityAdapter upcomingActivityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AuthGuard.require(this)) {
            finish();
            return;
        }
        setContentView(R.layout.activity_main);

        // Touching the database here forces SQLiteOpenHelper to create the
        // .db file (and run onCreate/onUpgrade) the first time the app runs.
        dbHelper = new DatabaseHelper(this);
        ((TextView) findViewById(R.id.textGreeting)).setText(
            getString(R.string.format_greeting, AuthSession.getUserName(this)));
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d(TAG, "Database opened successfully. Version = " + db.getVersion());

        setupActionButtons();
        findViewById(R.id.buttonLogout).setOnClickListener(v -> confirmLogout());
        findViewById(R.id.buttonProfile).setOnClickListener(v ->
            startActivity(new Intent(this, ProfileActivity.class)));
        setupUpcomingActivitiesList();
        setupFeaturePreviews();
        NavigationHelper.setup(this, R.id.navHome);
    }

    private void setupFeaturePreviews() {
        View.OnClickListener listener = v -> new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_feature_title)
                .setMessage(R.string.dialog_feature_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        findViewById(R.id.cardCollaboration).setOnClickListener(listener);
        findViewById(R.id.cardMaps).setOnClickListener(listener);
        findViewById(R.id.cardCurrency).setOnClickListener(listener);
    }

    private void confirmLogout() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.label_logout, (dialog, which) -> {
                    AuthSession.clear(this);
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationHelper.resetSelection(this, R.id.navHome);
    }

    /**
    * "Add New Trip" opens the Add Trip screen, while trip-scoped expenses are
    * reached through the trip list.
     */
    private void setupActionButtons() {
        Button buttonAddTrip = findViewById(R.id.buttonAddTrip);
        Button buttonMyTrips = findViewById(R.id.buttonMyTrips);
        Button buttonExpenses = findViewById(R.id.buttonExpenses);

        buttonAddTrip.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddTripActivity.class)));
        buttonMyTrips.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MyTripsActivity.class)));
        buttonExpenses.setOnClickListener(v ->
            startActivity(new Intent(MainActivity.this, MyTripsActivity.class)));
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
