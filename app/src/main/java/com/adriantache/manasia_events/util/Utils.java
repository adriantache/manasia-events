package com.adriantache.manasia_events.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.adriantache.manasia_events.custom_class.Event;

import java.io.IOException;
import java.io.InputStream;
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

import static android.content.Context.MODE_PRIVATE;
import static com.adriantache.manasia_events.EventDetail.NOTIFY_SETTING;
import static com.adriantache.manasia_events.EventDetail.SHARED_PREFERENCES_TAG;

/**
 * Class to store general utility functions
 **/
public final class Utils {
    private static final int ERROR_VALUE = -1;

    private Utils() {
        throw new AssertionError("No Utils Instances are allowed!");
    }

    //date related methods
    public static String extractDayOrMonth(String s, boolean day) {
        if (TextUtils.isEmpty(s)) return "ERROR";

        String[] parts = s.split("-");

        if (parts.length != 3) return "ERROR";

        if (day) return parts[2];
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
        Date formattedDate = convertDate(date, false);
        Date today = getToday(true);

        if (formattedDate != null)
            return formattedDate.compareTo(today);

            //failure will default to show actions and let the user decide
        else return ERROR_VALUE;
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
            formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (getNoon) {
            //set time to noon to prevent sending annoying notifications
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.setTime(formattedDate);
            calendar.set(Calendar.HOUR, +12);
            formattedDate = calendar.getTime();
        }

        return formattedDate;
    }

    public static long calculateDelay(String eventDate) {
        Date event = convertDate(eventDate, true);
        Date today = getToday(false);

        return event.getTime() - today.getTime();
    }

    //make date look nicer for display in the notification text
    public static String prettyDate(String date) {
        return extractDayOrMonth(date, false) + " " + extractDayOrMonth(date, true);
    }

    public static boolean isEventToday(String date) {
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH);

        String[] parts = date.split("-");
        int eventDay = Integer.parseInt(parts[0]);
        int eventMonth = Integer.parseInt(parts[1]);

        return day == eventDay && month == eventMonth;
    }


    //utility methods
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

    public static boolean getNotifyAllSetting(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(SHARED_PREFERENCES_TAG, MODE_PRIVATE);
        return sharedPref.getBoolean(NOTIFY_SETTING, false);
    }

    //turn a 3 letter month and a day "int" into a date of the format yyyy-MM-dd
    public static String buildDate(String month, String day) {
        StringBuilder date = new StringBuilder();
        int monthNumber;

        //start with input checks
        if (day.length() > 2 || day.length() == 0) return null;

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

        //add current year, checking for edge cases
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getDefault());
        calendar.setTime(today);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentYear = calendar.get(Calendar.YEAR);
        if (currentMonth < 4 && monthNumber > 9)
            date.append(currentYear - 1);
        else if (currentMonth > 9 && monthNumber < 4)
            date.append(currentYear + 1);
        else
            date.append(currentYear);

        date.append("-");

        //add month
        if (monthNumber < 10) date.append("0");
        date.append(monthNumber);

        date.append("-");

        //add day
        if (day.length() == 1) date.append("0");
        date.append(day);

        return date.toString();
    }

    public static String getImageUrl(String image_tag) {
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

        return image_tag;
    }

    //get sample JSON string from file
    public static String getSampleJSON(Context context) {
        String file = null;

        try {
            InputStream inputStream = context.getResources().openRawResource(
                    context.getResources().getIdentifier("sample_json",
                            "raw", context.getPackageName()));

            if (inputStream != null) {
                int ch;
                StringBuilder sb = new StringBuilder();
                try {
                    while ((ch = inputStream.read()) != -1) {
                        sb.append((char) ch);
                    }
                } catch (IOException e) {
                    Log.e("GetSampleJSON", "Cannot read API key InputStream.", e);
                }

                if (sb.length() != 0) file = sb.toString();
            }
        } catch (Resources.NotFoundException e) {
            Log.e("GetSampleJSON", "Cannot open API key file.", e);
        }

        return file;
    }
}