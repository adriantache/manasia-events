package com.adriantache.manasia_events;

import android.app.ActivityOptions;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adriantache.manasia_events.adapter.EventAdapter;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;
import com.adriantache.manasia_events.util.Utils;
import com.adriantache.manasia_events.widget.EventWidget;
import com.adriantache.manasia_events.worker.UpdateEventsWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.State;
import androidx.work.WorkManager;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.adriantache.manasia_events.EventDetail.NOTIFY_SETTING;
import static com.adriantache.manasia_events.EventDetail.SHARED_PREFERENCES_TAG;
import static com.adriantache.manasia_events.db.EventContract.CONTENT_URI;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DATE;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DESCRIPTION;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_NOTIFY;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_PHOTO_URL;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_TITLE;
import static com.adriantache.manasia_events.notification.NotifyUtils.scheduleNotifications;

public class MainActivity extends AppCompatActivity {
    public static final String DBEventIDTag = "DBEventID";
    private static final String TAG = "MainActivity";
    private static final String LAST_UPDATE_TIME_LABEL = "LAST_UPDATE_TIME";
    private static final String REMOTE_URL = "REMOTE_URL";
    private static final String JSON_RESULT = "JSON_STRING";
    private static final String EVENTS_JSON_WORK_TAG = "eventsJsonWork";
    @BindView(R.id.list_view)
    ListView listView;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.constraint_layout)
    ConstraintLayout constraintLayout;
    @BindView(R.id.menu)
    ImageButton menu;
    @BindView(R.id.error)
    TextView error;
    @BindView(R.id.open_hours)
    TextView openHours;
    @BindView(R.id.open_or_closed)
    ImageView openOrClosed;
    long lastUpdateTime;
    private ArrayList<Event> events;
    private boolean notifyOnAllEvents;

    //todo refresh database if events are seriously outdated
    //todo use WorkManager to schedule database refresh
    //todo add open hours to app [today open until ...] and open/closed blob

    //todo dismiss notifications when opening activity from event details (what to do for multiple activities?)
    //todo replace ListView with RecyclerView

    //todo add food menu to app
    //todo redesign event details screen to move image to under nav and allow image resizing on click

    //todo add progress indicator circle while fetching/decoding events

    //closes app on back pressed to prevent infinite loop due to how the stack is built coming from a notification
    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    //update list from db when returning from EventDetail
    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //retrieve SharedPrefs before binding the ArrayAdapter
        getPreferences();

        //update events and display them, if available
        fetchEvents();

        //show snackbar if user hasn't chosen to be notified for all events
        if (!notifyOnAllEvents) showSnackbar();

        //update open hours TextView
        Utils.getOpenHours(openHours, openOrClosed);
    }

    //todo reschedule notifications on remote events fetch
    //todo schedule remote events fetch before 12 pm to prevent notifications fix deleting notifications
    private void fetchEvents() {
        //we read when the database was last fetched from remote, and if time is more than
        // one hour we refresh the remote source
        //todo rethink time interval since events are only refreshed daily at ~4am
        //todo trigger immediate events fetch if last update is >24h-4am, otherwise schedule update for 5am EEST/EET
        Calendar calendar = Calendar.getInstance();
        if (calendar.getTimeInMillis() - lastUpdateTime > 3600 * 1000) {
            //test network connectivity to prevent unnecessary remote update attempt
            ConnectivityManager cm = (ConnectivityManager)
                    getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = null;
            try {
                if (cm != null) {
                    activeNetwork = cm.getActiveNetworkInfo();
                }
            } catch (NullPointerException e) {
                Log.e(TAG, "Cannot get network info.", e);
            }
            if (activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting()) {
                //populate the global ArrayList of events by adding a work request to fetch JSON...
                Data remoteUrl = new Data.Builder().putString(REMOTE_URL, getRemoteURL()).build();
                OneTimeWorkRequest getEventJson = new OneTimeWorkRequest
                        .Builder(UpdateEventsWorker.class)
                        .setInputData(remoteUrl)
                        .addTag(EVENTS_JSON_WORK_TAG)
                        .build();
                WorkManager.getInstance().enqueue(getEventJson);

                //...then get the result...
                WorkManager.getInstance()
                        .getStatusById(getEventJson.getId())
                        .observe(MainActivity.this, workStatus -> {
                            if (workStatus != null && workStatus.getState().equals(State.SUCCEEDED)) {
                                //get results JSON
                                StringBuilder jsonResult = null;

                                try (BufferedReader bufferedReader =
                                             new BufferedReader(
                                                     new InputStreamReader(
                                                             getApplicationContext().openFileInput(JSON_RESULT)))) {

                                    jsonResult = new StringBuilder();
                                    int i;
                                    while ((i = bufferedReader.read()) != -1) {
                                        jsonResult.append((char) i);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                //decode the JSON into events ArrayList
                                ArrayList<Event> eventsTemp = null;
                                if (jsonResult != null && jsonResult.length() != 0) {
                                    eventsTemp = Utils.parseJSON(jsonResult.toString());
                                }

                                //if remote fetch is successful...
                                if (eventsTemp != null) {
                                    Log.i(TAG, "fetchEvents: Successfully fetched and decoded remote JSON.");

                                    // send events to the database...
                                    inputRemoteEventsIntoDatabase(eventsTemp);

                                    // and write fetch date into SharedPrefs
                                    lastUpdateTime = calendar.getTimeInMillis();
                                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putLong(LAST_UPDATE_TIME_LABEL, lastUpdateTime);
                                    editor.apply();

                                    //todo trigger notifications scheduling update here
                                }

                                //...finally run tasks post database update
                                afterDatabaseUpdate();
                            }
                        });
            }
        } else {
            //otherwise just run after update tasks as if we already fetched remote data
            afterDatabaseUpdate();
        }
    }

    //tasks which run on remote events refresh or in case that refresh is not possible
    private void afterDatabaseUpdate() {
        //...then reading that database (this also populates the ArrayList with the very important
        // DBEventID value to pass along throughout the app)
        events = (ArrayList<Event>) DBUtils.readDatabase(this);

        if (events != null) {
            //and since we're at it also update the widget(s) with the new event data
            Intent intent = new Intent(this, EventWidget.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
            // since it seems the onUpdate() is only fired on that:
            int[] ids = AppWidgetManager.getInstance(getApplication())
                    .getAppWidgetIds(new ComponentName(getApplication(), EventWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
            sendBroadcast(intent);

            populateListView();
        }
    }

    private void populateListView() {
        //populate list
        listView.setAdapter(new EventAdapter(this, events));
        listView.setEmptyView(error);

        //set click listener and transition animation
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event event = (Event) parent.getItemAtPosition(position);

            Intent intent = new Intent(getApplicationContext(), EventDetail.class);
            intent.putExtra(DBEventIDTag, event.getDatabaseID());

            //code to animate event details between activities
            ActivityOptions options = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (Utils.compareDateToToday(event.getDate()) < 0)
                    options = ActivityOptions
                            .makeSceneTransitionAnimation(this,
                                    Pair.create(view.findViewById(R.id.thumbnail), "thumbnail")
                            );
                else
                    options = ActivityOptions
                            .makeSceneTransitionAnimation(this,
                                    Pair.create(view.findViewById(R.id.thumbnail), "thumbnail"),
                                    Pair.create(view.findViewById(R.id.notify_status), "notifyStatus")
                            );
            }

            //if we can animate, do that, otherwise just open the EventDetail activity
            if (options != null) {
                startActivity(intent, options.toBundle());
            } else {
                startActivity(intent);
            }
        });

        menu.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
            startActivity(intent);
        });
    }

    private String getRemoteURL() {
        String remoteURL = null;

        //get API key from file
        try {
            if (Arrays.asList(getResources().getAssets().list("")).contains("dataURL.txt")) {
                AssetManager am = getApplicationContext().getAssets();
                InputStream inputStream = null;
                try {
                    inputStream = am.open("dataURL.txt");
                } catch (IOException e) {
                    Log.e(TAG, "Cannot read API key from file.", e);
                }

                if (inputStream != null) {
                    int ch;
                    StringBuilder sb = new StringBuilder();
                    try {
                        while ((ch = inputStream.read()) != -1) {
                            sb.append((char) ch);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Cannot read API key InputStream.", e);
                    }

                    if (sb.length() != 0) remoteURL = sb.toString();
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Cannot open API key file.", e);
        }

        return remoteURL;
    }

    private void inputRemoteEventsIntoDatabase(ArrayList<Event> remoteEvents) {
        if (remoteEvents != null) {
            //first of all transfer all notify statuses from the local database to the temporary remote database
            ArrayList<Event> DBEvents = (ArrayList<Event>) DBUtils.readDatabase(this);
            remoteEvents = Utils.updateNotifyInRemote(remoteEvents, DBEvents);

            //then delete ALL events from the local table1
            getContentResolver().delete(CONTENT_URI, null, null);

            //then add the remote events to the local database
            for (Event event : remoteEvents) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_EVENT_TITLE, event.getTitle());
                values.put(COLUMN_EVENT_DESCRIPTION, event.getDescription());
                values.put(COLUMN_EVENT_DATE, event.getDate());
                if (!TextUtils.isEmpty(event.getPhotoUrl()))
                    values.put(COLUMN_EVENT_PHOTO_URL, event.getPhotoUrl());
                values.put(COLUMN_EVENT_NOTIFY, event.getNotify());

                getContentResolver().insert(CONTENT_URI, values);
            }

            //update event notifications for all future events fetched from the remote database
            if (notifyOnAllEvents) scheduleNotifications(this, true);
        }
    }

    private void getPreferences() {
        SharedPreferences sharedPrefs = this.getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
        //set notify on every future event flag
        notifyOnAllEvents = sharedPrefs.getBoolean(NOTIFY_SETTING, false);
        //set time of last remote update
        lastUpdateTime = sharedPrefs.getLong(LAST_UPDATE_TIME_LABEL, 0);
    }

    private void refreshList() {
        events = (ArrayList<Event>) DBUtils.readDatabase(this);

        if (events != null)
            listView.setAdapter(new EventAdapter(this, events));
    }

    public void showSnackbar() {
        Snackbar snackbar = Snackbar.make(constraintLayout,
                "Would you like to be notified for all events?",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Activate", v -> {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(NOTIFY_SETTING, true);
            editor.apply();

            notifyOnAllEvents = true;

            //since we're activating the setting to always be notified, go ahead and schedule notifications
            scheduleNotifications(getApplicationContext(), true);

            Toast.makeText(this, "We will notify you for all future events.", Toast.LENGTH_SHORT).show();
            refreshList();
        });

        snackbar.show();
        View view = snackbar.getView();
        TextView textView = view.findViewById(android.support.design.R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
    }
}

//todo [HIGH] implement notification handling; auto-eliminate notifications in the past
//todo add notification on new events added to remote database
//todo extract all strings into XML
//todo fix any warnings/errors
//todo [IDEA] feature hub shops to attract clients (i.e. give them space in the app or include their events)
//todo create event info subclass
//todo add info about the hub somewhere (on logo click?) and indicate it visually
//todo figure out data storage (firebase? facebook api?)
//todo create method to keep database clean (<100 entries?)
//todo [low] translate app (modify class, ensure input of event translations)
//todo implement splash screen while fetching remote data
//todo implement SwipeRefreshLayout (is it really needed?)
