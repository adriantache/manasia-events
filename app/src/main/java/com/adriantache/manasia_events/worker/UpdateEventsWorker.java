package com.adriantache.manasia_events.worker;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

import androidx.work.Worker;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Custom class to trigger fetching remote events json file
 **/
public class UpdateEventsWorker extends Worker {
    private static final String REMOTE_URL = "REMOTE_URL";
    private static final String JSON_RESULT = "JSON_STRING";

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

        if (!TextUtils.isEmpty(jsonString) && jsonString.length() > 50) {
            try (PrintWriter printWriter = new PrintWriter(new File(
                    getApplicationContext().getFilesDir(), JSON_RESULT), "UTF-8")) {
                printWriter.write(jsonString);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return Result.SUCCESS;
        } else return Result.FAILURE;
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
}
