package com.example.tripexpenseplanner.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.R;
import com.example.tripexpenseplanner.model.Expense;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of expenses for one trip on the Expenses screen.
 * Each row lets the user Edit or Delete that expense.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    /**
     * Callback used by ExpensesActivity to react to Edit/Delete taps.
     */
    public interface OnExpenseActionListener {
        void onEditExpense(Expense expense);

        void onDeleteExpense(Expense expense);
    }

    private List<Expense> expenses = new ArrayList<>();
    private final OnExpenseActionListener listener;

    public ExpenseAdapter(OnExpenseActionListener listener) {
        this.listener = listener;
    }

    public void setExpenses(List<Expense> expenses) {
        this.expenses = expenses != null ? expenses : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        holder.textExpenseCategory.setText(expense.getCategory());
        holder.textExpenseAmount.setText(
                holder.itemView.getContext().getString(R.string.format_amount, expense.getAmount()));

        String paidBy = expense.getPaidBy();
        boolean hasPaidBy = !TextUtils.isEmpty(paidBy);
        holder.textExpensePaidBy.setVisibility(hasPaidBy ? View.VISIBLE : View.GONE);
        if (hasPaidBy) {
            holder.textExpensePaidBy.setText(
                    holder.itemView.getContext().getString(R.string.format_paid_by, paidBy));
        }

        holder.textExpenseDate.setText(expense.getExpenseDate());

        String description = expense.getDescription();
        boolean hasDescription = !TextUtils.isEmpty(description);
        holder.textExpenseDescription.setVisibility(hasDescription ? View.VISIBLE : View.GONE);
        holder.textExpenseDescription.setText(description);

        holder.buttonEditExpense.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditExpense(expense);
            }
        });
        holder.buttonDeleteExpense.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteExpense(expense);
            }
        });
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        final TextView textExpenseCategory;
        final TextView textExpenseAmount;
        final TextView textExpensePaidBy;
        final TextView textExpenseDate;
        final TextView textExpenseDescription;
        final View buttonEditExpense;
        final View buttonDeleteExpense;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            textExpenseCategory = itemView.findViewById(R.id.textExpenseCategory);
            textExpenseAmount = itemView.findViewById(R.id.textExpenseAmount);
            textExpensePaidBy = itemView.findViewById(R.id.textExpensePaidBy);
            textExpenseDate = itemView.findViewById(R.id.textExpenseDate);
            textExpenseDescription = itemView.findViewById(R.id.textExpenseDescription);
            buttonEditExpense = itemView.findViewById(R.id.buttonEditExpense);
            buttonDeleteExpense = itemView.findViewById(R.id.buttonDeleteExpense);
        }
    }
}
