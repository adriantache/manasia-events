package com.adriantache.manasia_events.worker;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.work.PeriodicWorkRequest;
import androidx.work.State;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import static com.adriantache.manasia_events.db.DBUtils.inputRemoteEventsIntoDatabase;
import static com.adriantache.manasia_events.util.CommonStrings.EVENTS_JSON_WORK_TAG;
import static com.adriantache.manasia_events.util.CommonStrings.JSON_RESULT;

/**
 * This class exists only as a middleman to add an initial delay to the
 * PeriodicWorkRequest which updates events from the remote source
 **/
public class TriggerUpdateEventsWorker extends Worker implements LifecycleOwner {
    private static final String TAG = "TriggerUpdateEventsWRK";

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
                                    inputRemoteEventsIntoDatabase(eventsTemp, getApplicationContext());
                                }
                            }
                        });

        mainHandler.post(myRunnable);

        return Result.SUCCESS;
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return new LifecycleRegistry(this);
    }
}
