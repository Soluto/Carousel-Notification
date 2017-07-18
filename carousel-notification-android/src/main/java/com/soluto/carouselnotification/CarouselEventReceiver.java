package com.soluto.carouselnotification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CarouselEventReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            int carouselEvent = bundle.getInt(CarouselConstants.EVENT_CAROUSEL_ITEM_CLICKED_KEY);
            CarouselSetUp carouselSetUp = bundle.getParcelable(CarouselConstants.CAROUSEL_SET_UP_KEY);

            //Respond only if both things are there
            if (carouselEvent > 0 && carouselSetUp != null && Carousel.current() != null)
                Carousel.current().handleClickEvent(carouselEvent, carouselSetUp);
        }
    }
}