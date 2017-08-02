package com.example.admin.bruinfeed;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DiningHallActivity extends AppCompatActivity {

    private static final String DiningHallTag = "DiningHallActivity";

    String url, meal;
    int activityLevel = 0;
    ArrayList<MealItem> menuItems = new ArrayList<>();
    TextView activityLevelTextView;

    String selectedDiningHall;

    private RecyclerView recyclerView;
    private SimpleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();
    SimpleSectionedRecyclerViewAdapter mSectionedAdapter;

    private ProgressBar activityLevelProgressBar;

    SwipeRefreshLayout menuRefresh;

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

            menuRefresh.setRefreshing(true);
            AsyncTaskRunner runner = new AsyncTaskRunner();
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

        recyclerView = (RecyclerView) findViewById(R.id.menuRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SimpleAdapter(getBaseContext(), menuItems);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        menuRefresh = (SwipeRefreshLayout) findViewById(R.id.menuRefresh);

        activityLevelTextView = (TextView) findViewById(R.id.activityLevelTextBox);

        // get current hour of day
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);

        // pick default meal based on time of day
        if (currentHour < 11) {
            navigation.setSelectedItemId(R.id.bottom_breakfast_button);
            meal = "Breakfast";
        } else if (currentHour < 17) {
            navigation.setSelectedItemId(R.id.bottom_lunch_button);
            meal = "Lunch";
        } else {
            navigation.setSelectedItemId(R.id.bottom_dinner_button);
            meal = "Dinner";
        }

        // TODO: CHOOSE MEAL BASED ON DINING PERIODS FOR EACH DINING HALL

        selectedDiningHall = getIntent().getStringExtra("SelectedDiningHall");
        activityLevel = getIntent().getIntExtra("ActivityLevel", 0);
        activityLevelProgressBar = (ProgressBar) findViewById(R.id.activityLevel);

        url = "http://menu.dining.ucla.edu/Menus/" + selectedDiningHall + "/" + meal;
        setTitle(meal + " at " + selectedDiningHall);

        menuRefresh.setColorSchemeColors(Color.rgb(244, 205, 65));

        menuRefresh.setRefreshing(true);

        // refreshes menu upon pull to refresh
        menuRefresh.setOnRefreshListener(
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
                Document doc = Jsoup.connect(url).timeout(10 * 1000).get();
                Elements links = doc.select("a.recipeLink, li.sect-item");
                menuItems.clear();
                sections.clear();

                for (Element link : links) {
                    if (link.tagName().equals("a")) {
                        String description = null;
                        Element parent = link.parent().parent();
                        if (parent != null) {
                            Elements descriptionElement = parent.select("div.tt-description");
                            String url = link.attr("href");
                            if (descriptionElement.size() > 0 && descriptionElement.get(0) != null) {
                                description = parent.select("div.tt-description").text();
                                menuItems.add(new MealItem(link.ownText(), description, url));
                            }
                            else {
                                menuItems.add(new MealItem(link.ownText(), "No description available", url));
                            }
                        }
                    } else if (link.tagName().equals("li")) {
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(menuItems.size(), link.ownText()));
                    }
                }

                // Add adapter to the sectionAdapter
                SimpleSectionedRecyclerViewAdapter.Section[] dummy = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
                mSectionedAdapter = new
                        SimpleSectionedRecyclerViewAdapter(getBaseContext(), R.layout.section, R.id.section_text, mAdapter);
                mSectionedAdapter.setSections(sections.toArray(dummy));

                updateMenuRecyclerView();

            } catch (SocketTimeoutException e) {    // TODO: CHECK NUMBER OF EXCEPTIONS OCCURRED
                Log.e(DiningHallTag, e.toString());
                updateMenuRecyclerView();
                reload(R.string.connection_timeout);
            } catch (IOException | IllegalArgumentException e) {
                Log.e(DiningHallTag, e.toString());
                updateMenuRecyclerView();
                reload(R.string.retry_connection);
            }

            return "success";
        }
    }

    public void updateMenuRecyclerView() {
        // update RecyclerView with menu and set action bar title on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                recyclerView.setAdapter(mSectionedAdapter);
                mAdapter.notifyDataSetChanged();

                /*
                if (menuItemNames.isEmpty()) {
                    final Snackbar emptyMenuSnackbar = Snackbar.make(findViewById(R.id.menuRecyclerView), R.string.empty_menu, Snackbar.LENGTH_INDEFINITE);
                    emptyMenuSnackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            emptyMenuSnackbar.dismiss();
                        }
                    });
                    emptyMenuSnackbar.show();
                }
                */

                setTitle(meal + " at " + selectedDiningHall);
                if (activityLevel == 0) {
                    activityLevelTextView.setText("Activity Level at " + selectedDiningHall + " is currently unavailable");
                } else {
                    activityLevelTextView.setText("Activity Level at " + selectedDiningHall + " is " + activityLevel + "%");
                }
                activityLevelProgressBar.setProgress(activityLevel);
                menuRefresh.setRefreshing(false);
            }
        });
    }

    public void reload(int error) {
        Snackbar reloadSnackbar = Snackbar.make(findViewById(R.id.menuRecyclerView), error, Snackbar.LENGTH_INDEFINITE);
        reloadSnackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menuRefresh.setRefreshing(true);
                AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
                asyncTaskRunner.execute(url);
            }
        });
        reloadSnackbar.show();
    }
}
