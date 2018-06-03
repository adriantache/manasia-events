package com.adriantache.manasia_events.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.widget.RemoteViews;

import com.adriantache.manasia_events.EventDetail;
import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

import static com.adriantache.manasia_events.MainActivity.DBEventIDTag;
import static com.adriantache.manasia_events.util.Utils.compareDateToToday;
import static com.adriantache.manasia_events.util.Utils.extractDayOrMonth;

/**
 * Widget for displaying next Manasia event
 **/
public class EventWidget extends AppWidgetProvider {
    private static Bitmap bitmap;
    private Context context;
    private Event event;
    private AppWidgetManager appWidgetManager;
    private int[] appWidgetIds;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        this.context = context;
        this.appWidgetManager = appWidgetManager;
        this.appWidgetIds = appWidgetIds;

        event = getEvent(context);

        if (event != null) {
            updateWidgetContents(context, appWidgetManager, appWidgetIds);

            new BitmapAsyncTask().execute(event.getPhotoUrl());
        }
    }

    @Nullable
    private Event getEvent(Context context) {
        //fetch the events array from the database
        ArrayList<Event> events = (ArrayList<Event>) DBUtils.readDatabase(context);

        //fetch the closest event to today
        Event event = null;
        if (events != null && events.size() != 0) {
            for (Event event1 : events) {
                if (compareDateToToday(event1.getDate()) > -1)
                    event = event1;
            }
            //if all events are in the past, just get the most recent one;
            //should look better than if we just display placeholder text
            if (event == null) event = events.get(0);
        }
        return event;
    }

    private void updateWidgetContents(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.event_widget);

            //set the notification text
            if (event != null) {
                views.setTextViewText(R.id.title, event.getTitle());
                views.setTextViewText(R.id.date,
                        extractDayOrMonth(event.getDate(), true)
                                + "\n"
                                + extractDayOrMonth(event.getDate(), false));
            }

            //set intent to open that event's details
            Intent intent = new Intent(context, EventDetail.class);
            intent.putExtra(DBEventIDTag, event.getDatabaseID());
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 3,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.relative_layout, pendingIntent);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private void updateWidgetImages() {
        for (int appWidgetId : appWidgetIds) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.event_widget);

            //set the notification text
            if (event != null) {
                views.setTextViewText(R.id.title, event.getTitle());
                views.setTextViewText(R.id.date,
                        extractDayOrMonth(event.getDate(), true)
                                + "\n"
                                + extractDayOrMonth(event.getDate(), false));
            }

            //set the notification image
            if (bitmap != null)
                views.setImageViewBitmap(R.id.thumbnail, bitmap);

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private class BitmapAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... strings) {
            String url;

            if (strings != null && strings.length != 0) {
                url = strings[0];

                try {
                    bitmap = Picasso.get().load(url).resize(880,520).centerCrop().get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updateWidgetImages();
        }
    }
}
