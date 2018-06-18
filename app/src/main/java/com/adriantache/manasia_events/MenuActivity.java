package com.adriantache.manasia_events;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.widget.ListView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuActivity extends AppCompatActivity {
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.nonAlcoholic)
    CardView nonAlcoholic;
    @BindView(R.id.beer)
    CardView beer;
    @BindView(R.id.cocktails)
    CardView cocktails;
    @BindView(R.id.longDrinks)
    CardView longDrinks;
    @BindView(R.id.shots)
    CardView shots;
    @BindView(R.id.spirits)
    CardView spirits;
    @BindView(R.id.wine)
    CardView wine;
    @BindView(R.id.spritz)
    CardView spritz;
    @BindView(R.id.cider)
    CardView cider;
    @BindView(R.id.listView)
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);
    }
}
