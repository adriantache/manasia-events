package com.adriantache.manasia_events;

import android.app.ActivityOptions;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
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
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adriantache.manasia_events.adapter.EventAdapter;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;
import com.adriantache.manasia_events.util.Utils;
import com.adriantache.manasia_events.widget.EventWidget;
import com.adriantache.manasia_events.worker.TriggerUpdateEventsWorker;
import com.adriantache.manasia_events.worker.UpdateEventsWorker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.State;
import androidx.work.WorkManager;

import static com.adriantache.manasia_events.db.DBUtils.inputRemoteEventsIntoDatabase;
import static com.adriantache.manasia_events.notification.NotifyUtils.scheduleNotifications;
import static com.adriantache.manasia_events.util.Utils.calculateDelay;
import static com.adriantache.manasia_events.util.Utils.getRefreshDate;

public class MainActivity extends AppCompatActivity {
    public static final String DBEventIDTag = "DBEventID";
    private static final String TAG = "MainActivity";
    private static final String REMOTE_URL = "REMOTE_URL";
    private static final String ENQUEUE_EVENTS_JSON_WORK_TAG = "enqueueEventsJsonWork";
    private static final String EVENTS_JSON_WORK_TAG = "eventsJsonWork";
    private static final String JSON_RESULT = "JSON_STRING";
    private static final String SHARED_PREFERENCES_TAG = "preferences";
    private static final String NOTIFY_SETTING = "notify";
    private static final String FIRST_LAUNCH_SETTING = "notify";
    private static final String LAST_UPDATE_TIME_SETTING = "LAST_UPDATE_TIME";
    ListView listView;
    ImageView logo;
    ConstraintLayout constraintLayout;
    Button menu;
    TextView error;
    TextView openHours;
    long lastUpdateTime;
    private ArrayList<Event> events;
    private boolean notifyOnAllEvents;
    private boolean firstLaunch;

    //todo dismiss notifications when opening activity from event details (what to do for multiple activities?)

    //todo add food menu to app
    //todo redesign event details screen to move image to under nav and allow image resizing on click

    //todo add progress indicator circle while fetching/decoding events
    //todo add hub details on logo click

    //todo prevent triggering closed Toast every time you visit MainActivity (maybe use StartActivityForResult instead and only show if no result?)

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

        listView = findViewById(R.id.list_view);
        logo = findViewById(R.id.logo);
        constraintLayout = findViewById(R.id.constraint_layout);
        menu = findViewById(R.id.menu);
        error = findViewById(R.id.error);
        openHours = findViewById(R.id.open_hours);

        //get shared prefs
        getPreferences();

        //update open hours TextView
        Utils.getOpenHours(openHours, this, firstLaunch);
        //reset the first launch flag so it doesn't remain set to false
        //todo rethink this mechanism, user might not always return from EventDetail or MenuActivity...
        //todo ...might make more sense to just use startActivityForResult with those activities
        SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FIRST_LAUNCH_SETTING, true);
        editor.apply();

        //populate the list initially since theoretically the latest events are already in the DB
        updateFromDatabase();

        //update events and display them, if available
        schedulePeriodicRemoteEventsFetch();

        //show snackbar if user hasn't chosen to be notified for all events
        if (!notifyOnAllEvents) showSnackbar();
    }

    //todo reschedule notifications on remote events fetch
    private void schedulePeriodicRemoteEventsFetch() {
        //normally just schedule a periodic work request to fetch events daily
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

            //schedule the periodic work request for 5am, which will trigger the actual work request
            OneTimeWorkRequest getEventJson = new OneTimeWorkRequest
                    .Builder(TriggerUpdateEventsWorker.class)
                    .setInitialDelay(calculateDelay(getRefreshDate()), TimeUnit.MILLISECONDS)
                    .setInputData(remoteUrl)
                    .addTag(ENQUEUE_EVENTS_JSON_WORK_TAG)
                    .build();
            WorkManager.getInstance().enqueue(getEventJson);
        }
    }

    //tasks which run on remote events refresh or in case that refresh is not possible
    public void updateFromDatabase() {
        //...then reading that database (this also populates the ArrayList with the very important
        // DBEventID value to pass along throughout the app)
        events = (ArrayList<Event>) DBUtils.readDatabase(this);

        //first check to see if events are missing (database is empty) or stale (last updated >25 hours ago)
        Calendar calendar = Calendar.getInstance();
        //get shared prefs again to see if lastUpdateTime changed
        getPreferences();
        if (events == null || calendar.getTimeInMillis() - lastUpdateTime > 90000000L) {
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

                //schedule the periodic work request for 5am, which will trigger the actual work request
                OneTimeWorkRequest getEventJson = new OneTimeWorkRequest
                        .Builder(UpdateEventsWorker.class)
                        .setInputData(remoteUrl)
                        .addTag(EVENTS_JSON_WORK_TAG)
                        .build();
                WorkManager.getInstance().enqueue(getEventJson);

                WorkManager.getInstance()
                        .getStatusById(getEventJson.getId())
                        .observe(MainActivity.this, workStatus -> {
                            if (workStatus != null && workStatus.getState().equals(State.SUCCEEDED)) {
                                //once we have confirmation the loader has succeeded, we use a character-
                                //based stream to read the file and get the JSON string
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

                                //we decode the JSON into events ArrayList
                                ArrayList<Event> eventsTemp = null;
                                if (jsonResult != null && jsonResult.length() != 0) {
                                    eventsTemp = Utils.parseJSON(jsonResult.toString());
                                }

                                //and if remote fetch is successful...
                                if (eventsTemp != null) {
                                    Log.i(TAG, "fetch events: Successfully fetched and decoded remote JSON.");
                                    // we send events to the database...
                                    inputRemoteEventsIntoDatabase(eventsTemp, getApplicationContext());
                                }

                                //...and finally run tasks post database update
                                updateFromDatabase();
                            }
                        });
            }
        }

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

        //todo figure out why this isn't working
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
            if (getResources().getAssets().list("") != null &&
                    Arrays.asList(getResources().getAssets().list("")).contains("dataURL.txt")) {
                AssetManager am = getApplicationContext().getAssets();
                InputStream inputStream = am.open("dataURL.txt");

                int ch;
                StringBuilder sb = new StringBuilder();
                while ((ch = inputStream.read()) != -1) {
                    sb.append((char) ch);
                }

                if (sb.length() != 0) remoteURL = sb.toString();
            }
        } catch (IOException e) {
            Log.e(TAG, "Cannot open API key file.", e);
        }

        return remoteURL;
    }

    private void getPreferences() {
        SharedPreferences sharedPrefs = this.getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
        //set notify on every future event flag
        notifyOnAllEvents = sharedPrefs.getBoolean(NOTIFY_SETTING, false);
        //set time of last remote update
        lastUpdateTime = sharedPrefs.getLong(LAST_UPDATE_TIME_SETTING, 0);
        //set whether this is the first launch of MainActivity to prevent open hours Toast when coming back
        firstLaunch = sharedPrefs.getBoolean(FIRST_LAUNCH_SETTING, true);
    }

    //todo figure out if we still care about this, all it does is change notify status, would only trigger if notifyForAllEvents is false
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
        TextView textView = view.findViewById(R.id.snackbar_text);
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
//todo replace ListView with RecyclerView