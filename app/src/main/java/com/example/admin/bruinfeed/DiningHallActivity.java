package com.example.admin.bruinfeed;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_hall);

        recyclerView = (RecyclerView) findViewById(R.id.menuRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SimpleAdapter(getBaseContext(), menuItems);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

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

        if (activityLevel == 0) {
            activityLevelTextView.setText("Activity Level at " + selectedDiningHall + " is currently unavailable");
        } else {
            activityLevelTextView.setText("Activity Level at " + selectedDiningHall + " is " + activityLevel + "%");
        }
        activityLevelProgressBar.setProgress(activityLevel);

        getMeals(selectedDiningHall, meal);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_breakfast_button:
                    getMeals(selectedDiningHall, "Breakfast");
                    break;
                case R.id.bottom_lunch_button:
                    getMeals(selectedDiningHall, "Lunch");
                    break;
                case R.id.bottom_dinner_button:
                    getMeals(selectedDiningHall, "Dinner");
                    break;
            }

            return true;
        }

    };

    public void getMeals(String selectedDiningHall, String meal) {
        DatabaseHandler db = new DatabaseHandler(this);
        List<MealItem> allItems = db.getAllMealItems();

        menuItems.clear();
        sections.clear();
        String section = "";

        // TODO: RETRIEVE USING SQL QUERIES
        for (MealItem mealItem : allItems) {
            if (mealItem.getHall().equals(selectedDiningHall) && mealItem.getMeal().equals(meal)) {
                menuItems.add(mealItem);
                if (!mealItem.getSection().equals(section)) {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(menuItems.size() - 1, mealItem.getSection()));
                    section = mealItem.getSection();
                }
            }
        }

        // Add adapter to the sectionAdapter
        SimpleSectionedRecyclerViewAdapter.Section[] array = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter = new SimpleSectionedRecyclerViewAdapter(getBaseContext(), R.layout.section, R.id.section_text, mAdapter);
        mSectionedAdapter.setSections(sections.toArray(array));

        recyclerView.setAdapter(mSectionedAdapter);
        recyclerView.getRecycledViewPool().clear();
        mAdapter.notifyDataSetChanged();
    }
}
