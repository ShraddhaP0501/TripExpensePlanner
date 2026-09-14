package com.example.tripexpenseplanner.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.R;
import com.example.tripexpenseplanner.model.ParticipantBalance;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays each participant's Paid / Share / Balance on the Trip Summary screen.
 * Read-only — no edit or delete actions, since these rows are calculated, not stored.
 */
public class ParticipantBalanceAdapter extends RecyclerView.Adapter<ParticipantBalanceAdapter.BalanceViewHolder> {

    /** Anything smaller than this (in rupees) is treated as "settled up" to avoid rounding noise. */
    private static final double SETTLED_THRESHOLD = 0.004;

    private static final int COLOR_POSITIVE = Color.parseColor("#2E7D32");
    private static final int COLOR_NEGATIVE = Color.parseColor("#C62828");
    private static final int COLOR_SETTLED = Color.parseColor("#757575");

    private List<ParticipantBalance> balances = new ArrayList<>();

    public void setBalances(List<ParticipantBalance> balances) {
        this.balances = balances != null ? balances : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BalanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant_balance, parent, false);
        return new BalanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BalanceViewHolder holder, int position) {
        ParticipantBalance balance = balances.get(position);
        android.content.Context context = holder.itemView.getContext();

        holder.textParticipantName.setText(balance.getParticipantName());
        holder.textPaidAmount.setText(
                context.getString(R.string.label_paid_amount) + ": " +
                        context.getString(R.string.format_amount, balance.getTotalPaid()));
        holder.textShareAmount.setText(
                context.getString(R.string.label_share_amount) + ": " +
                        context.getString(R.string.format_amount, balance.getTotalShare()));

        double net = balance.getBalance();
        if (net > SETTLED_THRESHOLD) {
            String amountText = context.getString(R.string.format_amount, net);
            holder.textBalance.setText(context.getString(R.string.format_gets_back, amountText));
            holder.textBalance.setTextColor(COLOR_POSITIVE);
        } else if (net < -SETTLED_THRESHOLD) {
            String amountText = context.getString(R.string.format_amount, -net);
            holder.textBalance.setText(context.getString(R.string.format_owes_amount, amountText));
            holder.textBalance.setTextColor(COLOR_NEGATIVE);
        } else {
            holder.textBalance.setText(R.string.msg_settled_up);
            holder.textBalance.setTextColor(COLOR_SETTLED);
        }
    }

    @Override
    public int getItemCount() {
        return balances.size();
    }

    static class BalanceViewHolder extends RecyclerView.ViewHolder {
        final TextView textParticipantName;
        final TextView textPaidAmount;
        final TextView textShareAmount;
        final TextView textBalance;

        BalanceViewHolder(@NonNull View itemView) {
            super(itemView);
            textParticipantName = itemView.findViewById(R.id.textParticipantName);
            textPaidAmount = itemView.findViewById(R.id.textPaidAmount);
            textShareAmount = itemView.findViewById(R.id.textShareAmount);
            textBalance = itemView.findViewById(R.id.textBalance);
        }
    }
}
