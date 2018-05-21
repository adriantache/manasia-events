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
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
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

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class EventDetail extends AppCompatActivity {
    private final static String manasia_notification_channel = "Manasia Event Reminder";
    private static final String DBEventIDTag = "DBEventID";
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
    @BindView (R.id.notify_status)
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

                    //todo remove this and replace it with some kind of scheduling
                    //todo implement notifications in the main Event class, then run a method to reset and then set all notifications (might be inefficient)
                    showNotification(event);
                }
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
            Log.d(TAG, "updateDatabase: " + result + " DBEventID: " + DBEventID);
        }

        if (result == ERROR_VALUE) Log.d(TAG, "updateDatabase: Error writing event to database.");
    }

    //method that handles clicking the back button to create an artificial back stack to MainActivity
    //todo test if TaskStackBuilder is usable now
    private void backToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        //todo test animations here
        startActivity(intent);
    }

    //todo implement real notification system (probably with a service)
    //todo schedule notification https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
    //http://droidmentor.com/schedule-notifications-using-alarmmanager/
    //https://developer.android.com/topic/performance/scheduling
    private void showNotification(Event event) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the actual notification channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel(manasia_notification_channel, manasia_notification_channel, importance);

            //set a description for the channel
            String description = "A channel which shows notifications about events at Manasia";
            channel.setDescription(description);

            //set notification LED colour
            channel.setLightColor(Color.MAGENTA);

            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        //create an intent to open the event details activity when the user clicks the notification
        Intent intent = new Intent(getApplicationContext(), EventDetail.class);
        intent.putExtra(DBEventIDTag, DBEventID);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //put together the PendingIntent
        PendingIntent pendingIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, intent, FLAG_UPDATE_CURRENT);

        //todo figure out TaskStackBuilder, maybe it's better than my solution
        //https://developer.android.com/guide/components/activities/tasks-and-back-stack
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//        stackBuilder.addParentStack(MainActivity.class);
//        stackBuilder.addNextIntent(intent);
//        PendingIntent pendingIntent = stackBuilder.getPendingIntent(1,PendingIntent.FLAG_ONE_SHOT);

        //get event details to show in the notification
        String notificationTitle = "Manasia event: " + event.getTitle();
        String notificationText = event.getDate() + " at Stelea Spatarul 13, Bucuresti";

        //build the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), manasia_notification_channel)
                        .setSmallIcon(R.drawable.ic_manasia_small)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //trigger the notification
        //todo figure out how to schedule this instead of just showing it
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        notificationManager.notify(1, notificationBuilder.build());

        //todo display a snackbar to offer notification on all events https://www.androidhive.info/2015/09/android-material-design-snackbar-example/
        //Snackbar.make(snackbar_layout, "TESTED", LENGTH_SHORT);
    }
}

//todo create intent to open calendar to schedule event ?
//todo create intent to open FB event page ?
//todo setting to always notify on the day of the event
//todo implement SnackBar to ask if user wants to always get notified
