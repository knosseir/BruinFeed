package com.example.admin.bruinfeed;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
import java.util.ArrayList;
import java.util.List;

public class MealItemActivity extends AppCompatActivity {

    private static final String MealItemTag = "MealItemActivity";

    String name, description, url, hall, meal, section, descriptors;
    boolean favorite;
    MealItem selectedItem;
    DatabaseHandler db;
    SharedPreferences favSettings;
    SharedPreferences.Editor editor;

    public static final String FAVORITE_PREFERENCES_NAME = "FavPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_item);

        db = new DatabaseHandler(this);

        selectedItem = getIntent().getParcelableExtra("MealItem");
        name = selectedItem.getName();

        description = selectedItem.getDescription();
        url = selectedItem.getUrl();
        hall = selectedItem.getHall();
        meal = selectedItem.getMeal();
        section = selectedItem.getSection();
        descriptors = selectedItem.getDescriptors();

        favSettings = getSharedPreferences(FAVORITE_PREFERENCES_NAME, 0);
        editor = favSettings.edit();

        favorite = favSettings.getBoolean(selectedItem.getName(), false);

        setTitle(name);

        TextView foundAt = (TextView) findViewById(R.id.found_at_list);

        List<MealItem> foundAtList = new ArrayList<>();

        for (MealItem meal : db.getAllMealItems()) {
            if (meal.getName().equals(name)) {
                    foundAtList.add(meal);
            }
        }

        String foundAtText = name + " can be found at: " + '\n';
        for (MealItem meal : foundAtList) {
            foundAtText += meal.getHall() + " for " + meal.getMeal() + '\n';
        }
        foundAt.setText(foundAtText);

        String descriptorText = "More information about " + name + ":" + '\n' + descriptors;

        TextView descriptorList = (TextView) findViewById(R.id.descriptor_list);

        descriptorList.setText(descriptorText);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.meal_item, menu);

        MenuItem fav_button = menu.findItem(R.id.action_favorite);

        if (favorite) {  // item is a favorite
            fav_button.setIcon(getResources().getDrawable(R.drawable.ic_star_black_24dp));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                // TODO: ONLY WORKS TWICE

                if (toggleFavorite(selectedItem)) {
                    Toast.makeText(getBaseContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                    item.setEnabled(true);
                    item.setIcon(R.drawable.ic_star_black_24dp);
                    favorite = true;
                } else {
                    Toast.makeText(getBaseContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                    item.setEnabled(false);
                    item.setIcon(R.drawable.ic_star_border_black_24dp);
                    favorite = false;
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean toggleFavorite(MealItem mealItem) {
        if (favorite) {
            editor.putBoolean(mealItem.getName(), false);    // item is already a favorite, so unfavorite it
            editor.commit();
            return false;
        } else {
            editor.putBoolean(mealItem.getName(), true);    // item is not already a favorite, so mark it as a favorite
            editor.commit();
            return true;
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {
            String nutritionFactsHtml = "";

            try {
                Document document = Jsoup.connect(params[0]).timeout(10 * 1000).get();

                Elements nutritionFactElements = document.select("div.nfbox");

                if (nutritionFactElements.size() > 0 && nutritionFactElements.get(0) != null) {
                    nutritionFactsHtml = nutritionFactElements.get(0).toString();
                } else {
                    // TODO: handle case with no nutrition facts available
                }

            } catch (IOException e) {
                Log.e(MealItemTag, e.toString());
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