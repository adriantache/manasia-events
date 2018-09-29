package com.adriantache.manasia_events.worker;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import android.support.annotation.NonNull;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * This class exists only as a middleman to add an initial delay to the
 * PeriodicWorkRequest which updates events from the remote source
 **/
public class TriggerUpdateEventsWorker extends Worker {
    private static final String EVENTS_JSON_WORK_TAG = "eventsJsonWork";

    public TriggerUpdateEventsWorker(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        PeriodicWorkRequest getEventJson = new PeriodicWorkRequest
                .Builder(UpdateEventsWorker.class, 24, TimeUnit.HOURS)
                .setInputData(getInputData())
                .addTag(EVENTS_JSON_WORK_TAG)
                .build();
        WorkManager.getInstance().enqueue(getEventJson);

        return Result.SUCCESS;
    }
}
