package com.adriantache.manasia_events;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.list_view) ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    //todo create custom list layout

    //todo create event class and event info subclass

    //todo create custom adapter to display custom list layout

    //todo add onclick and toggle logic for buttons and logo

    //todo create second activity to display event details

    //todo create intent to open location of the event

    //todo create intent to open calendar to schedule event

    //todo add info about the hub somewhere (on logo click?) and indicate it visually

    //todo implement SwipeRefreshLayout

    //todo figure out data storage (firebase? facebook api?)

    //todo implement SharedPreferences to store toggle and notification option

    //todo implement notification permission request (or activity)

    //todo implement notification system

}
