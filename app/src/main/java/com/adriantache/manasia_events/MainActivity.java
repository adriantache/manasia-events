package com.adriantache.manasia_events;

import android.app.ActivityOptions;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.transition.TransitionManager;
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
import com.adriantache.manasia_events.loader.EventLoader;
import com.adriantache.manasia_events.util.Utils;
import com.adriantache.manasia_events.widget.EventWidget;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.adriantache.manasia_events.EventDetail.NOTIFY;
import static com.adriantache.manasia_events.EventDetail.SHARED_PREFERENCES_TAG;
import static com.adriantache.manasia_events.db.EventContract.CONTENT_URI;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_CATEGORY_IMAGE;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DATE;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_DESCRIPTION;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_NOTIFY;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_PHOTO_URL;
import static com.adriantache.manasia_events.db.EventContract.EventEntry.COLUMN_EVENT_TITLE;
import static com.adriantache.manasia_events.notification.NotifyUtils.scheduleNotifications;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Event>> {
    public static final String DBEventIDTag = "DBEventID";
    private static final String TAG = "MainActivity";
    public ArrayList<Event> events;
    @BindView(R.id.list_view)
    ListView listView;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.busy_level)
    TextView busy_level;
    @BindView(R.id.constraint_layout)
    ConstraintLayout constraint_layout;
    @BindView(R.id.menu)
    Button menu;
    private String REMOTE_URL;
    private boolean notifyOnAllEvents;
    private boolean layoutAnimated = false;

    //todo test if necessary after using TaskStackBuilder
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

        if (!notifyOnAllEvents) showSnackbar();

        //get remote URL or use local data
        REMOTE_URL = getRemoteURL();

        //todo prevent unnecessary database updates
        //populate the global ArrayList of events by updating database and...
        getSupportLoaderManager().initLoader(1, null, this).forceLoad();
        //...then reading that database (this also populates the ArrayList with the very important
        //DBEventID value to pass along throughout the app
        events = (ArrayList<Event>) DBUtils.readDatabase(this);
        //and since we're at it also update the widget(s) with the new event data
        Intent intent = new Intent(this, EventWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), EventWidget.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);

        if (events != null) {
            populateListView();
        }

        //code to minimize and maximize logo on click (maybe not terribly useful, but it looks neat)
        //todo modify code to show some useful info instead of just minimizing logo (figure out available size)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            logo.setOnClickListener(v -> minimizeLogo());
        }

        //todo figure out how to fetch this (ideally same place we store the JSON or database)
        updateBusyLevel();
    }

    private void populateListView() {
        //populate list
        //todo set empty list text view and progress bar
        listView.setAdapter(new EventAdapter(this, events));

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
                                    Pair.create(view.findViewById(R.id.thumbnail), "thumbnail"),
                                    Pair.create(view.findViewById(R.id.category_image), "category_image")
                            );
                else
                    options = ActivityOptions
                            .makeSceneTransitionAnimation(this,
                                    Pair.create(view.findViewById(R.id.thumbnail), "thumbnail"),
                                    Pair.create(view.findViewById(R.id.notify_status), "notify_status"),
                                    Pair.create(view.findViewById(R.id.category_image), "category_image")
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
                values.put(COLUMN_EVENT_CATEGORY_IMAGE, event.getCategory_image());
                values.put(COLUMN_EVENT_NOTIFY, event.getNotify());

                getContentResolver().insert(CONTENT_URI, values);
            }

            //update event notifications for all future events fetched from the remote database
            if (notifyOnAllEvents) scheduleNotifications(this, true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void minimizeLogo() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(getApplicationContext(), R.layout.activity_main_animate);
        ConstraintSet initialConstraintSet = new ConstraintSet();
        initialConstraintSet.clone(getApplicationContext(), R.layout.activity_main);
        ConstraintLayout mConstraintLayout = findViewById(R.id.constraint_layout);
        TransitionManager.beginDelayedTransition(mConstraintLayout);
        if (!layoutAnimated) {
            constraintSet.applyTo(mConstraintLayout);
            layoutAnimated = true;
        } else {
            initialConstraintSet.applyTo(mConstraintLayout);
            layoutAnimated = false;
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void updateBusyLevel() {
        //todo implement actual code
        String busyLevel = "Prietenos";
        busy_level.setText(busyLevel);
        switch (busyLevel) {
            case "Lejer":
                busy_level.setTextColor(0xff2196F3);
                break;
            case "Prietenos":
                busy_level.setTextColor(0xff4CAF50);
                break;
            case "Optim":
                busy_level.setTextColor(0xffE91E63);
                break;
            case "Full":
                busy_level.setTextColor(0xfff44336);
                break;
            default:
                busy_level.setTextColor(0xff000000);
                break;
        }
    }

    private void getPreferences() {
        SharedPreferences sharedPrefs = this.getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
        notifyOnAllEvents = sharedPrefs.getBoolean(NOTIFY, false);
    }


    //todo refactor this when we change the way we get event data
    private void refreshList() {
        events = (ArrayList<Event>) DBUtils.readDatabase(this);

        if (events != null)
            listView.setAdapter(new EventAdapter(this, events));
    }

    public void showSnackbar() {
        Snackbar snackbar = Snackbar.make(constraint_layout,
                "Would you like to be notified for all events?",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Activate", v -> {
            SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(NOTIFY, true);
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

    @NonNull
    @Override
    public Loader<List<Event>> onCreateLoader(int id, Bundle args) {
        if (TextUtils.isEmpty(REMOTE_URL))
            return new EventLoader(this, Utils.getSampleJSON(this));
        else
            return new EventLoader(this, REMOTE_URL);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Event>> loader, List<Event> data) {
        inputRemoteEventsIntoDatabase((ArrayList<Event>) data);
        events = (ArrayList<Event>) DBUtils.readDatabase(this);
        populateListView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Event>> loader) {
        listView.setAdapter(new EventAdapter(this, new ArrayList<>()));
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
