package com.soluto.carouselnotificationmaster;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import com.soluto.carouselnotification.AnalyticsWriter;
import com.soluto.carouselnotification.Carousel;
import com.soluto.carouselnotification.CarouselItem;
import com.soluto.carouselnotification.LoggingApi;

public class MainActivity extends AppCompatActivity {
    Button btnQuote;
    Button btnNasa;
    TextView tvStatus;
    TextView tvResponse;
    ProgressDialog progressDialog;

    private final String FETCHING = "Status = fetching data";
    private final String BUILDING_CAROUSEL = "Status = Data detched. building carousel";
    private final String CAROUSELPREPARING = "Status = Caraousal preparing. will be be up in few seconds!";
    private final String FETCHING_ERROR = "Status = Seems like this url is not working. Should try others.";
    private final String PARSING_ERROR = "Status = Seems like data format of this spi has changed. Should try others.";


    public static final String TYPE_QUOTE = "Quote";
    public static final String TYPE_IMAGE_MARS = "MarsImage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpLayout();

        onNewIntent(getIntent());
    }

    private void setUpLayout() {
        btnQuote  = (Button) findViewById(R.id.btnQuote);
        btnNasa = (Button) findViewById(R.id.btnMovies);
        tvStatus = (TextView) findViewById(R.id.tvCurrentStatus);
        tvResponse = (TextView) findViewById(R.id.tvResponse);
        btnQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchQuotes();
            }
        });

        btnNasa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchMarsPhotos();
            }
        });
    }

    private void fetchMarsPhotos() {
        buildMarsCarousel();
    }

    /**
     * once we receive data from api, we start building the carousel
     */
    private void buildMarsCarousel() {
        Carousel carousel = Carousel.with(this, "myID", new AnalyticsWriter() {
            @Override
            public void write(String eventName, Map<String, Object> extraData) {

            }
        }, new LoggingApi() {
            @Override
            public void debug(String msg) {

            }

            @Override
            public void info(String msg) {

            }

            @Override
            public void warn(String msg) {

            }

            @Override
            public void warn(String msg, Throwable t) {

            }

            @Override
            public void error(String msg) {

            }

            @Override
            public void error(String msg, Throwable t) {

            }
        }).beginTransaction();
        carousel.setContentTitle("Opportunity on Mars!").setContentText("Check out these photographs by Opportunity on Mars.");
        /*for (int i = 0; i < 4 ; i++ ) {
            //Notice here. In case you want to preserve data of each item, you can save a gson string
            // of the object in carousel item's id.  Though not much recommended.
            String imageUrl = "https://solutokb.staging.wpengine.com/wp-content/uploads/notificationCrousel" + String.valueOf(i + 1) + ".png";

            CarouselItem cItem = new CarouselItem(String.valueOf(i), "Title", "Desc", imageUrl);
            carousel.addCarouselItem(cItem);
        }*/
        int color = Color.argb(255,105,105,105);
        CarouselItem cItem1 = new CarouselItem("1", null, "Go through the music on your phone and clear songs you moved on from.", "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_1.png", null, Color.BLACK);
        carousel.addCarouselItem(cItem1);
        CarouselItem cItem2 = new CarouselItem("2", null, "Delete duplicate, blurry, and other photos that are hogging your storage.", "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_2.png", null, Color.GRAY);
        carousel.addCarouselItem(cItem2);
        CarouselItem cItem3 = new CarouselItem("3", null, "We all have those apps we don’t use anymore, delete them!", "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_3.png", null, color);
        carousel.addCarouselItem(cItem3);
        CarouselItem cItem4 = new CarouselItem("4", null, "In your messaging apps delete conversations with lots of media.", "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_4.png", null, Color.BLUE);
        carousel.addCarouselItem(cItem4);
        CarouselItem cItem5 = new CarouselItem("5", null, null, "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_5.png", null, Color.BLACK);
        carousel.addCarouselItem(cItem5);
        carousel.setBigContentText("text").setBigContentTitle("Title");

        carousel.buildCarousel();
      /*  ArrayList<RoverItem> photoList = new ArrayList<RoverItem>();
        try {
            JSONObject photoOb = new JSONObject(s);
            JSONArray photos = photoOb.getJSONArray("photos");
            Type type = new TypeToken<RoverItem>() {
            }.getType();
            for (int i = 0; i < photos.length() && i < 10 ; i = i+2 ) {

                RoverItem item = new Gson().fromJson(photos.get(i).toString(), type);
                photoList.add(item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            tvStatus.setText(PARSING_ERROR);
        }

        if (photoList.size() > 0) {
            tvStatus.setText(CAROUSELPREPARING);
            //Here we build the carousel
            Carousel carousel = Carousel.with(this).beginTransaction();
            carousel.setContentTitle("Opportunity on Mars!").setContentText("Check out these photographs by Opportunity on Mars.");
            for ( RoverItem photo : photoList) {

                //Notice here. In case you want to preserve data of each item, you can save a gson string
                // of the object in carousel item's id.  Though not much recommended.
                String imageUrl = photo.getImg_src();

                imageUrl = imageUrl.replaceFirst("http:","https:");

                CarouselItem cItem = new CarouselItem(new Gson().toJson(photo).toString(), photo.getEarth_date(), null, imageUrl);

                //Additionally we can set a type to it. It is useful if we are showing more than one type
                //of data in carousel. so that we know, where to go when an item is clicked.
                cItem.setType(TYPE_IMAGE_MARS);
                carousel.addCarouselItem(cItem);
            }
            carousel.buildCarousel();
        }*/
    }

    private void fetchQuotes() {
        tvStatus.setText( FETCHING);
        showDialog("Fetching Quotes");
        JSONParser.OnConnectionListener onConnectionListener = new JSONParser.OnConnectionListener() {
            @Override
            public void onResponse(String s) {
                hideDialog();
                tvStatus.setText( BUILDING_CAROUSEL);
                tvResponse.setText("Fetched Result = " + s);
                buildQuoteCarousel(s);
            }

            @Override
            public void onError() {
                hideDialog();
                tvStatus.setText(FETCHING_ERROR);

            }
        };
        JSONParser jsonParser = new JSONParser(onConnectionListener);
        jsonParser.fetchJson("http://quotesondesign.com/wp-json/posts?filter[orderby]=rand&filter[posts_per_page]=10");

    }

    /**
     * once we receive data from api, we start building the carousel
     * @param s : response string
     */
    private void buildQuoteCarousel(String s) {
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(Carousel.CAROUSEL_ITEM_CLICKED_KEY)) {
                CarouselItem carouselItem = (CarouselItem) extras.getParcelable(Carousel.CAROUSEL_ITEM_CLICKED_KEY);
                Toast.makeText(this, carouselItem.getTitle() + carouselItem.getDescription(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showDialog(String message) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void hideDialog() {
        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog = null;
        }
    }
}
