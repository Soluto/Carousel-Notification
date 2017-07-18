package com.soluto.carouselnotification;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.NotificationCompat;

import java.util.ArrayList;

/**
 * It contains basic information abour carousel once it is built.
 * Purpose is to reset implements Parcelable {@link Carousel} instance in case it is killed during the process
 * Created by Shailesh on 08/01/17.
 */

public class CarouselSetUp implements Parcelable {

    public ArrayList<CarouselItem> carouselItems;

    public String contentTitle;
    public String contentText; //title and text while it is small
    public String bigContentTitle;
    public String bigContentText; //title and text when it becomes large
    public int carouselNotificationId = 9873715; //Random id for notification. Will cancel any
    // notification that have existing same id.

    public  int currentStartIndex = 0; //Variable that keeps track of where the startIndex is
    public int notificationPriority = NotificationCompat.PRIORITY_DEFAULT;
    public String smallIcon;
    public int smallIconResourceId = -1; //check before setting it that it does exists
    public String largeIcon;
    public String caraousalPlaceholder;
    public CarouselItem currentItem;
    public boolean isOtherRegionClickable = false;
    public boolean isImagesInCarousel = true;

    public CarouselSetUp() {
        //default set up
    }

    public CarouselSetUp (ArrayList<CarouselItem> carouselItems, String contentTitle, String contentText,
                          String bigContentTitle, String bigContentText, int carouselNotificationId,
                          int currentStartIndex,String smallIcon, int smallIconResourceId,
                          String largeIcon, String caraousalPlaceholder, CarouselItem currentItem, boolean isOtherRegionClickable, boolean isImagesInCarousel) {
        this.carouselItems = carouselItems;
        this.contentTitle = contentTitle;
        this.contentText = contentText;
        this.bigContentTitle = bigContentTitle;
        this.bigContentText = bigContentText;
        this.carouselNotificationId = carouselNotificationId;
        this.currentStartIndex = currentStartIndex;
        this.smallIcon = smallIcon;
        this.smallIconResourceId = smallIconResourceId;
        this.largeIcon = largeIcon;
        this.caraousalPlaceholder = caraousalPlaceholder;
        this.currentItem = currentItem;
        this.isOtherRegionClickable = isOtherRegionClickable;
        this.isImagesInCarousel = isImagesInCarousel;
    }



    protected CarouselSetUp(Parcel in) {
        if (in.readByte() == 0x01) {
            carouselItems = new ArrayList<CarouselItem>();
            in.readList(carouselItems, CarouselItem.class.getClassLoader());
        } else {
            carouselItems = null;
        }
        contentTitle = in.readString();
        contentText = in.readString();
        bigContentTitle = in.readString();
        bigContentText = in.readString();
        carouselNotificationId = in.readInt();
        currentStartIndex = in.readInt();
        notificationPriority = in.readInt();
        smallIcon = in.readString();
        smallIconResourceId = in.readInt();
        largeIcon = in.readString();
        caraousalPlaceholder = in.readString();
        currentItem = (CarouselItem) in.readValue(CarouselItem.class.getClassLoader());
        isOtherRegionClickable = in.readByte() != 0x00;
        isImagesInCarousel = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (carouselItems == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(carouselItems);
        }
        dest.writeString(contentTitle);
        dest.writeString(contentText);
        dest.writeString(bigContentTitle);
        dest.writeString(bigContentText);
        dest.writeInt(carouselNotificationId);
        dest.writeInt(currentStartIndex);
        dest.writeInt(notificationPriority);
        dest.writeString(smallIcon);
        dest.writeInt(smallIconResourceId);
        dest.writeString(largeIcon);
        dest.writeString(caraousalPlaceholder);
        dest.writeValue(currentItem);
        dest.writeByte((byte) (isOtherRegionClickable ? 0x01 : 0x00));
        dest.writeByte((byte) (isImagesInCarousel ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CarouselSetUp> CREATOR = new Parcelable.Creator<CarouselSetUp>() {
        @Override
        public CarouselSetUp createFromParcel(Parcel in) {
            return new CarouselSetUp(in);
        }

        @Override
        public CarouselSetUp[] newArray(int size) {
            return new CarouselSetUp[size];
        }
    };
}