package com.example.admin.bruinfeed;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.miguelcatalan.materialsearchview.MaterialSearchView;

import java.util.ArrayList;
import java.util.List;

public class MealPeriodActivity extends AppCompatActivity {

    String selectedMeal;
    List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();
    List<MealItem> menuItems = new ArrayList<>();
    List<MealItem> allItems = new ArrayList<>();

    private RecyclerView recyclerView;
    private SimpleAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    SimpleSectionedRecyclerViewAdapter mSectionedAdapter;

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

        DatabaseHandler db = new DatabaseHandler(this);
        allItems = db.getAllMealItems();

        String diningHall = allItems.get(0).getHall();
        sections.add(new SimpleSectionedRecyclerViewAdapter.Section(0, diningHall));

        // TODO: ADD SECTIONS FROM EACH DINING HALL
        for (MealItem mealItem : allItems) {
            if (mealItem.getMeal().equals(selectedMeal)) {
                menuItems.add(mealItem);
                if (!mealItem.getHall().equals(diningHall)) {
                    sections.add(new SimpleSectionedRecyclerViewAdapter.Section(menuItems.size() - 1, mealItem.getHall()));
                    diningHall = mealItem.getHall();
                }
            }
        }

        final List<MealItem> search = new ArrayList<>(menuItems);

        // Add adapter to the sectionAdapter
        SimpleSectionedRecyclerViewAdapter.Section[] array = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter = new SimpleSectionedRecyclerViewAdapter(getBaseContext(), R.layout.section, R.id.section_text, mAdapter);
        //mSectionedAdapter.setSections(sections.toArray(array));

        // Update RecyclerView
        recyclerView.setAdapter(mSectionedAdapter);
        mAdapter.notifyDataSetChanged();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MaterialSearchView searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                List<MealItem> queryItems = new ArrayList<>();
                for (MealItem mealItem : search) {
                    if (mealItem.getName().toLowerCase().contains(query.toLowerCase()) && !queryItems.contains(mealItem))
                        queryItems.add(mealItem);
                }

                menuItems.clear();
                sections.clear();
                menuItems.addAll(queryItems);

                mSectionedAdapter.notifyDataSetChanged();
                mAdapter.notifyDataSetChanged();

                setTitle("Search results for: " + query);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MaterialSearchView searchView = (MaterialSearchView) findViewById(R.id.search_view);

        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
    }
}
