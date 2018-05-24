package com.adriantache.manasia_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.adriantache.manasia_events.EventDetail.NOTIFY;
import static com.adriantache.manasia_events.MainActivity.DBEventIDTag;

public class SettingsActivity extends AppCompatActivity {
    private static final int MAIN_ACTIVITY = 1;
    private static final int EVENT_ACTIVITY = 2;
    @BindView(R.id.back)
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        final int activity = getIntent().getExtras().getInt("activity");
        final int DBEventID = getIntent().getExtras().getInt(DBEventIDTag);

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
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

        @Override
        //in the future, remember to add this line to the styles.xml theme:
        //<item name="preferenceTheme">@style/PreferenceThemeOverlay</item>
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
            Preference notify = findPreference(NOTIFY);
            notify.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(notify.getContext());
            boolean preferenceString = preferences.getBoolean(NOTIFY, false);
            onPreferenceChange(notify, preferenceString);
        }


        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            preference.setSummary(stringValue);
            Preference notify = findPreference(NOTIFY);

            return true;
        }


    }
}