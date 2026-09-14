package com.example.tripexpenseplanner;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.adapter.TripAdapter;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Trip;

import java.util.List;

/**
 * Lists every trip stored in SQLite. Tapping a trip opens Trip Details for it.
 * Itinerary and expenses are not shown here yet.
 */
public class MyTripsActivity extends AppCompatActivity implements TripAdapter.OnTripClickListener {

    private DatabaseHelper dbHelper;
    private TripAdapter tripAdapter;

    private RecyclerView recyclerTrips;
    private TextView textEmptyTrips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_trips);

        dbHelper = new DatabaseHelper(this);

        recyclerTrips = findViewById(R.id.recyclerTrips);
        textEmptyTrips = findViewById(R.id.textEmptyTrips);

        tripAdapter = new TripAdapter(this);
        recyclerTrips.setLayoutManager(new LinearLayoutManager(this));
        recyclerTrips.setAdapter(tripAdapter);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        NavigationHelper.setup(this, R.id.navMyTrips);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time this screen becomes visible again, so a trip added,
        // edited or deleted elsewhere (Add Trip / Trip Details) is always reflected.
        loadTrips();
        NavigationHelper.resetSelection(this, R.id.navMyTrips);
    }

    private void loadTrips() {
        List<Trip> trips = dbHelper.getAllTrips();
        tripAdapter.setTrips(trips);

        boolean isEmpty = trips.isEmpty();
        textEmptyTrips.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerTrips.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onTripClick(Trip trip) {
        Intent intent = new Intent(this, TripDetailsActivity.class);
        intent.putExtra(TripDetailsActivity.EXTRA_TRIP_ID, trip.getId());
        startActivity(intent);
    }
}
