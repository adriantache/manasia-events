package com.adriantache.manasia_events.util;

import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.adriantache.manasia_events.custom_class.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.stream.Collectors;

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

        Date formattedDate = convertDate(date,false);
        Date today = getToday(true);

        if (formattedDate != null)
            return formattedDate.compareTo(today);

            //failure will default to show actions and let the user decide
        else return DATE_ERROR;
    }

    private static Date getToday(boolean getMidnight) {
        Date today = new Date();

        if (getMidnight) {
            //fix to set time to midnight -1 second, to ensure events from today are shown
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.setTime(today);
            calendar.set(Calendar.HOUR, -12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, -1);
            today = calendar.getTime();
        }

        return today;
    }

    private static Date convertDate(String date, boolean getNoon) {
        Date formattedDate = null;

        try {
            formattedDate = new SimpleDateFormat("dd.MM.yyyy", Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (getNoon) {
            //set time to noon to prevent sending annoying notifications
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.setTime(formattedDate);
            calendar.set(Calendar.HOUR, +12);
//            calendar.set(Calendar.MINUTE, 0);
//            calendar.set(Calendar.SECOND, 0);
            formattedDate = calendar.getTime();
        }

        return formattedDate;
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

    /**
     * Method searches whether an Event exists in an ArrayList
     * todo use this or remove this
     *
     * @param event      Event object to be searched
     * @param eventsTemp ArrayList to search in
     * @return Returns 0 if event not found, <0 for partial match and >0 if found
     */
    public static int searchEventInArrayList(Event event, @Nullable ArrayList<Event> eventsTemp) {
        if (eventsTemp == null) return 0;

        //search the array for the event
        if (Build.VERSION.SDK_INT >= 24) {
            ArrayList<Event> result = (ArrayList<Event>) eventsTemp.parallelStream()
                    .filter(a -> a.getTitle().equals(event.getTitle()) &&
                            a.getDescription().equals(event.getDescription()) &&
                            a.getDate().equals(event.getDate()) &&
                            a.getPhotoUrl().equals(event.getPhotoUrl()) &&
                            a.getCategory_image() == event.getCategory_image())
                    .collect(Collectors.toList());

            if (result.size() > 0) return result.size();

            result = (ArrayList<Event>) eventsTemp.parallelStream()
                    .filter(a -> (a.getTitle().equals(event.getTitle()) &&
                            a.getDescription().equals(event.getDescription()) ||
                            a.getDate().equals(event.getDate()) ||
                            a.getPhotoUrl().equals(event.getPhotoUrl()) ||
                            a.getCategory_image() == event.getCategory_image())
                            ||
                            (a.getDescription().equals(event.getDescription()) &&
                                    a.getTitle().equals(event.getTitle()) ||
                                    a.getDate().equals(event.getDate()) ||
                                    a.getPhotoUrl().equals(event.getPhotoUrl()) ||
                                    a.getCategory_image() == event.getCategory_image())
                    )
                    .collect(Collectors.toList());

            if (result.size() == 1) {
                //this sets the DBEventID it has found, but this isn't working with the way we
                //currently process the return value for this method
                int DBEventID = result.get(0).getDatabaseID();
            } else if (result.size() > 0) return -result.size();
        } else {
            int foundMatch = 0;
            int foundPartial = 0;

            for (Event a : eventsTemp) {
                if (a.getTitle().equals(event.getTitle()) &&
                        a.getDescription().equals(event.getDescription()) &&
                        a.getDate().equals(event.getDate()) &&
                        a.getPhotoUrl().equals(event.getPhotoUrl()) &&
                        a.getCategory_image() == event.getCategory_image())
                    foundMatch++;
                else if ((a.getTitle().equals(event.getTitle()) &&
                        a.getDescription().equals(event.getDescription()) ||
                        a.getDate().equals(event.getDate()) ||
                        a.getPhotoUrl().equals(event.getPhotoUrl()) ||
                        a.getCategory_image() == event.getCategory_image())
                        ||
                        (a.getDescription().equals(event.getDescription()) &&
                                a.getTitle().equals(event.getTitle()) ||
                                a.getDate().equals(event.getDate()) ||
                                a.getPhotoUrl().equals(event.getPhotoUrl()) ||
                                a.getCategory_image() == event.getCategory_image()))
                    foundPartial++;
            }

            if (foundMatch > 0) return foundMatch;
            else if (foundPartial > 0) return -foundPartial;
        }
        return 0;
    }

    public static long calculateDelay(String eventDate) {
        Date event = convertDate(eventDate,true);
        Date today = getToday(false);

        return event.getTime() - today.getTime();
    }
}