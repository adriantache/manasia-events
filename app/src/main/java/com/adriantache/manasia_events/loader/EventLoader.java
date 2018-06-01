package com.adriantache.manasia_events.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.util.Utils;
import com.squareup.picasso.Picasso;

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

/**
 * Loader to fetch remote JSON file and decode it into a List of Events
 **/
public class EventLoader extends AsyncTaskLoader<List<Event>> {
    private static String remoteURL;

    public EventLoader(Context context, String URL) {
        super(context);
        remoteURL = URL;
    }

    private static ArrayList<Event> parseJSON(String JSON, Context context) {
        if (TextUtils.isEmpty(JSON)) return null;

        ArrayList<Event> events = new ArrayList<>();

        //parse JSON String
        try {
            JSONObject root = new JSONObject(JSON);
            JSONArray event_title = root.getJSONArray("event_title");

            for (int i = 0; i < event_title.length(); i++) {
                JSONObject child = event_title.getJSONObject(i);
                String title = child.getString("name");
                String date = Utils.buildDate(child.getString("month"), child.getString("day"));
                String description = child.optString("description_long");
                if (TextUtils.isEmpty(description)) description = child.getString("description");

                String photo_url = child.optString("image_url");
                Bitmap photo = null;

                if (!TextUtils.isEmpty(photo_url)) {
                    try {
                        photo = Picasso.get().load(Utils.getImageUrl(photo_url)).get();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (photo == null) {
                    photo = BitmapFactory.decodeResource(context.getResources(), R.drawable.manasia_logo);
                }

                //give the description breathing room
                if (description != null)
                    description = description.replace("\n", "\n\n");

                if (date != null && title != null && description != null)
                    events.add(new Event(date, title, description, photo, R.drawable.hub));
            }
        } catch (JSONException e) {
            Log.e("parseJSON", "Cannot parse JSON", e);
        }

        return events;
    }

    @Override
    public List<Event> loadInBackground() {
        List<Event> events = null;

        try {
            events = parseJSON(getJSON(remoteURL), getContext());
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
