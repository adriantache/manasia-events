package com.adriantache.manasia_events.util;

/**
 * Class to hold common strings to make debugging easier.
 **/
public class CommonStrings {
    //database related
    public static final String DB_EVENT_ID_TAG = "DBEventID";
    public static final String REMOTE_URL = "REMOTE_URL";

    //WorkerManager related
    public static final String ENQUEUE_EVENTS_JSON_WORK_TAG = "enqueueEventsJsonWork";
    public static final String EVENTS_JSON_WORK_TAG = "eventsJsonWork";
    public static final String EVENTS_JSON_WORK_TAG_FORCED = "eventsJsonWork-FORCED";
    public static final String NOTIFICATION_WORK_TAG = "notificationWork";

    //notification related
    public static final String MANASIA_NOTIFICATION_CHANNEL = "Manasia Event Reminder";
    public static final String MANASIA_NOTIFICATION_CHANNEL_GROUP = "Manasia Events";

    //SharedPrefs
    public static final String NOTIFY_SETTING = "notify";
    public static final String FIRST_LAUNCH_SETTING = "firstLaunch";
    public static final String LAST_UPDATE_TIME_SETTING = "LAST_UPDATE_TIME";

    //values
    public static final String SOURCE_ACTIVITY = "activity";
    public static final int SOURCE_MAIN_ACTIVITY = 1;
    public static final int SOURCE_EVENT_ACTIVITY = 2;
    public static final int SOURCE_DRINKS_MENU_ACTIVITY = 3;
    public static final int SOURCE_FOOD_MENU_ACTIVITY = 4;
    public static final int ERROR_VALUE = -1;
    public static final int EVENT_UPDATE_HOUR = 5;

    private CommonStrings() {
        throw new IllegalStateException("Class should not be instantiated!");
    }
}
