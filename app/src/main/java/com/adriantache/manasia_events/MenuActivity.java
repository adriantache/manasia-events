package com.adriantache.manasia_events;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
    @BindView(R.id.drinksDetail)
    TextView drinksDetail;
    @BindView(R.id.constraint_layout)
    ConstraintLayout constraintLayout;

    boolean menuItemsHidden = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

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

    private void animateLayout(View v) {
        Log.i("ONCLICK", "onClick: " + v.getId());

        //workaround because IDs are apparently no longer final
        //todo fill in all categories
        if (v.getId() == nonAlcoholic.getId()) {
            toggleMenuItemVisibility();
            drinksDetail.setText(Html.fromHtml("<h2>CAFEA/BAUTURI CALDE</h2><b><p>Espresso scurt/lung</p><p>Cappuccino</p>" +
                    "<p>Cappuccino Vienez</p> <p>Caffe Latte</p> <p>Iced coffee</b><i><small> (30ml of either irish cream/amaretto/whiskey)" +
                    "</small></i><b></p> <p>Ceai Cald</p> <p>Ciocolata calda</p>" +
                    "</b>" +
                    "<br><h2>RACORITOARE</h2><b><p>Limonada clasica</b><i><small> /400ml</small></i><b></p>" +
                    "<p>Limonada cu piure fructe</b><i><small> /400ml</small></i><b></p>" +
                    "<p>Limonada cu fructe de sezon</b><i><small> /400ml</small></i><b></p>" +
                    "<p>Coca-Cola<small>/</small>Sprite<small>/</small>Fanta<small>/</small>Tonic</b><i><small> /400ml</small></i><b></p>" +
                    "<p>Red-Bull</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Santal fructe pahar</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Fritz Kola</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Mischmasch</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Mellow apple/orange</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Fritz Mate</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Apa plata <small>/</small>minerala</b><i><small> /500ml</small></i><b></p>" +
                    "<p>Apa minerala</b><i><small> /750ml</small></i><b></p>" +
                    "</b>" +
                    "<br><h2>BERE 0% alc / RADLER</h2><b><p>Ciuc Natur Radler Lemon</b><i><small> (0.0% alc) 500 ml</small></i><b></p>" +
                    "<p>Ciuc Natur Radler Lemon</b><i><small> (1.9% alc) 500 ml</small></i><b></p>" +
                    "<p>Heineken</b><i><small> (0.0% alc) 500 ml</small></i><b></p>" +
                    "</b>"));
            showSnackbar();
        } else if (v.getId() == beer.getId()) {
            toggleMenuItemVisibility();
        } else if (v.getId() == cocktails.getId()) {
            toggleMenuItemVisibility();
        } else if (v.getId() == longDrinks.getId()) {
            toggleMenuItemVisibility();
        } else if (v.getId() == shots.getId()) {
            toggleMenuItemVisibility();
        } else if (v.getId() == spirits.getId()) {
            toggleMenuItemVisibility();
        } else if (v.getId() == wine.getId()) {
            toggleMenuItemVisibility();
        } else if (v.getId() == spritz.getId()) {
            toggleMenuItemVisibility();
        } else if (v.getId() == cider.getId()) {
            toggleMenuItemVisibility();
        }
    }

    //todo set back button functionality to show menu items if menuItemsHidden is true instead of going to MainActivity

    private void toggleMenuItemVisibility() {
        if (!menuItemsHidden) {
            nonAlcoholic.setVisibility(View.GONE);
            beer.setVisibility(View.GONE);
            cocktails.setVisibility(View.GONE);
            longDrinks.setVisibility(View.GONE);
            shots.setVisibility(View.GONE);
            spirits.setVisibility(View.GONE);
            wine.setVisibility(View.GONE);
            spritz.setVisibility(View.GONE);
            cider.setVisibility(View.GONE);
        } else {
            nonAlcoholic.setVisibility(View.VISIBLE);
            beer.setVisibility(View.VISIBLE);
            cocktails.setVisibility(View.VISIBLE);
            longDrinks.setVisibility(View.VISIBLE);
            shots.setVisibility(View.VISIBLE);
            spirits.setVisibility(View.VISIBLE);
            wine.setVisibility(View.VISIBLE);
            spritz.setVisibility(View.VISIBLE);
            cider.setVisibility(View.VISIBLE);
        }
    }

    //snackbar on list detail to link to manasia food
    //todo dismiss this if user navigates back from drinks detail
    public void showSnackbar() {
        Snackbar snackbar = Snackbar.make(constraintLayout,
                "Hungry? Visit our friends at:",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Manasia Food", v -> {
            Uri manasiaFood = Uri.parse("https://www.facebook.com/manasiafood/");
            Intent intent = new Intent(Intent.ACTION_VIEW, manasiaFood);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        snackbar.show();
        View view = snackbar.getView();
        TextView textView = view.findViewById(android.support.design.R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private class MenuClickListener implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onClick(View v) {
            animateLayout(v);
        }
    }
}
