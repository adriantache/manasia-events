package com.adriantache.manasia_events.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class EventAdapter extends ArrayAdapter<Event> {
    public EventAdapter(@NonNull Context context, @NonNull List<Event> objects) {
        super(context, 0, objects);
    }

    //todo personalize this adapter for the layout

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.thumbnail = convertView.findViewById(R.id.thumbnail);
            holder.category = convertView.findViewById(R.id.category);
            holder.title = convertView.findViewById(R.id.title);
            holder.date = convertView.findViewById(R.id.date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = getItem(position);

        //todo create and set placeholder image
        if (event != null) {
            if (event.getPhotoUrl() != null)
                Picasso.get().load(event.getPhotoUrl()).into(holder.thumbnail);
            else
                holder.thumbnail.setImageResource(R.drawable.powered_by_guardian);

            holder.category.setText(event.getCategory());
            holder.title.setText(event.getTitle());
            holder.date.setText(event.getDate());
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView thumbnail;
        TextView category;
        TextView title;
        TextView date;
    }
}
