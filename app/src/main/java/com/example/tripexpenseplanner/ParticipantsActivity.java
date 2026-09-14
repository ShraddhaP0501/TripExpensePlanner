package com.example.tripexpenseplanner;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.adapter.ParticipantAdapter;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Participant;
import com.example.tripexpenseplanner.model.Trip;

import java.util.List;

/**
 * Lists every participant that belongs to one trip.
 * Lets the user add, edit, and delete participants for that trip.
 * Expense splitting across participants is not implemented here yet.
 */
public class ParticipantsActivity extends AppCompatActivity implements ParticipantAdapter.OnParticipantActionListener {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final String TAG = "ParticipantsActivity";
    private static final long NO_TRIP_ID = -1L;

    private DatabaseHelper dbHelper;
    private ParticipantAdapter participantAdapter;
    private long tripId = NO_TRIP_ID;

    private TextView textParticipantsTitle;
    private RecyclerView recyclerParticipants;
    private TextView textEmptyParticipants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_participants);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_TRIP_ID);

        if (tripId == NO_TRIP_ID) {
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textParticipantsTitle = findViewById(R.id.textParticipantsTitle);
        recyclerParticipants = findViewById(R.id.recyclerParticipants);
        textEmptyParticipants = findViewById(R.id.textEmptyParticipants);

        participantAdapter = new ParticipantAdapter(this);
        recyclerParticipants.setLayoutManager(new LinearLayoutManager(this));
        recyclerParticipants.setAdapter(participantAdapter);

        findViewById(R.id.buttonAddParticipant).setOnClickListener(v -> openAddParticipant());

        updateTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time this screen becomes visible again, so participants
        // added/edited/deleted via Add/Edit Participant are always reflected.
        loadParticipants();
    }

    /**
     * Shows the trip's name in the header, e.g. "Participants: Goa Trip".
     */
    private void updateTitle() {
        Trip trip = dbHelper.getTrip(tripId);
        String label = getString(R.string.title_participants);
        if (trip != null) {
            label = label + ": " + trip.getTripName();
        }
        textParticipantsTitle.setText(label);
        setTitle(label);
    }

    private void loadParticipants() {
        List<Participant> participants = dbHelper.getParticipantsByTrip(tripId);
        participantAdapter.setParticipants(participants);

        boolean isEmpty = participants.isEmpty();
        textEmptyParticipants.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerParticipants.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    private void openAddParticipant() {
        Intent intent = new Intent(this, AddEditParticipantActivity.class);
        intent.putExtra(AddEditParticipantActivity.EXTRA_TRIP_ID, tripId);
        startActivity(intent);
    }

    @Override
    public void onEditParticipant(Participant participant) {
        Intent intent = new Intent(this, AddEditParticipantActivity.class);
        intent.putExtra(AddEditParticipantActivity.EXTRA_TRIP_ID, tripId);
        intent.putExtra(AddEditParticipantActivity.EXTRA_PARTICIPANT_ID, participant.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteParticipant(Participant participant) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_participant_title)
                .setMessage(R.string.dialog_delete_participant_message)
                .setPositiveButton(R.string.label_delete, (dialog, which) -> deleteParticipant(participant))
                .setNegativeButton(R.string.label_cancel, null)
                .show();
    }

    private void deleteParticipant(Participant participant) {
        try {
            int rowsDeleted = dbHelper.deleteParticipant(participant.getId());
            if (rowsDeleted > 0) {
                Toast.makeText(this, R.string.msg_participant_deleted, Toast.LENGTH_SHORT).show();
                loadParticipants();
            } else {
                Toast.makeText(this, R.string.error_deleting_participant, Toast.LENGTH_LONG).show();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting participant id=" + participant.getId(), e);
            Toast.makeText(this, R.string.error_deleting_participant, Toast.LENGTH_LONG).show();
        }
    }
}
