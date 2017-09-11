package com.example.admin.bruinfeed;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    public static final String FAVORITE_PREFERENCES_NAME = "FavPrefs";

    DatabaseHandler db = new DatabaseHandler(this);
    List<MealItem> allMeals = new ArrayList<>();
    List<MealItem> favMeals = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);

        Spannable text = new SpannableString("Favorites");
        text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        setTitle(text);

        allMeals = db.getAllMealItems();
        getFavorites(allMeals);
    }

    @Override
    public void onResume() {
        super.onResume();

        // update favorites list in case user added or removed meals from it
        favMeals.clear();
        getFavorites(allMeals);
    }

    public void getFavorites(List<MealItem> meals) {
        SharedPreferences favSettings = getSharedPreferences(FAVORITE_PREFERENCES_NAME, 0);

        for (MealItem meal : meals) {
            if (favSettings.getBoolean(meal.getName(), false) && !favMeals.contains(meal)) {
                favMeals.add(meal);
            }
        }

        // check for empty favorites list
        if (favMeals.isEmpty()) {
            Snackbar.make(findViewById(R.id.favoritesRecyclerView), R.string.empty_favorites, Snackbar.LENGTH_INDEFINITE).show();
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favoritesRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        SimpleAdapter mAdapter = new SimpleAdapter(getBaseContext(), favMeals);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }
}
