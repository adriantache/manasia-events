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

import com.adriantache.manasia_events.databinding.ActivityDrinksMenuBinding;
import com.adriantache.manasia_events.util.CommonStrings;
import com.google.android.material.snackbar.Snackbar;

import static android.text.Html.FROM_HTML_SEPARATOR_LINE_BREAK_HEADING;
import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.adriantache.manasia_events.util.CommonStrings.FIRST_LAUNCH_SETTING;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_ACTIVITY;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_DRINKS_MENU_ACTIVITY;
import static com.adriantache.manasia_events.util.CommonStrings.SOURCE_FOOD_MENU_ACTIVITY;

public class DrinksMenuActivity extends AppCompatActivity {
    private ActivityDrinksMenuBinding binding;
    boolean menuItemsHidden = false;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_drinks_menu);

        binding.nonAlcoholic.setOnClickListener(new MenuClickListener());
        binding.beer.setOnClickListener(new MenuClickListener());
        binding.cocktails.setOnClickListener(new MenuClickListener());
        binding.longDrinks.setOnClickListener(new MenuClickListener());
        binding.shots.setOnClickListener(new MenuClickListener());
        binding.spirits.setOnClickListener(new MenuClickListener());
        binding.wine.setOnClickListener(new MenuClickListener());
        binding.spritz.setOnClickListener(new MenuClickListener());
        binding.cider.setOnClickListener(new MenuClickListener());

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
            binding.drinksDetail.setText(null);
            toggleMenuItemVisibility();
            binding.title.setText("manasia drinks");
            snackbar.dismiss();
        } else if (SOURCE_ACTIVITY == SOURCE_FOOD_MENU_ACTIVITY) {
            Intent intent = new Intent(getApplicationContext(), FoodMenuActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    private void openDetailView(View v) {
        //workaround because IDs are apparently no longer final
        if (v.getId() == binding.nonAlcoholic.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("non-alcoholic drinks");
            binding.categoryImageView.setImageResource(R.drawable.non_alcoholic);
            binding.drinksDetail.setText(Html.fromHtml("<h2>CAFEA/BAUTURI CALDE</h2><b><p>Espresso scurt/lung</p>" +
                    "<p>Cappuccino</p><p>Cappuccino Vienez</p> <p>Caffe Latte</p> <p>Iced coffee</b><i>" +
                    "<small> (30ml of either irish cream/amaretto/whiskey)" +
                    "</small></i><b></p> <p>Ceai Cald</p> <p>Ciocolata calda</p>" +
                    "</b>" +
                    "<br><h2>RACORITOARE</h2><b><p>Limonada clasica</b><i><small> /400ml</small></i><b></p>" +
                    "<p>Limonada cu piure fructe</b><i><small> /400ml</small></i><b></p>" +
                    "<p>Limonada cu fructe de sezon</b><i><small> /400ml</small></i><b></p>" +
                    "<p>Coca-Cola<small>/</small>Sprite<small>/</small>Fanta<small>/</small>Tonic</b><i>" +
                    "<small> /400ml</small></i><b></p>" +
                    "<p>Red-Bull</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Santal fructe pahar</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Fritz Kola</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Mischmasch</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Mellow apple/orange</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Fritz Mate</b><i><small> /330ml</small></i><b></p>" +
                    "<p>Apa plata <small>/</small>minerala</b><i><small> /500ml</small></i><b></p>" +
                    "<p>Apa minerala</b><i><small> /750ml</small></i><b></p>" +
                    "</b>" +
                    "<br><h2>BERE 0% alc / RADLER</h2><b><p>Ciuc Natur Radler Lemon</b><i><small> " +
                    "(0.0% alc) 500 ml</small></i><b></p>" +
                    "<p>Ciuc Natur Radler Lemon</b><i><small> (1.9% alc) 500 ml</small></i><b></p>" +
                    "<p>Heineken</b><i><small> (0.0% alc) 500 ml</small></i><b></p>" +
                    "</b>" +
                    "<br><h2>COCKTAILS FARA ALCOOL</h2><b>" +
                    "<p>Virgin Pina Colada</b><i><small> (pineapple juice, coconut syrup, cream)</small></i><b></p>" +
                    "<p>Manasia Summer</b><i><small> (puree passion fruit, pineapple juice, orange juice, grenadine, lemon fresh, mint)</small></i><b></p>" +
                    "<p>Green Apple</b><i><small> (lime, apple juice, brown sugar)</small></i><b></p>" +
                    "</b>", FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.beer.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("beer");
            binding.categoryImageView.setImageResource(R.drawable.beer);
            binding.drinksDetail.setText(Html.fromHtml("<h2>BERE DRAUGHT</h2><b>" +
                    "<p>Ciuc Premium</b><i><small> (5.0% alc) 500 ml</small></i><b></p>" +
                    "<p>Primator Weizenbier</b><i><small> (5.0% alc) 500 ml</small></i><b></p>" +
                    "</b><br><h2>BERE STICLA</h2><b>" +
                    "<p>Heineken</b><i><small> (5.0% alc) 500 ml</small></i><b></p>" +
                    "<p>Desperados</b><i><small> Tequila beer (5.9% alc) 400 ml</small></i><b></p>" +
                    "<p>Desperados</b><i><small> Tequila beer (5.9% alc) 250 ml</small></i><b></p>" +
                    "<p>Ciuc Premium</b><i><small> (5.0% alc) 500 ml</small></i><b></p>" +
                    "<p>Silva Dark</b><i><small> (7.0% alc) 500 ml</small></i><b></p>" +
                    "<p>Silva Original</b><i><small> (5.1% alc) 500 ml</small></i><b></p>" +
                    "<p>Silva RPA</b><i><small> (5.5% alc) 500 ml</small></i><b></p>" +
                    "<p>Gambrinus Vintage</b><i><small> (5.5% alc) 500 ml</small></i><b></p>" +
                    "<p>Zaganu Blonda</b><i><small> (5.3% alc) 500 ml</small></i><b></p>" +
                    "<p>Zaganu Brun</b><i><small> (7.0% alc) 500 ml</small></i><b></p>" +
                    "<p>Zaganu Rosie <small>/</small>IPA</b><i><small> (7.0% alc / 5.7% alc) 330 ml</small></i><b></p>" +
                    "<p>Primator English Pale Ale</b><i><small> (5.0% alc) 500 ml</small></i><b></p>" +
                    "</b>" +
                    "<br><h2>BERE 0% alc / RADLER</h2><b>" +
                    "<p>Ciuc Natur Radler Lemon</b><i><small> (0.0% alc) 500 ml</small></i><b></p>" +
                    "<p>Ciuc Natur Radler Lemon</b><i><small> (1.9% alc) 500 ml</small></i><b></p>" +
                    "<p>Heineken</b><i><small> (0.0% alc) 500 ml</small></i><b></p>" +
                    "</b>", FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.cocktails.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("cocktails");
            binding.categoryImageView.setImageResource(R.drawable.cocktails);
            binding.drinksDetail.setText(Html.fromHtml("<h2>COCKTAILS FARA ALCOOL</h2><b>" +
                    "<p>Virgin Pina Colada</b><i><small> (pineapple juice, coconut syrup, cream)</small></i><b></p>" +
                    "<p>Manasia Summer</b><i><small> (puree passion fruit, pineapple juice, orange juice, grenadine, lemon fresh, mint)</small></i><b></p>" +
                    "<p>Green Apple</b><i><small> (lime, apple juice, brown sugar)</small></i><b></p>" +
                    "</b>" +
                    "<br><h2>COCKTAILS</h2><b>" +
                    "<p>Mojito Cubanez</b><i><small> (60ml rom alb, lime, menta, sirop zahar brun, splash apa minerala)</small></i><b></p>" +
                    "<p>White Russian</b><i><small> (40ml vodka, 20ml kahlua, lapte, frisca lichida)</small></i><b></p>" +
                    "<p>Godfather</b><i><small> (30ml Jack Daniels, 30ml Disaronno, 20ml fresh, boabe de cafea, splash apa minerala)</small></i><b></p>" +
                    "<p>Margarita</b><i><small> (40ml tequila gold, 20ml triplu sec, sirop zahar brun, lime)</small></i><b></p>" +
                    "<p>Long Island Iced Tea</b><i><small> (20ml vodka, 20ml rom alb, 20ml gin, 20ml triplu sec, sirop zahar brun, lime, coca-cola)</small></i><b></p>" +
                    "<p>Porn Star Martini</b><i><small> (40ml Absolut Vanilla, 20ml Passoa, puree passion fruit)</small></i><b></p>" +
                    "<p>Tequila Sunrise</b><i><small> (40ml Tequila, suc portocale, grenadine)</small></i><b></p>" +
                    "<p>Sex on the Beach</b><i><small> (30ml vodka, 30ml peach liquor, suc portocale + cranberry)</small></i><b></p>" +
                    "<p>Mimoza</b><i><small> (150ml prosecco, suc portocale)</small></i><b></p>" +
                    "<p>Cosmopolitan</b><i><small> (40ml vodka, 20ml triplu sec, suc cranberry)</small></i><b></p>" +
                    "<p>Caipiroska</b><i><small> (50ml vodka, lime, zahar brun)</small></i><b></p>" +
                    "<p>Vodka Martini</b><i><small> (30ml vodka, 30ml vermut dry)</small></i><b></p>" +
                    "<p>Campari Orange</b><i><small> (50ml Campari, suc portocale)</small></i><b></p>" +
                    "<p>Linchburg Lemonade</b><i><small> (40ml Jack Daniels, 20ml triplu sec, sweet&sour, sprite)</small></i><b></p>" +
                    "<p>Ciresica</b><i><small> (30ml vodka, 30ml amaretto, sour, suc cirese)</small></i><b></p>" +
                    "<p>Orgasm</b><i><small> (20ml kahlua, 20ml amaretto, 20ml bailey's, cream)</small></i><b></p>" +
                    "</b>", FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.longDrinks.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("long drinks");
            binding.categoryImageView.setImageResource(R.drawable.long_drinks);
            binding.drinksDetail.setText(Html.fromHtml("<h2>LONG DRINKS /270ml</h2><b>" +
                    "<p>Cuba Libre</b><i><small> (40ml rom Havana 3yo, lime, Coca-Cola)</small></i><b></p>" +
                    "<p>Gin Tonic</b><i><small> (40ml gin Beefeater, lime, grapefruit, apa tonica)</small></i><b></p>" +
                    "<p>Vodka Juice</b><i><small> (40ml vodka Wyborowa, suc de fructe Santal)</small></i><b></p>" +
                    "<p>Whiskey Cola</b><i><small> (40ml whiskey Jameson, Coca-Cola)</small></i><b></p>" +
                    "<p>Whiskey/Vodka Energy</b><i><small> (40ml vodka/whiskey, Red Bull)</small></i><b></p>" +
                    "<p>Bloody Mary</b><i><small> (40ml vodka Wyborowa, suc de rosii, Santal, sare, piper, Tabasco, telina)</small></i><b></p>" +
                    "</b>", FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.shots.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("shots");
            binding.categoryImageView.setImageResource(R.drawable.shots);
            binding.drinksDetail.setText(Html.fromHtml("<h2>SHOTS /30ml</h2><b>" +
                    "<p>Tequila</p>" +
                    "<p>Jagermeister</p>" +
                    "<p>B52</p>" +
                    "<p>Baby Guinness</p>" +
                    "<p>Stroh</p>" +
                    "<p>Godfather</p>" +
                    "<p>Cozonac</p>" +
                    "<p>Tatratea</p>" +
                    "<p>Kamikaze (3 shots)</p>" +
                    "</b>", FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.spirits.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("spirits");
            binding.categoryImageView.setImageResource(R.drawable.spirits);
            binding.drinksDetail.setText(Html.fromHtml("<h2>SPIRITS /50ml</h2><b>" +
                    "<p>Wyborowa</p>" +
                    "<p>Absolut</p>" +
                    "<p>Beluga</p>" +
                    "<p>Havana 3yo</p>" +
                    "<p>Bacardi White</p>" +
                    "<p>Bacardi Oakheart</p>" +
                    "<p>Jameson</p>" +
                    "<p>Jack Daniels</p>" +
                    "<p>Jim Beam White</p>" +
                    "<p>Glenfiddich</p>" +
                    "<p>Martel VS</p>" +
                    "<p>Beefeater</p>" +
                    "<p>Whitley Neill</p>" +
                    "<p>Jagermeister</p>" +
                    "<p>Disaronno</p>" +
                    "<p>Baileys Irish Cream</p>" +
                    "</b>", FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.wine.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("wine");
            binding.categoryImageView.setImageResource(R.drawable.wine);
            binding.drinksDetail.setText(Html.fromHtml("<h2>VIN ALB (150ml /750ml)</h2><b>" +
                    "<p>Prahova Valley</b><i><small> (dry, sauvignon blanc)</small></i><b></p>" +
                    "<p>La Origine Sauvignon Blanc</b><i><small> (dry)</small></i><b></p>" +
                    "<p>Byzantium</b><i><small> (dry, cupaj de chardonnay, feteasca alba, sauvignon blanc)</small></i><b></p>" +
                    "<p>Colina</b><i><small> (feteasca alba)</small></i><b></p>" +
                    "<p>Pelin Frizzante</p>" +
                    "<p>Floarea Soarelui</b><i><small> (medium dry, feteasca regala)</small></i><b></p>" +
                    "</b>" +
                    "<br><h2>VIN ROSE (150ml /750ml)</h2><b>" +
                    "<p>Prahova Valley</b><i><small> (medium dry)</small></i><b></p>" +
                    "<p>La Origine Roze</b><i><small> (dry)</small></i><b></p>" +
                    "<p>Byzantium</b><i><small> (dry, shiraz)</small></i><b></p>" +
                    "<p>Colina</b><i><small> (shiraz rose)</small></i><b></p>" +
                    "<p>Pelin Frizzante</p>" +
                    "<p>Floarea Soarelui</b><i><small> (medium dry, merlot)</small></i><b></p>" +
                    "<br><h2>VIN ROSU (150ml /750ml)</h2><b>" +
                    "<p>Prahova Valley</b><i><small> (dry, cabernet sauvignon)</small></i><b></p>" +
                    "<p>La Origine Feteasca Neagra</b><i><small> (dry)</small></i><b></p>" +
                    "<p>Byzantium</b><i><small> (dry, cupaj feteasca neagra, shiraz, cabernet franc)</small></i><b></p>" +
                    "<p>Colina</b><i><small> (cabernet sauvignon)</small></i><b></p>" +
                    "<p>Floarea Soarelui</b><i><small> (medium dry, feteasca neagra)</small></i><b></p>" +
                    "</b>", FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.spritz.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("spritz");
            binding.categoryImageView.setImageResource(R.drawable.spritz);
            binding.drinksDetail.setText(Html.fromHtml("<h2>SPRITZ</h2><b>" +
                    "<p>Aperol Spritz</b><i><small> (150ml prosecco, 40ml aperol, apa minerala)</small></i><b></p>" +
                    "<p>Hugo</b><i><small> (150ml prosecco, sirop soc, lime, menta, apa minerala)</small></i><b></p>" +
                    "<p>Prosecco 150ml</p>" +
                    "<p>Prosecco 750ml</p>" +
                    "<br><p>Frizza roze/alb 150ml</b><i><small> (Vinca)</small></i><b></p>" +
                    "<p>Frizza roze/alb 750ml</b><i><small> (Vinca)</small></i><b></p>" +
                    "</b>", FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        } else if (v.getId() == binding.cider.getId()) {
            toggleMenuItemVisibility();
            binding.title.setText("cider");
            binding.categoryImageView.setImageResource(R.drawable.cider);
            binding.drinksDetail.setText(Html.fromHtml("<h2>CIDRU (4.5% alc.)</h2><b>" +
                    "<p>Strongbow Gold Apple</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Strongbow Red Berries</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Strongbow Elderflower</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Cidru mandru mere</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Cidru mandru visine</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Livada Secreta mere</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Old Mout Kiwi&Lime</b><i><small> /500 ml</small></i><b></p>" +
                    "<p>Old Mout Passion Fruit&Apple</b><i><small> /500 ml</small></i><b></p>" +
                    "<p>Old Mout Summer Berries</b><i><small> /500 ml</small></i><b></p>" +
                    "</b>", FROM_HTML_SEPARATOR_LINE_BREAK_HEADING, null, null));
        }

        showSnackbar();
    }

    private void toggleMenuItemVisibility() {
        if (!menuItemsHidden) {
            binding.nonAlcoholic.setVisibility(View.GONE);
            binding.beer.setVisibility(View.GONE);
            binding.cocktails.setVisibility(View.GONE);
            binding.longDrinks.setVisibility(View.GONE);
            binding.shots.setVisibility(View.GONE);
            binding.spirits.setVisibility(View.GONE);
            binding.wine.setVisibility(View.GONE);
            binding.spritz.setVisibility(View.GONE);
            binding.cider.setVisibility(View.GONE);
            menuItemsHidden = true;
        } else {
            binding.nonAlcoholic.setVisibility(View.VISIBLE);
            binding.beer.setVisibility(View.VISIBLE);
            binding.cocktails.setVisibility(View.VISIBLE);
            binding.longDrinks.setVisibility(View.VISIBLE);
            binding.shots.setVisibility(View.VISIBLE);
            binding.spirits.setVisibility(View.VISIBLE);
            binding.wine.setVisibility(View.VISIBLE);
            binding.spritz.setVisibility(View.VISIBLE);
            binding.cider.setVisibility(View.VISIBLE);
            menuItemsHidden = false;
        }
    }

    //snackbar on list detail to link to manasia food
    private void showSnackbar() {
        snackbar = Snackbar.make(binding.constraintLayout,
                "Hungry? Visit the food menu:",
                Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("Manasia Food", v -> {
            Intent intent = new Intent(this, FoodMenuActivity.class);
            intent.putExtra(SOURCE_ACTIVITY, SOURCE_DRINKS_MENU_ACTIVITY);
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
