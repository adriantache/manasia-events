package com.adriantache.manasia_events;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.db.DBUtils;
import com.adriantache.manasia_events.notification.NotifyUtils;
import com.adriantache.manasia_events.util.Utils;
import com.github.zagum.switchicon.SwitchIconView;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.adriantache.manasia_events.MainActivity.DBEventIDTag;
import static com.adriantache.manasia_events.notification.NotifyUtils.scheduleNotifications;

public class EventDetail extends AppCompatActivity {
    private final static String manasia_notification_channel = "Manasia Event Reminder";
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
    @BindView(R.id.notify_icon)
    SwitchIconView notify_icon;
    @BindView(R.id.notify)
    LinearLayout notify;
    @BindView(R.id.scrollView)
    ScrollView scrollView;
    private Event event = null;
    private int DBEventID = ERROR_VALUE;

    @Override
    public void onBackPressed() {
        backToMainActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);

        //get the event for which to display details
        Intent intent = getIntent();
        DBEventID = Objects.requireNonNull(intent.getExtras()).getInt(DBEventIDTag);

        if (DBEventID != ERROR_VALUE)
            event = DBUtils.getEventFromDatabase(this, DBEventID);
        else
            Toast.makeText(this, "Error getting event ID.", Toast.LENGTH_SHORT).show();

        if (event != null)
            populateDetails();
        else
            Toast.makeText(this, "Error getting event from database.", Toast.LENGTH_SHORT).show();

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

        //only add notification for events in the future (or today)
        if (Utils.compareDateToToday(event.getDate()) < 0) {
            notify_icon.setEnabled(false);

            //also hide the notification indicator up top
            notify_status.setVisibility(View.INVISIBLE);
        } else
            notify.setOnClickListener(v -> {
                if (event.getNotify() == 1) {
                    notify_icon.setIconEnabled(false);
                    Toast.makeText(getApplicationContext(), "Disabled notification.", Toast.LENGTH_SHORT).show();
                    notify_status.setImageResource(R.drawable.alarm);
                    event.setNotify(0);
                    updateDatabase();
                } else {
                    notify_icon.setIconEnabled(true);
                    Toast.makeText(getApplicationContext(), "We will notify you on the day of the event.", Toast.LENGTH_SHORT).show();
                    notify_status.setImageResource(R.drawable.alarm_accent);
                    event.setNotify(1);
                    updateDatabase();

                    //if we set the notify flag, use the Snackbar to prompt the user to always get notified
                    showSnackbar();
                }

                scheduleNotifications(getApplicationContext(), false);
            });
    }

    private void populateDetails() {
        //populate fields with details
        if (!TextUtils.isEmpty(event.getPhotoUrl()))
            Picasso.get().load(event.getPhotoUrl()).into(thumbnail);
        else
            thumbnail.setImageResource(R.drawable.manasia_logo);
        category_image.setImageResource(event.getCategory_image());
        day.setText(Utils.extractDate(event.getDate(), true));
        month.setText(Utils.extractDate(event.getDate(), false));
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
    //todo test if TaskStackBuilder is usable now
    //todo [IDEA] scroll list in MainActivity to appropriate event
    private void backToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }

    //todo [IDEA] always notify on the day of the event
    public void showSnackbar() {
        Snackbar snackbar = Snackbar.make(scrollView,
                "Would you like to be notified for all events?",
                Snackbar.LENGTH_SHORT);
        snackbar.show();
        View view = snackbar.getView();
        TextView textView = view.findViewById(android.support.design.R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
    }
}

//todo [IDEA] create intent to open calendar to schedule event ?
//todo [IDEA] create intent to open FB event page ?