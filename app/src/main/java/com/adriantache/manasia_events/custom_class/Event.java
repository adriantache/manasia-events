package com.adriantache.manasia_events.custom_class;

public class Event {

    private String date;
    private String title;
    private String description;
    private String photoUrl;
    private String category;

    public Event (String date, String title, String description, String photoUrl, String category){
        this.date = date;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
