package com.adriantache.manasia_events;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;

import static com.adriantache.manasia_events.MainActivity.DBEventIDTag;

public class SettingsActivity extends AppCompatActivity {
    private static final String SHARED_PREFERENCES_TAG = "preferences";
    private static final String NOTIFY_SETTING = "notify";
    private static final String FIRST_LAUNCH_SETTING = "notify";
    private static final String LAST_UPDATE_TIME_SETTING = "LAST_UPDATE_TIME";
    private static final int MAIN_ACTIVITY = 1;
    private static final int EVENT_ACTIVITY = 2;
    ImageView back;
    TextView devTools;
    Button notificationSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final int activity = getIntent().getExtras().getInt("activity");
        final int DBEventID = getIntent().getExtras().getInt(DBEventIDTag);

        back = findViewById(R.id.back);
        back.setOnClickListener(v -> {
            if (activity == MAIN_ACTIVITY) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            } else if (activity == EVENT_ACTIVITY) {
                Intent intent = new Intent(getApplicationContext(), EventDetail.class);
                intent.putExtra(DBEventIDTag, DBEventID);
                startActivity(intent);
            }
        });

        notificationSettings = findViewById(R.id.notification_settings);
        notificationSettings.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });

        devTools = findViewById(R.id.dev_tools);
        SharedPreferences sharedPrefs = this.getSharedPreferences(SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
        //set notify on every future event flag
        boolean notifyOnAllEvents = sharedPrefs.getBoolean(NOTIFY_SETTING, false);
        //set time of last remote update
        long lastUpdateTime = sharedPrefs.getLong(LAST_UPDATE_TIME_SETTING, 0);
        Calendar calendar = Calendar.getInstance();
        long timeSinceLUT = (calendar.getTimeInMillis() - lastUpdateTime)/1000/3600;
        //set whether this is the first launch of MainActivity to prevent open hours Toast when coming back
        boolean firstLaunch = sharedPrefs.getBoolean(FIRST_LAUNCH_SETTING, true);
        String displayText = "Dev Info: Time since LUT = " + timeSinceLUT + " hours; NotifyAll = "
                + notifyOnAllEvents + ";\n FirstLaunch = " + firstLaunch +".";
        devTools.setTextColor(0xffD4E157);
        devTools.setBackgroundColor(0xff795548);
        devTools.setText(displayText);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        //in the future, remember to add this line to the styles.xml theme:
        //<item name="preferenceTheme">@style/PreferenceThemeOverlay</item>
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName(SHARED_PREFERENCES_TAG);
            addPreferencesFromResource(R.xml.preferences);
        }
    }
}