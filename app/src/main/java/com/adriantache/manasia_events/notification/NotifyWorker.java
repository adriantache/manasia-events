package com.adriantache.manasia_events.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.adriantache.manasia_events.EventDetail;
import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;

import androidx.work.WorkManager;
import androidx.work.Worker;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

/**
 * Custom class to trigger scheduled notifications
 **/
public class NotifyWorker extends Worker {
    private static final int ERROR_VALUE = -1;

    @NonNull
    @Override
    public WorkerResult doWork() {
        triggerNotification();

        return WorkerResult.SUCCESS;
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    private void triggerNotification() {
        final String manasia_notification_channel = "Manasia Event Reminder";
        final String DBEventIDTag = "DBEventID";
        final int DBEventID = getInputData().getInt(DBEventIDTag, ERROR_VALUE);

        Event event = null;

        if (DBEventID == ERROR_VALUE)
            Toast.makeText(getApplicationContext(), "Error retrieving Event ID", Toast.LENGTH_SHORT).show();
        else
            event = DBUtils.getEventFromDatabase(getApplicationContext(), DBEventID);

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the actual notification channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel(manasia_notification_channel, manasia_notification_channel, importance);

            //set a description for the channel
            String description = "A channel which shows notifications about events at Manasia";
            channel.setDescription(description);

            //set notification LED colour
            channel.setLightColor(Color.MAGENTA);

            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        //create an intent to open the event details activity when the user clicks the notification
        Intent intent = new Intent(getApplicationContext(), EventDetail.class);
        intent.putExtra(DBEventIDTag, DBEventID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //put together the PendingIntent
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, intent, FLAG_UPDATE_CURRENT);

        //todo figure out TaskStackBuilder, maybe it's better than my solution
        //https://developer.android.com/guide/components/activities/tasks-and-back-stack
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(intent);
//        PendingIntent pendingIntent = stackBuilder.getPendingIntent(1,PendingIntent.FLAG_ONE_SHOT);

        //get event details to show in the notification
        String notificationTitle = "Manasia event: " + event.getTitle();
        String notificationText = event.getDate() + " at Stelea Spatarul 13, Bucuresti";

        //build the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), manasia_notification_channel)
                        .setSmallIcon(R.drawable.ic_manasia_small)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //trigger the notification
        //todo figure out how to schedule this instead of just showing it
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, notificationBuilder.build());
    }
}
