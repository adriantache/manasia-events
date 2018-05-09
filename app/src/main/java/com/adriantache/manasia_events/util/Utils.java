package com.adriantache.manasia_events.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

        if (formattedDate != null) {
            return formattedDate.compareTo(today);
        }
        //failure will default to show actions and let the user decide
        else return DATE_ERROR;


    }
}