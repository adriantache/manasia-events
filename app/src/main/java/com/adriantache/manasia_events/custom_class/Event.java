package com.adriantache.manasia_events.custom_class;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class Event implements Parcelable {
    private String date;
    private String title;
    private String description;
    private String photoUrl;
    private int category_image;
    private int notify = 0;
    private int databaseID = -1;

    /**
     * Constructor for events fetched from remote data source
     * @param date Event date, of format dd.MM.yyyy
     * @param title Event title
     * @param description Event description
     * @param photoUrl Event photo URL, can be null
     * @param category_image Event category, represented by a category image resource ID
     */
    public Event(String date, String title, String description, @Nullable String photoUrl, int category_image) {
        this.date = date;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.category_image = category_image;
    }

    /**
     * Constructor for events fetched from the local database
     * @param databaseID Local database _ID
     * @param date Event date, of format dd.MM.yyyy
     * @param title Event title
     * @param description Event description
     * @param photoUrl Event photo URL, can be null
     * @param category_image Event category, represented by a category image resource ID
     * @param notify Flag to trigger notification for this event; can be 0 or 1
     */
    public Event(int databaseID, String date, String title, String description, String photoUrl, int category_image, int notify) {
        this.databaseID = databaseID;
        this.date = date;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
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
    public String getPhotoUrl() {
        return photoUrl;
    }
    public int getNotify() {
        return notify;
    }
    public int getDatabaseID() {
        return databaseID;
    }

    //setters
    public void setNotify(int notify) {
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
        this.notify = in.readInt();
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
        dest.writeInt(notify);
    }
}