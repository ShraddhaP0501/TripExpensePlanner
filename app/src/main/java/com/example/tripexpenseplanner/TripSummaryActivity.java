package com.example.tripexpenseplanner;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.adapter.ParticipantBalanceAdapter;
import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.CategoryTotal;
import com.example.tripexpenseplanner.model.Expense;
import com.example.tripexpenseplanner.model.ExpenseParticipant;
import com.example.tripexpenseplanner.model.Participant;
import com.example.tripexpenseplanner.model.ParticipantBalance;
import com.example.tripexpenseplanner.model.SettlementEntry;
import com.example.tripexpenseplanner.model.Trip;
import com.example.tripexpenseplanner.model.TripActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shows a complete read-only summary of one trip, built entirely from data
 * already in SQLite — no external APIs are used:
 *
 * - Trip Overview: name, destination, start/end date, duration.
 * - Statistics: number of activities, number of expenses, total expense,
 *   number of participants.
 * - Category-wise expense totals.
 * - Balances (paid vs. share) and a simplified "who owes whom" settlement list.
 *
 * Everything is recalculated fresh from the "trips", "activities", "expenses",
 * "expense_participants" and "participants" tables each time the screen is shown.
 */
public class TripSummaryActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final long NO_TRIP_ID = -1L;
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN, Locale.US);

    private DatabaseHelper dbHelper;
    private long tripId = NO_TRIP_ID;

    private TextView textSummaryTitle;

    // Trip Overview
    private TextView textOverviewTripName;
    private TextView textOverviewDestination;
    private TextView textOverviewDates;
    private TextView textOverviewDuration;

    // Statistics
    private TextView textActivityCount;
    private TextView textExpenseCount;
    private TextView textParticipantCount;
    private TextView textOverviewTotalExpense;

    // Category-wise expenses
    private TextView textNoCategoryTotals;
    private LinearLayout containerCategoryTotals;

    // Balances / settlement
    private TextView textNoSummaryData;
    private LinearLayout containerSummaryContent;
    private RecyclerView recyclerBalances;
    private TextView textAllSettled;
    private LinearLayout containerSettlements;

    private ParticipantBalanceAdapter balanceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_summary);

        dbHelper = new DatabaseHelper(this);
        tripId = getIntent().getLongExtra(EXTRA_TRIP_ID, NO_TRIP_ID);

        if (tripId == NO_TRIP_ID) {
            Toast.makeText(this, R.string.error_trip_not_found, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        textSummaryTitle = findViewById(R.id.textSummaryTitle);

        textOverviewTripName = findViewById(R.id.textOverviewTripName);
        textOverviewDestination = findViewById(R.id.textOverviewDestination);
        textOverviewDates = findViewById(R.id.textOverviewDates);
        textOverviewDuration = findViewById(R.id.textOverviewDuration);

        textActivityCount = findViewById(R.id.textActivityCount);
        textExpenseCount = findViewById(R.id.textExpenseCount);
        textParticipantCount = findViewById(R.id.textParticipantCount);
        textOverviewTotalExpense = findViewById(R.id.textOverviewTotalExpense);

        textNoCategoryTotals = findViewById(R.id.textNoCategoryTotals);
        containerCategoryTotals = findViewById(R.id.containerCategoryTotals);

        textNoSummaryData = findViewById(R.id.textNoSummaryData);
        containerSummaryContent = findViewById(R.id.containerSummaryContent);
        recyclerBalances = findViewById(R.id.recyclerBalances);
        textAllSettled = findViewById(R.id.textAllSettled);
        containerSettlements = findViewById(R.id.containerSettlements);

        balanceAdapter = new ParticipantBalanceAdapter();
        recyclerBalances.setLayoutManager(new LinearLayoutManager(this));
        recyclerBalances.setAdapter(balanceAdapter);

        findViewById(R.id.buttonBack).setOnClickListener(v -> finish());

        updateTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recalculate every time this screen becomes visible, so it always reflects
        // the latest trip details, activities, expenses, splits, and participants.
        loadSummary();
    }

    private void updateTitle() {
        Trip trip = dbHelper.getTrip(tripId);
        String label = getString(R.string.title_trip_summary);
        if (trip != null) {
            label = label + ": " + trip.getTripName();
        }
        textSummaryTitle.setText(label);
        setTitle(label);
    }

    private void loadSummary() {
        Trip trip = dbHelper.getTrip(tripId);
        List<Participant> participants = dbHelper.getParticipantsByTrip(tripId);
        List<TripActivity> activities = dbHelper.getActivitiesByTrip(tripId);
        List<Expense> expenses = dbHelper.getExpensesByTrip(tripId);
        double totalExpense = dbHelper.getTotalExpenseForTrip(tripId);
        List<CategoryTotal> categoryTotals = dbHelper.getCategoryWiseExpenseTotals(tripId);

        renderOverview(trip);
        renderStatistics(activities.size(), expenses.size(), participants.size(), totalExpense);
        renderCategoryTotals(categoryTotals);
        renderBalancesAndSettlements(participants, expenses);
    }

    /**
     * Trip Overview section: name, destination, date range and duration —
     * all read straight from the "trips" table, no calculation involved except
     * the day count.
     */
    private void renderOverview(Trip trip) {
        if (trip == null) {
            return;
        }
        textOverviewTripName.setText(getString(R.string.format_trip_name, trip.getTripName()));
        textOverviewDestination.setText(getString(R.string.format_destination, trip.getDestination()));
        textOverviewDates.setText(getString(R.string.format_date_range, trip.getStartDate(), trip.getEndDate()));

        Integer durationDays = computeDurationInDays(trip.getStartDate(), trip.getEndDate());
        if (durationDays != null) {
            textOverviewDuration.setText(getString(R.string.format_duration_days, durationDays));
        } else {
            textOverviewDuration.setText(R.string.msg_duration_unavailable);
        }
    }

    /**
     * Trip duration in days, counting both the start day and the end day
     * (e.g. 2026-10-10 to 2026-10-15 is 6 days). Returns null if either date
     * can't be parsed, so the UI can show "not available" instead of crashing.
     */
    private Integer computeDurationInDays(String startDate, String endDate) {
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            if (start == null || end == null) {
                return null;
            }
            long diffMillis = end.getTime() - start.getTime();
            long diffDays = diffMillis / (24L * 60 * 60 * 1000);
            return (int) diffDays + 1;
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * Statistics section: simple counts (activities, expenses, participants)
     * plus the total expense already computed by DatabaseHelper's SUM() query.
     */
    private void renderStatistics(int activityCount, int expenseCount, int participantCount, double totalExpense) {
        textActivityCount.setText(getString(R.string.format_activity_count, activityCount));
        textExpenseCount.setText(getString(R.string.format_expense_count, expenseCount));
        textParticipantCount.setText(getString(R.string.format_participant_count, participantCount));

        String totalText = getString(R.string.format_amount, totalExpense);
        textOverviewTotalExpense.setText(getString(R.string.format_total_expense_label, totalText));
    }

    /**
     * Category-wise Expenses section: one line per category, from a SQL
     * GROUP BY + SUM() query (DatabaseHelper.getCategoryWiseExpenseTotals).
     */
    private void renderCategoryTotals(List<CategoryTotal> categoryTotals) {
        containerCategoryTotals.removeAllViews();

        if (categoryTotals.isEmpty()) {
            textNoCategoryTotals.setVisibility(View.VISIBLE);
            return;
        }
        textNoCategoryTotals.setVisibility(View.GONE);

        for (CategoryTotal categoryTotal : categoryTotals) {
            TextView line = new TextView(this);
            String amountText = getString(R.string.format_amount, categoryTotal.getTotalAmount());
            line.setText(getString(R.string.format_category_amount, categoryTotal.getCategory(), amountText));
            line.setTextColor(getColor(R.color.textPrimary));
            line.setTextSize(15f);
            line.setPadding(0, 0, 0, dpToPx(6));
            containerCategoryTotals.addView(line);
        }
    }

    /**
     * Balances + Who Owes Whom section: needs at least one participant to mean
     * anything, so it shows its own message when the trip has none yet.
     */
    private void renderBalancesAndSettlements(List<Participant> participants, List<Expense> expenses) {
        if (participants.isEmpty()) {
            textNoSummaryData.setVisibility(View.VISIBLE);
            containerSummaryContent.setVisibility(View.GONE);
            return;
        }
        textNoSummaryData.setVisibility(View.GONE);
        containerSummaryContent.setVisibility(View.VISIBLE);

        List<ParticipantBalance> balances = computeBalances(participants, expenses);
        balanceAdapter.setBalances(balances);

        List<SettlementEntry> settlements = computeSettlements(balances);
        renderSettlements(settlements);
    }

    /**
     * Builds one {@link ParticipantBalance} per trip participant: how much they
     * paid in total across every expense, versus their total share of those
     * expenses (from expense_participants).
     *
     * Amounts are accumulated as integer cents (not floating point) while summing,
     * so repeated addition across many expenses never drifts away from an exact
     * multiple of one paisa/cent — only the final totals are converted back to
     * rupees (double) for display.
     */
    private List<ParticipantBalance> computeBalances(List<Participant> participants, List<Expense> expenses) {
        Map<Long, Long> totalPaidCents = new HashMap<>();
        Map<Long, Long> totalShareCents = new HashMap<>();
        Map<String, Long> participantIdByName = new HashMap<>();
        for (Participant participant : participants) {
            totalPaidCents.put(participant.getId(), 0L);
            totalShareCents.put(participant.getId(), 0L);
            participantIdByName.put(participant.getName(), participant.getId());
        }

        for (Expense expense : expenses) {
            Long payerId = participantIdByName.get(expense.getPaidBy());
            if (payerId != null) {
                long amountCents = Math.round(expense.getAmount() * 100);
                totalPaidCents.put(payerId, totalPaidCents.get(payerId) + amountCents);
            }

            List<ExpenseParticipant> shares = dbHelper.getExpenseParticipantsByExpense(expense.getId());
            for (ExpenseParticipant share : shares) {
                Long participantId = share.getParticipantId();
                if (totalShareCents.containsKey(participantId)) {
                    long shareCents = Math.round(share.getShareAmount() * 100);
                    totalShareCents.put(participantId, totalShareCents.get(participantId) + shareCents);
                }
            }
        }

        List<ParticipantBalance> balances = new ArrayList<>();
        for (Participant participant : participants) {
            double paid = totalPaidCents.get(participant.getId()) / 100.0;
            double share = totalShareCents.get(participant.getId()) / 100.0;
            balances.add(new ParticipantBalance(participant.getId(), participant.getName(), paid, share));
        }
        return balances;
    }

    /**
     * Turns the list of overall balances into a simple "who owes whom" list using
     * a straightforward greedy match: the person owed the most is paid first by
     * the person who owes the most, and so on until every balance is settled.
     * This keeps the number of payments small without needing a complex algorithm.
     *
     * Every amount is tracked as integer cents rather than a decimal, so a balance
     * can only ever reach exactly zero — never left dangling a fraction of a paisa
     * away from zero the way repeated floating-point subtraction could.
     */
    private List<SettlementEntry> computeSettlements(List<ParticipantBalance> balances) {
        List<MutableCents> creditors = new ArrayList<>();
        List<MutableCents> debtors = new ArrayList<>();

        for (ParticipantBalance balance : balances) {
            long netCents = Math.round(balance.getBalance() * 100);
            if (netCents > 0) {
                creditors.add(new MutableCents(balance.getParticipantName(), netCents));
            } else if (netCents < 0) {
                debtors.add(new MutableCents(balance.getParticipantName(), -netCents));
            }
        }

        creditors.sort((a, b) -> Long.compare(b.cents, a.cents));
        debtors.sort((a, b) -> Long.compare(b.cents, a.cents));

        List<SettlementEntry> settlements = new ArrayList<>();
        int creditorIndex = 0;
        int debtorIndex = 0;

        while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
            MutableCents debtor = debtors.get(debtorIndex);
            MutableCents creditor = creditors.get(creditorIndex);

            long settleCents = Math.min(debtor.cents, creditor.cents);
            settlements.add(new SettlementEntry(debtor.name, creditor.name, settleCents / 100.0));

            debtor.cents -= settleCents;
            creditor.cents -= settleCents;

            if (debtor.cents == 0) {
                debtorIndex++;
            }
            if (creditor.cents == 0) {
                creditorIndex++;
            }
        }

        return settlements;
    }

    /**
     * Draws the settlement list as one line per entry, or an "all settled" message.
     */
    private void renderSettlements(List<SettlementEntry> settlements) {
        containerSettlements.removeAllViews();

        if (settlements.isEmpty()) {
            textAllSettled.setVisibility(View.VISIBLE);
            return;
        }
        textAllSettled.setVisibility(View.GONE);

        for (SettlementEntry entry : settlements) {
            TextView line = new TextView(this);
            String amountText = getString(R.string.format_amount, entry.getAmount());
            line.setText(getString(R.string.format_settlement, entry.getFromName(), entry.getToName(), amountText));
            line.setTextColor(getColor(R.color.textPrimary));
            line.setTextSize(16f);
            line.setPadding(0, 0, 0, dpToPx(8));
            containerSettlements.addView(line);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    /** A mutable running balance (in integer cents) used only while computing settlements. */
    private static class MutableCents {
        final String name;
        long cents;

        MutableCents(String name, long cents) {
            this.name = name;
            this.cents = cents;
        }
    }
}
