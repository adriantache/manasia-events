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
import android.widget.ImageView;
import android.widget.TextView;

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
    @BindView(R.id.drinksDetail)
    TextView drinksDetail;
    @BindView(R.id.constraint_layout)
    ConstraintLayout constraintLayout;
    @BindView(R.id.title)
    TextView title;

    boolean menuItemsHidden = false;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        ButterKnife.bind(this);

        nonAlcoholic.setOnClickListener(new MenuClickListener());
        beer.setOnClickListener(new MenuClickListener());
        cocktails.setOnClickListener(new MenuClickListener());
        longDrinks.setOnClickListener(new MenuClickListener());
        shots.setOnClickListener(new MenuClickListener());
        spirits.setOnClickListener(new MenuClickListener());
        wine.setOnClickListener(new MenuClickListener());
        spritz.setOnClickListener(new MenuClickListener());
        cider.setOnClickListener(new MenuClickListener());

        back.setOnClickListener(v -> {
            back();
        });
    }

    private void back() {
        if (menuItemsHidden) {
            drinksDetail.setText(null);
            toggleMenuItemVisibility();
            title.setText("manasia menu");
            snackbar.dismiss();
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        back();
    }

    //todo set category image as offset background for each category and display it with the details
    private void animateLayout(View v) {
        Log.i("ONCLICK", "onClick: " + v.getId());

        //workaround because IDs are apparently no longer final
        if (v.getId() == nonAlcoholic.getId()) {
            toggleMenuItemVisibility();
            title.setText("non-alcoholic drinks");
            drinksDetail.setText(Html.fromHtml("<h2>CAFEA/BAUTURI CALDE</h2><b><p>Espresso scurt/lung</p>" +
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
                    "</b>"));
            showSnackbar();
        } else if (v.getId() == beer.getId()) {
            toggleMenuItemVisibility();
            title.setText("beer");
            drinksDetail.setText(Html.fromHtml("<h2>BERE DRAUGHT</h2><b>" +
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
                    "</b>"));
            showSnackbar();
        } else if (v.getId() == cocktails.getId()) {
            toggleMenuItemVisibility();
            title.setText("cocktails");
            drinksDetail.setText(Html.fromHtml("<h2>COCKTAILS FARA ALCOOL</h2><b>" +
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
                    "</b>"));
            showSnackbar();
        } else if (v.getId() == longDrinks.getId()) {
            toggleMenuItemVisibility();
            title.setText("long drinks");
            drinksDetail.setText(Html.fromHtml("<h2>LONG DRINKS /270ml</h2><b>" +
                    "<p>Cuba Libre</b><i><small> (40ml rom Havana 3yo, lime, Coca-Cola)</small></i><b></p>" +
                    "<p>Gin Tonic</b><i><small> (40ml gin Beefeater, lime, grapefruit, apa tonica)</small></i><b></p>" +
                    "<p>Vodka Juice</b><i><small> (40ml vodka Wyborowa, suc de fructe Santal)</small></i><b></p>" +
                    "<p>Whiskey Cola</b><i><small> (40ml whiskey Jameson, Coca-Cola)</small></i><b></p>" +
                    "<p>Whiskey/Vodka Energy</b><i><small> (40ml vodka/whiskey, Red Bull)</small></i><b></p>" +
                    "<p>Bloody Mary</b><i><small> (40ml vodka Wyborowa, suc de rosii, Santal, sare, piper, Tabasco, telina)</small></i><b></p>" +
                    "</b>"));
            showSnackbar();
        } else if (v.getId() == shots.getId()) {
            toggleMenuItemVisibility();
            title.setText("shots");
            drinksDetail.setText(Html.fromHtml("<h2>SHOTS /30ml</h2><b>" +
                    "<p>Tequila</p>" +
                    "<p>Jagermeister</p>" +
                    "<p>B52</p>" +
                    "<p>Baby Guinness</p>" +
                    "<p>Stroh</p>" +
                    "<p>Godfather</p>" +
                    "<p>Cozonac</p>" +
                    "<p>Tatratea</p>" +
                    "<p>Kamikaze (3 shots)</p>" +
                    "</b>"));
            showSnackbar();
        } else if (v.getId() == spirits.getId()) {
            toggleMenuItemVisibility();
            title.setText("spirits");
            drinksDetail.setText(Html.fromHtml("<h2>SPIRITS /50ml</h2><b>" +
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
                    "</b>"));
            showSnackbar();
        } else if (v.getId() == wine.getId()) {
            toggleMenuItemVisibility();
            title.setText("wine");
            drinksDetail.setText(Html.fromHtml("<h2>VIN ALB (150ml /750ml)</h2><b>" +
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
                    "</b>"));
            showSnackbar();
        } else if (v.getId() == spritz.getId()) {
            toggleMenuItemVisibility();
            title.setText("spritz");
            drinksDetail.setText(Html.fromHtml("<h2>SPRITZ</h2><b>" +
                    "<p>Aperol Spritz</b><i><small> (150ml prosecco, 40ml aperol, apa minerala)</small></i><b></p>" +
                    "<p>Hugo</b><i><small> (150ml prosecco, sirop soc, lime, menta, apa minerala)</small></i><b></p>" +
                    "<p>Prosecco 150ml</p>" +
                    "<p>Prosecco 750ml</p>" +
                    "<br><p>Frizza roze/alb 150ml</b><i><small> (Vinca)</small></i><b></p>" +
                    "<p>Frizza roze/alb 750ml</b><i><small> (Vinca)</small></i><b></p>" +
                    "</b>"));
            showSnackbar();
        } else if (v.getId() == cider.getId()) {
            toggleMenuItemVisibility();
            title.setText("cider");
            drinksDetail.setText(Html.fromHtml("<h2>CIDRU (4.5% alc.)</h2><b>" +
                    "<p>Strongbow Gold Apple</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Strongbow Red Berries</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Strongbow Elderflower</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Cidru mandru mere</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Cidru mandru visine</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Livada Secreta mere</b><i><small> /330 ml</small></i><b></p>" +
                    "<p>Old Mout Kiwi&Lime</b><i><small> /500 ml</small></i><b></p>" +
                    "<p>Old Mout Passion Fruit&Apple</b><i><small> /500 ml</small></i><b></p>" +
                    "<p>Old Mout Summer Berries</b><i><small> /500 ml</small></i><b></p>" +
                    "</b>"));
            showSnackbar();
        }
    }

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
            menuItemsHidden = true;
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
            menuItemsHidden = false;
        }
    }

    //snackbar on list detail to link to manasia food
    private void showSnackbar() {
        snackbar = Snackbar.make(constraintLayout,
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
