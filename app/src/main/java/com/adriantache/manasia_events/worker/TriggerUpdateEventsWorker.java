package com.adriantache.manasia_events.worker;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;
import com.adriantache.manasia_events.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.work.PeriodicWorkRequest;
import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static android.content.Context.MODE_PRIVATE;
import static com.adriantache.manasia_events.EventDetail.NOTIFY_SETTING;
import static com.adriantache.manasia_events.EventDetail.SHARED_PREFERENCES_TAG;
import static com.adriantache.manasia_events.db.EventContract.CONTENT_URI;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DATE;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DESCRIPTION;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_NOTIFY;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_PHOTO_URL;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_TITLE;
import static com.adriantache.manasia_events.notification.NotifyUtils.scheduleNotifications;

/**
 * This class exists only as a middleman to add an initial delay to the
 * PeriodicWorkRequest which updates events from the remote source
 **/
public class TriggerUpdateEventsWorker extends Worker implements LifecycleOwner {
    private static final String EVENTS_JSON_WORK_TAG = "eventsJsonWork";
    private static final String JSON_RESULT = "JSON_STRING";
    private static final String TAG = "TriggerUpdateEventsWRK";
    private static final String LAST_UPDATE_TIME_LABEL = "LAST_UPDATE_TIME";

    public TriggerUpdateEventsWorker(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        //first remove any existing work that is scheduled to prevent duplicates due to
        //inconsistent trigger time
        WorkManager.getInstance().cancelAllWorkByTag(EVENTS_JSON_WORK_TAG);

        //then reschedule all future work to run daily
        PeriodicWorkRequest getEventJson = new PeriodicWorkRequest
                .Builder(UpdateEventsWorker.class, 24, TimeUnit.HOURS)
                .setInputData(getInputData())
                .addTag(EVENTS_JSON_WORK_TAG)
                .build();

        //and of course enqueue...
        WorkManager.getInstance().enqueue(getEventJson);

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper());

        Runnable myRunnable = () ->
                //...then get the result...
                WorkManager.getInstance()
                        .getStatusById(getEventJson.getId())
                        .observe(TriggerUpdateEventsWorker.this, workStatus -> {
                            if (workStatus != null && workStatus.getState().equals(State.SUCCEEDED)) {
                                //get results JSON
                                StringBuilder jsonResult = null;

                                try (BufferedReader bufferedReader =
                                             new BufferedReader(
                                                     new InputStreamReader(
                                                             getApplicationContext().openFileInput(JSON_RESULT)))) {

                                    jsonResult = new StringBuilder();
                                    int i;
                                    while ((i = bufferedReader.read()) != -1) {
                                        jsonResult.append((char) i);
                                    }

                                    //delete file after reading it to prevent caching in case of errors
                                    getApplicationContext().deleteFile(JSON_RESULT);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //decode the JSON into events ArrayList
                                ArrayList<Event> eventsTemp = null;
                                if (jsonResult != null && jsonResult.length() != 0) {
                                    eventsTemp = Utils.parseJSON(jsonResult.toString());
                                }

                                //if remote fetch is successful...
                                if (eventsTemp != null) {
                                    Log.i(TAG, "fetchEvents: Successfully fetched and decoded remote JSON.");

                                    // send events to the database...
                                    inputRemoteEventsIntoDatabase(eventsTemp);

                                    // and write fetch date into SharedPrefs
                                    Calendar calendar = Calendar.getInstance();
                                    long lastUpdateTime = calendar.getTimeInMillis();
                                    SharedPreferences sharedPref = getApplicationContext()
                                            .getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putLong(LAST_UPDATE_TIME_LABEL, lastUpdateTime);
                                    editor.apply();

                                    //todo trigger notifications scheduling update here, currently happens in method below
                                }
                            }
                        });

        mainHandler.post(myRunnable);

        return Result.SUCCESS;
    }

    private void inputRemoteEventsIntoDatabase(ArrayList<Event> remoteEvents) {
        if (remoteEvents != null) {
            //first of all transfer all notify statuses from the local database to the temporary remote database
            ArrayList<Event> DBEvents = (ArrayList<Event>) DBUtils.readDatabase(getApplicationContext());
            remoteEvents = Utils.updateNotifyInRemote(remoteEvents, DBEvents);

            //then delete ALL events from the local table
            getApplicationContext().getContentResolver().delete(CONTENT_URI, null, null);

            //then add the remote events to the local database
            for (Event event : remoteEvents) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_EVENT_TITLE, event.getTitle());
                values.put(COLUMN_EVENT_DESCRIPTION, event.getDescription());
                values.put(COLUMN_EVENT_DATE, event.getDate());
                if (!TextUtils.isEmpty(event.getPhotoUrl()))
                    values.put(COLUMN_EVENT_PHOTO_URL, event.getPhotoUrl());
                values.put(COLUMN_EVENT_NOTIFY, event.getNotify());

                getApplicationContext().getContentResolver().insert(CONTENT_URI, values);
            }

            SharedPreferences sharedPrefs = getApplicationContext()
                    .getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
            //set notify on every future event flag
            boolean notifyOnAllEvents = sharedPrefs.getBoolean(NOTIFY_SETTING, false);

            //update event notifications for all future events fetched from the remote database
            if (notifyOnAllEvents) scheduleNotifications(getApplicationContext(), true);
            else{
                //todo seems to me like I'm forgetting to update notifications on individual events so I added this, need to check it
                scheduleNotifications(getApplicationContext(), false);
            }
        }
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return new LifecycleRegistry(this);
    }
}
