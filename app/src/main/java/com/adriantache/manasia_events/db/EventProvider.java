package com.adriantache.manasia_events.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.adriantache.manasia_events.db.EventContract.CONTENT_AUTHORITY;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.CATEGORY_HUB;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.CATEGORY_MUSIC;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.CATEGORY_SHOP;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_CATEGORY_IMAGE;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DATE;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DESCRIPTION;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_NOTIFY;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_TITLE;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.TABLE_NAME;
import static com.adriantache.manasia_events.db.EventContract.EventEntry._ID;

/**
 * ContentProvider to keep database access separate from the app and ensure sanity checks on the input data
 **/
public class EventProvider extends ContentProvider {
    private static final int ERROR_VALUE = -1;
    private static final int EVENTS = 100;
    private static final int SINGLE_EVENT = 101;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(CONTENT_AUTHORITY, "events", EVENTS);
        uriMatcher.addURI(CONTENT_AUTHORITY, "events/#", SINGLE_EVENT);
    }

    private EventDBHelper eventDBHelper;

    @Override
    public boolean onCreate() {
        eventDBHelper = new EventDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        SQLiteDatabase db = eventDBHelper.getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case EVENTS:
                cursor = db.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SINGLE_EVENT:
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI" + uri);
        }
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case EVENTS:
                SQLiteDatabase db = eventDBHelper.getWritableDatabase();

                long id = ERROR_VALUE;
                if (testValues(values))
                    id = db.insert(TABLE_NAME, null, values);

                if (id != ERROR_VALUE) return ContentUris.withAppendedId(uri, id);
                else
                    return null;
            default:
                throw new IllegalArgumentException("Cannot insert unknown URI" + uri);
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case EVENTS:
                SQLiteDatabase db = eventDBHelper.getWritableDatabase();

                if (testValues(values))
                    return db.update(TABLE_NAME, values, selection, selectionArgs);
                else
                    return ERROR_VALUE;

            case SINGLE_EVENT:
                SQLiteDatabase db2 = eventDBHelper.getWritableDatabase();

                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                if (testValues(values))
                    return db2.update(TABLE_NAME, values, selection, selectionArgs);
                else
                    return ERROR_VALUE;

            default:
                throw new IllegalArgumentException("Cannot update unknown URI" + uri);
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case EVENTS:
                SQLiteDatabase db = eventDBHelper.getWritableDatabase();

                return db.delete(TABLE_NAME, selection, selectionArgs);
            case SINGLE_EVENT:
                SQLiteDatabase db2 = eventDBHelper.getWritableDatabase();
                selection = _ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                return db2.delete(TABLE_NAME, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Cannot delete unknown URI" + uri);
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        //we don't need to bother with this method, so we'll leave it unused
        return null;
    }

    //sanity checks for data going into the database
    private boolean testValues(@Nullable ContentValues values) {
        if (values == null) throw new IllegalArgumentException("No values received");

        String name = values.getAsString(COLUMN_EVENT_TITLE);
        if (name == null)
            throw new IllegalArgumentException("Event requires a title");

        String description = values.getAsString(COLUMN_EVENT_DESCRIPTION);
        if (description == null)
            throw new IllegalArgumentException("Event requires a description");

        String date = values.getAsString(COLUMN_EVENT_DATE);
        if (date == null)
            throw new IllegalArgumentException("Event requires a date");
        //todo add test for date format yyyy-MM-dd

        int gender = values.getAsInteger(COLUMN_EVENT_CATEGORY_IMAGE);
        switch (gender) {
            case CATEGORY_HUB:
            case CATEGORY_MUSIC:
            case CATEGORY_SHOP:
                break;
            default:
                throw new IllegalArgumentException("Illegal category");
        }

        Integer notify = values.getAsInteger(COLUMN_EVENT_NOTIFY);
        if (notify != null && notify != 0 && notify != 1)
            throw new IllegalArgumentException("Illegal notification option");

        return true;
    }
}
