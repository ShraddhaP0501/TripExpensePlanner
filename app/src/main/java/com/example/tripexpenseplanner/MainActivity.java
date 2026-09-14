package com.example.tripexpenseplanner;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.database.DatabaseHelper;

/**
 * Entry point of the Trip Expense and Planner app.
 * Step 1: Displays a simple welcome screen.
 * Step 2: Opens the SQLite database on launch so its creation can be verified
 *         (no UI for trips/expenses yet — that comes in a later step).
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TripExpensePlanner";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Touching the database here forces SQLiteOpenHelper to create the
        // .db file (and run onCreate/onUpgrade) the first time the app runs.
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.d(TAG, "Database opened successfully. Version = " + db.getVersion());
    }
}
