package com.example.admin.bruinfeed;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class MealItemActivity extends AppCompatActivity {

    String name, description, url;
    MealItem selectedItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_item);

        selectedItem = getIntent().getParcelableExtra("MealItem");
        name = selectedItem.getName();
        description = selectedItem.getDescription();
        url = selectedItem.getUrl();

        setTitle(name);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.meal_item, menu);

        MenuItem fav_button = menu.findItem(R.id.action_favorite);

        DatabaseHandler db = new DatabaseHandler(this);
        List<MealItem> favoritesList = db.getAllMealItems();

        for (MealItem meal : favoritesList) {
            if (meal.getName().equals(selectedItem.getName())) {  // item is a favorite
                fav_button.setIcon(getResources().getDrawable(R.drawable.ic_star_black_24dp));
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                if (addFavorite(selectedItem)) {
                    Toast.makeText(getBaseContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                    item.setEnabled(true);
                    item.setIcon(R.drawable.ic_star_black_24dp);
                }
                else {
                    deleteFavorite(selectedItem);
                    Toast.makeText(getBaseContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    item.setEnabled(false);
                    item.setIcon(R.drawable.ic_star_border_black_24dp);
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean addFavorite(MealItem mealItem) {
        DatabaseHandler db = new DatabaseHandler(this);
        List<MealItem> meals = db.getAllMealItems();

        for (MealItem meal : meals) {
            if (meal.getName().equals(mealItem.getName())) {
                return false;   // user is attempting to add a duplicate favorite meal item
            }
        }

        db.addMealItem(mealItem);

        return true;
    }

    public boolean deleteFavorite(MealItem mealItem) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.deleteMealItem(mealItem.getName());
        return true;
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String nutritionFactsHtml = "";

            try {
                Document document = Jsoup.connect(params[0]).timeout(10 * 1000).get();

                Elements nutritionFactElements = document.select("div.nfbox");

                if (nutritionFactElements.get(0) != null && nutritionFactElements.size() > 0) {
                    nutritionFactsHtml = nutritionFactElements.get(0).toString();
                }

            } catch (IOException e) {
                Log.e("error", e.toString()); // TODO: MAKE MEALITEMACTIVITY TAG
            }
            return nutritionFactsHtml;
        }

        @Override
        protected void onPostExecute(String s) {
            TextView nutritionFacts = (TextView) findViewById(R.id.nutrition_facts);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                nutritionFacts.setText(Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT));
            } else {
                nutritionFacts.setText(Html.fromHtml(s));
            }
        }
    }
}