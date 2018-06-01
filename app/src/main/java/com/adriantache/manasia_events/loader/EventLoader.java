package com.adriantache.manasia_events.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.util.Utils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Loader to fetch remote JSON file and decode it into a List of Events
 **/
public class EventLoader extends AsyncTaskLoader<List<Event>> {
    private static String remoteURL;

    public EventLoader(Context context, String URL) {
        super(context);
        remoteURL = URL;
    }

    @Override
    public List<Event> loadInBackground() {
        List<Event> events = null;

        try {
            events = Utils.parseJSON(getJSON(remoteURL));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return events;
    }

    //OKHTTP implementation
    private String getJSON(String url) throws IOException, NullPointerException {
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
