package com.soluto.carouselnotification;

import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Shailesh on 06/01/17.
 */

public class CarouselItem implements Parcelable {
    String id; //id can store a key id or entire object as a gson string. At developer discretion
    String title;
    String description;
    String photo_url;
    String image_file_location;
    String image_file_name;
    String type;
    String targetUrl;
    int textColor;


    public CarouselItem( String photo_url) {
        this(null, null, null, photo_url, null, Color.BLACK);
    }

    public CarouselItem() {
        this(null, null, null, null, null, Color.BLACK);
    }

    /**
     * Constructor with id. Recommended as it is easy to tell which item was clicked
     * @param id String id of the item. Later it can be checked if it is the item that was clicked.
     * @param title
     * @param description
     * @param photo_url
     */
    public CarouselItem(String id, String title, String description, String photo_url, String targetUrl, int textColor) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.photo_url = photo_url;
        this.targetUrl = targetUrl;
        this.textColor = textColor;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }

    public String getImage_file_location() {
        return image_file_location;
    }

    public void setImage_file_location(String image_file_location) {
        this.image_file_location = image_file_location;
    }

    public String getImage_file_name() {
        return image_file_name;
    }

    public void setImage_file_name(String image_file_name) {
        this.image_file_name = image_file_name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public void setTargetUrl(String targetUrl) {
        this.targetUrl = targetUrl;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    protected CarouselItem(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        photo_url = in.readString();
        image_file_location = in.readString();
        image_file_name = in.readString();
        type = in.readString();
        targetUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(photo_url);
        dest.writeString(image_file_location);
        dest.writeString(image_file_name);
        dest.writeString(type);
        dest.writeString(targetUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CarouselItem> CREATOR = new Parcelable.Creator<CarouselItem>() {
        @Override
        public CarouselItem createFromParcel(Parcel in) {
            return new CarouselItem(in);
        }

        @Override
        public CarouselItem[] newArray(int size) {
            return new CarouselItem[size];
        }
    };
}