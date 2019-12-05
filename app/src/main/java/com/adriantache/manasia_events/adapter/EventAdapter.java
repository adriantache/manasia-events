package com.adriantache.manasia_events.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;
import com.adriantache.manasia_events.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.adriantache.manasia_events.util.CommonStrings.NOTIFY_SETTING;
import static com.adriantache.manasia_events.util.Utils.compareDateToToday;

public class EventAdapter extends ArrayAdapter<Event> {
    private final List<Event> events = new ArrayList<>();

    public EventAdapter(@NonNull Context context, @NonNull List<Event> objects) {
        super(context, 0, objects);
        this.events.addAll(objects);
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    @NonNull
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();

            holder.thumbnail = convertView.findViewById(R.id.thumbnail);
            holder.day = convertView.findViewById(R.id.day);
            holder.month = convertView.findViewById(R.id.month);
            holder.title = convertView.findViewById(R.id.title);
            holder.notifyStatus = convertView.findViewById(R.id.notify_status);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Event event = getItem(position);

        if (event != null) {
            if (!TextUtils.isEmpty(event.getPhotoUrl())) {
                Picasso.get().load(event.getPhotoUrl()).fit().centerCrop().into(holder.thumbnail);
                holder.thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);

                //gray out event thumbnail and ImageView background if it's in the past
                if (compareDateToToday(event.getDate()) < 0) {
                    ColorMatrix matrix = new ColorMatrix();
                    matrix.setSaturation(0);

                    ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                    holder.thumbnail.setColorFilter(filter);
                    holder.thumbnail.setBackgroundResource(R.color.colorDeselected);
                } else {
                    holder.thumbnail.setColorFilter(null);
                    holder.thumbnail.setBackgroundResource(R.color.colorAccent);
                }
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
            if (compareDateToToday(event.getDate()) < 0)
                holder.notifyStatus.setVisibility(View.INVISIBLE);
            else holder.notifyStatus.setVisibility(View.VISIBLE);

            //change notification image depending on whether the user has set it to notify them
            SharedPreferences sharedPref = getDefaultSharedPreferences(getContext());
            boolean notifyOnAllEvents = sharedPref.getBoolean(NOTIFY_SETTING, false);
            if (holder.notifyStatus.getVisibility() == View.VISIBLE) {
                if (event.getNotify() == 1 || notifyOnAllEvents)
                    holder.notifyStatus.setImageResource(R.drawable.alarm_accent);
                else holder.notifyStatus.setImageResource(R.drawable.alarm);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView thumbnail;
        TextView day;
        TextView month;
        TextView title;
        ImageView notifyStatus;
    }
}