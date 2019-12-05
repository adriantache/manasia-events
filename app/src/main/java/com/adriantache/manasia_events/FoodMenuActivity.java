package com.adriantache.manasia_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.adriantache.manasia_events.util.CommonStrings;
import com.google.android.material.snackbar.Snackbar;

import static android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.adriantache.manasia_events.util.CommonStrings.FIRST_LAUNCH_SETTING;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_ACTIVITY;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_DRINKS_MENU_ACTIVITY;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_FOOD_MENU_ACTIVITY;

public class FoodMenuActivity extends AppCompatActivity {
    ImageView back;
    CardView specialMenus;
    CardView soup;
    CardView mainCourse;
    CardView burgers;
    CardView pasta;
    CardView salads;
    CardView appetizers;
    CardView dessert;
    CardView other;
    TextView foodDetail;
    ConstraintLayout constraintLayout;
    TextView title;
    ImageView categoryImageView;

    boolean menuItemsHidden = false;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_menu);

        back = findViewById(R.id.back);
        specialMenus = findViewById(R.id.specialMenus);
        soup = findViewById(R.id.soup);
        mainCourse = findViewById(R.id.mainCourse);
        burgers = findViewById(R.id.burgers);
        pasta = findViewById(R.id.pasta);
        salads = findViewById(R.id.salads);
        appetizers = findViewById(R.id.appetizers);
        dessert = findViewById(R.id.dessert);
        other = findViewById(R.id.other);
        foodDetail = findViewById(R.id.foodDetail);
        constraintLayout = findViewById(R.id.constraint_layout);
        title = findViewById(R.id.title);
        categoryImageView = findViewById(R.id.categoryImageView);

        specialMenus.setOnClickListener(new MenuClickListener());
        soup.setOnClickListener(new MenuClickListener());
        mainCourse.setOnClickListener(new MenuClickListener());
        burgers.setOnClickListener(new MenuClickListener());
        pasta.setOnClickListener(new MenuClickListener());
        salads.setOnClickListener(new MenuClickListener());
        appetizers.setOnClickListener(new MenuClickListener());
        dessert.setOnClickListener(new MenuClickListener());
        other.setOnClickListener(new MenuClickListener());

        back.setOnClickListener(v -> back());

        //inform MainActivity that this isn't first launch
        SharedPreferences sharedPref = getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FIRST_LAUNCH_SETTING, false);
        editor.apply();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        //test whether we came back from a menu or not
        final int SOURCE_ACTIVITY = getIntent().getIntExtra(CommonStrings.SOURCE_ACTIVITY, 0);

        if (menuItemsHidden) {
            foodDetail.setText(null);
            toggleMenuItemVisibility();
            title.setText("manasia food");
            snackbar.dismiss();
        } else if (SOURCE_ACTIVITY == SOURCE_DRINKS_MENU_ACTIVITY) {
            Intent intent = new Intent(getApplicationContext(), DrinksMenuActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    private void openDetailView(View v) {
        //workaround because IDs are apparently no longer final
        if (v.getId() == specialMenus.getId()) {
            toggleMenuItemVisibility();
            title.setText("special menus");
            categoryImageView.setImageResource(R.drawable.special_menus);
            foodDetail.setText(Html.fromHtml("<h2>BREAKFAST</h2> " +
                            "<small>(Daily 12am - 2pm)</small>" +
                            "<p><b>Two eggs fried / boiled / omelette</b></p>" +
                            "<h4>TOPPING</h4>" +
                            "<b><p>Vegetables</p></b>" +
                            "<p><i><small>tomatoes/cucumbers/peppers/olives/pickles/broccoli/onions/grilled zucchini</small></i></p>" +
                            "<b><p>Cheese</p></b>" +
                            "<p><i><small>cottage cheese/gorgonzola/brie</small></i></p>" +
                            "<b><p>Meat</p></b>" +
                            "<p><i><small>crispy bacon/prosciutto crudo</small></i></p>" +
                            "<h2>LUNCH MENU</h2> " +
                            "<small>(Monday - Friday 12am - 4pm)</small>" +
                            "<b><p>V1 MENU</p></b>" +
                            "<p><i><small>Chicken schnitzel, French fries, white cabbage salad</small></i></p>" +
                            "<b><p>V2 MENU</p></b>" +
                            "<p><i><small>Fried cheese, French fries, white cabbage salad</small></i></p>" +
                            "<b><p>V3 MENU</p></b>" +
                            "<p><i><small>Baked chicken drumsticks, mashed potatoes, mixed salad</small></i></p>" +
                            "<b><p>V4 MENU</p></b>" +
                            "<p><i><small>Grilled chicken, mashed potatoes, mixed salad</small></i></p>" +
                            "<b><p>V5 MENU - TODAY's SPECIALS</p></b>" +
                            "<h4>SPECIAL MENU: ANY MENU + SOUP</h4><p/>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == soup.getId()) {
            toggleMenuItemVisibility();
            title.setText("soup");
            categoryImageView.setImageResource(R.drawable.soup);
            foodDetail.setText(Html.fromHtml("<h2>SOUPS</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == mainCourse.getId()) {
            toggleMenuItemVisibility();
            title.setText("main courses");
            categoryImageView.setImageResource(R.drawable.main_course);
            foodDetail.setText(Html.fromHtml("<h2>MAIN COURSES</h2> " +
                            "<p><b>Pork ribs, french fries, dill and yogurt sauce</b></p>" +
                            "<p><b>Shrimps with garlic, butter ond white wine sauce, foccacia</b></p>" +
                            "<p><b>Lamb pastrami wth polenta and garlic sauce</b></p>" +
                            "<p><b>Grilled chicken breast with quattro formaggi sauce and french fries</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == burgers.getId()) {
            toggleMenuItemVisibility();
            title.setText("burgers");
            categoryImageView.setImageResource(R.drawable.burgers);
            foodDetail.setText(Html.fromHtml("<h2>BURGERS</h2> " +
                            "<p><b>Manasia Burger</b></p>" +
                            "<i><small>(Beef meat/lamb meat, mixed salad leaves, smoked cheese, grilled zucchini, tomatoes, cheese sauce, Manasia Food sauce)</small></i>" +
                            "<p><b>Vegetarian Burger</b></p>" +
                            "<i><small>(Grilled halloumi, mixed salad leaves, tomatoes, grilled zucchini, Pesto sauce, Manasia Food sauce)</small></i>" +
                            "<p/><h4>SPECIAL MENU: ANY BURGER + FRENCH FRIES</h4><p/>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        }
        //todo finish food menu input
        else if (v.getId() == pasta.getId()) {
            toggleMenuItemVisibility();
            title.setText("pasta");
            categoryImageView.setImageResource(R.drawable.pasta);
            foodDetail.setText(Html.fromHtml("<h2>PASTA</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == salads.getId()) {
            toggleMenuItemVisibility();
            title.setText("salads");
            categoryImageView.setImageResource(R.drawable.salad);
            foodDetail.setText(Html.fromHtml("<h2>SALADS</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == appetizers.getId()) {
            toggleMenuItemVisibility();
            title.setText("appetizers");
            categoryImageView.setImageResource(R.drawable.appetizers);
            foodDetail.setText(Html.fromHtml("<h2>APPETIZERS</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == dessert.getId()) {
            toggleMenuItemVisibility();
            title.setText("dessert");
            categoryImageView.setImageResource(R.drawable.dessert);
            foodDetail.setText(Html.fromHtml("<h2>DESSERTS</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == other.getId()) {
            toggleMenuItemVisibility();
            title.setText("other");
            categoryImageView.setImageResource(R.drawable.other);
            foodDetail.setText(Html.fromHtml("<h2>OTHER</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        }

        showSnackbar();
    }

    private void toggleMenuItemVisibility() {
        if (!menuItemsHidden) {
            specialMenus.setVisibility(View.GONE);
            soup.setVisibility(View.GONE);
            mainCourse.setVisibility(View.GONE);
            burgers.setVisibility(View.GONE);
            pasta.setVisibility(View.GONE);
            salads.setVisibility(View.GONE);
            appetizers.setVisibility(View.GONE);
            dessert.setVisibility(View.GONE);
            other.setVisibility(View.GONE);
            menuItemsHidden = true;
        } else {
            specialMenus.setVisibility(View.VISIBLE);
            soup.setVisibility(View.VISIBLE);
            mainCourse.setVisibility(View.VISIBLE);
            burgers.setVisibility(View.VISIBLE);
            pasta.setVisibility(View.VISIBLE);
            salads.setVisibility(View.VISIBLE);
            appetizers.setVisibility(View.VISIBLE);
            dessert.setVisibility(View.VISIBLE);
            other.setVisibility(View.VISIBLE);
            menuItemsHidden = false;
        }
    }

    //snackbar on list detail to link to manasia food
    //todo modify behaviour of back button to take you back to other menu in case you came from there
    private void showSnackbar() {
        snackbar = Snackbar.make(constraintLayout,
                "Thirsty? Visit the drinks menu:",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Manasia Hub", v -> {
            Intent intent = new Intent(this, DrinksMenuActivity.class);
            intent.putExtra(SOURCE_ACTIVITY, SOURCE_FOOD_MENU_ACTIVITY);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        snackbar.show();
        View view = snackbar.getView();
        TextView textView = view.findViewById(R.id.snackbar_text);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    private class MenuClickListener implements View.OnClickListener {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public void onClick(View v) {
            openDetailView(v);
        }
    }
}
