package com.adriantache.manasia_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;
import com.adriantache.manasia_events.util.Utils;
import com.github.zagum.switchicon.SwitchIconView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import static android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.adriantache.manasia_events.notification.NotifyUtils.scheduleNotifications;
import static com.adriantache.manasia_events.util.CommonStrings.DB_EVENT_ID_TAG;
import static com.adriantache.manasia_events.util.CommonStrings.ERROR_VALUE;
import static com.adriantache.manasia_events.util.CommonStrings.FIRST_LAUNCH_SETTING;
import static com.adriantache.manasia_events.util.CommonStrings.NOTIFY_SETTING;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_EVENT_ACTIVITY;

public class EventDetail extends AppCompatActivity {
    private static final String TAG = "EventDetail";
    private ImageView thumbnail;
    private TextView day;
    private TextView month;
    private TextView title;
    private ImageView notifyStatus;
    private TextView description;
    private SwitchIconView notifyIcon;
    private LinearLayout notify;
    private TextView notifyLabel;
    private ConstraintLayout constraintLayout;
    private Event event = null;
    private int dbEventId = ERROR_VALUE;
    //todo determine if we need this at this level
    private SharedPreferences sharedPref;

    //todo zoom photo on click
    //todo [BUG] find NOTIFY_SETTINGS problem
    //todo set notifyOnAllEvents and settings reading to default to TRUE

    @Override
    public void onBackPressed() {
        backToMainActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        thumbnail = findViewById(R.id.thumbnail);
        day = findViewById(R.id.day);
        month = findViewById(R.id.month);
        title = findViewById(R.id.title);
        notifyStatus = findViewById(R.id.notify_status);
        description = findViewById(R.id.description);
        ImageView back = findViewById(R.id.back);
        TextView call = findViewById(R.id.call);
        TextView map = findViewById(R.id.map);
        TextView location = findViewById(R.id.location);
        notifyIcon = findViewById(R.id.notify_icon);
        notify = findViewById(R.id.notify);
        notifyLabel = findViewById(R.id.notify_label);
        constraintLayout = findViewById(R.id.constraint_layout);

        //get the event for which to display details
        Intent intent = getIntent();
        dbEventId = Objects.requireNonNull(intent.getExtras()).getInt(DB_EVENT_ID_TAG);
        if (dbEventId != ERROR_VALUE)
            event = DBUtils.getEventFromDatabase(this, dbEventId);
        else
            Toast.makeText(this, "Error getting event ID.", Toast.LENGTH_SHORT).show();

        //populate the activity with event details
        if (event != null) {
            populateDetails();
        } else {
            Toast.makeText(this, "Error getting event from database.", Toast.LENGTH_SHORT).show();
            finish();
        }

        back.setOnClickListener(v -> backToMainActivity());

        call.setOnClickListener(v -> {
            Intent phoneIntent = new Intent(Intent.ACTION_DIAL);
            phoneIntent.setData(Uri.parse("tel:004 0736 760 063"));
            if (phoneIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(phoneIntent);
            }
        });

        map.setOnClickListener(v -> {
            String geoLocation = "geo:0,0?q=Manasia Hub, Stelea Spătarul, nr.13, 030211 Bucharest, Romania";
            Intent locationIntent = new Intent(Intent.ACTION_VIEW);
            locationIntent.setData(Uri.parse(geoLocation));
            if (locationIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(locationIntent);
            }
        });

        location.setOnClickListener(v -> {
            String geoLocation = "geo:0,0?q=Manasia Hub, Stelea Spătarul, nr.13, 030211 Bucharest, Romania";
            Intent locationIntent = new Intent(Intent.ACTION_VIEW);
            locationIntent.setData(Uri.parse(geoLocation));
            if (locationIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(locationIntent);
            }
        });

        //inform MainActivity that this isn't first launch
        //todo replace with startActivityForResult
        sharedPref = getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FIRST_LAUNCH_SETTING, false);
        editor.apply();
    }

    private void populateDetails() {
        //populate fields with details
        if (!TextUtils.isEmpty(event.getPhotoUrl())) {
            Picasso.get().load(event.getPhotoUrl()).centerCrop().fit().into(thumbnail);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setBackgroundResource(R.color.colorAccent);
        } else {
            thumbnail.setImageResource(R.drawable.manasia_logo);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER);
            thumbnail.setBackgroundResource(R.color.blue_grey100);
        }
        day.setText(Utils.extractDayOrMonth(event.getDate(), true));
        month.setText(Utils.extractDayOrMonth(event.getDate(), false));
        title.setText(event.getTitle());
        description.setText(event.getDescription());

        //set on click listener and details for the notify button
        setNotifyDetails();
    }

    private void setNotifyDetails() {
        //if event is in the past, set some defaults and no onClickListener
        if (Utils.compareDateToToday(event.getDate()) < 0) {
            //hide the notification indicator up top
            notifyStatus.setVisibility(View.INVISIBLE);

            //and gray out the SwitchIconView
            notifyIcon.setIconEnabled(false);
            notifyIcon.setEnabled(false);

            return;
        }

        //read notify setting to determine if notifyOnAllEvents is true
        sharedPref = getDefaultSharedPreferences(getApplicationContext());
        boolean notifyOnAllEvents = sharedPref.getBoolean(NOTIFY_SETTING, true);

        //set notify status appearance
        if (event.getNotify() == 1 || notifyOnAllEvents)
            notifyStatus.setImageResource(R.drawable.alarm_accent);
        else
            notifyStatus.setImageResource(R.drawable.alarm);

        //set notify button appearance and onClickListener
        if (notifyOnAllEvents) {
            notifyIcon.setIconEnabled(true);
            notifyLabel.setText(getString(R.string.notifying));

            notify.setOnClickListener(v -> showSnackbar(false));
        } else {
            notify.setOnClickListener(v -> {
                if (event.getNotify() == 1) {
                    notifyIcon.setIconEnabled(false);
                    notifyLabel.setText(getString(R.string.notify));

                    event.setNotify(0);
                    updateDatabase();

                    Toast.makeText(getApplicationContext(), "Disabled notification.",
                            Toast.LENGTH_SHORT).show();
                } else {
                    notifyIcon.setIconEnabled(true);
                    notifyLabel.setText(getString(R.string.notifying));

                    event.setNotify(1);
                    updateDatabase();

                    //since we set the notify flag, use the Snackbar to prompt the user to always get notified
                    showSnackbar(true);
                }

                scheduleNotifications(getApplicationContext(), false);
            });
        }
    }

    //show a snackbar inviting the user to activate notification for all events
    public void showSnackbar(boolean promptAlwaysNotify) {
        if (promptAlwaysNotify) {
            Snackbar snackbar = Snackbar.make(constraintLayout,
                    "You will be notified on the day of the event.\n" +
                            "Would you like to be notified for all events?",
                    Snackbar.LENGTH_LONG);

            snackbar.setAction("Activate", v -> {
                sharedPref = getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(NOTIFY_SETTING, true);
                editor.apply();

                //since we're activating the setting to always be notified, go ahead and schedule notifications
                scheduleNotifications(getApplicationContext(), true);

                Toast.makeText(this, "We will notify you for all future events.",
                        Toast.LENGTH_SHORT).show();

                setNotifyDetails();
            });

            //center snackbar text
            View view = snackbar.getView();
            TextView textView = view.findViewById(R.id.snackbar_text);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);

            snackbar.show();
        } else {
            Snackbar snackbar = Snackbar.make(constraintLayout,
                    "You are already being notified for all events.\n" +
                            "Do you want to change this option?",
                    Snackbar.LENGTH_LONG);

            snackbar.setAction("Settings", v -> {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("activity", SOURCE_EVENT_ACTIVITY);
                settingsIntent.putExtra(DB_EVENT_ID_TAG, dbEventId);
                startActivity(settingsIntent);
            });

            //center snackbar text
            View view = snackbar.getView();
            TextView textView = view.findViewById(R.id.snackbar_text);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);

            snackbar.show();
        }
    }

    //method to update event in the local database
    private void updateDatabase() {
        int result = ERROR_VALUE;
        if (dbEventId != ERROR_VALUE) {
            result = DBUtils.updateEventToDatabase(this, dbEventId, event);
        }

        if (result == ERROR_VALUE) Log.d(TAG, "updateDatabase: Error writing event to database.");
    }

    //method that handles clicking the back button to create an artificial back stack to MainActivity
    private void backToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}

//todo [IDEA] create intent to open calendar to schedule event ?
//todo [IDEA] create intent to open FB event page ?
