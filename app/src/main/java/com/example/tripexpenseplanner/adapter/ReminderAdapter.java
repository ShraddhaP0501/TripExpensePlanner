package com.example.tripexpenseplanner.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tripexpenseplanner.R;
import com.example.tripexpenseplanner.model.Reminder;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of reminders for one trip. Each row lets the user cancel
 * that reminder — there is no edit action for reminders yet.
 */
public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {

    /**
     * Callback used by RemindersActivity to react to a Cancel tap.
     */
    public interface OnReminderActionListener {
        void onCancelReminder(Reminder reminder);
    }

    private List<Reminder> reminders = new ArrayList<>();
    private final OnReminderActionListener listener;

    public ReminderAdapter(OnReminderActionListener listener) {
        this.listener = listener;
    }

    public void setReminders(List<Reminder> reminders) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder reminder = reminders.get(position);

        holder.textReminderTitle.setText(reminder.getTitle());
        holder.textReminderType.setText(reminder.getReminderType());
        holder.textReminderDateTime.setText(holder.itemView.getContext().getString(
                R.string.format_reminder_datetime, reminder.getReminderDate(), reminder.getReminderTime()));

        String note = reminder.getNote();
        boolean hasNote = !TextUtils.isEmpty(note);
        holder.textReminderNote.setVisibility(hasNote ? View.VISIBLE : View.GONE);
        holder.textReminderNote.setText(note);

        holder.buttonCancelReminder.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelReminder(reminder);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminders.size();
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        final TextView textReminderTitle;
        final TextView textReminderType;
        final TextView textReminderDateTime;
        final TextView textReminderNote;
        final View buttonCancelReminder;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            textReminderTitle = itemView.findViewById(R.id.textReminderTitle);
            textReminderType = itemView.findViewById(R.id.textReminderType);
            textReminderDateTime = itemView.findViewById(R.id.textReminderDateTime);
            textReminderNote = itemView.findViewById(R.id.textReminderNote);
            buttonCancelReminder = itemView.findViewById(R.id.buttonCancelReminder);
        }
    }
}
