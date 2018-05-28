package com.adriantache.manasia_events.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.adriantache.manasia_events.R;
import com.adriantache.manasia_events.custom_class.Event;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static android.content.Context.MODE_PRIVATE;
import static com.adriantache.manasia_events.EventDetail.NOTIFY;
import static com.adriantache.manasia_events.EventDetail.SHARED_PREFERENCES_TAG;

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

        Date formattedDate = convertDate(date, false);
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
     * todo use this method or remove it
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
        Date event = convertDate(eventDate, true);
        Date today = getToday(false);

        return event.getTime() - today.getTime();
    }

    public static boolean getNotifyAllSetting(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
        return sharedPref.getBoolean(NOTIFY, false);
    }

    public static ArrayList<Event> parseJSON(String JSON) {
        if (TextUtils.isEmpty(JSON)) return null;

        ArrayList<Event> events = new ArrayList<>();

        //parse JSON String
        try {
            JSONObject root = new JSONObject(JSON);
            JSONArray event_title = root.getJSONArray("event_title");

            for (int i = 0; i < event_title.length(); i++) {
                JSONObject child = event_title.getJSONObject(i);
                String title = child.getString("name");
                String date = buildDate(child.getString("month"), child.getString("day"));
                String description = child.getString("description");
                String image_url = child.optString("image_url");
                if (!TextUtils.isEmpty(image_url)) image_url = getImageUrl(image_url);

                if (date != null && title != null && description != null)
                    events.add(new Event(date, title, description, image_url, R.drawable.hub));
            }
        } catch (JSONException e) {
            Log.e("parseJSON", "Cannot parse JSON", e);
        }

        return events;
    }

    private static String buildDate(String month, String day) {
        StringBuilder date = new StringBuilder();

        if (day.length() > 2 || day.length() == 0) return null;

        if (day.length() == 1) date.append("0");
        date.append(day);
        date.append(".");

        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(today);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);

        int monthNumber;

        switch (month) {
            case "JAN":
                monthNumber = 1;
                break;
            case "FEB":
                monthNumber = 2;
                break;
            case "MAR":
                monthNumber = 3;
                break;
            case "APR":
                monthNumber = 4;
                break;
            case "MAY":
                monthNumber = 5;
                break;
            case "JUN":
                monthNumber = 6;
                break;
            case "JUL":
                monthNumber = 7;
                break;
            case "AUG":
                monthNumber = 8;
                break;
            case "SEP":
                monthNumber = 9;
                break;
            case "OCT":
                monthNumber = 10;
                break;
            case "NOV":
                monthNumber = 11;
                break;
            case "DEC":
                monthNumber = 12;
                break;
            default:
                return null;
        }

        if (monthNumber < 10) {
            date.append("0");
            date.append(monthNumber);
        } else date.append(monthNumber);
        date.append(".");

        if (currentMonth < 4 && monthNumber > 9)
            date.append(currentYear - 1);
        else if (currentMonth > 9 && monthNumber < 4)
            date.append(currentYear + 1);
        else
            date.append(currentYear);

        Log.i("REMOVE THIS", "buildDate: " + date.toString());

        return date.toString();
    }

    private static String getImageUrl(String image_tag) {
        image_tag = image_tag
                .replace("scontent-yyz1-1.xx.fbcdn.net", "scontent.fotp3-1.fna.fbcdn.net")
                .replace("&amp;", "&")
                .replace("\\\"", "\"");

        Pattern pattern = Pattern.compile("data-plsi=(?:[\"\'])(.+?)(?:[\"\'])(?:.+?)");
        Matcher matcher = pattern.matcher(image_tag);

        if (matcher.find()) {
            image_tag = matcher.group(1);
        } else {
            Pattern pattern2 = Pattern.compile("data-ploi=(?:[\"\'])(.+?)(?:[\"\'])(?:.+?)");
            Matcher matcher2 = pattern2.matcher(image_tag);

            if (matcher2.find()) {
                image_tag = matcher2.group(1);
            }
        }

        Log.i("REMOVE THIS", "buildDate: " + image_tag);

        return image_tag;
    }
}