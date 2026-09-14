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

import com.example.tripexpenseplanner.adapter.ExpenseAdapter;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Expense;
import com.example.tripexpenseplanner.model.Trip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

/**
 * Lists every expense that belongs to one trip, and shows their total.
 * Lets the user add, edit, and delete expenses for that trip.
 * Each expense's payer and equal split among participants is set up in
 * AddEditExpenseActivity; the resulting balances are shown on Trip Summary.
 */
public class ExpensesActivity extends AppCompatActivity implements ExpenseAdapter.OnExpenseActionListener {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final String TAG = "ExpensesActivity";
    private static final long NO_TRIP_ID = -1L;

    private DatabaseHelper dbHelper;
    private ExpenseAdapter expenseAdapter;
    private long tripId = NO_TRIP_ID;

    private TextView textExpensesTitle;
    private TextView textTotalExpense;
    private RecyclerView recyclerExpenses;
    private TextView textEmptyExpenses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_TRIP_ID);

        if (tripId == NO_TRIP_ID) {
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textExpensesTitle = findViewById(R.id.textExpensesTitle);
        textTotalExpense = findViewById(R.id.textTotalExpense);
        recyclerExpenses = findViewById(R.id.recyclerExpenses);
        textEmptyExpenses = findViewById(R.id.textEmptyExpenses);

        expenseAdapter = new ExpenseAdapter(this);
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerExpenses.setAdapter(expenseAdapter);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());
        findViewById(R.id.buttonAddExpense).setOnClickListener(v -> openAddExpense());

        updateTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload every time this screen becomes visible again, so expenses
        // added/edited/deleted via Add/Edit Expense are always reflected,
        // and the total stays accurate.
        loadExpenses();
    }

    /**
     * Shows the trip's name in the header, e.g. "Expenses: Goa Trip".
     */
    private void updateTitle() {
        Trip trip = dbHelper.getTrip(tripId);
        String label = getString(R.string.title_expenses);
        if (trip != null) {
            label = label + ": " + trip.getTripName();
        }
        textExpensesTitle.setText(label);
        setTitle(label);
    }

    private void loadExpenses() {
        List<Expense> expenses = dbHelper.getExpensesByTrip(tripId);
        expenseAdapter.setExpenses(expenses);

        boolean isEmpty = expenses.isEmpty();
        textEmptyExpenses.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerExpenses.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

        double total = dbHelper.getTotalExpenseForTrip(tripId);
        textTotalExpense.setText(getString(R.string.format_amount, total));
    }

    private void openAddExpense() {
        Intent intent = new Intent(this, AddEditExpenseActivity.class);
        intent.putExtra(AddEditExpenseActivity.EXTRA_TRIP_ID, tripId);
        startActivity(intent);
    }

    @Override
    public void onEditExpense(Expense expense) {
        Intent intent = new Intent(this, AddEditExpenseActivity.class);
        intent.putExtra(AddEditExpenseActivity.EXTRA_TRIP_ID, tripId);
        intent.putExtra(AddEditExpenseActivity.EXTRA_EXPENSE_ID, expense.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteExpense(Expense expense) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_delete_expense_title)
                .setMessage(R.string.dialog_delete_expense_message)
                .setPositiveButton(R.string.label_delete, (dialog, which) -> deleteExpense(expense))
                .setNegativeButton(R.string.label_cancel, null)
                .show();
    }

    private void deleteExpense(Expense expense) {
        try {
            // Splits reference this expense via a foreign key — they must go first,
            // otherwise SQLite refuses to delete the expense they still point to.
            dbHelper.deleteExpenseParticipantsByExpense(expense.getId());
            int rowsDeleted = dbHelper.deleteExpense(expense.getId());
            if (rowsDeleted > 0) {
                Toast.makeText(this, R.string.msg_expense_deleted, Toast.LENGTH_SHORT).show();
                loadExpenses();
            } else {
                Toast.makeText(this, R.string.error_deleting_expense, Toast.LENGTH_LONG).show();
            }
        } catch (SQLiteException e) {
            Log.e(TAG, "Error deleting expense id=" + expense.getId(), e);
            Toast.makeText(this, R.string.error_deleting_expense, Toast.LENGTH_LONG).show();
        }
    }
}
