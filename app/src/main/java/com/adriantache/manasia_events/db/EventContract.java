package com.adriantache.manasia_events.db;

import android.provider.BaseColumns;

import com.adriantache.manasia_events.R;

/**
 * Contract to store SQLite DB structure
 **/
public final class EventContract {

    public EventContract() {/*empty constructor*/}

    public static class PetEntry implements BaseColumns {
        public static final String TABLE_NAME = "events";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_EVENT_TITLE = "title";
        public static final String COLUMN_EVENT_DESCRIPTION = "description";
        public static final String COLUMN_EVENT_DATE = "date";
        public static final String COLUMN_EVENT_PHOTO_URL = "photo_url";
        public static final String COLUMN_EVENT_CATEGORY_IMAGE = "category_image";
        public static final String COLUMN_EVENT_NOTIFY = "notify";

        //possible values for category image column
        public static final int CATEGORY_HUB = R.drawable.hub;
        public static final int CATEGORY_MUSIC = R.drawable.music;
        public static final int CATEGORY_SHOP = R.drawable.shop;
    }
}