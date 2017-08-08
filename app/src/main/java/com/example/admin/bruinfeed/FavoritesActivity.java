package com.example.admin.bruinfeed;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    DatabaseHandler db = new DatabaseHandler(this);
    List<MealItem> meals;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);
        setTitle("Favorites");

        meals =  db.getAllMealItems();
        displayFavorites(meals);
    }

    @Override
    public void onResume() {
        super.onResume();

        // update favorites list in case user added or removed meals from it
        meals =  db.getAllMealItems();
        displayFavorites(meals);
    }

    public void displayFavorites(List<MealItem> meals) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.favoritesRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        SimpleAdapter mAdapter = new SimpleAdapter(getBaseContext(), meals);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
    }
}
