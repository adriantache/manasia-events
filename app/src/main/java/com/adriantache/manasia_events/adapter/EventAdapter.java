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
import com.adriantache.manasia_events.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.adriantache.manasia_events.util.Utils.getNotifyAllSetting;

public class EventAdapter extends ArrayAdapter<Event> {
    private final List<Event> events = new ArrayList<>();

    public EventAdapter(@NonNull Context context, @NonNull List<Event> objects) {
        super(context, 0, objects);
        this.events.clear();
        this.events.addAll(objects);
    }

    @Override
    public int getCount() {
        return events.size();
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
            if (!TextUtils.isEmpty(event.getPhotoUrl())) {
                Picasso.get().load(event.getPhotoUrl()).fit().centerCrop().into(holder.thumbnail);
                holder.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                holder.thumbnail.setBackgroundResource(R.color.colorAccent);
            } else {
                holder.thumbnail.setImageResource(R.drawable.manasia_logo);
                holder.thumbnail.setScaleType(ImageView.ScaleType.CENTER);
                holder.thumbnail.setScrollY(0);
                holder.thumbnail.setBackgroundResource(R.color.blue_grey100);
            }

            holder.day.setText(Utils.extractDayOrMonth(event.getDate(), true));
            holder.month.setText(Utils.extractDayOrMonth(event.getDate(), false));
            holder.title.setText(event.getTitle());

            //hide notification group if event is in the past
            if (Utils.compareDateToToday(event.getDate()) < 0)
                holder.notify_status.setVisibility(View.INVISIBLE);
            else holder.notify_status.setVisibility(View.VISIBLE);

            //change notification image depending on whether the user has set it to notify them
            if (holder.notify_status.getVisibility() == View.VISIBLE) {
                if (event.getNotify() == 1 || getNotifyAllSetting(getContext()))
                    holder.notify_status.setImageResource(R.drawable.alarm_accent);
                else holder.notify_status.setImageResource(R.drawable.alarm);
            }
        }

        return convertView;
    }

    public static class ViewHolder {
        @BindView(R.id.thumbnail)
        ImageView thumbnail;
        @BindView(R.id.day)
        TextView day;
        @BindView(R.id.month)
        TextView month;
        @BindView(R.id.title)
        TextView title;
        @BindView(R.id.notify_status)
        ImageView notify_status;

        private ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}