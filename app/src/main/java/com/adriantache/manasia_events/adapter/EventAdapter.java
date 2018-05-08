package com.adriantache.manasia_events.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.util.Utils;
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
            holder.day.setText(Utils.extractDate(event.getDate(), true));
            holder.month.setText(Utils.extractDate(event.getDate(), false));
            holder.title.setText(event.getTitle());

            //hide notification group if event is in the past
            if (Utils.compareDateToToday(event.getDate()) < 0)
                holder.bookmark_layout.setVisibility(View.INVISIBLE);
            else holder.bookmark_layout.setVisibility(View.VISIBLE);

            //change notification image depending on whether the user has set it to notify them
            if (holder.bookmark_layout.getVisibility() == View.VISIBLE) {
                if (event.getNotify()) holder.bookmark.setImageResource(R.drawable.alarm_accent);
                else holder.bookmark.setImageResource(R.drawable.alarm);
            }
        }

        return convertView;
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