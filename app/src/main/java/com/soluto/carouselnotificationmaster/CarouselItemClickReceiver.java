package com.soluto.carouselnotificationmaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.soluto.carouselnotification.Carousel;
import com.soluto.carouselnotification.CarouselItem;


/**
 * Created by Shailesh on 08/01/17.
 */

public class CarouselItemClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            CarouselItem item = bundle.getParcelable(Carousel.CAROUSEL_ITEM_CLICKED_KEY);
            String id = bundle.getString(Carousel.CAROUSEL_NOTIFICATION_ID_KEY);
            if (item != null) {

            } else {  //Meaning other region is clicked and isOtherRegionClick is set to true.
                Toast.makeText(context, "Other region clicked", Toast.LENGTH_LONG).show();
            }

        }
    }
}
