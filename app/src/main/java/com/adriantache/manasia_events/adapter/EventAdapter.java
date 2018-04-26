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
import android.widget.TextView;

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

        private ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}