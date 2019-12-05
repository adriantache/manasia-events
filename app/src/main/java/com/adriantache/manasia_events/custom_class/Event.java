package com.adriantache.manasia_events.custom_class;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Event {
    private ArrayList<String> eventTags;
    private String date; //date of format yyyy-MM-dd
    private String title;
    private String description;
    private String photoUrl;
    private long databaseID;
    private int notify = 0;

    /**
     * Constructor for events fetched from remote data source
     *
     * @param date        Event date, of format yyyy-MM-dd
     * @param title       Event title
     * @param description Event description
     * @param photoUrl    Event photo URL, can be null
     */
    public Event(long databaseID, String date, String title, String description, @Nullable String photoUrl,
                 ArrayList<String> eventTags) {
        this.databaseID = databaseID;
        this.date = date;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.eventTags = eventTags;
    }

    /**
     * Constructor for events fetched from the local database
     *
     * @param databaseID  Local database _ID
     * @param date        Event date, of format yyyy-MM-dd
     * @param title       Event title
     * @param description Event description
     * @param photoUrl    Event photo URL, can be null
     * @param notify      Flag to trigger notification for this event; can be 0 or 1
     */
    public Event(long databaseID, String date, String title, String description, String photoUrl,
                 ArrayList<String> eventTags, int notify) {
        this.databaseID = databaseID;
        this.date = date;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.eventTags = eventTags;
        this.notify = notify;
    }

    //getters
    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public int getNotify() {
        return notify;
    }

    //setters
    public void setNotify(int notify) {
        this.notify = notify;
    }

    public long getDatabaseID() {
        return databaseID;
    }

    public ArrayList<String> getEventTags() {
        return eventTags;
    }
}