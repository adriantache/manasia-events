package com.adriantache.manasia_events.notification;

import android.content.Context;

import com.adriantache.manasia_events.custom_class.Event;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import static com.adriantache.manasia_events.MainActivity.DBEventIDTag;
import static com.adriantache.manasia_events.db.DBUtils.readDatabase;
import static com.adriantache.manasia_events.util.Utils.calculateDelay;
import static com.adriantache.manasia_events.util.Utils.compareDateToToday;

/**
 * Class that stores various functionality related to notifications and scheduling
 **/
public class NotifyUtils {
    //set a tag in order to be able to disable all work if needed
    private static final String workTag = "notificationWork";

    private static void resetAllWork() {
        WorkManager.getInstance().cancelAllWorkByTag(workTag);
    }

    /**
     * Method to read all events from the database and set notifications for the ones that
     * the user selected to be notified for.
     *
     * todo [IDEA] allow user to be notified for all events but opt out of some
     *
     * @param context application context for database operation
     * @param addAll flag to determine if user will be notified for all events in the future
     */
    public static void scheduleNotifications(Context context, boolean addAll) {
        ArrayList<Event> events = (ArrayList<Event>) readDatabase(context);
        if (events == null || events.size() == 0) return;

        resetAllWork();

        for (Event event : events) {
            if (addAll && compareDateToToday(event.getDate()) > -1) {
                addNotification(event.getDatabaseID(), event.getDate());
            }

            if (event.getNotify() == 1)
                addNotification(event.getDatabaseID(), event.getDate());
        }
    }

    private static void addNotification(int DBEventID, String eventDate) {
        //store DBEventID to pass it to the PendingIntent and open the appropriate event page on notification click
        Data inputData = new Data.Builder().putInt(DBEventIDTag, DBEventID).build();

        OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                .setInitialDelay(calculateDelay(eventDate), TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(workTag)
                .build();

        WorkManager.getInstance().enqueue(notificationWork);
    }
}