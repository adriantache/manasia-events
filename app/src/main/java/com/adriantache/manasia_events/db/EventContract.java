package com.adriantache.manasia_events.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract to store SQLite DB structure
 **/
public final class EventContract {
    static final String CONTENT_AUTHORITY = "com.adriantache.manasia_events";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    private static final String PATH_EVENTS = "events";
    public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_EVENTS);

    public EventContract() {/*empty constructor*/}

    public static class EventEntry implements BaseColumns {
        static final String TABLE_NAME = "events";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_EVENT_TITLE = "title";
        public static final String COLUMN_EVENT_DESCRIPTION = "description";
        public static final String COLUMN_EVENT_DATE = "date";
        public static final String COLUMN_EVENT_PHOTO_URL = "photo_url";
        public static final String COLUMN_EVENT_NOTIFY = "notify";
        public static final String COLUMN_TAGS = "tags";
    }
}