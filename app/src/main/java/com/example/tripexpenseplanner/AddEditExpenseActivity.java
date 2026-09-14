package com.example.tripexpenseplanner;

import android.app.DatePickerDialog;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Expense;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Screen for creating a new Expense for a trip, and also for editing an existing one.
 * Pass {@link #EXTRA_TRIP_ID} (required) and, in edit mode, {@link #EXTRA_EXPENSE_ID}
 * in the launching Intent.
 * Validates the input, then inserts/updates one row in the "expenses" table.
 */
public class AddEditExpenseActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    public static final String EXTRA_EXPENSE_ID = "extra_expense_id";
    private static final String TAG = "AddEditExpense";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final long NO_ID = -1L;
    /** Position 0 in expense_categories is the "Select Category" placeholder, not a real category. */
    private static final int CATEGORY_PLACEHOLDER_POSITION = 0;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    private DatabaseHelper dbHelper;
    private long tripId = NO_ID;
    /** -1 while creating a new expense; the real expense id while editing one. */
    private long editingExpenseId = NO_ID;

    private TextView textFormTitle;
    private Spinner spinnerCategory;
    private TextView textErrorCategory;
    private TextInputLayout layoutAmount;
    private TextInputLayout layoutPaidBy;
    private TextInputLayout layoutExpenseDate;

    private TextInputEditText editAmount;
    private TextInputEditText editPaidBy;
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

        textFormTitle = findViewById(R.id.textFormTitle);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        textErrorCategory = findViewById(R.id.textErrorCategory);
        layoutAmount = findViewById(R.id.layoutAmount);
        layoutPaidBy = findViewById(R.id.layoutPaidBy);
        layoutExpenseDate = findViewById(R.id.layoutExpenseDate);

        editAmount = findViewById(R.id.editAmount);
        editPaidBy = findViewById(R.id.editPaidBy);
        editExpenseDate = findViewById(R.id.editExpenseDate);
        editExpenseDescription = findViewById(R.id.editExpenseDescription);
        buttonSaveExpense = findViewById(R.id.buttonSaveExpense);

        editExpenseDate.setOnClickListener(v -> showDatePicker());

        findViewById(R.id.buttonCancelExpense).setOnClickListener(v -> finish());
        buttonSaveExpense.setOnClickListener(v -> validateAndSaveExpense());

        if (isEditMode()) {
            setTitle(R.string.title_edit_expense);
            textFormTitle.setText(R.string.title_edit_expense);
            buttonSaveExpense.setText(R.string.label_update_expense);
            prefillFieldsForEdit();
        } else {
            setTitle(R.string.title_add_expense);
            textFormTitle.setText(R.string.title_add_expense);
        }
    }

    private boolean isEditMode() {
        return editingExpenseId != NO_ID;
    }

    /**
     * Loads the existing expense and fills the form with its current values.
     */
    private void prefillFieldsForEdit() {
        Expense expense = dbHelper.getExpense(editingExpenseId);
        if (expense == null) {
            Toast.makeText(this, R.string.error_expense_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        selectCategory(expense.getCategory());
        editAmount.setText(String.valueOf(expense.getAmount()));
        editPaidBy.setText(expense.getPaidBy());
        editExpenseDate.setText(expense.getExpenseDate());
        editExpenseDescription.setText(expense.getDescription());
    }

    /**
     * Moves the Spinner's selection to match a category loaded from the database.
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
     * Validates every required field, then inserts or updates the expense in SQLite.
     */
    private void validateAndSaveExpense() {
        textErrorCategory.setVisibility(View.GONE);
        layoutAmount.setError(null);
        layoutPaidBy.setError(null);
        layoutExpenseDate.setError(null);

        String amountText = textOf(editAmount);
        String paidBy = textOf(editPaidBy);
        String expenseDate = textOf(editExpenseDate);
        String description = textOf(editExpenseDescription);

        boolean isValid = true;

        // Category is required — position 0 is just the "Select Category" placeholder.
        if (spinnerCategory.getSelectedItemPosition() == CATEGORY_PLACEHOLDER_POSITION) {
            textErrorCategory.setText(R.string.error_category_required);
            textErrorCategory.setVisibility(View.VISIBLE);
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

        if (TextUtils.isEmpty(paidBy)) {
            layoutPaidBy.setError(getString(R.string.error_paid_by_required));
            isValid = false;
        }
        if (TextUtils.isEmpty(expenseDate)) {
            layoutExpenseDate.setError(getString(R.string.error_expense_date_required));
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        String category = (String) spinnerCategory.getSelectedItem();
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
        saveExpense(expense);
    }

    /**
     * Inserts (or updates, in edit mode) the expense and reports the result to the user.
     */
    private void saveExpense(Expense expense) {
        try {
            if (isEditMode()) {
                int rowsUpdated = dbHelper.updateExpense(expense);
                if (rowsUpdated <= 0) {
                    throw new SQLiteException("Update affected 0 rows for expense id: " + expense.getId());
                }
                Log.d(TAG, "Expense updated, id = " + expense.getId());
                Toast.makeText(this, R.string.msg_expense_updated, Toast.LENGTH_SHORT).show();
            } else {
                long newExpenseId = dbHelper.insertExpense(expense);
                if (newExpenseId == -1) {
                    throw new SQLiteException("Insert returned -1 for expense category: " + expense.getCategory());
                }
                Log.d(TAG, "Expense saved with id = " + newExpenseId + " for trip id = " + tripId);
                Toast.makeText(this, R.string.msg_expense_saved, Toast.LENGTH_SHORT).show();
            }
            finish();
        } catch (SQLiteException e) {
            Log.e(TAG, "Error saving expense for trip id=" + tripId, e);
            Toast.makeText(this, R.string.error_saving_expense, Toast.LENGTH_LONG).show();
        }
    }

    private String textOf(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
