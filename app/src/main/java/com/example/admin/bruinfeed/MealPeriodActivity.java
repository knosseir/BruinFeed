package com.example.admin.bruinfeed;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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
        setTitle(selectedMeal);     // TODO: DESCRIPTORS DON'T APPEAR BECAUSE MAINACTIVITY DOES NOT HAVE ARRAYLIST OF DESCRIPTORS TO PASS THROUGH INTENT

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

        // Add adapter to the sectionAdapter
        SimpleSectionedRecyclerViewAdapter.Section[] array = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter = new SimpleSectionedRecyclerViewAdapter(getBaseContext(), R.layout.section, R.id.section_text, mAdapter);
        mSectionedAdapter.setSections(sections.toArray(array));

        // Update RecyclerView
        recyclerView.setAdapter(mSectionedAdapter);
        mAdapter.notifyDataSetChanged();
    }
}
