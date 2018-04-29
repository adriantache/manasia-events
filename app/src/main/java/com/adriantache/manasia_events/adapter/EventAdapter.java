package com.adriantache.manasia_events.adapter;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.adriantache.manasia_events.MainActivity;
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

            //todo hide notify button for past events

            //making a copy of the ViewHolder to set the image drawable without making the main
            // ViewHolder final (that results in a bug where each click changes every other item)
            //todo fix bug that I thought I had fixed :(
            //problem is assignment affects multiple list items
            final ImageView bookmarkIV = holder.bookmark;
            final Event tempEvent = event;
            //todo implement actual notification code
            //todo implement toggle mechanic, probably directly in the Event class
            //todo implement notifications in the main Event class, then run a method to reset and then set all notifications (might be inefficient)
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
                        showNotification(tempEvent);
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

    //todo implement real notification system (probably with a service)
    //todo schedule notification https://stackoverflow.com/questions/36902667/how-to-schedule-notification-in-android
    //http://droidmentor.com/schedule-notifications-using-alarmmanager/
    //https://developer.android.com/topic/performance/scheduling
    private void showNotification(Event event) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "manasia_notification";
            String description = "manasia notification channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("MANASIA", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        //todo rewrite this once we figure out data delivery
        //get latest event and Build notification
        String notificationTitle = "Manasia event: "+event.getTitle();
        String notificationText = event.getDate() + " at Stelea Spatarul 13, Bucuresti";
        int notificationLogo = event.getCategory_image();

        //todo improve intent and the notification in general, ideally point directly to notified event
        //maybe make activity open latest even by default, but how do you send it to it?
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getContext(), "MANASIA")
                .setSmallIcon(notificationLogo)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getContext());
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, mBuilder.build());
    }
}