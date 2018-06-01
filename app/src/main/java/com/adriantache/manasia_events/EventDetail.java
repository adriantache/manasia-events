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

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.adriantache.manasia_events.MainActivity.DBEventIDTag;
import static com.adriantache.manasia_events.notification.NotifyUtils.scheduleNotifications;
import static com.adriantache.manasia_events.util.Utils.getNotifyAllSetting;

public class EventDetail extends AppCompatActivity {
    public static final String SHARED_PREFERENCES_TAG = "preferences";
    public static final String NOTIFY = "notify";
    private static final String TAG = "EventDetail";
    private static final int ERROR_VALUE = -1;
    @BindView(R.id.thumbnail)
    ImageView thumbnail;
    @BindView(R.id.category_image)
    ImageView category_image;
    @BindView(R.id.day)
    TextView day;
    @BindView(R.id.month)
    TextView month;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.notify_status)
    ImageView notify_status;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.call)
    TextView call;
    @BindView(R.id.map)
    TextView map;
    @BindView(R.id.location)
    TextView location;
    @BindView(R.id.notify_icon)
    SwitchIconView notify_icon;
    @BindView(R.id.notify)
    LinearLayout notify;
    @BindView(R.id.notify_label)
    TextView notify_label;
    @BindView(R.id.constraint_layout)
    ConstraintLayout constraint_layout;
    @BindView(R.id.title_bar)
    LinearLayout title_bar;
    private Event event = null;
    private int DBEventID = ERROR_VALUE;
    private boolean notifyOnAllEvents;

    @Override
    public void onBackPressed() {
        backToMainActivity();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //read notify setting
        notifyOnAllEvents = getNotifyAllSetting(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);

        //read notify setting
        notifyOnAllEvents = getNotifyAllSetting(this);

        //get the event for which to display details
        Intent intent = getIntent();
        DBEventID = Objects.requireNonNull(intent.getExtras()).getInt(DBEventIDTag);

        if (DBEventID != ERROR_VALUE)
            event = DBUtils.getEventFromDatabase(this, DBEventID);
        else
            Toast.makeText(this, "Error getting event ID.", Toast.LENGTH_SHORT).show();

        if (event != null) {
            populateDetails();
            setNotifyOnClickListener(Utils.compareDateToToday(event.getDate()) < 0);
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
    }

    private void setNotifyOnClickListener(boolean pastEvent) {
        //only add notification for events in the future (or today)
        if (pastEvent) {
            notify_icon.setEnabled(false);

            //also hide the notification indicator up top
            notify_status.setVisibility(View.INVISIBLE);
        } else if (notifyOnAllEvents) {
            notify_icon.setIconEnabled(true);
            notify_label.setText(getString(R.string.notifying));
            notify_status.setImageResource(R.drawable.alarm_accent);

            notify.setOnClickListener(v -> showSnackbar());
        } else {
            notify.setOnClickListener(v -> {
                //read notify setting
                notifyOnAllEvents = getNotifyAllSetting(this);

                if (event.getNotify() == 1) {
                    notify_icon.setIconEnabled(false);
                    notify_status.setImageResource(R.drawable.alarm);
                    notify_label.setText(getString(R.string.notify));

                    event.setNotify(0);
                    updateDatabase();

                    Toast.makeText(getApplicationContext(), "Disabled notification.", Toast.LENGTH_SHORT).show();
                } else {
                    notify_icon.setIconEnabled(true);
                    notify_status.setImageResource(R.drawable.alarm_accent);
                    notify_label.setText(getString(R.string.notifying));

                    event.setNotify(1);
                    updateDatabase();

                    //if we set the notify flag, use the Snackbar to prompt the user to always get notified
                    showSnackbar();
                }

                scheduleNotifications(getApplicationContext(), false);
            });
        }
    }

    private void populateDetails() {
        //populate fields with details
        if (!TextUtils.isEmpty(event.getPhotoUrl())) {
            Picasso.get().load(event.getPhotoUrl()).fit().into(thumbnail);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
            thumbnail.setBackgroundResource(R.color.colorAccent);
        } else {
            thumbnail.setImageResource(R.drawable.manasia_logo);
            thumbnail.setScaleType(ImageView.ScaleType.CENTER);
            thumbnail.setBackgroundResource(R.color.blue_grey100);
        }
        category_image.setImageResource(event.getCategory_image());
        day.setText(Utils.extractDayOrMonth(event.getDate(), true));
        month.setText(Utils.extractDayOrMonth(event.getDate(), false));
        title.setText(event.getTitle());
        description.setText(event.getDescription());
        if (event.getNotify() == 1)
            notify_status.setImageResource(R.drawable.alarm_accent);
        else
            notify_status.setImageResource(R.drawable.alarm);

        //set notify button state depending on notify state
        notify_icon.setIconEnabled(event.getNotify() == 1);
    }

    //method to update event in the local database
    private void updateDatabase() {
        int result = ERROR_VALUE;
        if (DBEventID != ERROR_VALUE) {
            result = DBUtils.updateEventToDatabase(this, DBEventID, event);
        }

        if (result == ERROR_VALUE) Log.d(TAG, "updateDatabase: Error writing event to database.");
    }

    //method that handles clicking the back button to create an artificial back stack to MainActivity
    private void backToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    //todo [IDEA] always notify on the day of the event
    //todo call this from MainActivity as well
    //show a snackbar inviting the user to activate notification for all events
    public void showSnackbar() {
        //read notify setting
        notifyOnAllEvents = getNotifyAllSetting(this);

        if (!notifyOnAllEvents) {
            Snackbar snackbar = Snackbar.make(constraint_layout,
                    "You will be notified on the day of the event.\n" +
                            "Would you like to be notified for all events?",
                    Snackbar.LENGTH_LONG);

            snackbar.setAction("Activate", v -> {
                SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean(NOTIFY, true);
                editor.apply();

                notifyOnAllEvents = true;

                //since we're activating the setting to always be notified, go ahead and schedule notifications
                scheduleNotifications(getApplicationContext(), true);

                Toast.makeText(this, "We will notify you for all future events.", Toast.LENGTH_SHORT).show();

                boolean pastDate = Utils.compareDateToToday(event.getDate()) < 0;
                setNotifyOnClickListener(pastDate);
            });

            snackbar.show();

            View view = snackbar.getView();
            TextView textView = view.findViewById(android.support.design.R.id.snackbar_text);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
        } else {
            Snackbar snackbar = Snackbar.make(constraint_layout,
                    "You are already being notified for all events.\n" +
                            "Do you want to change this option?",
                    Snackbar.LENGTH_LONG);

            snackbar.setAction("Settings", v -> {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                settingsIntent.putExtra("activity", 2);
                settingsIntent.putExtra(DBEventIDTag, DBEventID);
                startActivity(settingsIntent);
            });

            snackbar.show();

            View view = snackbar.getView();
            TextView textView = view.findViewById(android.support.design.R.id.snackbar_text);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
        }
    }
}

//todo [IDEA] create intent to open calendar to schedule event ?
//todo [IDEA] create intent to open FB event page ?
