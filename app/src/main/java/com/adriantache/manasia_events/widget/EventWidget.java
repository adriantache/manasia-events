package com.adriantache.manasia_events.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;

import java.util.ArrayList;

import static com.adriantache.manasia_events.util.Utils.compareDateToToday;
import static com.adriantache.manasia_events.util.Utils.extractDayOrMonth;

/**
 * Widget for displaying next Manasia event
 **/
public class EventWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                 int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.event_widget);

        ArrayList<Event> events = (ArrayList<Event>) DBUtils.readDatabase(context);
        Event event = null;

        //fetch the closest event to today
        if (events != null) {
            for (Event event1 : events) {
                if (compareDateToToday(event1.getDate()) > -1)
                    event = event1;
            }
        }

        //if all events are in the past, just get the most recent one;
        //should look better than if we just display placeholder text
        if (event == null) event = events.get(0);

        if (event != null) {
            views.setTextViewText(R.id.title, event.getTitle());
            views.setTextViewText(R.id.date,
                    extractDayOrMonth(event.getDate(),true)
                            +"\n"
                            +extractDayOrMonth(event.getDate(),false));
            views.setImageViewUri(R.id.thumbnail, Uri.parse(event.getPhotoUrl()));
        }


        // Construct an Intent object includes web adresss.
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://erenutku.com"));
//        // In widget we are not allowing to use intents as usually. We have to use PendingIntent instead of 'startActivity'
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//        // Here the basic operations the remote view can do.
//        views.setOnClickPendingIntent(R.id.title, pendingIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}
