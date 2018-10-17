package com.adriantache.manasia_events.util;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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

import static com.adriantache.manasia_events.util.CommonStrings.ERROR_VALUE;

/**
 * Class to store general utility functions
 **/
public final class Utils {
    private Utils() {
        throw new AssertionError("No Utils Instances are allowed!");
    }

    //date related methods
    public static String extractDayOrMonth(String s, boolean day) {
        final String error = "ERROR";
        if (TextUtils.isEmpty(s)) return error;

        String[] parts = s.split("-");

        if (parts.length != 3) return error;

        if (day) return parts[2];
        else switch (parts[1]) {
            case "01":
                return "JAN";
            case "02":
                return "FEB";
            case "03":
                return "MAR";
            case "04":
                return "APR";
            case "05":
                return "MAY";
            case "06":
                return "JUN";
            case "07":
                return "JUL";
            case "08":
                return "AUG";
            case "09":
                return "SEP";
            case "10":
                return "OCT";
            case "11":
                return "NOV";
            case "12":
                return "DEC";
            default:
                return error;
        }
    }

    public static int compareDateToToday(String date) {
        Date formattedDate = convertDate(date, false);
        Date today = getToday(true);

        //todo replace compareTo with a custom method that takes into account when notifications are triggered
        if (formattedDate != null)
            return formattedDate.compareTo(today);

            //failure will default to show actions and let the user decide
        else return ERROR_VALUE;
    }

    private static Date getToday(boolean getMidnight) {
        Date today = new Date();

        //todo figure this thing out, we shouldn't be using a fix like this
        if (getMidnight) {
            //fix to set time to midnight -1 second, to ensure events from today are shown
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.setTime(today);
            calendar.set(Calendar.HOUR, -12);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.add(Calendar.SECOND, -1);
            today = calendar.getTime();
        }

        return today;
    }

    //get the date at which the remote file containing the events is refreshed
    public static long getRefreshDate() {
        Calendar c = Calendar.getInstance();

        //set the timezone of Bucharest since that's when the update time is set
        c.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));

        //we add a day and set time to 5 am
        c.add(Calendar.DATE, 1);
        c.set(Calendar.HOUR_OF_DAY, 5);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        return c.getTimeInMillis();
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

    public static long calculateDelay(long targetDate) {
        Date today = getToday(false);

        return targetDate - today.getTime();
    }

    //make date look nicer for display in the notification text
    public static String prettyDate(String date) {
        return extractDayOrMonth(date, false) + " " + extractDayOrMonth(date, true);
    }

    public static boolean isEventToday(String date) {
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH) + 1; //adding 1 because months start at 0 for some reason

        String[] parts = date.split("-");
        int eventDay = Integer.parseInt(parts[2]);
        int eventMonth = Integer.parseInt(parts[1]);

        return day == eventDay && month == eventMonth;
    }

    //set the message that informs people when/until when the bar is open
    public static void getOpenHours(TextView openHours, Context context, boolean generateToast) {
        TimeZone bucharest = TimeZone.getTimeZone("Europe/Bucharest");
        Calendar calendar = Calendar.getInstance(bucharest);

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        //add colors
        final int openColor = 0xff33691E;
        final int closedColor = 0xffBF360C;

        if (hour > 2 && hour < 12) {
            openHours.setText(R.string.closed_noon);
            openHours.setTextColor(closedColor);
            if (generateToast)
                Toast.makeText(context, R.string.closed_noon, Toast.LENGTH_SHORT).show();
        } else if (hour >= 12 && hour < 24) {
            if (day == 1) {
                openHours.setText(R.string.open_midnight);
            } else openHours.setText(R.string.open_2am);
            //set color
            openHours.setTextColor(openColor);
        } else if (hour >= 0 && hour <= 2) {
            if (day == 1) {
                openHours.setText(R.string.closed_noon);
                openHours.setTextColor(closedColor);
                if (generateToast)
                    Toast.makeText(context, R.string.closed_noon, Toast.LENGTH_SHORT).show();
            } else {
                openHours.setText(R.string.open_2am);
                openHours.setTextColor(openColor);
            }
        }
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

    //turn a 3 letter month and a day "int" into a date of the format yyyy-MM-dd
    private static String buildDate(String month, String day) {
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

    private static String getImageUrl(String imageTag) {
        imageTag = imageTag
                .replace("scontent-*.fbcdn.net", "scontent.fotp3-1.fna.fbcdn.net")
                .replace("&amp;", "&")
                .replace("\\\"", "\"");

        Pattern pattern = Pattern.compile("data-plsi=(?:[\"\'])(.+?)(?:[\"\'])(?:.+?)");
        Matcher matcher = pattern.matcher(imageTag);

        if (matcher.find()) {
            imageTag = matcher.group(1);
        } else {
            Pattern pattern2 = Pattern.compile("data-ploi=(?:[\"\'])(.+?)(?:[\"\'])(?:.+?)");
            Matcher matcher2 = pattern2.matcher(imageTag);

            if (matcher2.find()) {
                imageTag = matcher2.group(1);
            }
        }

        return imageTag;
    }

    public static ArrayList<Event> parseJSON(final String JSON) {
        if (TextUtils.isEmpty(JSON) || JSON.length() < 50) return new ArrayList<>();

        ArrayList<Event> events = new ArrayList<>();

        //parse JSON String
        try {
            JSONObject root = new JSONObject(JSON);
            JSONArray eventTitle = root.optJSONArray("event_title");

            //get each event
            for (int i = 0; i < eventTitle.length(); i++) {
                JSONObject child = eventTitle.optJSONObject(i);
                String title = child.optString("name");
                String date = buildDate(child.optString("month"), child.optString("day"));
                String description = child.optString("description_long");
                if (TextUtils.isEmpty(description))
                    description = child.optString("description");
                String imageUrl = child.optString("image_url");
                if (!TextUtils.isEmpty(imageUrl)) imageUrl = getImageUrl(imageUrl);

                //add all tags, if they exist, otherwise return an empty ArrayList
                JSONArray tags = child.optJSONArray("tags");
                ArrayList<String> eventTags = new ArrayList<>();
                if (tags != null)
                    for (int j = 0; j < tags.length(); j++) {
                        JSONObject tag = tags.optJSONObject(j);
                        String tagName = tag.optString("name");
                        if (!TextUtils.isEmpty(tagName)) {
                            //add the tags to the current event
                            eventTags.add(tagName);

                            //add each tag to the class-level tags, and either increment count or create a new one
                            if (Event.tags.containsKey(tagName)) {
                                int value = Event.tags.get(tagName);
                                Event.tags.put(tagName, ++value);
                            } else Event.tags.put(tagName, 1);
                        }
                    }

                //give the description breathing room
                if (description != null)
                    description = description.replace("\n", "\n\n");

                if (date != null && title != null && description != null)
                    events.add(new Event(date, title, description, imageUrl, eventTags));
            }
        } catch (JSONException e) {
            Log.e("parseJSON", "Cannot parse JSON", e);
            events = new ArrayList<>();
        }

        //add any recurring events here
        if (!events.isEmpty()) addRecurringEvents(events);

        return events;
    }

    private static void addRecurringEvents(ArrayList<Event> events) {
        //until Dec 11, add VLJ every Tuesday: https://www.facebook.com/events/526194581137224
        //generate next VLJ date, and pass parameters 3 for DAY_OF_THE_WEEK Tuesday, and limit date
        String nextVLJ = getNextWeeklyEvent(3, 1544565601000L);

        if (nextVLJ != null) {
            //generate tags ArrayList
            ArrayList<String> eventTags = new ArrayList<>();
            String vljTag = "Drinks";
            eventTags.add(vljTag);

            //also update tags HashMap
            if (Event.tags.containsKey(vljTag)) {
                int value = Event.tags.get(vljTag);
                Event.tags.put(vljTag, ++value);
            } else Event.tags.put(vljTag, 1);

            //generate the event
            Event vlj = new Event(nextVLJ, "Seară VLJ",
                    "Program pentru inițiați cu Vinul La Juma’ de preț.\n" +
                            "Licoarea bahică dezleagă limbile și unește sufletele. \n" +
                            "\n" +
                            "În fiecare marți, să curgă vinul!\n" +
                            "\n" +
                            "PS: #manasiafood are în meniu, special pentru eventul VLJ, \n" +
                            "<PIZZA>",
                    "https://scontent.fotp3-1.fna.fbcdn.net/v/t1.0-9/" +
                            "42967050_2259332690966868_4328291320184438784_n.jpg?" +
                            "_nc_cat=104&oh=504a1edc450cdcf0712192568844c3d0&oe=5C4F3C1C", eventTags);
            //calculate where we'll be inserting the event in the ArrayList
            int eventPosition = getEventPosition(vlj, events);

            events.add(eventPosition, vlj);
        }
    }

    private static String getNextWeeklyEvent(int weekday, long limitDate) {
        Calendar calendar = Calendar.getInstance();

        //check that event end date is not exceeded
        if (calendar.getTimeInMillis() > limitDate) return null;

        //get the current day of the week
        int dayOfTheWeek = calendar.get(Calendar.DAY_OF_WEEK);

        //if it's already that weekday, do nothing, otherwise move the date to next weekday
        if (dayOfTheWeek < weekday) {
            calendar.add(Calendar.DATE, weekday - dayOfTheWeek);
        } else if (dayOfTheWeek > weekday) {
            calendar.add(Calendar.DATE, 7 - dayOfTheWeek + weekday - 1);
        }

        return calendarToString(calendar);
    }

    private static String getNextDailyEvent(long limitDate) {
        Calendar calendar = Calendar.getInstance();

        //check that event end date is not exceeded
        if (calendar.getTimeInMillis() > limitDate) return null;

        return calendarToString(calendar);
    }

    private static String calendarToString(Calendar calendar) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(calendar.get(Calendar.YEAR));
        stringBuilder.append("-");

        int month = calendar.get(Calendar.MONTH) + 1;
        if (month < 10) stringBuilder.append(0);
        stringBuilder.append(month);
        stringBuilder.append("-");

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (day < 10) stringBuilder.append(0);
        stringBuilder.append(day);

        return stringBuilder.toString();
    }

    private static int getEventPosition(Event vlj, ArrayList<Event> events) {
        int position = 0;

        Date vljDate = convertDate(vlj.getDate(), false);
        if (vljDate == null) return 0;
        long vljTime = vljDate.getTime();

        for (Event event : events) {
            Date comparedDate = convertDate(event.getDate(), false);
            if (comparedDate == null) return 0;

            if (vljTime > comparedDate.getTime()) return position;
            else position++;
        }

        return position;
    }
}