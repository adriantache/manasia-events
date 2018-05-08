package com.adriantache.manasia_events.custom_class;

import android.os.Parcel;
import android.os.Parcelable;

public class Event implements Parcelable {
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
    public String getPhotoUrl() {
        return photoUrl;
    }
    public boolean getNotify() {
        return notify;
    }

    //setter for notify flag
    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    //implementation of Parcelable to transfer it to the EventDetails class
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    private Event(Parcel in) {
        this.date = in.readString();
        this.title = in.readString();
        this.description = in.readString();
        this.photoUrl = in.readString();
        this.category_image = in.readInt();
        this.notify = in.readInt() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(photoUrl);
        dest.writeInt(category_image);
        dest.writeInt(notify ? 1 : 0);
    }
}