package com.example.tripexpenseplanner;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Participant;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * Screen for adding a new participant to a trip, and also for editing an existing one.
 * Pass {@link #EXTRA_TRIP_ID} (required) and, in edit mode, {@link #EXTRA_PARTICIPANT_ID}
 * in the launching Intent.
 * Validates the input, then inserts/updates one row in the "participants" table.
 */
public class AddEditParticipantActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    public static final String EXTRA_PARTICIPANT_ID = "extra_participant_id";
    private static final String TAG = "AddEditParticipant";
    private static final long NO_ID = -1L;

    private DatabaseHelper dbHelper;
    private long tripId = NO_ID;
    /** -1 while creating a new participant; the real participant id while editing one. */
    private long editingParticipantId = NO_ID;

    private TextView textFormTitle;
    private TextInputLayout layoutParticipantName;
    private TextInputEditText editParticipantName;
    private Button buttonSaveParticipant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_participant);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_ID);
        editingParticipantId = getIntent().getLongExtra(EXTRA_PARTICIPANT_ID, NO_ID);

        if (tripId == NO_ID) {
            // A participant must always belong to a trip.
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textFormTitle = findViewById(R.id.textFormTitle);
        layoutParticipantName = findViewById(R.id.layoutParticipantName);
        editParticipantName = findViewById(R.id.editParticipantName);
        buttonSaveParticipant = findViewById(R.id.buttonSaveParticipant);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonCancelParticipant).setOnClickListener(v -> finish());
        buttonSaveParticipant.setOnClickListener(v -> validateAndSaveParticipant());

        if (isEditMode()) {
            setTitle(R.string.title_edit_participant);
            textFormTitle.setText(R.string.title_edit_participant);
            buttonSaveParticipant.setText(R.string.label_update_participant);
            prefillFieldsForEdit();
        } else {
            setTitle(R.string.title_add_participant);
            textFormTitle.setText(R.string.title_add_participant);
        }
    }

    private boolean isEditMode() {
        return editingParticipantId != NO_ID;
    }

    /**
     * Loads the existing participant and fills the form with its current value.
     */
    private void prefillFieldsForEdit() {
        Participant participant = dbHelper.getParticipant(editingParticipantId);
        if (participant == null) {
            Toast.makeText(this, R.string.error_participant_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        editParticipantName.setText(participant.getName());
    }

    /**
     * Validates the required field, then inserts or updates the participant in SQLite.
     */
    private void validateAndSaveParticipant() {
        layoutParticipantName.setError(null);

        String name = textOf(editParticipantName);

        if (TextUtils.isEmpty(name)) {
            layoutParticipantName.setError(getString(R.string.error_participant_name_required));
            return;
        }

        Participant participant = new Participant(tripId, name);
        if (isEditMode()) {
            participant.setId(editingParticipantId);
        }
        saveParticipant(participant);
    }

    /**
     * Inserts (or updates, in edit mode) the participant and reports the result to the user.
     */
    private void saveParticipant(Participant participant) {
        try {
            if (isEditMode()) {
                int rowsUpdated = dbHelper.updateParticipant(participant);
                if (rowsUpdated <= 0) {
                    throw new SQLiteException("Update affected 0 rows for participant id: " + participant.getId());
                }
                Log.d(TAG, "Participant updated, id = " + participant.getId());
                Toast.makeText(this, R.string.msg_participant_updated, Toast.LENGTH_SHORT).show();
            } else {
                long newParticipantId = dbHelper.insertParticipant(participant);
                if (newParticipantId == -1) {
                    throw new SQLiteException("Insert returned -1 for participant: " + participant.getName());
                }
                Log.d(TAG, "Participant saved with id = " + newParticipantId + " for trip id = " + tripId);
                Toast.makeText(this, R.string.msg_participant_saved, Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving participant for trip id=" + tripId, e);
            Toast.makeText(this, R.string.error_saving_participant, Toast.LENGTH_LONG).show();
        }
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
