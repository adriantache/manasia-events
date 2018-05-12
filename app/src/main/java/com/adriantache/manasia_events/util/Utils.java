package com.adriantache.manasia_events.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.EventDBHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static com.adriantache.manasia_events.db.EventContract.PetEntry.COLUMN_EVENT_CATEGORY_IMAGE;
import static com.adriantache.manasia_events.db.EventContract.PetEntry.COLUMN_EVENT_DATE;
import static com.adriantache.manasia_events.db.EventContract.PetEntry.COLUMN_EVENT_DESCRIPTION;
import static com.adriantache.manasia_events.db.EventContract.PetEntry.COLUMN_EVENT_NOTIFY;
import static com.adriantache.manasia_events.db.EventContract.PetEntry.COLUMN_EVENT_PHOTO_URL;
import static com.adriantache.manasia_events.db.EventContract.PetEntry.COLUMN_EVENT_TITLE;
import static com.adriantache.manasia_events.db.EventContract.PetEntry.TABLE_NAME;
import static com.adriantache.manasia_events.db.EventContract.PetEntry._ID;

/**
 * Class to store general utility functions
 **/
public final class Utils {
    private Utils() {
        throw new AssertionError("No Utils Instances are allowed!");
    }

    public static String extractDate(String s, boolean day) {
        String[] parts = s.split("\\.");

        if (parts.length == 0) return "ERROR";

        if (day) return parts[0];
        else switch (parts[1]) {
            case "01":
                return "January";
            case "02":
                return "February";
            case "03":
                return "March";
            case "04":
                return "April";
            case "05":
                return "May";
            case "06":
                return "June";
            case "07":
                return "July";
            case "08":
                return "August";
            case "09":
                return "September";
            case "10":
                return "October";
            case "11":
                return "November";
            case "12":
                return "December";
            default:
                return "ERROR";
        }
    }

    public static int compareDateToToday(String date) {
        final int DATE_ERROR = 999999;

        Date formattedDate = null;
        try {
            formattedDate = new SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date today = new Date();

        //fix to set time to midnight -1 second, to ensure events from today are shown
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(today);
        calendar.set(Calendar.HOUR, -12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, -1);
        today = calendar.getTime();

        if (formattedDate != null) {
            return formattedDate.compareTo(today);
        }

        //failure will default to show actions and let the user decide
        else return DATE_ERROR;
    }

    /**
     * Method to read all events from the local database, including database IDs
     * @param context Context for EventDBHelper
     * @return a List of all the events in the database
     * todo decide if we impose a limit on how many database entries to fetch
     */
    public static List<Event> readDatabase(Context context) {
        //get a readable database to get the array from
        EventDBHelper mDbHelper = new EventDBHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection =
                {_ID, COLUMN_EVENT_TITLE, COLUMN_EVENT_DESCRIPTION, COLUMN_EVENT_DATE,
                        COLUMN_EVENT_PHOTO_URL, COLUMN_EVENT_CATEGORY_IMAGE, COLUMN_EVENT_NOTIFY};
        Cursor cursor =
                db.query(TABLE_NAME, projection, null, null,
                        null, null, null);

        ArrayList<Event> DBEvents = new ArrayList<>();

        try {
            if (cursor.getCount() == 0) {
                cursor.close();
                db.close();
                return null;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DATE));
                String photoUrl = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_PHOTO_URL));
                int categoryImage = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_CATEGORY_IMAGE));
                int notify = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_NOTIFY));

                DBEvents.add(new Event(id, date, title, description, photoUrl, categoryImage, notify));
            }
        } finally {
            cursor.close();
        }

        db.close();
        return DBEvents;
    }

    /**
     * Method to get a single Event object from the database
     * @param context Context for EventDBHelper
     * @param DBEventID Unique database ID of the event, as fetched from the database
     * @return The event requested by ID
     */
    public static Event getEventFromDatabase(Context context, int DBEventID) {
        EventDBHelper mDbHelper = new EventDBHelper(context);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection =
                {_ID, COLUMN_EVENT_TITLE, COLUMN_EVENT_DESCRIPTION, COLUMN_EVENT_DATE,
                        COLUMN_EVENT_PHOTO_URL, COLUMN_EVENT_CATEGORY_IMAGE, COLUMN_EVENT_NOTIFY};
        String selection = _ID + " == ?";
        String selectionArgs[] = {String.valueOf(DBEventID)};
        Cursor cursor =
                db.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, null);

        Event event = null;

        try {
            if (cursor.getCount() == 0) {
                cursor.close();
                db.close();
                return null;
            }

            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex(_ID));
                String title = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_TITLE));
                String description = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DESCRIPTION));
                String date = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_DATE));
                String photoUrl = cursor.getString(cursor.getColumnIndex(COLUMN_EVENT_PHOTO_URL));
                int categoryImage = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_CATEGORY_IMAGE));
                int notify = cursor.getInt(cursor.getColumnIndex(COLUMN_EVENT_NOTIFY));

                event = new Event(id, date, title, description, photoUrl, categoryImage, notify);
            }
        } finally {
            cursor.close();
        }

        db.close();
        return event;
    }

    /**
     * Method to send an Event object to the database and update its details
     * @param context Context for EventDBHelper
     * @param DBEventID Unique database ID of the event, as fetched from the database
     * @param event Event object to be updated into the database
     * @param updateNotify Whether to update the notification flag (todo decide if necessary)
     * @return (long) Result of the insertion operation, should be == DBEventID
     */
    public static long updateEventToDatabase(Context context, int DBEventID, Event event, boolean updateNotify) {
        EventDBHelper mDbHelper = new EventDBHelper(context);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        //todo check if the line below is needed
        //values.put(_ID, DBEventID);
        values.put(COLUMN_EVENT_TITLE, event.getTitle());
        values.put(COLUMN_EVENT_DESCRIPTION, event.getDescription());
        values.put(COLUMN_EVENT_DATE, event.getDate());
        if (!TextUtils.isEmpty(event.getPhotoUrl()))
            values.put(COLUMN_EVENT_PHOTO_URL, event.getPhotoUrl());
        values.put(COLUMN_EVENT_CATEGORY_IMAGE, event.getCategory_image());
        if (updateNotify)
            values.put(COLUMN_EVENT_NOTIFY, event.getNotify());

        String selection = _ID + " == ?";
        String selectionArgs[] = {String.valueOf(DBEventID)};

        long result = db.update(TABLE_NAME, values, selection, selectionArgs);

        db.close();
        return result;
    }
}