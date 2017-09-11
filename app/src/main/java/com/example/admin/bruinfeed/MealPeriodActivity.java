package com.example.admin.bruinfeed;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MealPeriodActivity extends AppCompatActivity {

    public static final String FILTER_PREFERENCES_NAME = "FilterPrefs";

    String selectedMeal;
    List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();
    List<MealItem> menuItems = new ArrayList<>();
    List<MealItem> allItems = new ArrayList<>();
    List<MealItem> originalMenuItems = new ArrayList<>();
    List<SimpleSectionedRecyclerViewAdapter.Section> originalSections = new ArrayList<>();

    String vegan, vegetarian, no_nuts, nuts, no_dairy, dairy, no_eggs, eggs, no_wheat, wheat, no_soy, soy;

    private RecyclerView recyclerView;
    private SimpleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    SimpleSectionedRecyclerViewAdapter mSectionedAdapter;
    MaterialSearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_period);
        selectedMeal = getIntent().getStringExtra("selectedMeal");
        setTitle(selectedMeal);

        recyclerView = (RecyclerView) findViewById(R.id.mealListRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SimpleAdapter(getBaseContext(), menuItems);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        try {

            DatabaseHandler db = new DatabaseHandler(this);
            allItems = db.getAllMealItems();

            vegan = getResources().getString(R.string.vegan);
            vegetarian = getResources().getString(R.string.vegetarian);
            no_nuts = getResources().getString(R.string.no_nuts);
            nuts = getResources().getString(R.string.nuts);
            no_dairy = getResources().getString(R.string.no_dairy);
            dairy = getResources().getString(R.string.dairy);
            no_eggs = getResources().getString(R.string.no_eggs);
            eggs = getResources().getString(R.string.eggs);
            no_wheat = getResources().getString(R.string.no_wheat);
            wheat = getResources().getString(R.string.wheat);
            no_soy = getResources().getString(R.string.no_soy);
            soy = getResources().getString(R.string.soy);

            SharedPreferences filters = getSharedPreferences(FILTER_PREFERENCES_NAME, 0);
            SharedPreferences.Editor editor = filters.edit();

            String mealDescriptorArray[] = {vegan, vegetarian, no_nuts, no_dairy, no_eggs, no_wheat, no_soy};

            // uncheck all filters by default
            for (String descriptor : mealDescriptorArray) {
                editor.putBoolean(descriptor, false);
            }
            editor.apply();

            Calendar calendar = Calendar.getInstance();
            Date date = calendar.getTime();
            String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);

            String diningHall = "";

            // TODO: ADD SECTIONS FROM EACH DINING HALL
            for (MealItem mealItem : allItems) {
                if (mealItem.getMeal().equals(selectedMeal) && mealItem.getDate().equals(dateString)) {
                    menuItems.add(mealItem);
                    if (!mealItem.getHall().equals(diningHall)) {
                        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(menuItems.size() - 1, mealItem.getHall()));
                        diningHall = mealItem.getHall();
                    }
                }
            }

            originalMenuItems = new ArrayList<>(menuItems);
            originalSections = new ArrayList<>(sections);

            updateRecyclerView();

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            searchView = (MaterialSearchView) findViewById(R.id.search_view);
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
                            if (!mealItem.getHall().equals(section)) {
                                sections.add(new SimpleSectionedRecyclerViewAdapter.Section(queryItems.size() - 1, mealItem.getHall()));
                                section = mealItem.getHall();
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
        } catch (Exception e) {
            Log.e("MealPeriodActivity", e.toString());
        }
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
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu_no_map, menu);
        MaterialSearchView searchView = (MaterialSearchView) findViewById(R.id.search_view);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        final MenuItem filterButton = menu.findItem(R.id.dining_filter_button);
        filterButton.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                PopupMenu filterPopup = new PopupMenu(MealPeriodActivity.this, findViewById(R.id.dining_filter_button));
                filterPopup.getMenuInflater().inflate(R.menu.filter_popup, filterPopup.getMenu());

                SharedPreferences filters = getSharedPreferences(FILTER_PREFERENCES_NAME, 0);

                for (int i = 0; i < filterPopup.getMenu().size(); i++) {
                    MenuItem menuItem = filterPopup.getMenu().getItem(i);
                    if (filters.getBoolean(menuItem.getTitle().toString(), false)) {
                        menuItem.setChecked(true);
                    }
                }

                filterPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        item.setChecked(!item.isChecked());

                        // Keep the popup menu open
                        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
                        item.setActionView(new View(getBaseContext()));

                        toggleDescriptors(item.getTitle().toString(), item.isChecked());

                        return false;
                    }
                });
                filterPopup.show();
                return true;
            }
        });

        return true;
    }

    public void toggleDescriptors(String descriptor, boolean toggle) {
        SharedPreferences filters = getSharedPreferences(FILTER_PREFERENCES_NAME, 0);
        SharedPreferences.Editor editor = filters.edit();

        editor.putBoolean(descriptor, toggle);
        editor.apply();

        filterMenuItems();

        updateRecyclerView();
    }

    public void filterMenuItems() {
        menuItems.clear();
        sections.clear();
        menuItems.addAll(originalMenuItems);

        SharedPreferences filters = getSharedPreferences(FILTER_PREFERENCES_NAME, 0);

        // remove items that have been filtered out
        for (MealItem mealItem : originalMenuItems) {
            if (filters.getBoolean(vegan, false) && !mealItem.getDescriptors().contains(vegan)) {
                menuItems.remove(mealItem);
            }
            if (filters.getBoolean(vegetarian, false) && !mealItem.getDescriptors().contains(vegetarian)) {
                menuItems.remove(mealItem);
            }
            if (filters.getBoolean(no_nuts, false) && mealItem.getDescriptors().contains(nuts)) {
                menuItems.remove(mealItem);
            }
            if (filters.getBoolean(no_dairy, false) && mealItem.getDescriptors().contains(dairy)) {
                menuItems.remove(mealItem);
            }
            if (filters.getBoolean(no_eggs, false) && mealItem.getDescriptors().contains(eggs)) {
                menuItems.remove(mealItem);
            }
            if (filters.getBoolean(no_wheat, false) && mealItem.getDescriptors().contains(wheat)) {
                menuItems.remove(mealItem);
            }
            if (filters.getBoolean(no_soy, false) && mealItem.getDescriptors().contains(soy)) {
                menuItems.remove(mealItem);
            }
        }

        if (menuItems.size() > 0) {
            // update sections after items have been filtered out
            String hall = "";
            for (int i = 0; i < menuItems.size(); i++) {
                MealItem mealItem = menuItems.get(i);
                if (!mealItem.getHall().equals(hall)) {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(i, mealItem.getHall()));
                    hall = mealItem.getHall();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }
}
