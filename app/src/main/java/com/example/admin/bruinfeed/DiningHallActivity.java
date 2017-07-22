package com.example.admin.bruinfeed;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class DiningHallActivity extends AppCompatActivity {

    String url, meal;
    ArrayList<String> menuItemNames = new ArrayList<>();

    ArrayAdapter<String> gridViewArrayAdapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_breakfast_button:
                    return true;
                case R.id.bottom_lunch_button:
                    return true;
                case R.id.bottom_dinner_button:
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_hall);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        String selectedDiningHall = getIntent().getStringExtra("SelectedDiningHall");
        setTitle(selectedDiningHall);

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
        else {
            navigation.setSelectedItemId(R.id.bottom_dinner_button);
            meal = "Dinner";
        }

        url = "http://menu.dining.ucla.edu" + selectedDiningHall + "/" + meal;
        Log.e("URL", url);

        DiningHallActivity.AsyncTaskRunner runner = new DiningHallActivity.AsyncTaskRunner();
        runner.execute("hello");

        GridView menuItemGrid = (GridView) findViewById(R.id.menuGrid);

        gridViewArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItemNames);
        menuItemGrid.setAdapter(gridViewArrayAdapter);
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Document doc = Jsoup.connect(url).get();
                Elements links = doc.select("a.recipeLink");

                for (Element link : links) {
                    menuItemNames.add(link.ownText());      // TODO: CHECK THAT LINK HAS TEXT USING HASTEXT()
                    Log.e("menu item", link.ownText());
                }

                // update GridView with list of dining halls on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridViewArrayAdapter.notifyDataSetChanged();
                    }
                });

            } catch (IOException | IllegalArgumentException e) {    // TODO: CHECK NUMBER OF EXCEPTIONS OCCURRED
                Log.e("error", "This should never happen");
            }

            return "success";
        }
    }
}
