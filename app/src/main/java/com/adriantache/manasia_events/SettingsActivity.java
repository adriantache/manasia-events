package com.adriantache.manasia_events;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import static com.adriantache.manasia_events.EventDetail.SHARED_PREFERENCES_TAG;
import static com.adriantache.manasia_events.MainActivity.DBEventIDTag;

public class SettingsActivity extends AppCompatActivity {
    private static final int MAIN_ACTIVITY = 1;
    private static final int EVENT_ACTIVITY = 2;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        back = findViewById(R.id.back);

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