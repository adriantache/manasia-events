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
        arrayList.add(new Event("27.04.2018",
                "Lansare videoclip Baieti Cuminti la Linia 1",
                "Băieți Cuminți are back si lanseaza clip la \"Valuri\", Linia 1, ofc, ca au venit si banii de pe iutub. \uD83C\uDF0A\uD83C\uDF7E \n" +
                        "E un clip misto, asa, de vacanta \uD83C\uDF34\n" +
                        "E gen bring your own popcorn,il proiectam, dupa care facem party, tot la Manasia Hub \uD83C\uDF5A\uD83E\uDD62\n" +
                        "\n" +
                        "Vineri 27 aprilie, Stelea Spatarul nr. 13.\n" +
                        "\n" +
                        "Intrare libera.",
                "https://scontent.fotp3-3.fna.fbcdn.net/v/t1.0-9/31059487_351218945388018_5238109661428711424_n.jpg?_nc_cat=0&oh=08c060473048a2661fff3575f42e7b5d&oe=5B6939B5",
                R.drawable.music));
        arrayList.add(new Event("22.04.2018",
                "Pre-Owned Market",
                "• PRE-OWNED MARKET\n" +
                        "• 22 aprilie | 12:00 pm | Manasia Hub | \n" +
                        "• Diggers | Shopping | Music | No Bad Vibes!\n" +
                        "_______________________________________________\n" +
                        "\n" +
                        "Facem o mare nebunie de Pre-Owned Market, duminică, 22 aprilie la hubul Manasia, începand cu 12:00pm. \n" +
                        "Te ajutăm să te detașezi de niște lucruri mișto, pe care alții le-ar găsi și mai mișto, chiar folosindu-le cu drag!\n" +
                        "\n" +
                        "Uite care e combinația: fie că e vorba de cadourile pe care le-ai primit și de care nu te-ai mai atins, de goblenurile de la bunica, hainele care încă stau cu etichetă în dulap sau sneakerșii de pe Ebay așteptați 3 saptămâni, doar ca să constați ca erau mici, adu-le pe toate la Pre-Owned Market.\n" +
                        "\n" +
                        "Uite așa, ți-ai scos banii de 1 mai! \n" +
                        "Cu ocazia asta o pui și de un outfit nou de festival. Oricum, știi și tu că te-a mai văzut lumea cu rochia aia, așa că... dă-o mai departe! \n" +
                        "\n" +
                        "Hai să faci afacere și să îți iei un Rembrandt la colecție, gantere de 10 kile, vinyl cu Jackson sau Eminem și cine știe ce alte haine îți vor face cu ochiul.\n" +
                        "\n" +
                        "Vinde, cumpără, combină... pe muzică fină, în curte la cald, totul e frumos! \n" +
                        "\n" +
                        "• Dj set : Olaru \uD83C\uDF9B️ https://soundcloud.com/olarugeorge\n" +
                        "Vinyl selections by Madpiano \uD83C\uDFB6 https://www.facebook.com/madpiano.ro\n" +
                        "• Special drinks & food available \n" +
                        "_______________________________________________\n" +
                        "\n" +
                        "Locuri limitate! Pentru înscriere PM sau mail pe bucharestinart@gmail.com și îți vom trimite cele necesare.\n" +
                        "| no junk please, photo selection will be made\n" +
                        "| nu aduce nimic din ce nici tu nu ai cumpăra \n" +
                        "_______________________________________________\n" +
                        "\n" +
                        "Intrare libera pentru vizitatori.\n" +
                        "Eveniment susținut de ▲ BUCHAREST IN ART ▲", "https://i.imgur.com/8TIPwNB.png",
                R.drawable.shop));
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
                R.drawable.music));
        arrayList.add(new Event("21.04.2018",
                "Record Store Day by MadPiano",
                "The best day of shopping for audiophiles and music-lovers, \n" +
                        "Record Store Day by MadPiano is back for 2018.\n" +
                        "This year marking the 11th anniversary of Record Store Day around the world, an awareness-raising celebration of the brick-and-mortar independent record store as a cultural hub for hardcore vinyl geeks and casual music fans alike.\n" +
                        "\n" +
                        "Line-up:\n" +
                        "14:00 >> Lazar Cristian\n" +
                        "16:00 >> Rareş Gall (Gotgroove?)\n" +
                        "18:00 >> Posh/posh111\n" +
                        "20:00 >> Live Ambient Guitar Session w/ Leonte George\n" +
                        "\n" +
                        "*OFF 10% for all vinyl\n" +
                        "*Tombola with vinyls for all buyers on this day\n" +
                        "*In addition we are offering exclusive releases\n" +
                        "\n" +
                        "Get social:\n" +
                        "MadPiano Record Store\n" +
                        ">> http://madpiano.ro/\n" +
                        ">> https://soundcloud.com/madpiano_ro\n" +
                        ">> https://goo.gl/rkrVNa\n" +
                        "Manasia Hub\n" +
                        "Stelea Spătarul 13, Bucharest, Romania, 030211",
                "https://scontent.fotp3-3.fna.fbcdn.net/v/t1.0-9/30729359_1138878096253651_5540754426124626056_n.jpg?_nc_cat=0&oh=bd41574512f8ab8870a20e447641935a&oe=5B9947FB",
                R.drawable.shop));
        arrayList.add(new Event("20.04.2018",
                "Discotek Bash w/ Iancu & Groovemanescu",
                "Discotek Bash w/ Iancu & Groovemanescu\n" +
                        "\n" +
                        "In need of that familiar feeling for a FRI night out? \n" +
                        "\n" +
                        "Something of a house party that actually takes over a whole garden filled up with friends? :D \n" +
                        "\n" +
                        "Well ... you got it ... our dear friends and colleagues are taking over the DJ desk for a proper DiscoTech bash!\n" +
                        "\n" +
                        "Expect an immersive house sound where bits and pieces of tech groove and disco memorabilia are woven together just in time for the perfect tequila sunrise ;) \n" +
                        "\n" +
                        "Manasia Hub | real people | real stories \n" +
                        "Stelea Spataru 13",
                "https://scontent.fotp3-3.fna.fbcdn.net/v/t1.0-9/30739942_2136764383223700_5742396593584209920_n.jpg?_nc_cat=0&oh=1653c8e3bff2cfd90dd7c0517d9c2f41&oe=5B55BCA8",
                R.drawable.music));
        arrayList.add(new Event("14.04.2018",
                "Dirty Disco pres. Eugen Radescu and Wefa at Manasia Hub",
                "Dirty Disco w/ Eugen Rădescu & Wefa\n" +
                        "\n" +
                        "A deliberate attempt to get your body moving while losing yourself in the garden, on the dancefloor or ... everywhere in between. \n" +
                        "\n" +
                        "A mash-up of disco-pop euphoria, oriental extravaganza with a pinch of Romanian manele for a decadent after taste. \n" +
                        "\n" +
                        "One of those mornings that have a sweet flavour of '''i don't remember much, bur I know I had it all. ''\n" +
                        ".\n" +
                        "Manasia Hub | real people | real stories \n" +
                        "Stelea Spataru 13",
                "https://scontent.fotp3-3.fna.fbcdn.net/v/t1.0-9/29793163_2129934620573343_3532333566979473408_n.jpg?_nc_cat=0&oh=da3d50362ed5e55f097133e727abe502&oe=5B67A779",
                R.drawable.music));
        arrayList.add(new Event("13.04.2018",
                "Mălina & Tugay și Tugay & Mălina la Linia 1",
                "Malina si Tugay – combinatie de Du Bye Bye\n" +
                        "Malina e un mare fan al imperativului categoric si al fetelor cu par la subrat. Ii place Sylvia Plath si cateodata o citeste cu manele pe fundal. \uD83D\uDC85\uD83D\uDCDA⚔️\n" +
                        "Tugay e proud member of Alex si Tugay & Tugay si Alex si zice ca l-a calcat masina dar a reinviat, s-a lasat de CocaCola si vine sa puna muzica.\n" +
                        "Amandoi la Linia 1 \uD83C\uDFB0\uD83D\uDCBD\uD83D\uDC23\n" +
                        "\n" +
                        "\n" +
                        "Vineri 13 aprilie Manasia Hub\n" +
                        "\n" +
                        "Intrare libera.",
                "https://scontent.fotp3-3.fna.fbcdn.net/v/t1.0-9/30442737_346746755835237_3773800850313445376_n.jpg?_nc_cat=0&oh=0a60fe77a48085f0cd13d07c11ea3888&oe=5B93C571",
                R.drawable.music));
        arrayList.add(new Event("06.04.2018",
                "Glittoris la Linia 1",
                "Visul cel mai mare al Dianei (de om care pune muzică, gen) e ca doi oameni între care există o tensiune sexuală să ajungă să se pupe pentru prima oară la petrecerea ei. \uD83D\uDC49\uD83D\uDC4C\uD83D\uDC8D\n" +
                        "Să știe că Diana a fost pețitoare. Se dansează cu plante și femei pe pe rnb, tehno și brockhampton. \uD83C\uDF31\uD83D\uDCBD\n" +
                        "De Linia 1 nu mai zicem că știți deja. ➖\n" +
                        "\n" +
                        "https://www.youtube.com/c/Linia1prezinta\n" +
                        "https://www.facebook.com/manasiahub\n" +
                        "\n" +
                        "Vineri 6 Aprilie\n" +
                        "Manasia Hub (Str. Stelea Spatarul nr. 13)\n" +
                        "\n" +
                        "Intrare libera.",
                "https://scontent.fotp3-3.fna.fbcdn.net/v/t1.0-9/29573169_343846172791962_1467685779540178961_n.jpg?_nc_cat=0&oh=59195fcc6236cec1ff599860b64518ac&oe=5B9457A3",
                R.drawable.music));

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
