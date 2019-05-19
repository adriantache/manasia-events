package com.adriantache.manasia_events.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import static com.adriantache.manasia_events.util.CommonStrings.DB_EVENT_ID_TAG;
import static com.adriantache.manasia_events.util.Utils.compareDateToToday;
import static com.adriantache.manasia_events.util.Utils.extractDayOrMonth;
import static com.adriantache.manasia_events.util.Utils.isEventToday;

/**
 * Widget for displaying next Manasia event
 **/
public class EventWidget extends AppWidgetProvider {
    private static Bitmap bitmap;
    private static Context context;
    private static Event event;
    private static AppWidgetManager appWidgetManager;
    private static int[] appWidgetIds;

    private static void updateWidgetContents(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.event_widget);

            //set the notification text
            if (event != null) {
                String title = event.getTitle();

                //shorten title if it's longer than 50 chars
                if (title.length() > 50) {
                    title = title.substring(0, 50) + "...";
                }

                if (isEventToday(event.getDate())) {
                    views.setTextViewText(R.id.title, title);
                    views.setTextViewText(R.id.date, "TODAY");
                } else {
                    views.setTextViewText(R.id.title, title);
                    views.setTextViewText(R.id.date,
                            extractDayOrMonth(event.getDate(), true)
                                    + "\n"
                                    + extractDayOrMonth(event.getDate(), false));
                }

                //set intent to open that event's details
                Intent intent = new Intent(context, EventDetail.class);
                intent.putExtra(DB_EVENT_ID_TAG, event.getDatabaseID());
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 3,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT);
                views.setOnClickPendingIntent(R.id.relative_layout, pendingIntent);
            }

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        EventWidget.context = context;
        EventWidget.appWidgetManager = appWidgetManager;
        EventWidget.appWidgetIds = appWidgetIds;

        //todo fetch remote events before updating widget

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
        Event closestEvent = null;
        if (events != null && !events.isEmpty()) {
            for (Event event1 : events) {
                if (compareDateToToday(event1.getDate()) > -1)
                    closestEvent = event1;
            }
            //if all events are in the past, just get the most recent one
            //should look better than if we just display placeholder text
            if (closestEvent == null) closestEvent = events.get(0);
        }
        return closestEvent;
    }

    private static class BitmapAsyncTask extends AsyncTask<String, Void, Void> {

        private static void updateWidgetImages() {
            for (int appWidgetId : appWidgetIds) {
                // Construct the RemoteViews object
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.event_widget);

                //set the notification text
                if (event != null) {
                    String title = event.getTitle();

                    //shorten title if it's longer than 50 chars
                    if (title.length() > 50) {
                        title = title.substring(0, 50) + "...";
                    }

                    if (isEventToday(event.getDate())) {
                        views.setTextViewText(R.id.title, title);
                        views.setTextViewText(R.id.date, "TODAY");
                    } else {
                        views.setTextViewText(R.id.title, title);
                        views.setTextViewText(R.id.date,
                                extractDayOrMonth(event.getDate(), true)
                                        + "\n"
                                        + extractDayOrMonth(event.getDate(), false));
                    }

                    //set intent to open that event's details
                    Intent intent = new Intent(context, EventDetail.class);
                    intent.putExtra(DB_EVENT_ID_TAG, event.getDatabaseID());
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 3,
                            intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    views.setOnClickPendingIntent(R.id.relative_layout, pendingIntent);
                }

                //set the notification image
                if (bitmap != null)
                    views.setImageViewBitmap(R.id.thumbnail, bitmap);
                else {
                    //get image size in pixels
                    int width = (int) context.getResources().getDimension(R.dimen.widget_width);
                    int height = (int) (width * 0.3477f); //using the image aspect ratio

                    //get the manasia logo as default image
                    Bitmap logo = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.manasia_logo_white);
                    //resize the image to the widget size
                    logo = Bitmap.createScaledBitmap(logo, width, height, true);
                    views.setImageViewBitmap(R.id.thumbnail, logo);
                }

                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }

        @Override
        protected Void doInBackground(String... strings) {
            String url;

            if (strings != null && strings.length != 0) {
                url = strings[0];

                try {
                    //get screen density as a multiplier to the widget dimens
                    int height = (int) context.getResources().getDimension(R.dimen.widget_image_height);
                    int width = (int) context.getResources().getDimension(R.dimen.widget_width);

                    //get the image and resize it for the widget size to prevent wasting memory
                    bitmap = Picasso.get()
                            .load(url)
                            .resize(width, height)
                            .centerCrop()
                            .get();
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
