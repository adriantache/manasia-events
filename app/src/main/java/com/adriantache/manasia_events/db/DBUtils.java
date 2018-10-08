package com.adriantache.manasia_events.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.text.TextUtils;

import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.util.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.adriantache.manasia_events.db.EventContract.CONTENT_URI;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DATE;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DESCRIPTION;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_NOTIFY;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_PHOTO_URL;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_TITLE;
import static com.adriantache.manasia_events.db.EventContract.EventEntry._ID;
import static com.adriantache.manasia_events.notification.NotifyUtils.scheduleNotifications;

/**
 * Class to store general utility functions for database operations
 **/
public final class DBUtils {
    private static final String SHARED_PREFERENCES_TAG = "preferences";
    private static final String NOTIFY_SETTING = "notify";
    private static final String LAST_UPDATE_TIME_LABEL = "LAST_UPDATE_TIME";

    private DBUtils() {
        throw new AssertionError("No Utils Instances are allowed!");
    }

    /**
     * Method to read all events from the local database, including database IDs
     *
     * @param context Context for EventDBHelper
     * @return a List of all the events in the database
     */
    public static List<Event> readDatabase(Context context) {
        String[] projection =
                {_ID, COLUMN_EVENT_TITLE, COLUMN_EVENT_DESCRIPTION, COLUMN_EVENT_DATE,
                        COLUMN_EVENT_PHOTO_URL, COLUMN_EVENT_NOTIFY};

        //order by date since FB stores them in a random order
        String sortOrder = COLUMN_EVENT_DATE + " DESC";

        Cursor cursor = context.getContentResolver().query(CONTENT_URI, projection, null, null, sortOrder);

        if (cursor == null) {
            return null;
        }

        ArrayList<Event> DBEvents = new ArrayList<>();

        try {
            if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DATE));
                String photoUrl = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_PHOTO_URL));
                int notify = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_NOTIFY));

                DBEvents.add(new Event(id, date, title, description, photoUrl, notify));
            }
        } finally {
            cursor.close();
        }

        return DBEvents;
    }

    /**
     * Method to get a single Event object from the database
     *
     * @param context   Context for EventDBHelper
     * @param DBEventID Unique database ID of the event, as fetched from the database
     * @return The event requested by ID
     */
    public static Event getEventFromDatabase(Context context, int DBEventID) {
        String[] projection =
                {_ID, COLUMN_EVENT_TITLE, COLUMN_EVENT_DESCRIPTION, COLUMN_EVENT_DATE,
                        COLUMN_EVENT_PHOTO_URL, COLUMN_EVENT_NOTIFY};
        String selection = _ID + " == ?";
        String selectionArgs[] = {String.valueOf(DBEventID)};

        Cursor cursor = context.getContentResolver().query(CONTENT_URI, projection, selection, selectionArgs, null);

        if (cursor == null) return null;

        Event event = null;

        try {
            if (cursor.getCount() == 0) {
                cursor.close();
                return null;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DATE));
                String photoUrl = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_PHOTO_URL));
                int notify = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_NOTIFY));

                event = new Event(id, date, title, description, photoUrl, notify);
            }
        } finally {
            cursor.close();
        }

        return event;
    }

    /**
     * Method to send an Event object to the database and update its details
     *
     * @param context   Context for EventDBHelper
     * @param DBEventID Unique database ID of the event, as fetched from the database
     * @param event     Event object to be updated into the database
     * @return (long) Result of the insertion operation, should be == DBEventID
     */
    public static int updateEventToDatabase(Context context, int DBEventID, Event event) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_EVENT_TITLE, event.getTitle());
        values.put(COLUMN_EVENT_DESCRIPTION, event.getDescription());
        values.put(COLUMN_EVENT_DATE, event.getDate());
        if (!TextUtils.isEmpty(event.getPhotoUrl()))
            values.put(COLUMN_EVENT_PHOTO_URL, event.getPhotoUrl());
        values.put(COLUMN_EVENT_NOTIFY, event.getNotify());

        String selection = _ID + " == ?";
        String selectionArgs[] = {String.valueOf(DBEventID)};

        return context.getContentResolver().update(CONTENT_URI, values, selection, selectionArgs);
    }

    public static void inputRemoteEventsIntoDatabase(ArrayList<Event> remoteEvents, Context context) {
        if (remoteEvents != null) {
            //first of all transfer all notify statuses from the local database to the temporary remote database
            ArrayList<Event> DBEvents = (ArrayList<Event>) DBUtils.readDatabase(context);
            remoteEvents = Utils.updateNotifyInRemote(remoteEvents, DBEvents);

            //then delete ALL events from the local table
            context.getContentResolver().delete(CONTENT_URI, null, null);

            //then add the remote events to the local database
            for (Event event : remoteEvents) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_EVENT_TITLE, event.getTitle());
                values.put(COLUMN_EVENT_DESCRIPTION, event.getDescription());
                values.put(COLUMN_EVENT_DATE, event.getDate());
                if (!TextUtils.isEmpty(event.getPhotoUrl()))
                    values.put(COLUMN_EVENT_PHOTO_URL, event.getPhotoUrl());
                values.put(COLUMN_EVENT_NOTIFY, event.getNotify());

                context.getContentResolver().insert(CONTENT_URI, values);
            }

            SharedPreferences sharedPrefs = context
                    .getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
            //set notify on every future event flag
            boolean notifyOnAllEvents = sharedPrefs.getBoolean(NOTIFY_SETTING, false);

            // and write fetch date as well into SharedPrefs
            Calendar calendar = Calendar.getInstance();
            long lastUpdateTime = calendar.getTimeInMillis();
            SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putLong(LAST_UPDATE_TIME_LABEL, lastUpdateTime);
            editor.apply();

            //update event notifications for all future events fetched from the remote database
            //todo consider replacing this with a WorkManager chain and refactor it
            if (notifyOnAllEvents) scheduleNotifications(context, true);
            else {
                //todo seems to me like I'm forgetting to update notifications on individual events so I added this, need to check it
                scheduleNotifications(context, false);
            }
        }
    }
}
