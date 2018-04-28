package com.adriantache.manasia_events.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventAdapter extends ArrayAdapter<Event> {
    public EventAdapter(@NonNull Context context, @NonNull List<Event> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = getItem(position);

        if (event != null) {
            if (!TextUtils.isEmpty(event.getPhotoUrl()))
                Picasso.get().load(event.getPhotoUrl()).into(holder.thumbnail);
            else
                holder.thumbnail.setImageResource(R.drawable.manasia_logo);

            holder.category_image.setImageResource(event.getCategory_image());
            holder.day.setText(extractDate(event.getDate(), true));
            holder.month.setText(extractDate(event.getDate(), false));
            holder.title.setText(event.getTitle());

            //making a copy of the ViewHolder to set the image drawable without making the main
            // ViewHolder final (that results in a bug where each click changes every other item)
            //todo fix bug that I thought I had fixed :(
            //problem is assignment affects multiple list items
            final ImageView bookmarkIV = holder.bookmark;
            final Event tempEvent = event;
            //todo implement actual notification code
            //todo implement toggle mechanic, probably directly in the Event class
            holder.bookmark_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tempEvent.getNotify()) {
                        Toast.makeText(getContext(), "Disabled notification.", Toast.LENGTH_SHORT).show();
                        bookmarkIV.setImageResource(R.drawable.bookmark);
                        tempEvent.setNotify(false);
                    } else {
                        Toast.makeText(getContext(), "We will notify you on the day of the event.", Toast.LENGTH_SHORT).show();
                        bookmarkIV.setImageResource(R.drawable.bookmark_green);
                        tempEvent.setNotify(true);
                    }
                }
            });
        }

        return convertView;
    }

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

    static class ViewHolder {
        @BindView(R.id.thumbnail)
        ImageView thumbnail;
        @BindView(R.id.day)
        TextView day;
        @BindView(R.id.month)
        TextView month;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.category_image)
        ImageView category_image;
        @BindView(R.id.bookmark)
        ImageView bookmark;
        @BindView(R.id.bookmark_layout)
        LinearLayout bookmark_layout;

        private ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}