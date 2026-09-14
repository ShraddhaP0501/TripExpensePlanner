package com.example.tripexpenseplanner.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.tripexpenseplanner.R;
import com.example.tripexpenseplanner.TripDetailsActivity;

/**
 * Fires when a reminder's scheduled alarm goes off (even if the app isn't running)
 * and shows a local notification for it. Registered in AndroidManifest.xml.
 * Nothing here talks to a server — the notification is built entirely from the
 * extras {@link ReminderScheduler} put into the alarm's Intent.
 */
public class ReminderReceiver extends BroadcastReceiver {

    public static final String EXTRA_REMINDER_ID = "extra_reminder_id";
    public static final String EXTRA_TRIP_ID = "extra_trip_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TYPE = "extra_type";
    public static final String EXTRA_NOTE = "extra_note";

    private static final String CHANNEL_ID = "trip_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        long reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L);
        long tripId = intent.getLongExtra(EXTRA_TRIP_ID, -1L);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String type = intent.getStringExtra(EXTRA_TYPE);
        String note = intent.getStringExtra(EXTRA_NOTE);

        createNotificationChannel(context);
        showNotification(context, reminderId, tripId, title, type, note);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(context.getString(R.string.notification_channel_description));
        manager.createNotificationChannel(channel);
    }

    private void showNotification(Context context, long reminderId, long tripId,
                                   String title, String type, String note) {
        String contentTitle = !TextUtils.isEmpty(type) ? (type + ": " + title) : title;
        String contentText = !TextUtils.isEmpty(note)
                ? note
                : context.getString(R.string.notification_default_text);

        Intent openIntent = new Intent(context, TripDetailsActivity.class);
        openIntent.putExtra(TripDetailsActivity.EXTRA_TRIP_ID, tripId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int requestCode = (int) reminderId;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        PendingIntent contentIntent = PendingIntent.getActivity(context, requestCode, openIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        // Android 13+ requires POST_NOTIFICATIONS to actually be granted before
        // posting — if the user denied it, we simply skip showing the notification
        // rather than crash with a SecurityException.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(context).notify(requestCode, builder.build());
    }
}
