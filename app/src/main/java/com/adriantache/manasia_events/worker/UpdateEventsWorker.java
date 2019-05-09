package com.adriantache.manasia_events.worker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.util.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.adriantache.manasia_events.db.DBUtils.inputRemoteEventsIntoDatabase;
import static com.adriantache.manasia_events.util.CommonStrings.REMOTE_URL;

/**
 * Custom class to trigger fetching remote events json file
 **/
public class UpdateEventsWorker extends Worker {
    private static final String TAG = "UpdateEventsWRK";

    public UpdateEventsWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        final String remoteUrl = getInputData().getString(REMOTE_URL);

        String jsonString = null;
        try {
            jsonString = getJSON(remoteUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (jsonString != null && jsonString.length() > 50) {
            storeJSON(jsonString);
            return Result.success();
        } else return Result.failure();
    }

    //OKHTTP implementation
    private String getJSON(String url) throws IOException {
        //override timeouts to ensure receiving full JSON
        OkHttpClient.Builder b = new OkHttpClient.Builder();
        b.connectTimeout(15, TimeUnit.SECONDS);
        b.readTimeout(15, TimeUnit.SECONDS);
        b.writeTimeout(15, TimeUnit.SECONDS);
        OkHttpClient client = b.build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        String JSON = null;
        try {
            JSON = response.body().string();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return JSON;
    }

    private void storeJSON(String jsonResult) {
        //decode the JSON into events ArrayList
        ArrayList<Event> eventsTemp = null;
        if (jsonResult != null && jsonResult.length() != 0) {
            eventsTemp = Utils.parseJSON(jsonResult);
        }

        //if remote fetch is successful...
        if (eventsTemp != null) {
            Log.i(TAG, "fetchEvents: Successfully fetched and decoded remote JSON.");

            // send events to the database...
            inputRemoteEventsIntoDatabase(eventsTemp, getApplicationContext());
        }
    }
}
