package com.example.admin.bruinfeed;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;

public class DiningHallActivity extends AppCompatActivity {

    private static final String DiningHallTag = "DiningHallActivity";

    String url, meal;
    GridView menuItemGrid;
    ArrayList<String> menuItemNames = new ArrayList<>();

    ArrayAdapter<String> gridViewArrayAdapter;
    String selectedDiningHall;

    SwipeRefreshLayout menuGridRefresh;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_breakfast_button:
                    meal = "Breakfast";
                    break;
                case R.id.bottom_lunch_button:
                    meal = "Lunch";
                    break;
                case R.id.bottom_dinner_button:
                    meal = "Dinner";
                    break;
            }
            url = "http://menu.dining.ucla.edu/Menus/" + selectedDiningHall + "/" + meal;
            Log.e(DiningHallTag, url);

            menuGridRefresh.setRefreshing(true);
            DiningHallActivity.AsyncTaskRunner runner = new DiningHallActivity.AsyncTaskRunner();
            runner.execute(url);

            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_hall);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        menuGridRefresh = (SwipeRefreshLayout) findViewById(R.id.menuGridRefresh);

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

        selectedDiningHall = getIntent().getStringExtra("SelectedDiningHall");

        url = "http://menu.dining.ucla.edu/Menus/" + selectedDiningHall + "/" + meal;
        Log.e(DiningHallTag, url);
        setTitle(meal + " at " + selectedDiningHall);

        DiningHallActivity.AsyncTaskRunner runner = new DiningHallActivity.AsyncTaskRunner();
        runner.execute(url);

        menuItemGrid = (GridView) findViewById(R.id.menuGrid);

        gridViewArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, menuItemNames);
        menuItemGrid.setAdapter(gridViewArrayAdapter);

        menuGridRefresh.setRefreshing(true);
        // refreshes menu grid upon pull to refresh
        menuGridRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
                        asyncTaskRunner.execute(url);
                    }
                }
        );
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                menuItemNames.clear();
                Document doc = Jsoup.connect(params[0]).get();
                Elements links = doc.select("a.recipeLink");

                for (Element link : links) {
                    menuItemNames.add(link.ownText());      // TODO: CHECK THAT LINK HAS TEXT USING HASTEXT()
                    Log.e(DiningHallTag, link.ownText());
                }

                updateGrid();

                if (menuItemNames.isEmpty()) {
                    final Snackbar emptyMenuSnackbar = Snackbar.make(findViewById(R.id.menuGrid), R.string.empty_menu, Snackbar.LENGTH_INDEFINITE);
                    emptyMenuSnackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            emptyMenuSnackbar.dismiss();
                        }
                    });
                    emptyMenuSnackbar.show();
                }

            } catch (SocketTimeoutException e) {    // TODO: CHECK NUMBER OF EXCEPTIONS OCCURRED
                Log.e(DiningHallTag, e.toString());
                updateGrid();
                reload(R.string.connection_timeout);
            } catch (IOException | IllegalArgumentException e) {
                Log.e(DiningHallTag, e.toString());
                updateGrid();
                reload(R.string.retry_connection);
            }

            return "success";
        }
    }

    public void updateGrid() {
        // update GridView with menu and set action bar title on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                gridViewArrayAdapter.notifyDataSetChanged();
                setTitle(meal + " at " + selectedDiningHall);
                menuGridRefresh.setRefreshing(false);
            }
        });
    }

    public void reload(int error) {
        Snackbar reloadSnackbar = Snackbar.make(findViewById(R.id.menuGrid), error, Snackbar.LENGTH_INDEFINITE);
        reloadSnackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menuGridRefresh.setRefreshing(true);
                        AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
                        asyncTaskRunner.execute(url);
                    }
                });
                reloadSnackbar.show();
    }
}
