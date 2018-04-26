package com.adriantache.manasia_events;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import com.adriantache.manasia_events.adapter.EventAdapter;
import com.adriantache.manasia_events.custom_class.Event;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.list_view) ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //activate ButterKnife
        ButterKnife.bind(this);

        //populate list
        //todo replace dummy data with real data, eventually
        EventAdapter eventAdapter = new EventAdapter(this,dummyData());
        listView.setAdapter(eventAdapter);

    }

    //todo create custom list layout

    //create dummy data objects to populate the list
    private List<Event> dummyData(){
        ArrayList<Event> arrayList = new ArrayList<>();

        //todo add dummy data objects
        arrayList.add(new Event("21.04.2018",
                "Record Shop Day After Party w/ The Groovers Delight",
                "High time to dig into the vinyl sound people ! ♥ \n" +
                        "From dusk till down, from garden to vinyl shop and beyond :D, " +
                        "we're delighted to celebrate the authentic vinyl sound with the " +
                        "right spin and a twist: a VINYL BACK TO BACK DJ set by The Groovers Delight. \n" +
                        "Gifted selectors, Vlad Oscar & Cipri M Marc have been digging for " +
                        "some proper funk & disco groove that promises to turn the night into " +
                        "a 'let your body take control' kinda' thing.\n" + "\n" +
                        "Record Store Day by MadPiano AFTER PARTY | 23:00 \n" +
                        "Manasia Hub | real stories | real people |\n" +
                        "Stelea Spataru 13",
                "https://i.imgur.com/v2OBKYS.jpg",
                R.drawable.shopping_basket_white_48x48));
        arrayList.add(new Event("21.04.2018",
                "Record Shop Day After Party w/ The Groovers Delight",
                "High time to dig into the vinyl sound people ! ♥ \n" +
                        "From dusk till down, from garden to vinyl shop and beyond :D, " +
                        "we're delighted to celebrate the authentic vinyl sound with the " +
                        "right spin and a twist: a VINYL BACK TO BACK DJ set by The Groovers Delight. \n" +
                        "Gifted selectors, Vlad Oscar & Cipri M Marc have been digging for " +
                        "some proper funk & disco groove that promises to turn the night into " +
                        "a 'let your body take control' kinda' thing.\n" + "\n" +
                        "Record Store Day by MadPiano AFTER PARTY | 23:00 \n" +
                        "Manasia Hub | real stories | real people |\n" +
                        "Stelea Spataru 13",
                "https://i.imgur.com/v2OBKYS.jpg",
                R.drawable.shopping_basket_white_48x48));
        arrayList.add(new Event("21.04.2018",
                "Record Shop Day After Party w/ The Groovers Delight",
                "High time to dig into the vinyl sound people ! ♥ \n" +
                        "From dusk till down, from garden to vinyl shop and beyond :D, " +
                        "we're delighted to celebrate the authentic vinyl sound with the " +
                        "right spin and a twist: a VINYL BACK TO BACK DJ set by The Groovers Delight. \n" +
                        "Gifted selectors, Vlad Oscar & Cipri M Marc have been digging for " +
                        "some proper funk & disco groove that promises to turn the night into " +
                        "a 'let your body take control' kinda' thing.\n" + "\n" +
                        "Record Store Day by MadPiano AFTER PARTY | 23:00 \n" +
                        "Manasia Hub | real stories | real people |\n" +
                        "Stelea Spataru 13",
                "https://i.imgur.com/v2OBKYS.jpg",
                R.drawable.shopping_basket_white_48x48));

        return arrayList;
    }

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
