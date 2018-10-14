package com.adriantache.manasia_events.util;

/**
 * Class to hold common strings to make debugging easier.
 **/
public class CommonStrings {
    public static final String DB_EVENT_ID_TAG = "DBEventID";
    public static final String REMOTE_URL = "REMOTE_URL";
    public static final String ENQUEUE_EVENTS_JSON_WORK_TAG = "enqueueEventsJsonWork";
    public static final String EVENTS_JSON_WORK_TAG = "eventsJsonWork";
    public static final String JSON_RESULT = "JSON_STRING";
    public static final String NOTIFY_SETTING = "notify";
    public static final String FIRST_LAUNCH_SETTING = "notify";
    public static final String LAST_UPDATE_TIME_SETTING = "LAST_UPDATE_TIME";
    public static final String NOTIFICATION_WORK_TAG = "notificationWork";
    public static final String MANASIA_NOTIFICATION_CHANNEL = "Manasia Event Reminder";
    public static final String MANASIA_NOTIFICATION_CHANNEL_GROUP = "Manasia Events";
    public static final int SOURCE_MAIN_ACTIVITY = 1;
    public static final int SOURCE_EVENT_ACTIVITY = 2;
    public static final int ERROR_VALUE = -1;

    public CommonStrings() {
        throw new IllegalStateException("Class should not be instantiated!");
    }
}
