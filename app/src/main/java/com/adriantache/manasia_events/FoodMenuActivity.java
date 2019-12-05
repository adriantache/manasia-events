package com.adriantache.manasia_events;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.adriantache.manasia_events.databinding.ActivityFoodMenuBinding;
import com.adriantache.manasia_events.util.CommonStrings;
import com.google.android.material.snackbar.Snackbar;

import static android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.adriantache.manasia_events.util.CommonStrings.FIRST_LAUNCH_SETTING;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_ACTIVITY;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_DRINKS_MENU_ACTIVITY;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_FOOD_MENU_ACTIVITY;

public class FoodMenuActivity extends AppCompatActivity {
    private ActivityFoodMenuBinding binding;

    boolean menuItemsHidden = false;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_menu);

        binding.specialMenus.setOnClickListener(new MenuClickListener());
        binding.soup.setOnClickListener(new MenuClickListener());
        binding.mainCourse.setOnClickListener(new MenuClickListener());
        binding.burgers.setOnClickListener(new MenuClickListener());
        binding.pasta.setOnClickListener(new MenuClickListener());
        binding.salads.setOnClickListener(new MenuClickListener());
        binding.appetizers.setOnClickListener(new MenuClickListener());
        binding.dessert.setOnClickListener(new MenuClickListener());
        binding.other.setOnClickListener(new MenuClickListener());

        binding.back.setOnClickListener(v -> back());

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
            binding.foodDetail.setText(null);
            toggleMenuItemVisibility();
            binding.title.setText("manasia food");
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
        if (v.getId() == binding.specialMenus.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("special menus");
            binding.categoryImageView.setImageResource(R.drawable.special_menus);
            binding.foodDetail.setText(Html.fromHtml("<h2>BREAKFAST</h2> " +
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
        } else if (v.getId() == binding.soup.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("soup");
            binding.categoryImageView.setImageResource(R.drawable.soup);
            binding.foodDetail.setText(Html.fromHtml("<h2>SOUPS</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.mainCourse.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("main courses");
            binding.categoryImageView.setImageResource(R.drawable.main_course);
            binding.foodDetail.setText(Html.fromHtml("<h2>MAIN COURSES</h2> " +
                            "<p><b>Pork ribs, french fries, dill and yogurt sauce</b></p>" +
                            "<p><b>Shrimps with garlic, butter ond white wine sauce, foccacia</b></p>" +
                            "<p><b>Lamb pastrami wth polenta and garlic sauce</b></p>" +
                            "<p><b>Grilled chicken breast with quattro formaggi sauce and french fries</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.burgers.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("burgers");
            binding.categoryImageView.setImageResource(R.drawable.burgers);
            binding.foodDetail.setText(Html.fromHtml("<h2>BURGERS</h2> " +
                            "<p><b>Manasia Burger</b></p>" +
                            "<i><small>(Beef meat/lamb meat, mixed salad leaves, smoked cheese, grilled zucchini, tomatoes, cheese sauce, Manasia Food sauce)</small></i>" +
                            "<p><b>Vegetarian Burger</b></p>" +
                            "<i><small>(Grilled halloumi, mixed salad leaves, tomatoes, grilled zucchini, Pesto sauce, Manasia Food sauce)</small></i>" +
                            "<p/><h4>SPECIAL MENU: ANY BURGER + FRENCH FRIES</h4><p/>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        }
        //todo finish food menu input
        else if (v.getId() == binding.pasta.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("pasta");
            binding.categoryImageView.setImageResource(R.drawable.pasta);
            binding.foodDetail.setText(Html.fromHtml("<h2>PASTA</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.salads.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("salads");
            binding.categoryImageView.setImageResource(R.drawable.salad);
            binding.foodDetail.setText(Html.fromHtml("<h2>SALADS</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.appetizers.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("appetizers");
            binding.categoryImageView.setImageResource(R.drawable.appetizers);
            binding.foodDetail.setText(Html.fromHtml("<h2>APPETIZERS</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.dessert.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("dessert");
            binding.categoryImageView.setImageResource(R.drawable.dessert);
            binding.foodDetail.setText(Html.fromHtml("<h2>DESSERTS</h2> " +
                            "<p><b>Turkey meat soup with home made noodles</b></p>" +
                            "<p><b>Tomato cream soup with parmesan chips</b></p>" +
                            "<p><b>Beef soup</b></p>" +
                            "<p><b>Chicken soup with homemade dumplings</b></p>" +
                            "<p><b>Vegetable soup</b></p>",
                    FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.other.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("other");
            binding.categoryImageView.setImageResource(R.drawable.other);
            binding.foodDetail.setText(Html.fromHtml("<h2>OTHER</h2> " +
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
            binding.specialMenus.setVisibility(View.GONE);
            binding.soup.setVisibility(View.GONE);
            binding.mainCourse.setVisibility(View.GONE);
            binding.burgers.setVisibility(View.GONE);
            binding.pasta.setVisibility(View.GONE);
            binding.salads.setVisibility(View.GONE);
            binding.appetizers.setVisibility(View.GONE);
            binding.dessert.setVisibility(View.GONE);
            binding.other.setVisibility(View.GONE);
            menuItemsHidden = true;
        } else {
            binding.specialMenus.setVisibility(View.VISIBLE);
            binding.soup.setVisibility(View.VISIBLE);
            binding.mainCourse.setVisibility(View.VISIBLE);
            binding.burgers.setVisibility(View.VISIBLE);
            binding.pasta.setVisibility(View.VISIBLE);
            binding.salads.setVisibility(View.VISIBLE);
            binding.appetizers.setVisibility(View.VISIBLE);
            binding.dessert.setVisibility(View.VISIBLE);
            binding.other.setVisibility(View.VISIBLE);
            menuItemsHidden = false;
        }
    }

    //snackbar on list detail to link to manasia food
    //todo modify behaviour of back button to take you back to other menu in case you came from there
    private void showSnackbar() {
        snackbar = Snackbar.make(binding.constraintLayout,
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
