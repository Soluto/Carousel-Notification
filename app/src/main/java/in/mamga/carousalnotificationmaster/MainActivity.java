package in.mamga.carousalnotificationmaster;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;

import in.mamga.carousalnotification.AnalyticsWriter;
import in.mamga.carousalnotification.Carousal;
import in.mamga.carousalnotification.CarousalConstants;
import in.mamga.carousalnotification.CarousalItem;
import in.mamga.carousalnotification.LoggingApi;

public class MainActivity extends AppCompatActivity {
    Button btnQuote;
    Button btnNasa;
    TextView tvStatus;
    TextView tvResponse;
    ProgressDialog progressDialog;

    private final String FETCHING = "Status = fetching data";
    private final String BUILDING_CAROUSAL = "Status = Data detched. building carousal";
    private final String CAROUSALPREPARING = "Status = Caraousal preparing. will be be up in few seconds!";
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
        buildMarsCarousal();
    }

    /**
     * once we receive data from api, we start building the carousal
     */
    private void buildMarsCarousal() {
        Carousal carousal = Carousal.with(this, new AnalyticsWriter() {
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
        carousal.setContentTitle("Opportunity on Mars!").setContentText("Check out these photographs by Opportunity on Mars.");
        /*for (int i = 0; i < 4 ; i++ ) {
            //Notice here. In case you want to preserve data of each item, you can save a gson string
            // of the object in carousal item's id.  Though not much recommended.
            String imageUrl = "https://solutokb.staging.wpengine.com/wp-content/uploads/notificationCrousel" + String.valueOf(i + 1) + ".png";

            CarousalItem cItem = new CarousalItem(String.valueOf(i), "Title", "Desc", imageUrl);
            carousal.addCarousalItem(cItem);
        }*/
        int color = Color.argb(255,105,105,105);
        CarousalItem cItem1 = new CarousalItem("1", null, "Go through the music on your phone and clear songs you moved on from.", "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_1.png", null, Color.BLACK);
        carousal.addCarousalItem(cItem1);
        CarousalItem cItem2 = new CarousalItem("2", null, "Delete duplicate, blurry, and other photos that are hogging your storage.", "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_2.png", null, Color.GRAY);
        carousal.addCarousalItem(cItem2);
        CarousalItem cItem3 = new CarousalItem("3", null, "We all have those apps we don’t use anymore, delete them!", "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_3.png", null, color);
        carousal.addCarousalItem(cItem3);
        CarousalItem cItem4 = new CarousalItem("4", null, "In your messaging apps delete conversations with lots of media.", "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_4.png", null, Color.BLUE);
        carousal.addCarousalItem(cItem4);
        CarousalItem cItem5 = new CarousalItem("5", null, null, "https://solutokb.staging.wpengine.com/wp-content/uploads/carouselgallery_C_5.png", null, Color.BLACK);
        carousal.addCarousalItem(cItem5);
        carousal.setBigContentText("text").setBigContentTitle("Title");

        carousal.buildCarousal();
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
            tvStatus.setText(CAROUSALPREPARING);
            //Here we build the carousal
            Carousal carousal = Carousal.with(this).beginTransaction();
            carousal.setContentTitle("Opportunity on Mars!").setContentText("Check out these photographs by Opportunity on Mars.");
            for ( RoverItem photo : photoList) {

                //Notice here. In case you want to preserve data of each item, you can save a gson string
                // of the object in carousal item's id.  Though not much recommended.
                String imageUrl = photo.getImg_src();

                imageUrl = imageUrl.replaceFirst("http:","https:");

                CarousalItem cItem = new CarousalItem(new Gson().toJson(photo).toString(), photo.getEarth_date(), null, imageUrl);

                //Additionally we can set a type to it. It is useful if we are showing more than one type
                //of data in carousal. so that we know, where to go when an item is clicked.
                cItem.setType(TYPE_IMAGE_MARS);
                carousal.addCarousalItem(cItem);
            }
            carousal.buildCarousal();
        }*/
    }

    private void fetchQuotes() {
        tvStatus.setText( FETCHING);
        showDialog("Fetching Quotes");
        JSONParser.OnConnectionListener onConnectionListener = new JSONParser.OnConnectionListener() {
            @Override
            public void onResponse(String s) {
                hideDialog();
                tvStatus.setText( BUILDING_CAROUSAL);
                tvResponse.setText("Fetched Result = " + s);
                buildQuoteCarousal(s);
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
     * once we receive data from api, we start building the carousal
     * @param s : response string
     */
    private void buildQuoteCarousal(String s) {
    }

    @Override
    public void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(Carousal.CAROUSAL_ITEM_CLICKED_KEY)) {
                CarousalItem carousalItem = (CarousalItem) extras.getParcelable(Carousal.CAROUSAL_ITEM_CLICKED_KEY);
                Toast.makeText(this, carousalItem.getTitle() + carousalItem.getDescription(), Toast.LENGTH_LONG).show();
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
