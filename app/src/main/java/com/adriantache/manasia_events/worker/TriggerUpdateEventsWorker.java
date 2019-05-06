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
    private static final String TAG = "TriggerUpdateEventsWRK";

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

        //trigger additional tasks every time the work is completed
//        listenableFuture.addListener(this::onWorkCompleted, Runnable::run);

        return Result.success();
    }

//    private void onWorkCompleted() {
//        //get results JSON
//        StringBuilder jsonResult = null;
//
//        try (BufferedReader bufferedReader =
//                     new BufferedReader(
//                             new InputStreamReader(
//                                     getApplicationContext().openFileInput(JSON_RESULT)))) {
//
//            jsonResult = new StringBuilder();
//            int i;
//            while ((i = bufferedReader.read()) != -1) {
//                jsonResult.append((char) i);
//            }
//
//            //delete file after reading it to prevent caching in case of errors
//            getApplicationContext().deleteFile(JSON_RESULT);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        //decode the JSON into events ArrayList
//        ArrayList<Event> eventsTemp = null;
//        if (jsonResult != null && jsonResult.length() != 0) {
//            eventsTemp = Utils.parseJSON(jsonResult.toString());
//        }
//
//        //if remote fetch is successful...
//        if (eventsTemp != null) {
//            Log.i(TAG, "fetchEvents: Successfully fetched and decoded remote JSON.");
//
//            // send events to the database...
//            inputRemoteEventsIntoDatabase(eventsTemp, getApplicationContext());
//        }
//    }
}
