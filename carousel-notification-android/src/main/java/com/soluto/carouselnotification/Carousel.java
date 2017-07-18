package com.soluto.carouselnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.soluto.carouselnotification.R;

/**
 * Created by Shailesh on 06/01/17.
 */

public class Carousel {
    private static Carousel carousel;
    private Context context;
    private ArrayList<CarouselItem> carouselItems;
    private String contentTitle, contentText; //title and text while it is small
    private String bigContentTitle, bigContentText; //title and text when it becomes large
    private String currentItemTitle, currentItemDescription;
    private AnalyticsWriter analyticsWriter;
    private LoggingApi logger;
    private String notificationId;
    private NotificationCompat.Builder mBuilder;
    private int carouselNotificationId = 9873715; //Random id for notification. Will cancel any
    // notification that have existing same id.

    private RemoteViews bigView;
    private static int currentStartIndex = 0; //Variable that keeps track of where the startIndex is
    private static int notificationPriority = NotificationCompat.PRIORITY_DEFAULT;
    Notification foregroundNote;

    static Bitmap appIcon;
    static Bitmap smallIcon;
    static int smallIconResourceId = -1; //check before setting it that it does exists
    static Bitmap largeIcon;
    static Bitmap caraousalPlaceholder;

    private CarouselItem currentItem;
    private Bitmap currentItemBitmap;

    private CarouselSetUp carouselSetUp;
    private String smallIconPath, largeIconPath, placeHolderImagePath; //Stores path of these images if set by user

    private boolean isOtherRegionClickable = true;
    private boolean isImagesInCarousel = true;
    Date timeOnPage = new Date();


    public static final String CAROUSEL_ITEM_CLICKED_KEY = "CarouselNotificationItemClickedKey";
    public static final String CAROUSEL_NOTIFICATION_ID_KEY = "CarouselNotificationIdKey";

    private Carousel(Context context, String notificationId, AnalyticsWriter analyticsWriter, LoggingApi logger) {
        this.context = context;
        this.analyticsWriter = analyticsWriter;
        this.logger = logger;
        this.notificationId = notificationId;
        mBuilder = new NotificationCompat.Builder(context);
    }

    /**
     * Doubly locked singleton pattern
     *
     * @param context : Required for Notifications
     * @return carousel instance of this class
     */
    public static Carousel with(Context context, String notificationId, AnalyticsWriter analyticsWriter, LoggingApi logger) {
        if (carousel == null) {
            synchronized (Carousel.class) {
                if (carousel == null) {
                    carousel = new Carousel(context, notificationId, analyticsWriter, logger);
                    try {
                        appIcon = CarouselUtilities.carouselDrawableToBitmap(context.getPackageManager().getApplicationIcon(context.getPackageName()));
                    } catch (PackageManager.NameNotFoundException e) {
                        appIcon = null;
                        logger.error("Unable to retrieve app Icon", e);
                    }

                }
            }
        }
        return carousel;
    }

    public static Carousel current(){
        return carousel;
    }

    //=========================================CAROUSEL SETTERS ===================================//
    /**
     * function to begin carousel trnsaction
     * It only cleans up existing carousel if exists
     */
    public Carousel beginTransaction() {
        timeOnPage = new Date();
        clearCarouselIfExists();
        return this;
    }

    /**
     * function to add a carousel item to the array-list
     *
     * @param carouselItem : item to be added
     */
    public Carousel addCarouselItem(CarouselItem carouselItem) {
        if (carouselItem != null) {
            if (carouselItems == null) {
                carouselItems = new ArrayList<CarouselItem>();
            }
            carouselItems.add(carouselItem);
        } else {
            logger.error("Null carousel can't be added!");
        }
        return this;
    }

    /**
     * sets title of notification
     *
     * @param title : Title need to be non null
     */
    public Carousel setContentTitle(String title) {
        if (title != null) {
            this.contentTitle = title;
        } else {
            logger.error("Null parameter");
        }
        return this;
    }

    /**
     * sets content text of notification
     *
     * @param contentText : contentText need to be non null
     */
    public Carousel setContentText(String contentText) {
        if (contentText != null) {
            this.contentText = contentText;
        } else {
            logger.error("Null parameter");
        }
        return this;
    }

    /**
     * sets big content text of notification
     *
     * @param bigContentText : bigContentText need to be non null
     */
    public Carousel setBigContentText(String bigContentText) {
        if (bigContentText != null) {
            this.bigContentText = bigContentText;
        } else {
            logger.error("Null parameter");
        }
        return this;
    }

    /**
     * sets big content title of notification
     *
     * @param bigContentTitle : bigContentTitle need to be non null
     */
    public Carousel setBigContentTitle(String bigContentTitle) {
        if (bigContentTitle != null) {
            this.bigContentTitle = bigContentTitle;
        } else {
            logger.error("Null parameter");
        }
        return this;
    }

    /**
     * sets priority of the carousel notificaition. By default it is NotificationCompat.PRIORITY_DEFAULT
     *
     * @param priority : needs to be in permissible range (-2 to 2)
     * @return
     */
    public Carousel setNotificationPriority(int priority) {
        if (priority >= NotificationCompat.PRIORITY_MIN && priority <= NotificationCompat.PRIORITY_MAX) {
            notificationPriority = priority;
        } else {
            logger.info("Invalid priority");
        }
        return this;
    }

    /**
     * sets small Icon based on the resource id provided
     *
     * @param resourceId : like R.drawable.smallIcon
     * @return
     */
    public Carousel setSmallIconResource(int resourceId) {
        //First we need to check it if it is a valid resource.
        try {
            smallIcon = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            smallIcon = null;
            logger.error("Unable to decode resource", e);
        }

        if (smallIcon != null) {  //meaning a valid resource
            smallIconResourceId = resourceId;
        }
        return this;
    }


    /**
     * sets largeIcon based on the resource id provided
     *
     * @param resourceId : like R.drawable.smallIcon
     * @return
     */
    public Carousel setLargeIcon(int resourceId) {
        try {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            logger.error("Unable to decode resource", e);
        }
        return this;
    }

    public Carousel setLargeIcon(Bitmap large) {
        if (large != null) {
            largeIcon = large;
        } else {
            largeIcon = null;
            logger.info("Null parameter");
        }
        return this;
    }

    /**
     * sets largeIcon based on the resource id provided
     *
     * @param resourceId : like R.drawable.smallIcon
     * @return
     */
    public Carousel setCarouselPlaceHolder(int resourceId) {
        try {
            caraousalPlaceholder = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            caraousalPlaceholder = null;
            logger.error("Unable to decode resource", e);
        }
        return this;
    }

    public Carousel setCarouselPlaceHolder(Bitmap placeholder) {
        if (placeholder != null) {
            placeholder = placeholder;
        } else {
            placeholder = null;
            logger.info("Null parameter");
        }
        return this;
    }

    public Carousel setOtherRegionClickable(boolean isOtherRegionClickable) {
        this.isOtherRegionClickable = isOtherRegionClickable;
        return this;
    }


    //=======================================SETTING UP CAROUSEL ===================================//

    /**
     * Function to be called by user.
     * 1) A carousel items will be set up
     * 2) An image download thread will kick in.
     */
    public void buildCarousel() {
       // initiateCarouselTransaction();
        if (false)
        return;
        boolean isImagesInCarous = false;
        int numberofImages = 0;
        if (carouselItems != null && carouselItems.size() > 0) {
            for (CarouselItem item : carouselItems) {
                if (!TextUtils.isEmpty(item.getPhoto_url())) {
                    isImagesInCarous = true;
                    numberofImages++;
                }
            }
            if (isImagesInCarous) {
                BasicImageDownloader basicDownloader = new BasicImageDownloader(context, carouselItems
                        , numberofImages, new BasicImageDownloader.OnDownloadsCompletedListener() {
                    @Override
                    public void onComplete() {
                        initiateCarouselTransaction();
                    }
                }, logger);
                basicDownloader.startAllDownloads();
            } else {
                this.isImagesInCarousel = false;
                initiateCarouselTransaction();
            }
        }
    }

    /**
     * Here actual transaction starts
     * Set up will be saved here
     */
    private void initiateCarouselTransaction() {
        currentStartIndex = 0;
        if (carouselItems != null && carouselItems.size() > 0) {
            prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex));
        }

    }


    /**
     * All Item variables are set here. After this showCarousel is hit.
     *
     * @param currentItem
     */
    private void prepareVariablesForCarouselAndShow(CarouselItem currentItem) {
        if (this.currentItem == null) {
            this.currentItem = new CarouselItem();
        }
        if (currentItem != null) {
            writeEvent("StepView", getBaseAnalyticsExtraData());
            this.currentItem = currentItem;
            currentItemTitle = currentItem.getTitle();
            currentItemDescription = currentItem.getDescription();
            currentItemBitmap = getCarouselBitmap(currentItem);

        }
        showCarousel();
    }

    /**
     * final function which displays carousel. Make sure carouselItems and pending intents are
     * set before calling this method. Otherwise noting will happen.
     */
    private void showCarousel() {

        if (carouselItems != null && carouselItems.size() > 0) {

            if (carouselSetUp == null || carouselSetUp.carouselNotificationId != carouselNotificationId) {
                //First save this set up into a carousel setup item
                carouselSetUp = saveCarouselSetUp();
            } else {
                carouselSetUp.currentStartIndex = currentStartIndex;
                carouselSetUp.currentItem = currentItem;
            }
            //First set up all the icons
            setUpCarouselIcons();
            setUpCarouselTitles();

            bigView = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.carousel_notification_item);

            //set up what needs to be visible and what not in the carousel
            setUpCarouselLayout(bigView);

            setUpCarouselItems(bigView);
            setPendingIntents(bigView);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(contentTitle).setContentText(contentText)
                    .setSmallIcon(smallIconResourceId).setLargeIcon(largeIcon)
                    .setPriority(notificationPriority);

            Intent carouselIntent = new Intent(CarouselConstants.CAROUSEL_EVENT_FIRED_INTENT_FILTER);
            Bundle bundle = new Bundle();
            bundle.putInt(CarouselConstants.EVENT_CAROUSEL_ITEM_CLICKED_KEY, CarouselConstants.EVENT_OTHER_REGION_CLICKED);
            bundle.putParcelable(CarouselConstants.CAROUSEL_SET_UP_KEY, carouselSetUp);
            carouselIntent.putExtras(bundle);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, CarouselConstants.EVENT_OTHER_REGION_CLICKED, carouselIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pIntent);

            mBuilder.setCustomBigContentView(bigView);
            foregroundNote = mBuilder.build();

            // now show notification..
            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyManager.notify(carouselNotificationId, foregroundNote);
        } else {
            logger.error("Empty item array or of length less than 2");
        }

    }

    /**
     * function to retrieve bitmap for the carousel if exists, otherwise send placeholders
     *
     * @param item
     * @return
     */
    private Bitmap getCarouselBitmap(CarouselItem item) {
        Bitmap bitmap = null;
        if (item != null) {
            if (!TextUtils.isEmpty(item.getImage_file_name()) && !TextUtils.isEmpty(item.getImage_file_location())) {
                bitmap = CarouselUtilities.carouselLoadImageFromStorage(item.getImage_file_location(), item.getImage_file_name());
                //Notice it will execute only if an image actually exists
                if (bitmap != null)
                    return bitmap;
            }
            //If no bitmap saved, try to send a custom one
            if (caraousalPlaceholder != null)
                return caraousalPlaceholder;
            else if (appIcon != null)
                return appIcon;
        }

        return bitmap;
    }


    /**
     * Handles visibilities of different items based upon availability of content
     *
     * @param bigView
     */
    private void setUpCarouselLayout(RemoteViews bigView) {
        bigView.setTextColor(R.id.tvCurrentDescriptionText, currentItem.getTextColor());
        bigView.setTextColor(R.id.tvCurrentTitleText, currentItem.getTextColor());

        if(currentStartIndex == 0){
            bigView.setViewVisibility(R.id.ivArrowLeft, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.ivArrowLeft, View.VISIBLE);
        }

        if(currentStartIndex  == carouselItems.size() - 1){
            bigView.setViewVisibility(R.id.ivArrowRight, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.ivArrowRight, View.VISIBLE);
        }

        if (TextUtils.isEmpty(bigContentTitle)) {
            bigView.setViewVisibility(R.id.tvCarouselTitle, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvCarouselTitle, View.VISIBLE);
        }
        if (TextUtils.isEmpty(currentItemTitle)) {
            bigView.setViewVisibility(R.id.tvCurrentTitleText, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvCurrentTitleText, View.VISIBLE);
        }
        if (TextUtils.isEmpty(currentItemDescription)) {
            bigView.setViewVisibility(R.id.tvCurrentDescriptionText, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvCurrentDescriptionText, View.VISIBLE);
        }
        if (!isImagesInCarousel) {
            bigView.setViewVisibility(R.id.ivCurrentImage, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.ivCurrentImage, View.VISIBLE);
        }

    }

    /**
     * Sets all titles/texts if they are null
     * They are set to app Icon if that is available. Otherwise at last are left as they are
     */
    private void setUpCarouselTitles() {
        if (TextUtils.isEmpty(contentTitle)) {
            setContentTitle(CarouselUtilities.carouselGetApplicationName(context));
        }

        if (bigContentTitle == null)
            bigContentTitle = "";
        if (bigContentText == null)
            bigContentText = "";
    }

    /**
     * Sets all bitmaps if they are null
     * They are set to app Icon if that is available. Otherwise at last are left as they are
     */
    private void setUpCarouselIcons() {
        if (appIcon != null) {
            if (largeIcon == null) {
                largeIcon = appIcon;
            }
            if (caraousalPlaceholder == null) {
                caraousalPlaceholder = appIcon;
            }
        } else {
            appIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_carousel_icon);
            if (largeIcon == null) {
                largeIcon = appIcon;
            }
            if (caraousalPlaceholder == null) {
                caraousalPlaceholder = appIcon;
            }
        }
        if (smallIconResourceId < 0) {
            smallIconResourceId = CarouselUtilities.carouselGetAppIconResourceId(context);
        }
        if (smallIconResourceId < 0) {
            smallIconResourceId = R.drawable.ic_carousel_icon;
        }
    }

    /**
     * sets us carousel items into the view.
     */
    private void setUpCarouselItems(RemoteViews bigView) {
        if (currentItemBitmap != null) {
            bigView.setImageViewBitmap(R.id.ivCurrentImage, currentItemBitmap);
        }
        bigView.setImageViewBitmap(R.id.ivCarouselAppIcon, largeIcon);
        bigView.setTextViewText(R.id.tvCarouselTitle, bigContentTitle);
       // bigView.setTextViewText(R.id.tvCarouselContent, bigContentText);
        bigView.setTextViewText(R.id.tvCurrentTitleText, currentItemTitle);
        bigView.setTextViewText(R.id.tvCurrentDescriptionText, currentItemDescription);
    }

    /**
     * creates pending intents for the clickable regions of the notification
     */
    private void setPendingIntents(RemoteViews bigView) {
        //right arrow
        PendingIntent rightArrowPendingIntent = getPendingIntent(CarouselConstants.EVENT_RIGHT_ARROW_CLICKED);
        bigView.setOnClickPendingIntent(R.id.ivArrowRight, rightArrowPendingIntent);
        //left arrow
        PendingIntent leftArrowPendingIntent = getPendingIntent(CarouselConstants.EVENT_LEFT_ARROW_CLICKED);
        bigView.setOnClickPendingIntent(R.id.ivArrowLeft, leftArrowPendingIntent);
        //current item
        PendingIntent currentItemPendingIntent = getPendingIntent(CarouselConstants.EVENT_CURRENT_ITEM_CLICKED);
        bigView.setOnClickPendingIntent(R.id.llItemLayout, currentItemPendingIntent);
    }

    /**
     * creates pending intents with added bundle information about the region clicked
     *
     * @param eventClicked : integer id of the region clicked
     * @return pendingIntent for the same
     */
    private PendingIntent getPendingIntent(int eventClicked) {
        Intent carouselIntent = new Intent(CarouselConstants.CAROUSEL_EVENT_FIRED_INTENT_FILTER);
        Bundle bundle = new Bundle();
        bundle.putInt(CarouselConstants.EVENT_CAROUSEL_ITEM_CLICKED_KEY, eventClicked);
        bundle.putParcelable(CarouselConstants.CAROUSEL_SET_UP_KEY, carouselSetUp);
        carouselIntent.putExtras(bundle);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, eventClicked, carouselIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pIntent;
    }

    /**
     * saves current set up into a {@link CarouselSetUp} object and returns it
     * this object is passed between broadcast receiver and this instance
     *
     * @return
     */
    private CarouselSetUp saveCarouselSetUp() {
        setUpfilePathOfImages();
        CarouselSetUp cr = new CarouselSetUp(carouselItems, contentTitle, contentText,
                bigContentTitle, bigContentText, carouselNotificationId,
                currentStartIndex, smallIconPath, smallIconResourceId, largeIconPath,
                placeHolderImagePath, currentItem, isOtherRegionClickable, isImagesInCarousel);
        return cr;
    }

    /**
     * If exists, it saves files into external directory and saves corresponding file path.
     */
    private void setUpfilePathOfImages() {
        if (smallIcon != null) {
            smallIconPath = CarouselUtilities.carouselSaveBitmapToInternalStorage(context, smallIcon,
                    CarouselConstants.CAROUSEL_SMALL_ICON_FILE_NAME);
        }
        if (largeIcon != null) {
            largeIconPath = CarouselUtilities.carouselSaveBitmapToInternalStorage(context, largeIcon,
                    CarouselConstants.CAROUSEL_LARGE_ICON_FILE_NAME);
        }
        if (caraousalPlaceholder != null) {
            placeHolderImagePath = CarouselUtilities.carouselSaveBitmapToInternalStorage(context, caraousalPlaceholder,
                    CarouselConstants.CAROUSEL_PLACEHOLDER_ICON_FILE_NAME);
        }
    }


    /**
     * Clears notification and empty's references if exists. Important to clear previous carousel
     * before starting a new one.
     *
     * @return
     */
    public Carousel clearCarouselIfExists() {
        if (carouselItems != null) {
            carouselItems.clear();

            smallIconResourceId = -1;
            isOtherRegionClickable = true;
            isImagesInCarousel = true;
            smallIcon = null;
            smallIconPath = null;
            largeIcon = null;
            placeHolderImagePath = null;
            caraousalPlaceholder = null;
            contentText = null;
            contentTitle = null;
            bigContentText = null;
            bigContentTitle = null;

            // now cancel notification..
            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyManager.cancel(carouselNotificationId);
        }
        //ToDo :  delete all cache files
        return this;
    }


    //========================================= HANDLING EVENTS ===================================//

    /**
     * Handles all click events from the carousel notification.
     *
     * @param clickEvent
     */
    public void handleClickEvent(int clickEvent, CarouselSetUp setUp) {
        //First we need to make sure that the set up is there. If the original instance is
        // killed and it is a new one, we need to instantiate all the values from setUp object
        verifyAndSetUpVariables(setUp);

        switch (clickEvent) {
            case CarouselConstants.EVENT_LEFT_ARROW_CLICKED:
                onLeftArrowClicked();
                break;
            case CarouselConstants.EVENT_RIGHT_ARROW_CLICKED:
                onRightArrowClicked();
                break;
            case CarouselConstants.EVENT_CURRENT_ITEM_CLICKED:
                onCurrentItemClicked();
                break;
            case CarouselConstants.EVENT_OTHER_REGION_CLICKED:
                onOtherRegionClicked ();
                break;
            default:
                break;
        }
    }


    /**
     * It first checks if it is a new instance and not the old one. If its the case, it just resets all
     * the values using the setup object
     *
     * @param setUp : {@link CarouselSetUp} object and carries the original set data.
     */
    private void verifyAndSetUpVariables(CarouselSetUp setUp) {
        //If it a new instance carousel stUp will be null
        if (carouselSetUp == null) {
            carouselItems = setUp.carouselItems;
            contentTitle = setUp.contentTitle;
            contentText = setUp.contentText;
            bigContentTitle = setUp.bigContentTitle;
            bigContentText = setUp.bigContentText;
            carouselNotificationId = setUp.carouselNotificationId;
            currentStartIndex = setUp.currentStartIndex;
            notificationPriority = setUp.notificationPriority;
            smallIconPath = setUp.smallIcon;
            largeIconPath = setUp.largeIcon;
            placeHolderImagePath = setUp.caraousalPlaceholder;
            currentItem = setUp.currentItem;
            isOtherRegionClickable = setUp.isOtherRegionClickable;
            isImagesInCarousel = setUp.isImagesInCarousel;


            setUpBitCarouselBitmapsFromSetUp();

        } else if (carouselSetUp != null && carouselNotificationId != setUp.carouselNotificationId) {
            carouselSetUp = null;
            verifyAndSetUpVariables(setUp);
        }
    }

    /**
     * If exists it loads bitmaps from file directory and saves them.
     */
    private void setUpBitCarouselBitmapsFromSetUp() {
        if (smallIconPath != null) {
            smallIcon = CarouselUtilities.carouselLoadImageFromStorage(smallIconPath, CarouselConstants.CAROUSEL_SMALL_ICON_FILE_NAME);
        }
        if (largeIconPath != null) {
            largeIcon = CarouselUtilities.carouselLoadImageFromStorage(largeIconPath, CarouselConstants.CAROUSEL_LARGE_ICON_FILE_NAME);
        }
        if (placeHolderImagePath != null) {
            caraousalPlaceholder = CarouselUtilities.carouselLoadImageFromStorage(placeHolderImagePath, CarouselConstants.CAROUSEL_PLACEHOLDER_ICON_FILE_NAME);
        }
    }

    private void onCurrentItemClicked() {
        writeAction("ItemSelected");
        sendItemClickedBroadcast(currentItem);
    }

    /**
     * This is caused when any other region than carousel items is clicked.
     */
    private void onOtherRegionClicked() {
        if (isOtherRegionClickable) {
            sendItemClickedBroadcast(currentItem);
        }
    }

    private void sendItemClickedBroadcast(CarouselItem cItem) {
        Intent i = new Intent();
        i.setAction(CarouselConstants.CAROUSEL_ITEM_CLICKED_INTENT_FILTER);
        Bundle bundle = new Bundle();
        bundle.putParcelable(CAROUSEL_ITEM_CLICKED_KEY, cItem);
        bundle.putString(CAROUSEL_NOTIFICATION_ID_KEY, notificationId);
        i.putExtras(bundle);

        context.getApplicationContext().sendBroadcast(i);

        try {
            clearCarouselIfExists();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable To send notification's pendingIntent", e);
        }
    }

    /**
     * Here we choose the items for left and right an call prepareVariablesForCarouselAndShow() thereafter
     */
    private void onLeftArrowClicked() {
        if (carouselItems != null && currentStartIndex > 0) {
            writeAction("Previous");
            currentStartIndex -= 1;
            prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex));
        }
    }

    private void onRightArrowClicked() {
        if (carouselItems != null && carouselItems.size() > currentStartIndex) {
            writeAction("Next");
            currentStartIndex += 1;
            prepareVariablesForCarouselAndShow(carouselItems.get(currentStartIndex));
        }
    }

    private void writeAction(String actionId){
        Date newDate = new Date();
        long diff = (newDate.getTime() - timeOnPage.getTime()) / 1000;
        timeOnPage = newDate;
        HashMap extraData = getBaseAnalyticsExtraData();
        extraData.put("ActionId", actionId);
        extraData.put("TimeOnStepInSec", diff);
        writeEvent("Click", extraData);
    }

    private HashMap getBaseAnalyticsExtraData(){
        HashMap extraData = new HashMap();
        extraData.put("StepIndex", currentStartIndex);
        extraData.put("TotalSteps", carouselItems.size());
        return extraData;
    }

    private void writeEvent(String eventName, Map<String, Object> extraData){
        if(this.analyticsWriter != null){
            try {
                this.analyticsWriter.write(eventName, extraData);
            } catch (Exception ex) {
                logger.warn("Unexpected exception while parse analytics json", ex);
            }
        }
    }
}
