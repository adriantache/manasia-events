package com.adriantache.manasia_events.worker;

import android.content.Context;
import android.support.annotation.NonNull;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import static com.adriantache.manasia_events.util.CommonStrings.EVENTS_JSON_WORK_TAG;

/**
 * This class exists only as a middleman to add an initial delay to the
 * PeriodicWorkRequest which updates events from the remote source
 **/
public class TriggerUpdateEventsWorker extends Worker {
    public TriggerUpdateEventsWorker(Context context, WorkerParameters workerParameters) {
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        //first remove any existing work that is scheduled to prevent duplicates due to
        // inconsistent trigger time
        WorkManager.getInstance().cancelAllWorkByTag(EVENTS_JSON_WORK_TAG);

        //set constraints to prevent running when it's not supposed to
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        //then reschedule all future work to run daily
        PeriodicWorkRequest getEventJson = new PeriodicWorkRequest
                .Builder(UpdateEventsWorker.class, 24, TimeUnit.HOURS)
                .setInputData(getInputData())
                .setConstraints(constraints)
                .addTag(EVENTS_JSON_WORK_TAG)
                .build();

        //and of course enqueue...
        WorkManager.getInstance().enqueue(getEventJson);

        return Result.success();
    }
}
