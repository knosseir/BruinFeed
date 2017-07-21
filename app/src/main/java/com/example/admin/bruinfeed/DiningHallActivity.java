package com.example.admin.bruinfeed;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DiningHallActivity extends AppCompatActivity {

    String url, meal;

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_breakfast_button:
                    mTextMessage.setText(R.string.title_breakfast);
                    return true;
                case R.id.bottom_lunch_button:
                    mTextMessage.setText(R.string.title_lunch);
                    return true;
                case R.id.bottom_dinner_button:
                    mTextMessage.setText(R.string.title_dinner);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_hall);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        String selectedDiningHall = getIntent().getStringExtra("SelectedDiningHall");

        // get current hour of day
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);

        // pick default meal based on time of day
        if (currentHour > 6 && currentHour < 11) {
            navigation.setSelectedItemId(R.id.bottom_breakfast_button);
            meal = "Breakfast";
        }
        else if (currentHour < 17) {
            navigation.setSelectedItemId(R.id.bottom_lunch_button);
            meal = "Lunch";
        }
        else if (currentHour < 21) {
            navigation.setSelectedItemId(R.id.bottom_dinner_button);
            meal = "Dinner";
        }

        url = selectedDiningHall + "/" + meal;
        Log.e("URL", url);
    }

    /*
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Document doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");

                for (Element link : links) {
                    Log.d("link titles", link.attr("href"));
                    String name = link.attr("href");
                    if (name.contains("/Menus/") && !name.contains("Breakfast")
                            && !name.contains("Lunch") && !name.contains("Dinner") && !name.contains("Full Menu")) {       // TODO: FIND BETTER WAY TO FILTER OUT BREAKFAST, LUNCH, AND DINNER LINKS FROM DINING HALL LIST
                        diningHallLinks.add(link);
                        diningHallNames.add(name);
                    }
                }

                // remove duplicates from diningHallNames ArrayList
                Set<String> diningHallTemp = new HashSet<>();
                diningHallTemp.addAll(diningHallNames);
                diningHallNames.clear();
                diningHallNames.addAll(diningHallTemp);

                // update GridView with list of dining halls on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridViewArrayAdapter.notifyDataSetChanged();
                    }
                });

            } catch (IOException | IllegalArgumentException e) {    // TODO: CHECK NUMBER OF EXCEPTIONS OCCURRED
                Log.e("error", "This should never happen");
                Snackbar reloadSnackbar = Snackbar.make(findViewById(R.id.diningHallGrid), R.string.retry_connection, Snackbar.LENGTH_INDEFINITE);
                reloadSnackbar.setAction(R.string.reconnecting, new MainActivity.ReconnectListener());
                reloadSnackbar.show();
            }

            return "success";
        }
    }
    */
}
