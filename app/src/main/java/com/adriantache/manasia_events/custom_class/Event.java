package com.adriantache.manasia_events.custom_class;

public class Event {

    private String date;
    private String title;
    private String description;
    private String photoUrl;
    private int category_image;
    private boolean notify = false;

    public Event(String date, String title, String description, String photoUrl, int category_image) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.category_image = category_image;
    }

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

    public String getPhotoUrl() {
        return photoUrl;
    }

    public boolean getNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }
}
