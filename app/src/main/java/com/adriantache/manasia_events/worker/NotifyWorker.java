package com.adriantache.manasia_events.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adriantache.manasia_events.EventDetail;
import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static androidx.core.app.NotificationCompat.CATEGORY_EVENT;
import static androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC;
import static com.adriantache.manasia_events.util.CommonStrings.DB_EVENT_ID_TAG;
import static com.adriantache.manasia_events.util.CommonStrings.ERROR_VALUE;
import static com.adriantache.manasia_events.util.CommonStrings.MANASIA_NOTIFICATION_CHANNEL;
import static com.adriantache.manasia_events.util.CommonStrings.MANASIA_NOTIFICATION_CHANNEL_GROUP;
import static com.adriantache.manasia_events.util.Utils.prettyDate;

/**
 * Custom class to trigger scheduled notifications
 **/
public class NotifyWorker extends Worker {
    public NotifyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        triggerNotification();

        return Result.success();
    }

    private void triggerNotification() {
        final long DBEventID = getInputData().getLong(DB_EVENT_ID_TAG, ERROR_VALUE);

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
        intent.putExtra(DB_EVENT_ID_TAG, DBEventID);
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

        //trigger the notification, using DBEventID hashCode as its ID in order to show multiple
        //notifications, if applicable, but no duplicates
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(Long.valueOf(DBEventID).hashCode(), notificationBuilder.build());
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
