package com.example.tripexpenseplanner.notification;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.tripexpenseplanner.model.Reminder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Schedules and cancels the local (device-only) alarm that backs one Reminder.
 * No Firebase, no server, no network calls — everything here uses only
 * {@link AlarmManager} and {@link ReminderReceiver}, both part of the Android
 * framework.
 */
public final class ReminderScheduler {

    private static final String TAG = "ReminderScheduler";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm";

    private ReminderScheduler() {
        // Static helper class — not meant to be instantiated.
    }

    /**
     * Schedules a one-time alarm for the given reminder's date and time.
     * Safe to call again for the same reminder (e.g. after rebooting the
     * device) — it simply replaces any alarm already scheduled for it.
     *
     * @return true if a real exact alarm was scheduled, false if the trigger
     * time was invalid/in the past, or the alarm had to fall back to an
     * inexact one because exact-alarm permission isn't granted (Android 12+).
     */
    public static boolean scheduleReminder(Context context, Reminder reminder) {
        Long triggerAtMillis = computeTriggerAtMillis(reminder);
        if (triggerAtMillis == null) {
            Log.w(TAG, "Could not parse date/time for reminder id=" + reminder.getId());
            return false;
        }
        if (triggerAtMillis <= System.currentTimeMillis()) {
            Log.w(TAG, "Reminder id=" + reminder.getId() + " is in the past; not scheduling");
            return false;
        }

        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return false;
        }

        PendingIntent pendingIntent = buildPendingIntent(context, reminder);

        boolean canScheduleExact = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            canScheduleExact = alarmManager.canScheduleExactAlarms();
        }

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            // No permission for exact alarms on this device — fall back to an
            // inexact alarm so the reminder still fires, just possibly a bit late.
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
        return canScheduleExact;
    }

    /**
     * Cancels the alarm previously scheduled for this reminder, if any.
     * Safe to call even if nothing was ever scheduled for it.
     */
    public static void cancelReminder(Context context, long reminderId) {
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        int requestCode = requestCodeFor(reminderId);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, flags);

        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    /**
     * Whether the app can currently schedule exact alarms. Always true below
     * Android 12 (API 31), where this restriction doesn't exist.
     */
    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }
        AlarmManager alarmManager = context.getSystemService(AlarmManager.class);
        return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }

    private static PendingIntent buildPendingIntent(Context context, Reminder reminder) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_REMINDER_ID, reminder.getId());
        intent.putExtra(ReminderReceiver.EXTRA_TRIP_ID, reminder.getTripId());
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, reminder.getTitle());
        intent.putExtra(ReminderReceiver.EXTRA_TYPE, reminder.getReminderType());
        intent.putExtra(ReminderReceiver.EXTRA_NOTE, reminder.getNote());

        int requestCode = requestCodeFor(reminder.getId());
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        return PendingIntent.getBroadcast(context, requestCode, intent, flags);
    }

    /**
     * Turns a (long) reminder id into the (int) request code AlarmManager/PendingIntent
     * needs, so each reminder's alarm can be scheduled and cancelled independently.
     */
    private static int requestCodeFor(long reminderId) {
        return (int) reminderId;
    }

    /**
     * Combines a reminder's date ("yyyy-MM-dd") and time ("HH:mm") into a single
     * trigger timestamp, or null if either value can't be parsed.
     */
    public static Long computeTriggerAtMillis(Reminder reminder) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_PATTERN, Locale.US);
        try {
            Date dateTime = dateTimeFormat.parse(reminder.getReminderDate() + " " + reminder.getReminderTime());
            return dateTime != null ? dateTime.getTime() : null;
        } catch (ParseException e) {
            return null;
        }
    }
}
