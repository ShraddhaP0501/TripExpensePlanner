package com.example.tripexpenseplanner;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Generic "coming soon" screen used by Trip Details for the Itinerary, Expenses,
 * Participants and Trip Summary sections, which are not implemented yet.
 * Each caller passes the section title ({@link #EXTRA_SCREEN_TITLE}) and the
 * trip id ({@link #EXTRA_TRIP_ID}) it was opened for, so this screen (and later,
 * the real feature screen that replaces it) knows which trip it belongs to.
 */
public class PlaceholderActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    public static final String EXTRA_SCREEN_TITLE = "extra_screen_title";
    private static final long NO_TRIP_ID = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_placeholder);

        String screenTitle = getIntent().getStringExtra(EXTRA_SCREEN_TITLE);
        long tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_TRIP_ID);

        TextView textTitle = findViewById(R.id.textPlaceholderTitle);
        TextView textTripId = findViewById(R.id.textPlaceholderTripId);

        textTitle.setText(screenTitle);
        setTitle(screenTitle);
        textTripId.setText(getString(R.string.format_trip_id, tripId));
    }
}
