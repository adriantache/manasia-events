package com.adriantache.manasia_events.notification;

import android.app.NotificationManager;
import android.content.Context;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.worker.NotifyWorker;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.adriantache.manasia_events.db.DBUtils.readDatabase;
import static com.adriantache.manasia_events.util.CommonStrings.DB_EVENT_ID_TAG;
import static com.adriantache.manasia_events.util.CommonStrings.NOTIFICATION_WORK_TAG;
import static com.adriantache.manasia_events.util.Utils.calculateDelay;
import static com.adriantache.manasia_events.util.Utils.compareDateToToday;

/**
 * Class that stores various functionality related to notifications and scheduling
 **/
public class NotifyUtils {
    //set a tag in order to be able to disable all work if needed

    public NotifyUtils() {
        throw new AssertionError("Cannot instantiate utility class.");
    }

    /**
     * Method to read all events from the database and set notifications for the ones that
     * the user selected to be notified for.
     * todo [IDEA] allow user to be notified for all events but opt out of some
     *
     * @param context application context for database operation and notification clearing
     * @param addAll  flag to determine if user will be notified for all events in the future
     */
    public static void scheduleNotifications(Context context, boolean addAll) {
        ArrayList<Event> events = (ArrayList<Event>) readDatabase(context);
        if (events == null || events.isEmpty()) return;

        //cancel all already set notifications in case the database IDs have shifted
        resetAllWork(context);

        //then add notifications for all events happening today or in the future
        for (Event event : events) {
            if (compareDateToToday(event.getDate()) > -1 && (addAll || event.getNotify() == 1)) {
                addNotification(event.getDatabaseID(), event.getDate());
            }
        }
    }

    private static void resetAllWork(Context context) {
        //cancel all pending work tasks
        WorkManager.getInstance().cancelAllWorkByTag(NOTIFICATION_WORK_TAG);

        //clear all notifications to prevent duplicates
        //todo [IMPORTANT] figure out database ID increment problem, should negate the need for this code
        NotificationManager notificationManager = (NotificationManager) context.
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    private static void addNotification(long dbEventID, String eventDate) {
        //store DBEventID to pass it to the PendingIntent and open the appropriate event page on notification click
        Data inputData = new Data.Builder().putLong(DB_EVENT_ID_TAG, dbEventID).build();

        //get delay until notification triggers
        long delay = calculateDelay(eventDate);

        //only trigger notification if notification for the event is in the future
        if (delay > 0) {
            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .addTag(NOTIFICATION_WORK_TAG)
                    .build();

            WorkManager.getInstance().enqueue(notificationWork);
        }
    }
}