package in.mamga.carousalnotification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shailesh on 06/01/17.
 */

public class Carousal {
    private static Carousal carousal;
    private Context context;
    private ArrayList<CarousalItem> carousalItems;
    private String contentTitle, contentText; //title and text while it is small
    private String bigContentTitle, bigContentText; //title and text when it becomes large
    private String currentItemTitle, currentItemDescription;
    private AnalyticsWriter analyticsWriter;
    private LoggingApi logger;
    private NotificationCompat.Builder mBuilder;
    private int carousalNotificationId = 9873715; //Random id for notification. Will cancel any
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

    private CarousalItem currentItem;
    private Bitmap currentItemBitmap;

    private CarousalSetUp carousalSetUp;
    private String smallIconPath, largeIconPath, placeHolderImagePath; //Stores path of these images if set by user

    private boolean isOtherRegionClickable = true;
    private boolean isImagesInCarousal = true;
    Date timeOnPage = new Date();


    public static final String CAROUSAL_ITEM_CLICKED_KEY = "CarousalNotificationItemClickedKey";

    private Carousal(Context context, AnalyticsWriter analyticsWriter, LoggingApi logger) {
        this.context = context;
        this.analyticsWriter = analyticsWriter;
        this.logger = logger;
        mBuilder = new NotificationCompat.Builder(context);
    }

    /**
     * Doubly locked singleton pattern
     *
     * @param context : Required for Notifications
     * @return carousal instance of this class
     */
    public static Carousal with(Context context, AnalyticsWriter analyticsWriter, LoggingApi logger) {
        if (carousal == null) {
            synchronized (Carousal.class) {
                if (carousal == null) {
                    carousal = new Carousal(context, analyticsWriter, logger);
                    try {
                        appIcon = CarousalUtilities.carousalDrawableToBitmap(context.getPackageManager().getApplicationIcon(context.getPackageName()));
                    } catch (PackageManager.NameNotFoundException e) {
                        appIcon = null;
                        logger.error("Unable to retrieve app Icon", e);
                    }

                }
            }
        }
        return carousal;
    }


    //=========================================CAROUSAL SETTERS ===================================//
    /**
     * function to begin carousal trnsaction
     * It only cleans up existing carousal if exists
     */
    public Carousal beginTransaction() {
        timeOnPage = new Date();
        clearCarousalIfExists();
        return this;
    }

    /**
     * function to add a carousal item to the array-list
     *
     * @param carousalItem : item to be added
     */
    public Carousal addCarousalItem(CarousalItem carousalItem) {
        if (carousalItem != null) {
            if (carousalItems == null) {
                carousalItems = new ArrayList<CarousalItem>();
            }
            carousalItems.add(carousalItem);
        } else {
            logger.error("Null carousal can't be added!");
        }
        return this;
    }

    /**
     * sets title of notification
     *
     * @param title : Title need to be non null
     */
    public Carousal setContentTitle(String title) {
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
    public Carousal setContentText(String contentText) {
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
    public Carousal setBigContentText(String bigContentText) {
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
    public Carousal setBigContentTitle(String bigContentTitle) {
        if (bigContentTitle != null) {
            this.bigContentTitle = bigContentTitle;
        } else {
            logger.error("Null parameter");
        }
        return this;
    }

    /**
     * sets priority of the carousal notificaition. By default it is NotificationCompat.PRIORITY_DEFAULT
     *
     * @param priority : needs to be in permissible range (-2 to 2)
     * @return
     */
    public Carousal setNotificationPriority(int priority) {
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
    public Carousal setSmallIconResource(int resourceId) {
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
    public Carousal setLargeIcon(int resourceId) {
        try {
            largeIcon = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            logger.error("Unable to decode resource", e);
        }
        return this;
    }

    public Carousal setLargeIcon(Bitmap large) {
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
    public Carousal setCarousalPlaceHolder(int resourceId) {
        try {
            caraousalPlaceholder = BitmapFactory.decodeResource(context.getResources(), resourceId);
        } catch (Exception e) {
            caraousalPlaceholder = null;
            logger.error("Unable to decode resource", e);
        }
        return this;
    }

    public Carousal setCarousalPlaceHolder(Bitmap placeholder) {
        if (placeholder != null) {
            placeholder = placeholder;
        } else {
            placeholder = null;
            logger.info("Null parameter");
        }
        return this;
    }

    public Carousal setOtherRegionClickable(boolean isOtherRegionClickable) {
        this.isOtherRegionClickable = isOtherRegionClickable;
        return this;
    }


    //=======================================SETTING UP CAROUSAL ===================================//

    /**
     * Function to be called by user.
     * 1) A carousal items will be set up
     * 2) An image download thread will kick in.
     */
    public void buildCarousal() {
       // initiateCarousalTransaction();
        if (false)
        return;
        boolean isImagesInCarous = false;
        int numberofImages = 0;
        if (carousalItems != null && carousalItems.size() > 0) {
            for (CarousalItem item : carousalItems) {
                if (!TextUtils.isEmpty(item.getPhoto_url())) {
                    isImagesInCarous = true;
                    numberofImages++;
                }
            }
            if (isImagesInCarous) {
                BasicImageDownloader basicDownloader = new BasicImageDownloader(context, carousalItems
                        , numberofImages, new BasicImageDownloader.OnDownloadsCompletedListener() {
                    @Override
                    public void onComplete() {
                        initiateCarousalTransaction();
                    }
                }, logger);
                basicDownloader.startAllDownloads();
            } else {
                this.isImagesInCarousal = false;
                initiateCarousalTransaction();
            }
        }
    }

    /**
     * Here actual transaction starts
     * Set up will be saved here
     */
    private void initiateCarousalTransaction() {
        currentStartIndex = 0;
        if (carousalItems != null && carousalItems.size() > 0) {
            prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex));
        }

    }


    /**
     * All Item variables are set here. After this showCarousal is hit.
     *
     * @param currentItem
     */
    private void prepareVariablesForCarousalAndShow(CarousalItem currentItem) {
        if (this.currentItem == null) {
            this.currentItem = new CarousalItem();
        }
        if (currentItem != null) {
            writeEvent("StepView", getBaseAnalyticsExtraData());
            this.currentItem = currentItem;
            currentItemTitle = currentItem.getTitle();
            currentItemDescription = currentItem.getDescription();
            currentItemBitmap = getCarousalBitmap(currentItem);

        }
        showCarousal();
    }

    /**
     * final function which displays carousal. Make sure carousalItems and pending intents are
     * set before calling this method. Otherwise noting will happen.
     */
    private void showCarousal() {

        if (carousalItems != null && carousalItems.size() > 0) {

            if (carousalSetUp == null || carousalSetUp.carousalNotificationId != carousalNotificationId) {
                //First save this set up into a carousal setup item
                carousalSetUp = saveCarousalSetUp();
            } else {
                carousalSetUp.currentStartIndex = currentStartIndex;
                carousalSetUp.currentItem = currentItem;
            }
            //First set up all the icons
            setUpCarousalIcons();
            setUpCarousalTitles();

            bigView = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.carousal_notification_item);

            //set up what needs to be visible and what not in the carousal
            setUpCarousalLayout(bigView);

            setUpCarousalItems(bigView);
            setPendingIntents(bigView);

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context);
            mBuilder.setContentTitle(contentTitle).setContentText(contentText)
                    .setSmallIcon(smallIconResourceId).setLargeIcon(largeIcon)
                    .setPriority(notificationPriority);

            if (isOtherRegionClickable) {
                Intent carousalIntent = new Intent(CarousalConstants.CAROUSAL_EVENT_FIRED_INTENT_FILTER);
                Bundle bundle = new Bundle();
                bundle.putInt(CarousalConstants.EVENT_CAROUSAL_ITEM_CLICKED_KEY, CarousalConstants.EVENT_OTHER_REGION_CLICKED);
                bundle.putParcelable(CarousalConstants.CAROUSAL_SET_UP_KEY, carousalSetUp);
                carousalIntent.putExtras(bundle);
                PendingIntent pIntent = PendingIntent.getBroadcast(context, CarousalConstants.EVENT_OTHER_REGION_CLICKED, carousalIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                mBuilder.setContentIntent(pIntent);
            }

            mBuilder.setCustomBigContentView(bigView);
            foregroundNote = mBuilder.build();

            // now show notification..
            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyManager.notify(carousalNotificationId, foregroundNote);
        } else {
            logger.error("Empty item array or of length less than 2");
        }

    }

    /**
     * function to retrieve bitmap for the carousal if exists, otherwise send placeholders
     *
     * @param item
     * @return
     */
    private Bitmap getCarousalBitmap(CarousalItem item) {
        Bitmap bitmap = null;
        if (item != null) {
            if (!TextUtils.isEmpty(item.getImage_file_name()) && !TextUtils.isEmpty(item.getImage_file_location())) {
                bitmap = CarousalUtilities.carousalLoadImageFromStorage(item.getImage_file_location(), item.getImage_file_name());
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
    private void setUpCarousalLayout(RemoteViews bigView) {
        bigView.setTextColor(R.id.tvCurrentDescriptionText, currentItem.getTextColor());
        bigView.setTextColor(R.id.tvCurrentTitleText, currentItem.getTextColor());

        if(currentStartIndex == 0){
            bigView.setViewVisibility(R.id.ivArrowLeft, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.ivArrowLeft, View.VISIBLE);
        }

        if(currentStartIndex  == carousalItems.size() - 1){
            bigView.setViewVisibility(R.id.ivArrowRight, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.ivArrowRight, View.VISIBLE);
        }

        if (TextUtils.isEmpty(bigContentTitle)) {
            bigView.setViewVisibility(R.id.tvCarousalTitle, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.tvCarousalTitle, View.VISIBLE);
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
        if (!isImagesInCarousal) {
            bigView.setViewVisibility(R.id.ivCurrentImage, View.GONE);
        } else {
            bigView.setViewVisibility(R.id.ivCurrentImage, View.VISIBLE);
        }

    }

    /**
     * Sets all titles/texts if they are null
     * They are set to app Icon if that is available. Otherwise at last are left as they are
     */
    private void setUpCarousalTitles() {
        if (TextUtils.isEmpty(contentTitle)) {
            setContentTitle(CarousalUtilities.carousalGetApplicationName(context));
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
    private void setUpCarousalIcons() {
        if (appIcon != null) {
            if (largeIcon == null) {
                largeIcon = appIcon;
            }
            if (caraousalPlaceholder == null) {
                caraousalPlaceholder = appIcon;
            }
        } else {
            appIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_carousal_icon);
            if (largeIcon == null) {
                largeIcon = appIcon;
            }
            if (caraousalPlaceholder == null) {
                caraousalPlaceholder = appIcon;
            }
        }
        if (smallIconResourceId < 0) {
            smallIconResourceId = CarousalUtilities.carousalGetAppIconResourceId(context);
        }
        if (smallIconResourceId < 0) {
            smallIconResourceId = R.drawable.ic_carousal_icon;
        }
    }

    /**
     * sets us carousal items into the view.
     */
    private void setUpCarousalItems(RemoteViews bigView) {
        if (currentItemBitmap != null) {
            bigView.setImageViewBitmap(R.id.ivCurrentImage, currentItemBitmap);
        }
        bigView.setImageViewBitmap(R.id.ivCarousalAppIcon, largeIcon);
        bigView.setTextViewText(R.id.tvCarousalTitle, bigContentTitle);
       // bigView.setTextViewText(R.id.tvCarousalContent, bigContentText);
        bigView.setTextViewText(R.id.tvCurrentTitleText, currentItemTitle);
        bigView.setTextViewText(R.id.tvCurrentDescriptionText, currentItemDescription);
    }

    /**
     * creates pending intents for the clickable regions of the notification
     */
    private void setPendingIntents(RemoteViews bigView) {
        //right arrow
        PendingIntent rightArrowPendingIntent = getPendingIntent(CarousalConstants.EVENT_RIGHT_ARROW_CLICKED);
        bigView.setOnClickPendingIntent(R.id.ivArrowRight, rightArrowPendingIntent);
        //left arrow
        PendingIntent leftArrowPendingIntent = getPendingIntent(CarousalConstants.EVENT_LEFT_ARROW_CLICKED);
        bigView.setOnClickPendingIntent(R.id.ivArrowLeft, leftArrowPendingIntent);
        //current item
        PendingIntent currentItemPendingIntent = getPendingIntent(CarousalConstants.EVENT_CURRENT_ITEM_CLICKED);
        bigView.setOnClickPendingIntent(R.id.llItemLayout, currentItemPendingIntent);
    }

    /**
     * creates pending intents with added bundle information about the region clicked
     *
     * @param eventClicked : integer id of the region clicked
     * @return pendingIntent for the same
     */
    private PendingIntent getPendingIntent(int eventClicked) {
        Intent carousalIntent = new Intent(CarousalConstants.CAROUSAL_EVENT_FIRED_INTENT_FILTER);
        Bundle bundle = new Bundle();
        bundle.putInt(CarousalConstants.EVENT_CAROUSAL_ITEM_CLICKED_KEY, eventClicked);
        bundle.putParcelable(CarousalConstants.CAROUSAL_SET_UP_KEY, carousalSetUp);
        carousalIntent.putExtras(bundle);
        PendingIntent pIntent = PendingIntent.getBroadcast(context, eventClicked, carousalIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pIntent;
    }

    /**
     * saves current set up into a {@link CarousalSetUp} object and returns it
     * this object is passed between broadcast receiver and this instance
     *
     * @return
     */
    private CarousalSetUp saveCarousalSetUp() {
        setUpfilePathOfImages();
        CarousalSetUp cr = new CarousalSetUp(carousalItems, contentTitle, contentText,
                bigContentTitle, bigContentText, carousalNotificationId,
                currentStartIndex, smallIconPath, smallIconResourceId, largeIconPath,
                placeHolderImagePath, currentItem, isOtherRegionClickable, isImagesInCarousal);
        return cr;
    }

    /**
     * If exists, it saves files into external directory and saves corresponding file path.
     */
    private void setUpfilePathOfImages() {
        if (smallIcon != null) {
            smallIconPath = CarousalUtilities.carousalSaveBitmapToInternalStorage(context, smallIcon,
                    CarousalConstants.CAROUSAL_SMALL_ICON_FILE_NAME);
        }
        if (largeIcon != null) {
            largeIconPath = CarousalUtilities.carousalSaveBitmapToInternalStorage(context, largeIcon,
                    CarousalConstants.CAROUSAL_LARGE_ICON_FILE_NAME);
        }
        if (caraousalPlaceholder != null) {
            placeHolderImagePath = CarousalUtilities.carousalSaveBitmapToInternalStorage(context, caraousalPlaceholder,
                    CarousalConstants.CAROUSAL_PLACEHOLDER_ICON_FILE_NAME);
        }
    }


    /**
     * Clears notification and empty's references if exists. Important to clear previous carousal
     * before starting a new one.
     *
     * @return
     */
    public Carousal clearCarousalIfExists() {
        if (carousalItems != null) {
            carousalItems.clear();

            smallIconResourceId = -1;
            isOtherRegionClickable = true;
            isImagesInCarousal = true;
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
            mNotifyManager.cancel(carousalNotificationId);
        }
        //ToDo :  delete all cache files
        return this;
    }


    //========================================= HANDLING EVENTS ===================================//

    /**
     * Handles all click events from the carousal notification.
     *
     * @param clickEvent
     */
    public void handleClickEvent(int clickEvent, CarousalSetUp setUp) {
        //First we need to make sure that the set up is there. If the original instance is
        // killed and it is a new one, we need to instantiate all the values from setUp object
        verifyAndSetUpVariables(setUp);

        switch (clickEvent) {
            case CarousalConstants.EVENT_LEFT_ARROW_CLICKED:
                onLeftArrowClicked();
                break;
            case CarousalConstants.EVENT_RIGHT_ARROW_CLICKED:
                onRightArrowClicked();
                break;
            case CarousalConstants.EVENT_CURRENT_ITEM_CLICKED:
                onCurrentItemClicked();
                break;
            case CarousalConstants.EVENT_OTHER_REGION_CLICKED:
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
     * @param setUp : {@link CarousalSetUp} object and carries the original set data.
     */
    private void verifyAndSetUpVariables(CarousalSetUp setUp) {
        //If it a new instance carousal stUp will be null
        if (carousalSetUp == null) {
            carousalItems = setUp.carousalItems;
            contentTitle = setUp.contentTitle;
            contentText = setUp.contentText;
            bigContentTitle = setUp.bigContentTitle;
            bigContentText = setUp.bigContentText;
            carousalNotificationId = setUp.carousalNotificationId;
            currentStartIndex = setUp.currentStartIndex;
            notificationPriority = setUp.notificationPriority;
            smallIconPath = setUp.smallIcon;
            largeIconPath = setUp.largeIcon;
            placeHolderImagePath = setUp.caraousalPlaceholder;
            currentItem = setUp.currentItem;
            isOtherRegionClickable = setUp.isOtherRegionClickable;
            isImagesInCarousal = setUp.isImagesInCarousal;


            setUpBitCarousalBitmapsFromSetUp();

        } else if (carousalSetUp != null && carousalNotificationId != setUp.carousalNotificationId) {
            carousalSetUp = null;
            verifyAndSetUpVariables(setUp);
        }
    }

    /**
     * If exists it loads bitmaps from file directory and saves them.
     */
    private void setUpBitCarousalBitmapsFromSetUp() {
        if (smallIconPath != null) {
            smallIcon = CarousalUtilities.carousalLoadImageFromStorage(smallIconPath, CarousalConstants.CAROUSAL_SMALL_ICON_FILE_NAME);
        }
        if (largeIconPath != null) {
            largeIcon = CarousalUtilities.carousalLoadImageFromStorage(largeIconPath, CarousalConstants.CAROUSAL_LARGE_ICON_FILE_NAME);
        }
        if (placeHolderImagePath != null) {
            caraousalPlaceholder = CarousalUtilities.carousalLoadImageFromStorage(placeHolderImagePath, CarousalConstants.CAROUSAL_PLACEHOLDER_ICON_FILE_NAME);
        }
    }

    private void onCurrentItemClicked() {
        writeAction("ItemSelected");
        sendItemClickedBroadcast(currentItem);
    }

    /**
     * This is caused when any other region than carousal items is clicked.
     */
    private void onOtherRegionClicked() {
        if (isOtherRegionClickable) {
            //We will send the broadcast and finish the carousal
            Intent i = new Intent();
            i.setAction(CarousalConstants.CAROUSAL_ITEM_CLICKED_INTENT_FILTER);
            Bundle bundle = new Bundle();
            bundle.putParcelable(CAROUSAL_ITEM_CLICKED_KEY, currentItem);
            i.putExtras(bundle);
            context.getApplicationContext().sendBroadcast(i);
            try {
                clearCarousalIfExists();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Unable To send notification's pendingIntent", e);
            }
        }
    }

    private void sendItemClickedBroadcast(CarousalItem cItem) {
        Intent i = new Intent();
        i.setAction(CarousalConstants.CAROUSAL_ITEM_CLICKED_INTENT_FILTER);
        Bundle bundle = new Bundle();
        bundle.putParcelable(CAROUSAL_ITEM_CLICKED_KEY, cItem);
        i.putExtras(bundle);

        context.getApplicationContext().sendBroadcast(i);

        try {
            clearCarousalIfExists();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Unable To send notification's pendingIntent", e);
        }
    }

    /**
     * Here we choose the items for left and right an call prepareVariablesForCarousalAndShow() thereafter
     */
    private void onLeftArrowClicked() {
        if (carousalItems != null && currentStartIndex > 0) {
            writeAction("Previous");
            currentStartIndex -= 1;
            prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex));
        }
    }

    private void onRightArrowClicked() {
        if (carousalItems != null && carousalItems.size() > currentStartIndex) {
            writeAction("Next");
            currentStartIndex += 1;
            prepareVariablesForCarousalAndShow(carousalItems.get(currentStartIndex));
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
        extraData.put("TotalSteps", carousalItems.size());
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
