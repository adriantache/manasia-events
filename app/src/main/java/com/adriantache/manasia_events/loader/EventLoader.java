package com.adriantache.manasia_events.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

import com.adriantache.manasia_events.custom_class.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.adriantache.manasia_events.util.Utils.buildDate;
import static com.adriantache.manasia_events.util.Utils.getImageUrl;

/**
 * Loader to fetch remote JSON file and decode it into a List of Events
 **/
public class EventLoader extends AsyncTaskLoader<List<Event>> {
    private String remoteURL;

    public EventLoader(Context context, String URL) {
        super(context);
        remoteURL = URL;
    }

    private static ArrayList<Event> parseJSON(String JSON) {
        if (TextUtils.isEmpty(JSON) || JSON.length() < 50) return new ArrayList<>();

        ArrayList<Event> events = new ArrayList<>();

        //parse JSON String
        try {
            JSONObject root = new JSONObject(JSON);
            JSONArray eventTitle = root.optJSONArray("event_title");

            for (int i = 0; i < eventTitle.length(); i++) {
                JSONObject child = eventTitle.optJSONObject(i);
                String title = child.optString("name");
                String date = buildDate(child.optString("month"), child.optString("day"));
                String description = child.optString("description_long");
                if (TextUtils.isEmpty(description))
                    description = child.optString("description");
                String imageUrl = child.optString("image_url");
                if (!TextUtils.isEmpty(imageUrl)) imageUrl = getImageUrl(imageUrl);

                //give the description breathing room
                if (description != null)
                    description = description.replace("\n", "\n\n");

                if (date != null && title != null && description != null)
                    events.add(new Event(date, title, description, imageUrl));
            }
        } catch (JSONException e) {
            Log.e("parseJSON", "Cannot parse JSON", e);
            events = new ArrayList<>();
        }

        return events;
    }

    @Override
    public List<Event> loadInBackground() {
        List<Event> events = null;

        try {
            if (remoteURL.length() > 100) {
                events = parseJSON(remoteURL);
            } else
                events = parseJSON(getJSON(remoteURL));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return events;
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
