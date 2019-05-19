package com.adriantache.manasia_events;

import android.app.ActivityOptions;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.Observer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.adriantache.manasia_events.adapter.EventAdapter;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;
import com.adriantache.manasia_events.util.Utils;
import com.adriantache.manasia_events.widget.EventWidget;
import com.adriantache.manasia_events.worker.TriggerUpdateEventsWorker;
import com.adriantache.manasia_events.worker.UpdateEventsWorker;
import com.ramotion.foldingcell.FoldingCell;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.adriantache.manasia_events.notification.NotifyUtils.scheduleNotifications;
import static com.adriantache.manasia_events.util.CommonStrings.DB_EVENT_ID_TAG;
import static com.adriantache.manasia_events.util.CommonStrings.ENQUEUE_EVENTS_JSON_WORK_TAG;
import static com.adriantache.manasia_events.util.CommonStrings.EVENTS_JSON_WORK_TAG;
import static com.adriantache.manasia_events.util.CommonStrings.EVENTS_JSON_WORK_TAG_FORCED;
import static com.adriantache.manasia_events.util.CommonStrings.EVENT_UPDATE_HOUR;
import static com.adriantache.manasia_events.util.CommonStrings.FIRST_LAUNCH_SETTING;
import static com.adriantache.manasia_events.util.CommonStrings.LAST_UPDATE_TIME_SETTING;
import static com.adriantache.manasia_events.util.CommonStrings.NOTIFICATION_WORK_TAG;
import static com.adriantache.manasia_events.util.CommonStrings.NOTIFY_SETTING;
import static com.adriantache.manasia_events.util.CommonStrings.REMOTE_URL;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_ACTIVITY;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_MAIN_ACTIVITY;
import static com.adriantache.manasia_events.util.Utils.calculateDelay;
import static com.adriantache.manasia_events.util.Utils.getRefreshDate;

public class MainActivity extends AppCompatActivity
        implements PopupMenu.OnMenuItemClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "MainActivity";
    long lastUpdateTime;
    private ListView listView;
    private ConstraintLayout constraintLayout;
    private ImageView menu;
    private TextView error;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Event> events;
    private boolean notifyOnAllEvents;
    private boolean firstLaunch;
    private Map<String, Integer> tagMap;
    private FoldingCell fc;
    private ArrayList<String> filtersSet = new ArrayList<>();

    //todo dismiss notifications when opening activity from event details (what to do for multiple activities?)

    //todo add food menu to app
    //todo redesign event details screen to move image to under nav and allow image resizing on click

    //todo add progress indicator circle while fetching/decoding events
    //todo add hub details on logo click

    //closes app on back pressed to prevent infinite loop due to how the stack is built coming from a notification
    @Override
    public void onBackPressed() {
        //fold filters on back press if they're open
        if (fc.isUnfolded()) fc.fold(false);
        else {
            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(homeIntent);
        }
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
        constraintLayout = findViewById(R.id.constraint_layout);
        menu = findViewById(R.id.menu);
        error = findViewById(R.id.error);
        TextView openHours = findViewById(R.id.open_hours);

        //set up SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        // implement FoldingCell
        fc = findViewById(R.id.folding_cell);
        // attach click listener to folding cell
        fc.setOnClickListener(v -> fc.toggle(false));
        //todo play with this
        //fc.initialize(90, 1000, Color.DKGRAY, 1);

        //set default preferences on first launch, in case this matters for some reason
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        //get shared prefs
        getPreferences();
        //show snackbar if user hasn't chosen to be notified for all events
        if (!notifyOnAllEvents) showSnackbar();

        //update open hours TextView
        Utils.getOpenHours(openHours, this, firstLaunch);
        //reset the first launch flag so it doesn't remain set to false
        //todo rethink this mechanism, user might not always return from EventDetail or DrinksMenuActivity...
        //todo ...might make more sense to just use startActivityForResult with those activities
        SharedPreferences sharedPref = getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FIRST_LAUNCH_SETTING, true);
        editor.apply();

        //populate the list initially since theoretically the latest events are already in the DB
        updateFromDatabase(true);

        //update events and display them, if available
        schedulePeriodicRemoteEventsFetch();

        // Create the observer which fetches WorkManager status.
        final Observer<List<WorkInfo>> workStatusObserver = list -> {
            if (list == null) return;

            StringBuilder sb = new StringBuilder();
            int i = 0;

            for (WorkInfo w : list) {
                sb.append(" \n");
                sb.append(i++);
                sb.append(". ");

                sb.append(w.getId());
                sb.append(" ");
                sb.append(w.getTags());
                sb.append(" ");

                WorkInfo.State state = w.getState();
                sb.append(state);
                sb.append(" ");

//                  not available in 1.0.1
//                  sb.append(w.getRunAttemptCount());

                //update main screen if we have a work success for the forced fetch
                if (w.getTags().contains(EVENTS_JSON_WORK_TAG_FORCED) &&
                        (state == WorkInfo.State.SUCCEEDED || state == WorkInfo.State.FAILED)) {
                    //refresh displayed events
                    if (state == WorkInfo.State.SUCCEEDED) updateFromDatabase(false);

                    //stop refresh indicator
                    if (swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);
                }
            }

            Log.i("WRK status", sb.toString());
        };
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        //todo prune unneeded observers
        WorkManager.getInstance().getWorkInfosByTagLiveData(EVENTS_JSON_WORK_TAG).observe(this, workStatusObserver);
        WorkManager.getInstance().getWorkInfosByTagLiveData(EVENTS_JSON_WORK_TAG_FORCED).observe(this, workStatusObserver);
        WorkManager.getInstance().getWorkInfosByTagLiveData(NOTIFICATION_WORK_TAG).observe(this, workStatusObserver);
        WorkManager.getInstance().pruneWork();
    }

    //todo reschedule notifications on remote events fetch (probably already happens)
    private void schedulePeriodicRemoteEventsFetch() {
        //todo normally just schedule a periodic work request to fetch events daily when setInitialDelay works
        //populate the global ArrayList of events by adding a work request to fetch JSON...
        Data remoteUrl = new Data.Builder().putString(REMOTE_URL, getRemoteURL()).build();

        //set constraints to the work to prevent it running when it's not useful
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        //schedule the periodic work request for 5am, which will trigger the actual work request
        //todo prevent triggering this if PeriodicWorkRequest already exists
        //todo replace this directly with PeriodicWorkRequest with setInitialDelay once alpha02 launches
        OneTimeWorkRequest getEventJson = new OneTimeWorkRequest
                .Builder(TriggerUpdateEventsWorker.class)
                .setInitialDelay(calculateDelay(getRefreshDate()), TimeUnit.MILLISECONDS)
                .setInputData(remoteUrl)
                .setConstraints(constraints)
                .addTag(ENQUEUE_EVENTS_JSON_WORK_TAG)
                .build();
        WorkManager.getInstance().enqueue(getEventJson);
    }

    //tasks which run on remote events refresh or in case that refresh is not possible
    //todo refactor this whole class, but this for sure, it's weird
    //todo test if local file has additional events compared to database, might signal problem with listenableFuture
    public void updateFromDatabase(boolean checkStale) {
        //...then reading that database (this also populates the ArrayList with the very important
        // DBEventID value to pass along throughout the app)
        events = (ArrayList<Event>) DBUtils.readDatabase(this);

        //only check if events are stale if we ask for it
        if (checkStale) {
            //first check to see if events are missing (database is empty) or stale (last updated
            // before normal update time)
            Calendar calendar = Calendar.getInstance();
            // calculating the amount of hours since EVENT_UPDATE_HOUR + 1 to compensate for difference in minutes
            int timeSinceEUH = calendar.get(Calendar.HOUR_OF_DAY) - EVENT_UPDATE_HOUR + 1;
            int hoursSinceLUT = (int) ((calendar.getTimeInMillis() - lastUpdateTime) / 1000 / 3600);

            //get shared prefs again to see if lastUpdateTime changed
            getPreferences();

            //check if events are empty or time since LUT > time since EUH
            if (events == null || hoursSinceLUT > timeSinceEUH) {
                Log.i("WRKStart", "Events are stale, triggering forced update.");

                triggerImmediateRemoteUpdate();
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

            //also compute tags HashMap from events ArrayList
            computeTagMap();
            //populate FoldingCell with tags
            populateFoldingCell();
        }
    }

    //todo check if work is already scheduled instead of cancelling or if it drifts significantly
    //todo fix network activeNetwork.isConnectedOrConnecting()
    private void triggerImmediateRemoteUpdate() {
        //populate the global ArrayList of events by adding a work request to fetch JSON...
        Data remoteUrl = new Data.Builder().putString(REMOTE_URL, getRemoteURL()).build();

        //set constraints to the work to prevent it running when it's not useful
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build();

        //build an immediate OneTimeWorkRequest to fetch events from remote
        OneTimeWorkRequest getEventJson = new OneTimeWorkRequest
                .Builder(UpdateEventsWorker.class)
                .setConstraints(constraints)
                .setInputData(remoteUrl)
                .addTag(EVENTS_JSON_WORK_TAG_FORCED)
                .build();
        //using beginUniqueWork to prevent re-enqueuing the same task while it is already running
        WorkManager.getInstance()
                .beginUniqueWork(EVENTS_JSON_WORK_TAG_FORCED, ExistingWorkPolicy.REPLACE, getEventJson)
                .enqueue();
    }

    private void populateListView() {
        //populate list
        listView.setAdapter(new EventAdapter(this, filter(events)));

        //todo figure out why this sometimes isn't working
        listView.setEmptyView(error);

        //set click listener and transition animation
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Event event = (Event) parent.getItemAtPosition(position);

            Intent intent = new Intent(getApplicationContext(), EventDetail.class);
            intent.putExtra(DB_EVENT_ID_TAG, event.getDatabaseID());

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
            PopupMenu popup = new PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.main_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(this);
            popup.show();
        });

        //stop refresh indicator
        if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
    }

    private void computeTagMap() {
        tagMap = new HashMap<>();

        for (Event event : events) {
            ArrayList<String> tags = event.getEventTags();

            if (!tags.isEmpty()) {
                for (String tag : tags) {
                    //add each tag to the class-level tags, and either increment count or create a new one
                    if (tagMap.containsKey(tag)) {
                        int value = tagMap.get(tag);
                        tagMap.put(tag, ++value);
                    } else tagMap.put(tag, 1);
                }
            } else {
                final String noTag = " NO TAG ";
                if (tagMap.containsKey(noTag)) {
                    int value = tagMap.get(noTag);
                    tagMap.put(noTag, ++value);
                } else tagMap.put(noTag, 1);
            }
        }

        //sort HashMap
        tagMap = new TreeMap<>(tagMap);
    }

    private void populateFoldingCell() {
        LinearLayout tagsLL = findViewById(R.id.tags_linear_layout);
        View[] views = makeTagTextViews();
        populateTagLinearLayout(tagsLL, views, this);
    }

    //turn tags into TextViews
    private View[] makeTagTextViews() {
        //create as many views as there are tags, plus 2 extra: title and reset button
        View[] views = new View[tagMap.size() + 2];
        int pointer = 0;

        //set padding to correct pixel value depending on screen density
        int viewPadding = Math.round(8 * getResources().getDisplayMetrics().density);

        //title TextView
        TextView titleTextView = new TextView(this);
        titleTextView.setText("TAGS: ");
        titleTextView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        titleTextView.setPadding(viewPadding, viewPadding, viewPadding, viewPadding);
        titleTextView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));

        //make titleTV the width of the screen so it displays on top
        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        titleTextView.setWidth(point.x);

        views[pointer++] = titleTextView;

        //tags TextViews
        for (HashMap.Entry<String, Integer> pair : tagMap.entrySet()) {
            //don't draw empty tags
            if (pair.getValue() == 0) continue;

            String tag = pair.getKey() + " (" + pair.getValue() + ")";

            TextView textView = new TextView(this);
            textView.setText(tag);
            textView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));

            if (!filtersSet.isEmpty() && filtersSet.contains(pair.getKey())) {
                textView.setBackgroundColor(Color.DKGRAY);
                textView.setTextColor(Color.WHITE);
            } else {
                textView.setBackgroundColor(Color.GRAY);
                textView.setTextColor(Color.WHITE);
            }

            //note to self: this uses lame pixels, not cool DiPs
            textView.setPadding(viewPadding, viewPadding, viewPadding, viewPadding);

            //add and apply a filter on click
            textView.setOnClickListener(v -> addFilter(pair.getKey()));

            views[pointer++] = textView;
        }

        //reset button
        TextView resetTextView = new TextView(this);
        resetTextView.setText("CLEAR ALL TAGS");
        resetTextView.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        resetTextView.setPadding(viewPadding, viewPadding, viewPadding, viewPadding);
        resetTextView.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        resetTextView.setBackgroundColor(Color.YELLOW);

        //make resetTextView the width of the screen so it displays on its own row
        resetTextView.setWidth(point.x);

        //reset all filters on click
        resetTextView.setOnClickListener(v -> resetFilters());

        views[pointer] = resetTextView;

        return views;
    }

    //add or remove an individual tag from the filter
    private void addFilter(String filter) {
        //set/remove individual tag filter
        if (filtersSet.isEmpty() || !filtersSet.contains(filter)) filtersSet.add(filter);
        else filtersSet.remove(filter);

        //update filters text to indicate filters are set
        TextView filtersText = findViewById(R.id.filters_text_view);
        if (filtersSet.isEmpty()) filtersText.setText(getString(R.string.filters));
        else filtersText.setText(getString(R.string.filters_set));

        //redraw filters
        populateFoldingCell();

        //finally, update UI
        populateListView();
    }

    //filter events by set tags
    private ArrayList<Event> filter(ArrayList<Event> events) {
        if (filtersSet.isEmpty()) return events;

        ArrayList<Event> filteredEvents = new ArrayList<>();
        final String noTag = " NO TAG ";

        for (Event event : events) {
            ArrayList<String> tags = event.getEventTags();

            //also add "no tag" events
            if (tags.isEmpty() && filtersSet.contains(noTag)) filteredEvents.add(event);

            for (String tag : tags) {
                if (filtersSet.contains(tag)) filteredEvents.add(event);
            }
        }

        return filteredEvents;
    }

    //remove all filters
    private void resetFilters() {
        filtersSet = new ArrayList<>();

        //update filters text to indicate filters are set
        TextView filtersText = findViewById(R.id.filters_text_view);
        if (filtersSet.isEmpty()) filtersText.setText(getString(R.string.filters));
        else filtersText.setText(getString(R.string.filters_set));

        //close the UI
        fc.fold(false);

        //update UI
        populateListView();

        //redraw filters
        populateFoldingCell();
    }

    /*
     *  Got this off https://stackoverflow.com/questions/6996837/android-multi-line-linear-layout
     *  Copyright 2011 Sherif, modified by me
     */
    private void populateTagLinearLayout(LinearLayout ll, View[] views, Context context) {
        if (views == null || ll == null) {
            Log.e(TAG, "populateTagLinearLayout: ERROR POPULATING TAG VIEWS");
            return;
        }

        ll.removeAllViews();

        Display display = getWindowManager().getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int maxWidth = point.x - 20;

        LinearLayout.LayoutParams params;
        LinearLayout newLL = new LinearLayout(context);
        newLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        newLL.setGravity(Gravity.CENTER | Gravity.CENTER_VERTICAL);
        newLL.setOrientation(LinearLayout.HORIZONTAL);

        int widthSoFar = 0;

        for (View view : views) {
            //leaving this in just in case I miscount at some point
            if (view == null) continue;

            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
            linearLayout.setLayoutParams(new ListView.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

            view.measure(0, 0);
            params = new LinearLayout.LayoutParams(view.getMeasuredWidth(),
                    LinearLayout.LayoutParams.WRAP_CONTENT);

            //calculate margin in pixels from our value in DiPs
            int marginSide = Math.round(4 * getResources().getDisplayMetrics().density);
            int marginTops = Math.round(4 * getResources().getDisplayMetrics().density);

            params.setMargins(marginSide, marginTops, marginSide, marginTops);

            linearLayout.addView(view, params);
            linearLayout.measure(0, 0);
            widthSoFar = widthSoFar + view.getMeasuredWidth() + 2 * marginSide;

            if (widthSoFar >= maxWidth) {
                ll.addView(newLL);

                newLL = new LinearLayout(context);
                newLL.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
                newLL.setOrientation(LinearLayout.HORIZONTAL);
                newLL.setGravity(Gravity.CENTER);
                params = new LinearLayout.LayoutParams(linearLayout
                        .getMeasuredWidth(), linearLayout.getMeasuredHeight());
                newLL.addView(linearLayout, params);
                widthSoFar = linearLayout.getMeasuredWidth();
            } else {
                newLL.addView(linearLayout);
            }
        }
        ll.addView(newLL);
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
            Log.e(TAG, "Cannot open API key file to generate remote event URL.", e);
        }

        return remoteURL;
    }

    private void getPreferences() {
        SharedPreferences sharedPrefs = getDefaultSharedPreferences(getApplicationContext());

        //set notify on every future event flag
        notifyOnAllEvents = sharedPrefs.getBoolean(NOTIFY_SETTING, false);
        //set time of last remote update
        lastUpdateTime = sharedPrefs.getLong(LAST_UPDATE_TIME_SETTING, 0);
        //set whether this is the first launch of MainActivity to prevent open hours Toast when coming back
        firstLaunch = sharedPrefs.getBoolean(FIRST_LAUNCH_SETTING, true);
    }

    //todo figure out if we still care about this, all it does is change notify status,
    // would only trigger if notifyForAllEvents is false
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
            SharedPreferences sharedPref = getDefaultSharedPreferences(getApplicationContext());

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

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.drinks_menu:
                Intent intent = new Intent(getApplicationContext(), DrinksMenuActivity.class);
                startActivity(intent);
                return true;
            case R.id.food_menu:
                Intent intent2 = new Intent(getApplicationContext(), FoodMenuActivity.class);
                startActivity(intent2);
                return true;
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra(SOURCE_ACTIVITY, SOURCE_MAIN_ACTIVITY);
                startActivity(settingsIntent);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onRefresh() {
        triggerImmediateRemoteUpdate();
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
//todo replace ListView with RecyclerView (and use this: https://github.com/saket/InboxRecyclerView)
//todo [idea] prevent notifications from triggering if user doesn't interact with them
//todo [IDEA] scroll to TODAY on startup and mark it somehow in the list
//todo [IDEA] alternative to above: colour events happening TODAY differently and change date to TODAY
//todo remove logs
//todo [IDEA] move food and drinks menu
