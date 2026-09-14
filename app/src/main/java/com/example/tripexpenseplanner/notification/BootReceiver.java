package com.example.tripexpenseplanner.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.tripexpenseplanner.database.DatabaseHelper;
import com.example.tripexpenseplanner.model.Reminder;

import java.util.List;

/**
 * Android clears every AlarmManager alarm when the device restarts, so any
 * reminder scheduled before a reboot would silently never fire again unless
 * something re-schedules it. This receiver runs once after boot completes,
 * reads every reminder still stored in SQLite, and re-schedules the ones that
 * are still in the future. Registered in AndroidManifest.xml for
 * ACTION_BOOT_COMPLETED.
 */
public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        List<Reminder> reminders = dbHelper.getAllReminders();

        int rescheduled = 0;
        for (Reminder reminder : reminders) {
            if (ReminderScheduler.scheduleReminder(context, reminder)) {
                rescheduled++;
            }
        }
        Log.d(TAG, "Re-scheduled " + rescheduled + " of " + reminders.size() + " reminder(s) after boot");
    }
}
