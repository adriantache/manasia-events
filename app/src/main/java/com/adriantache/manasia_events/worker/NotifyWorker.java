package com.adriantache.manasia_events.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.adriantache.manasia_events.EventDetail;
import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import androidx.work.Worker;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.support.v4.app.NotificationCompat.CATEGORY_EVENT;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;
import static com.adriantache.manasia_events.MainActivity.DBEventIDTag;
import static com.adriantache.manasia_events.util.Utils.prettyDate;

/**
 * Custom class to trigger scheduled notifications
 **/
public class NotifyWorker extends Worker {
    private static final int ERROR_VALUE = -1;
    private static final String MANASIA_NOTIFICATION_CHANNEL = "Manasia Event Reminder";
    private static final String MANASIA_NOTIFICATION_CHANNEL_GROUP = "Manasia Events";

    @NonNull
    @Override
    public Worker.Result doWork() {
        triggerNotification();

        return Worker.Result.SUCCESS;
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    private void triggerNotification() {
        final int DBEventID = getInputData().getInt(DBEventIDTag, ERROR_VALUE);

        Event event = null;

        if (DBEventID == ERROR_VALUE || DBEventID == 0)
            Log.i(getClass().toString(), "Invalid value for DBEventID!");
        else
            event = DBUtils.getEventFromDatabase(getApplicationContext(), DBEventID);

        if (event == null) {
            Log.i(getClass().toString(), "Cannot fetch event!");
            return;
        }

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !notificationChannelExists()) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the actual notification channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel(MANASIA_NOTIFICATION_CHANNEL, MANASIA_NOTIFICATION_CHANNEL, importance);

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

        //get event details to show in the notification
        String notificationTitle = "Manasia event: " + event.getTitle();
        String notificationText = "Today, " + prettyDate(event.getDate()) + ", at Stelea Spatarul 13, Bucuresti";

        //set the event image in the notification
        Bitmap largeImage = null;
        try {
            largeImage = Picasso.get().load(event.getPhotoUrl()).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //build the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), MANASIA_NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.drawable.ic_manasia_small)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setLargeIcon(largeImage)
                        .setCategory(CATEGORY_EVENT)
                        .setColor(0xFF4081)
                        .setVisibility(VISIBILITY_PUBLIC)
                        .setGroup(MANASIA_NOTIFICATION_CHANNEL_GROUP)
//                        .setStyle(new NotificationCompat.BigTextStyle()
//                                .bigText(event.getDescription()))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //trigger the notification, using DBEventID as its ID in order to show multiple
        //notifications, if applicable, but no duplicates
        //todo fix DBEventID autoincrement problem (results in duplicate notifications, requiring clearing notifications)
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(DBEventID, notificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private boolean notificationChannelExists() {
        NotificationManager manager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = null;

        if (manager != null)
            channel = manager.getNotificationChannel(MANASIA_NOTIFICATION_CHANNEL);

        return channel != null && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
    }
}
