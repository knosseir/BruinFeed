package com.example.admin.bruinfeed;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DiningHallActivity extends AppCompatActivity {

    private static final String DiningHallTag = "DiningHallActivity";

    String url, meal;
    int activityLevel = 0;
    ArrayList<MealItem> menuItems = new ArrayList<>();
    TextView activityLevelTextView;

    String selectedDiningHall;
    MaterialSearchView searchView;

    private RecyclerView recyclerView;
    private SimpleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();
    SimpleSectionedRecyclerViewAdapter mSectionedAdapter;

    List<MealItem> originalMenuItems = new ArrayList<>();
    List<SimpleSectionedRecyclerViewAdapter.Section> originalSections = new ArrayList<>();

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

        Toolbar toolbar = (Toolbar) findViewById(R.id.dining_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp));

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

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

        searchView = (MaterialSearchView) findViewById(R.id.dining_search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                onQueryTextChange(query);

                // hide keyboard after search is submitted and results are displayed
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // if search query is blank, reset results and show all meal items and sections
                if (newText.equals("")) {
                    menuItems.clear();
                    menuItems.addAll(originalMenuItems);

                    sections.clear();
                    sections.addAll(originalSections);

                    updateRecyclerView();

                    return true;
                }

                List<MealItem> queryItems = new ArrayList<>();
                String section = "";

                menuItems.clear();
                sections.clear();

                for (MealItem mealItem : originalMenuItems) {
                    if (mealItem.getName().toLowerCase().contains(newText.toLowerCase())) {
                        queryItems.add(mealItem);
                        if (!mealItem.getSection().equals(section)) {
                            sections.add(new SimpleSectionedRecyclerViewAdapter.Section(queryItems.size() - 1, mealItem.getSection()));
                            section = mealItem.getSection();
                        }
                    }
                }

                menuItems.addAll(queryItems);

                updateRecyclerView();

                return true;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
            }

            @Override
            public void onSearchViewClosed() {
            }
        });
    }

    public void updateRecyclerView() {
        // Add adapter to the sectionAdapter
        SimpleSectionedRecyclerViewAdapter.Section[] array = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter = new SimpleSectionedRecyclerViewAdapter(getBaseContext(), R.layout.section, R.id.section_text, mAdapter);
        mSectionedAdapter.setSections(sections.toArray(array));

        // Update RecyclerView
        recyclerView.setAdapter(mSectionedAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
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

        originalMenuItems = new ArrayList<>(menuItems);
        originalSections = new ArrayList<>(sections);

        updateRecyclerView();
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }
}
