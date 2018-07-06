package com.adriantache.manasia_events;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MenuActivity extends AppCompatActivity {
    //    @BindView(R.id.back)
//    ImageView back;
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
    private boolean layoutAnimated = false;

    //todo create snackbar on list detail to link to manasia food

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

        //test the list
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1);
        adapter.add("1");
        adapter.add("1");
        adapter.add("1");
        adapter.add("1");
        listView.setAdapter(adapter);

        //todo create transition for API<19
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            nonAlcoholic.setOnClickListener(new MenuClickListener());
            beer.setOnClickListener(new MenuClickListener());
            cocktails.setOnClickListener(new MenuClickListener());
            longDrinks.setOnClickListener(new MenuClickListener());
            shots.setOnClickListener(new MenuClickListener());
            spirits.setOnClickListener(new MenuClickListener());
            wine.setOnClickListener(new MenuClickListener());
            spritz.setOnClickListener(new MenuClickListener());
            cider.setOnClickListener(new MenuClickListener());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void animateLayout(View v){
        Log.i("ONCLICK", "onClick: " + v.getId());

        //hide categories and show list
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(getApplicationContext(), R.layout.activity_menu_animate);
        ConstraintSet initialConstraintSet = new ConstraintSet();
        initialConstraintSet.clone(getApplicationContext(), R.layout.activity_menu);
        ConstraintLayout constraintLayout = findViewById(R.id.constraint_layout);
        TransitionManager.beginDelayedTransition(constraintLayout);
        if (!layoutAnimated) {
            constraintSet.applyTo(constraintLayout);
            layoutAnimated = true;
        } else {
            initialConstraintSet.applyTo(constraintLayout);
            layoutAnimated = false;
        }
    }

    private class MenuClickListener implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onClick(View v) {
            animateLayout(v);
        }
    }
}
