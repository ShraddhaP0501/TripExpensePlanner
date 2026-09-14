package com.example.tripexpenseplanner;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Expense;
import com.example.tripexpenseplanner.model.ExpenseParticipant;
import com.example.tripexpenseplanner.model.Participant;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Screen for creating a new Expense for a trip, and also for editing an existing one.
 * Pass {@link #EXTRA_TRIP_ID} (required) and, in edit mode, {@link #EXTRA_EXPENSE_ID}
 * in the launching Intent.
 *
 * As well as Category / Amount / Description / Date, the user picks who paid
 * (from the trip's participants) and which participants share the expense.
 * The amount is split equally among the selected participants and each share
 * is stored as a row in "expense_participants".
 */
public class AddEditExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    public static final String EXTRA_EXPENSE_ID = "extra_expense_id";
    private static final String TAG = "AddEditExpense";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final long NO_ID = -1L;
    /** Position 0 in expense_categories is the "Select Category" placeholder, not a real category. */
    private static final int CATEGORY_PLACEHOLDER_POSITION = 0;
    /** Position 0 of the Paid By spinner is always the "Select Payer" placeholder we add ourselves. */
    private static final int PAYER_PLACEHOLDER_POSITION = 0;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    private DatabaseHelper dbHelper;
    private long tripId = NO_ID;
    /** -1 while creating a new expense; the real expense id while editing one. */
    private long editingExpenseId = NO_ID;

    /** Every participant of this trip, in the same order shown in the Split Among checklist. */
    private List<Participant> tripParticipants = new ArrayList<>();
    /** Maps each Split Among checkbox to the participant it represents. */
    private final Map<CheckBox, Participant> checkBoxParticipants = new LinkedHashMap<>();

    private TextView textFormTitle;
    private Spinner spinnerCategory;
    private TextView textErrorCategory;
    private Spinner spinnerPaidBy;
    private TextView textErrorPaidBy;
    private LinearLayout containerSplitParticipants;
    private TextView textErrorSplitParticipants;
    private TextInputLayout layoutAmount;
    private TextInputLayout layoutExpenseDate;

    private TextInputEditText editAmount;
    private TextInputEditText editExpenseDate;
    private TextInputEditText editExpenseDescription;

    private Button buttonSaveExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_ID);
        editingExpenseId = getIntent().getLongExtra(EXTRA_EXPENSE_ID, NO_ID);

        if (tripId == NO_ID) {
            // An expense must always belong to a trip.
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tripParticipants = dbHelper.getParticipantsByTrip(tripId);
        if (tripParticipants.isEmpty()) {
            // Both "who paid" and "split among" need at least one participant to choose from.
            Toast.makeText(this, R.string.msg_no_participants_for_split, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textFormTitle = findViewById(R.id.textFormTitle);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        textErrorCategory = findViewById(R.id.textErrorCategory);
        spinnerPaidBy = findViewById(R.id.spinnerPaidBy);
        textErrorPaidBy = findViewById(R.id.textErrorPaidBy);
        containerSplitParticipants = findViewById(R.id.containerSplitParticipants);
        textErrorSplitParticipants = findViewById(R.id.textErrorSplitParticipants);
        layoutAmount = findViewById(R.id.layoutAmount);
        layoutExpenseDate = findViewById(R.id.layoutExpenseDate);

        editAmount = findViewById(R.id.editAmount);
        editExpenseDate = findViewById(R.id.editExpenseDate);
        editExpenseDescription = findViewById(R.id.editExpenseDescription);
        buttonSaveExpense = findViewById(R.id.buttonSaveExpense);

        editExpenseDate.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonCancelExpense).setOnClickListener(v -> finish());
        buttonSaveExpense.setOnClickListener(v -> validateAndSaveExpense());

        setupPaidBySpinner();

        if (isEditMode()) {
            setTitle(R.string.title_edit_expense);
            textFormTitle.setText(R.string.title_edit_expense);
            buttonSaveExpense.setText(R.string.label_update_expense);
            prefillFieldsForEdit();
        } else {
            setTitle(R.string.title_add_expense);
            textFormTitle.setText(R.string.title_add_expense);
            // New expense: default to splitting equally among everyone, including the payer.
            buildSplitChecklist(new HashSet<>(participantIds(tripParticipants)));
        }

        NavigationHelper.setup(this, R.id.navMyTrips);
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationHelper.resetSelection(this, R.id.navMyTrips);
    }

    private boolean isEditMode() {
        return editingExpenseId != NO_ID;
    }

    private List<Long> participantIds(List<Participant> participants) {
        List<Long> ids = new ArrayList<>();
        for (Participant participant : participants) {
            ids.add(participant.getId());
        }
        return ids;
    }

    /**
     * Fills the "Paid By" spinner with a placeholder plus every participant's name.
     */
    private void setupPaidBySpinner() {
        List<String> options = new ArrayList<>();
        options.add(getString(R.string.placeholder_select_payer));
        for (Participant participant : tripParticipants) {
            options.add(participant.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPaidBy.setAdapter(adapter);
    }

    /**
     * Adds one checkbox per trip participant to the Split Among section, checking
     * exactly the ones whose id is in {@code checkedParticipantIds}.
     */
    private void buildSplitChecklist(Set<Long> checkedParticipantIds) {
        containerSplitParticipants.removeAllViews();
        checkBoxParticipants.clear();

        for (Participant participant : tripParticipants) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(participant.getName());
            checkBox.setChecked(checkedParticipantIds.contains(participant.getId()));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                    textErrorSplitParticipants.setVisibility(View.GONE));
            containerSplitParticipants.addView(checkBox);
            checkBoxParticipants.put(checkBox, participant);
        }
    }

    /**
     * Loads the existing expense (and its saved splits) and fills the form with
     * its current values.
     */
    private void prefillFieldsForEdit() {
        Expense expense = dbHelper.getExpense(editingExpenseId);
        if (expense == null) {
            Toast.makeText(this, R.string.error_expense_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        selectCategory(expense.getCategory());
        selectPayer(expense.getPaidBy());
        editAmount.setText(String.valueOf(expense.getAmount()));
        editExpenseDate.setText(expense.getExpenseDate());
        editExpenseDescription.setText(expense.getDescription());

        Set<Long> previouslySelectedIds = new HashSet<>();
        for (ExpenseParticipant share : dbHelper.getExpenseParticipantsByExpense(editingExpenseId)) {
            previouslySelectedIds.add(share.getParticipantId());
        }
        buildSplitChecklist(previouslySelectedIds);
    }

    /**
     * Moves the Category spinner's selection to match a category loaded from the database.
     */
    private void selectCategory(String category) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerCategory.getAdapter();
        if (adapter == null || category == null) {
            return;
        }
        int position = adapter.getPosition(category);
        if (position >= 0) {
            spinnerCategory.setSelection(position);
        }
    }

    /**
     * Moves the Paid By spinner's selection to match a participant name loaded from the database.
     */
    private void selectPayer(String paidByName) {
        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerPaidBy.getAdapter();
        if (adapter == null || paidByName == null) {
            return;
        }
        int position = adapter.getPosition(paidByName);
        if (position >= 0) {
            spinnerPaidBy.setSelection(position);
        }
    }

    /**
     * Opens a DatePickerDialog pre-filled with the field's current date (or today,
     * if empty) and writes the chosen date back into the field as "yyyy-MM-dd".
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        String existingValue = textOf(editExpenseDate);
        if (!TextUtils.isEmpty(existingValue)) {
            try {
                calendar.setTime(dateFormat.parse(existingValue));
            } catch (ParseException e) {
                // Ignore and fall back to today's date.
            }
        }

        new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    editExpenseDate.setText(dateFormat.format(selected.getTime()));
                    layoutExpenseDate.setError(null);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    /**
     * Validates every required field, then inserts or updates the expense (and its splits) in SQLite.
     */
    private void validateAndSaveExpense() {
        textErrorCategory.setVisibility(View.GONE);
        textErrorPaidBy.setVisibility(View.GONE);
        textErrorSplitParticipants.setVisibility(View.GONE);
        layoutAmount.setError(null);
        layoutExpenseDate.setError(null);

        String amountText = textOf(editAmount);
        String expenseDate = textOf(editExpenseDate);
        String description = textOf(editExpenseDescription);

        boolean isValid = true;

        // Category is required — position 0 is just the "Select Category" placeholder.
        if (spinnerCategory.getSelectedItemPosition() == CATEGORY_PLACEHOLDER_POSITION) {
            textErrorCategory.setText(R.string.error_category_required);
            textErrorCategory.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Who paid is required — position 0 is the "Select Payer" placeholder.
        if (spinnerPaidBy.getSelectedItemPosition() == PAYER_PLACEHOLDER_POSITION) {
            textErrorPaidBy.setText(R.string.error_paid_by_required);
            textErrorPaidBy.setVisibility(View.VISIBLE);
            isValid = false;
        }

        // Amount is required, must be a valid number, and must be greater than 0.
        double amount = 0;
        if (TextUtils.isEmpty(amountText)) {
            layoutAmount.setError(getString(R.string.error_amount_required));
            isValid = false;
        } else {
            try {
                amount = Double.parseDouble(amountText);
                if (amount <= 0) {
                    layoutAmount.setError(getString(R.string.error_amount_must_be_positive));
                    isValid = false;
                }
            } catch (NumberFormatException e) {
                layoutAmount.setError(getString(R.string.error_amount_invalid));
                isValid = false;
            }
        }

        if (TextUtils.isEmpty(expenseDate)) {
            layoutExpenseDate.setError(getString(R.string.error_expense_date_required));
            isValid = false;
        }

        // At least one participant must share the expense.
        List<Participant> selectedParticipants = new ArrayList<>();
        for (Map.Entry<CheckBox, Participant> entry : checkBoxParticipants.entrySet()) {
            if (entry.getKey().isChecked()) {
                selectedParticipants.add(entry.getValue());
            }
        }
        if (selectedParticipants.isEmpty()) {
            textErrorSplitParticipants.setText(R.string.error_split_participants_required);
            textErrorSplitParticipants.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        String category = (String) spinnerCategory.getSelectedItem();
        String paidBy = (String) spinnerPaidBy.getSelectedItem();

        Expense expense = new Expense(
                tripId,
                category,
                amount,
                paidBy,
                TextUtils.isEmpty(description) ? null : description,
                expenseDate
        );
        if (isEditMode()) {
            expense.setId(editingExpenseId);
        }
        saveExpense(expense, selectedParticipants);
    }

    /**
     * Inserts (or updates, in edit mode) the expense, then (re)saves its equal splits.
     */
    private void saveExpense(Expense expense, List<Participant> selectedParticipants) {
        try {
            long expenseId;
            if (isEditMode()) {
                int rowsUpdated = dbHelper.updateExpense(expense);
                if (rowsUpdated <= 0) {
                    throw new SQLiteException("Update affected 0 rows for expense id: " + expense.getId());
                }
                expenseId = expense.getId();
                // Splits may have changed (amount, payer, or who is included) — replace them from scratch.
                dbHelper.deleteExpenseParticipantsByExpense(expenseId);
                Log.d(TAG, "Expense updated, id = " + expenseId);
                Toast.makeText(this, R.string.msg_expense_updated, Toast.LENGTH_SHORT).show();
            } else {
                long newExpenseId = dbHelper.insertExpense(expense);
                if (newExpenseId == -1) {
                    throw new SQLiteException("Insert returned -1 for expense category: " + expense.getCategory());
                }
                expenseId = newExpenseId;
                Log.d(TAG, "Expense saved with id = " + expenseId + " for trip id = " + tripId);
                Toast.makeText(this, R.string.msg_expense_saved, Toast.LENGTH_SHORT).show();
            }

            saveEqualSplits(expenseId, expense.getAmount(), selectedParticipants);
            finish();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving expense for trip id=" + tripId, e);
            Toast.makeText(this, R.string.error_saving_expense, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Splits {@code totalAmount} equally among {@code selectedParticipants} and stores
     * each participant's share as one "expense_participants" row.
     *
     * Amounts are split using integer cents (not floating point) so the shares always
     * add up to exactly the total: the amount is divided evenly, and any leftover cent
     * (from a total that doesn't divide evenly) is given one-at-a-time to the first
     * few participants, in the order they're listed.
     *
     * Example: ₹1000 split 3 ways -> 100000 cents / 3 = 33333 cents each, remainder 1
     * -> shares are ₹333.34, ₹333.33, ₹333.33 (sum = ₹1000.00 exactly).
     */
    private void saveEqualSplits(long expenseId, double totalAmount, List<Participant> selectedParticipants) {
        int participantCount = selectedParticipants.size();
        long totalCents = Math.round(totalAmount * 100);
        long baseCents = totalCents / participantCount;
        long remainderCents = totalCents % participantCount;

        for (int i = 0; i < participantCount; i++) {
            long shareCents = baseCents + (i < remainderCents ? 1 : 0);
            double shareAmount = shareCents / 100.0;

            Participant participant = selectedParticipants.get(i);
            ExpenseParticipant share = new ExpenseParticipant(expenseId, participant.getId(), shareAmount);
            dbHelper.insertExpenseParticipant(share);
        }
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
