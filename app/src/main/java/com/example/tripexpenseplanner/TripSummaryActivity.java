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
import com.example.tripexpenseplanner.model.Expense;
import com.example.tripexpenseplanner.model.ExpenseParticipant;
import com.example.tripexpenseplanner.model.Participant;
import com.example.tripexpenseplanner.model.ParticipantBalance;
import com.example.tripexpenseplanner.model.SettlementEntry;
import com.example.tripexpenseplanner.model.Trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shows, for one trip: how much each participant paid versus their share of
 * every expense, their overall balance, and a simplified "who owes whom" list
 * so the group can settle up. Everything here is calculated fresh from the
 * "expenses" and "expense_participants" tables each time the screen is shown —
 * nothing on this screen is stored on its own.
 */
public class TripSummaryActivity extends AppCompatActivity {

    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    private static final long NO_TRIP_ID = -1L;

    private DatabaseHelper dbHelper;
    private long tripId = NO_TRIP_ID;

    private TextView textSummaryTitle;
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
        textNoSummaryData = findViewById(R.id.textNoSummaryData);
        containerSummaryContent = findViewById(R.id.containerSummaryContent);
        recyclerBalances = findViewById(R.id.recyclerBalances);
        textAllSettled = findViewById(R.id.textAllSettled);
        containerSettlements = findViewById(R.id.containerSettlements);

        balanceAdapter = new ParticipantBalanceAdapter();
        recyclerBalances.setLayoutManager(new LinearLayoutManager(this));
        recyclerBalances.setAdapter(balanceAdapter);

        updateTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recalculate every time this screen becomes visible, so it always reflects
        // the latest expenses, splits, and participants.
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
        List<ParticipantBalance> balances = computeBalances();

        if (balances.isEmpty()) {
            textNoSummaryData.setVisibility(View.VISIBLE);
            containerSummaryContent.setVisibility(View.GONE);
            return;
        }
        textNoSummaryData.setVisibility(View.GONE);
        containerSummaryContent.setVisibility(View.VISIBLE);

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
    private List<ParticipantBalance> computeBalances() {
        List<Participant> participants = dbHelper.getParticipantsByTrip(tripId);
        List<Expense> expenses = dbHelper.getExpensesByTrip(tripId);

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
