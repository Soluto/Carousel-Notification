package in.mamga.carousalnotificationmaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import in.mamga.carousalnotification.Carousal;
import in.mamga.carousalnotification.CarousalItem;


/**
 * Created by Shailesh on 08/01/17.
 */

public class CarousalItemClickReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            CarousalItem item = bundle.getParcelable(Carousal.CAROUSAL_ITEM_CLICKED_KEY);
            if (item != null) {

            } else {  //Meaning other region is clicked and isOtherRegionClick is set to true.
                Toast.makeText(context, "Other region clicked", Toast.LENGTH_LONG).show();
            }

        }
    }
}
