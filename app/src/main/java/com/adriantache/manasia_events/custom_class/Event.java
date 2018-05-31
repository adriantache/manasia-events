package com.adriantache.manasia_events.custom_class;

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

public class Event {
    private String date; //date of format yyyy-MM-dd
    private String title;
    private String description;
    private Bitmap photo;
    private int category_image;
    private int notify = 0;
    private int databaseID = -1;

    /**
     * Constructor for events fetched from remote data source
     *
     * @param date           Event date, of format yyyy-MM-dd
     * @param title          Event title
     * @param description    Event description
     * @param photo       Event photo URL, can be null
     * @param category_image Event category, represented by a category image resource ID
     */
    public Event(String date, String title, String description, @Nullable Bitmap photo, int category_image) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.photo = photo;
        this.category_image = category_image;
    }

    /**
     * Constructor for events fetched from the local database
     *
     * @param databaseID     Local database _ID
     * @param date           Event date, of format yyyy-MM-dd
     * @param title          Event title
     * @param description    Event description
     * @param photoUrl       Event photo URL, can be null
     * @param category_image Event category, represented by a category image resource ID
     * @param notify         Flag to trigger notification for this event; can be 0 or 1
     */
    public Event(int databaseID, String date, String title, String description, Bitmap photo, int category_image, int notify) {
        this.databaseID = databaseID;
        this.date = date;
        this.title = title;
        this.description = description;
        this.photo = photo;
        this.category_image = category_image;
        this.notify = notify;
    }

    //getters
    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public int getCategory_image() {
        return category_image;
    }

    public String getDescription() {
        return description;
    }

    public Bitmap getPhoto() {
        return photo;
    }

    public int getNotify() {
        return notify;
    }

    //setters
    public void setNotify(int notify) {
        this.notify = notify;
    }

    public int getDatabaseID() {
        return databaseID;
    }
}