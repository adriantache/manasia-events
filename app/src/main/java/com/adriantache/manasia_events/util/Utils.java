package com.adriantache.manasia_events.util;

import android.support.annotation.Nullable;

import com.adriantache.manasia_events.custom_class.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Class to store general utility functions
 **/
public final class Utils {
    private Utils() {
        throw new AssertionError("No Utils Instances are allowed!");
    }

    public static String extractDate(String s, boolean day) {
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

    public static int compareDateToToday(String date) {
        final int DATE_ERROR = 999999;

        Date formattedDate = null;
        try {
            formattedDate = new SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Date today = new Date();

        //fix to set time to midnight -1 second, to ensure events from today are shown
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(today);
        calendar.set(Calendar.HOUR, -12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, -1);
        today = calendar.getTime();

        if (formattedDate != null) {
            return formattedDate.compareTo(today);
        }

        //failure will default to show actions and let the user decide
        else return DATE_ERROR;
    }

    public static ArrayList<Event> updateNotifyInRemote(ArrayList<Event> remoteEvents, @Nullable ArrayList<Event> localEvents) {
        if (localEvents == null) return remoteEvents;

        //firstly remove all events that don't have a notify flag set or which are in the past
        Iterator<Event> iterator = localEvents.iterator();
        while (iterator.hasNext()) {
            Event localEvent = iterator.next();

            if (localEvent.getNotify() == 0) {
                iterator.remove();
                continue;
            }
            if (compareDateToToday(localEvent.getDate()) < 0)
                iterator.remove();
        }

        //then update the notify flag for remote events that match
        //reversing it because there are likely fewer local events that match
        for (Event localEvent : localEvents) {
            String title = localEvent.getTitle();
            String description = localEvent.getDescription();

            for (Event remoteEvent : remoteEvents) {
                if (remoteEvent.getTitle().equals(title) && remoteEvent.getDescription().equals(description))
                    remoteEvent.setNotify(1);
            }
        }

        return remoteEvents;
    }
}