package com.adriantache.manasia_events;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adriantache.manasia_events.custom_class.Event;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventDetail extends AppCompatActivity {
    @BindView(R.id.thumbnail)
    ImageView thumbnail;
    @BindView(R.id.category_image)
    ImageView category_image;
    @BindView(R.id.day)
    TextView day;
    @BindView(R.id.month)
    TextView month;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.bookmark)
    ImageView bookmark;
    @BindView(R.id.bookmark_layout)
    LinearLayout bookmark_layout;
    @BindView(R.id.description)
    TextView description;
    @BindView(R.id.back)
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);

        Event event = (Event) getIntent().getParcelableArrayListExtra("events").get(0);
        if (!TextUtils.isEmpty(event.getPhotoUrl()))
            Picasso.get().load(event.getPhotoUrl()).into(thumbnail);
        else
            thumbnail.setImageResource(R.drawable.manasia_logo);

        category_image.setImageResource(event.getCategory_image());
        day.setText(extractDate(event.getDate(), true));
        month.setText(extractDate(event.getDate(), false));
        title.setText(event.getTitle());
        description.setText(event.getDescription());

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    //todo set up onclicklisterens and intents for bottom buttons

    private String extractDate(String s, boolean day) {
        String[] parts = s.split("\\.");

        if (parts.length == 0) return "ERROR";

        if (day) return parts[0];
        else switch (parts[1]) {
            case "01":
                return "January";
            case "02":
                return "February";
            case "03":
                return "March";
            case "04":
                return "April";
            case "05":
                return "May";
            case "06":
                return "June";
            case "07":
                return "July";
            case "08":
                return "August";
            case "09":
                return "September";
            case "10":
                return "October";
            case "11":
                return "November";
            case "12":
                return "December";
            default:
                return "ERROR";
        }
    }
}
