package com.knosseir.admin.bruinfeed;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class MealItemActivity extends AppCompatActivity {

    private static final String MealItemTag = "MealItemActivity";
    public static final String FAVORITE_PREFERENCES_NAME = "FavPrefs";

    String name, description, url, hall, meal, section, descriptors;
    boolean favorite;
    MealItem selectedItem;
    DatabaseHandler db;
    SharedPreferences favSettings;
    SharedPreferences.Editor editor;
    Elements nutritionFactElements;
    Element servingSize, calories, caloriesFromFat, vitaminsElement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_item);

        db = new DatabaseHandler(this);

        // retrieve information about the selected meal item
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

        Spannable text = new SpannableString(name);
        text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        setTitle(text);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);

        // retrieve nutrition facts information asynchronously
        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(url);

        List<MealItem> foundAtList = new ArrayList<>();

        for (MealItem meal : db.getAllMealItems()) {
            if (meal.getName().equals(name)) {
                foundAtList.add(meal);
            }
        }

        String foundAtLabel = name + " can be found at: ";
        String foundAtValue = "";

        for (MealItem meal : foundAtList) {
            foundAtValue += meal.getHall() + " for " + meal.getMeal() + " on " + meal.getDate() + '\n';
        }

        ((TextView) findViewById(R.id.found_at_label)).setText(foundAtLabel);
        ((TextView) findViewById(R.id.found_at_list)).setText(foundAtValue);

        String descriptorText = "More information about " + name + ":";

        ((TextView) findViewById(R.id.descriptor_label)).setText(descriptorText);
        ((TextView) findViewById(R.id.descriptor_list)).setText(descriptors);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.meal_item, menu);

        MenuItem fav_button = menu.findItem(R.id.action_favorite);

        if (favorite) {  // if item is a user favorite, set favorite icon as already enabled
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
                    Snackbar.make(findViewById(R.id.meal_item_scroll_view), R.string.favorites_add, Snackbar.LENGTH_SHORT).show();
                    item.setEnabled(true);
                    item.setIcon(R.drawable.ic_star_black_24dp);
                    favorite = true;
                } else {
                    Snackbar.make(findViewById(R.id.meal_item_scroll_view), R.string.favorites_remove, Snackbar.LENGTH_SHORT).show();
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
            String ingredAllergensText = "";

            try {
                Document document = Jsoup.connect(params[0]).timeout(10 * 1000).get();

                nutritionFactElements = document.select("div.nfbox");
                servingSize = document.select("p.nfserv").first();
                calories = document.select("p.nfcal").first();
                caloriesFromFat = document.select("span.nffatcal").first();
                vitaminsElement = document.select("div.nfvitbar").first();

                Element ingredAllergensElement = document.select("div.ingred_allergen").get(0);

                String[] ingredAllergens = new String[ingredAllergensElement.children().size()];

                for (int i = 0; i < ingredAllergensElement.children().size(); i++) {
                    ingredAllergens[i] = ingredAllergensElement.child(i).ownText();
                }

                if (ingredAllergens.length == 2) {
                    ingredAllergensText = "<b> Ingredients: </b>" + ingredAllergens[0] + "<br /> <br />" + "<b> Allergens: </b>" + ingredAllergens[1];
                } else if (ingredAllergens.length == 1) {
                    ingredAllergensText = "<b> Ingredients: </b>" + ingredAllergens[0] + "<br /> <br />" + "<b> Allergens: </b> None";
                }

            } catch (Exception e) {
                Log.e(MealItemTag, e.toString());
                Snackbar.make(findViewById(R.id.meal_item_scroll_view), R.string.no_information, Snackbar.LENGTH_INDEFINITE).show();
                return null;
            }
            return ingredAllergensText;
        }

        @Override
        protected void onPostExecute(String ingredAllergensText) {
            if (ingredAllergensText == null) return;

            ((TextView) findViewById(R.id.serving_size_value)).setText(servingSize.ownText().substring(servingSize.ownText().lastIndexOf(" ") + 1));
            ((TextView) findViewById(R.id.calories_value)).setText(calories.ownText().substring(calories.ownText().lastIndexOf(" ") + 1));
            ((TextView) findViewById(R.id.fat_calories_value)).setText(caloriesFromFat.ownText().substring(caloriesFromFat.ownText().lastIndexOf(" ") + 1));

            for (Element element : nutritionFactElements.select("p.nfnutrient")) {
                if (element.ownText().contains("Calories")) {
                    ((TextView) findViewById(R.id.calories_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                } else if (element.text().contains("Total Fat")) {
                    ((TextView) findViewById(R.id.total_fat_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                    ((TextView) findViewById(R.id.total_fat_daily_percentage_value)).setText(element.select("span.nfdvvalnum").text() + "%");
                } else if (element.ownText().contains("Saturated Fat")) {
                    ((TextView) findViewById(R.id.saturated_fat_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                    ((TextView) findViewById(R.id.saturated_fat_daily_percentage_value)).setText(element.select("span.nfdvvalnum").text() + "%");
                } else if (element.ownText().contains("Trans Fat")) {
                    ((TextView) findViewById(R.id.trans_fat_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                } else if (element.text().contains("Cholesterol")) {
                    ((TextView) findViewById(R.id.cholesterol_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                    ((TextView) findViewById(R.id.cholesterol_daily_percentage_value)).setText(element.select("span.nfdvvalnum").text() + "%");
                } else if (element.text().contains("Sodium")) {
                    ((TextView) findViewById(R.id.sodium_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                    ((TextView) findViewById(R.id.sodium_daily_percentage_value)).setText(element.select("span.nfdvvalnum").text() + "%");
                } else if (element.text().contains("Total Carbohydrate")) {
                    ((TextView) findViewById(R.id.total_carbohydrate_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                    ((TextView) findViewById(R.id.total_carbohydrate_daily_percentage_value)).setText(element.select("span.nfdvvalnum").text() + "%");
                } else if (element.ownText().contains("Dietary Fiber")) {
                    ((TextView) findViewById(R.id.dietary_fiber_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                    ((TextView) findViewById(R.id.dietary_fiber_daily_percentage_value)).setText(element.select("span.nfdvvalnum").text() + "%");
                } else if (element.ownText().contains("Sugars")) {
                    ((TextView) findViewById(R.id.sugars_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                } else if (element.text().contains("Protein")) {
                    ((TextView) findViewById(R.id.protein_value)).setText(element.ownText().substring(element.ownText().lastIndexOf(" ") + 1));
                }
            }

            for (Element element : nutritionFactElements.select("div.nfvit")) {
                Element left = element.select("span.nfvitleft").first();
                Element right = element.select("span.nfvitright").first();

                if (left.text().contains("Vitamin A")) {
                    ((TextView) findViewById(R.id.vitamin_a_value)).setText(left.select("span.nfvitpct").first().ownText());
                } else if (left.text().contains("Calcium")) {
                    ((TextView) findViewById(R.id.calcium_value)).setText(left.select("span.nfvitpct").first().ownText());
                }
                if (right.text().contains("Vitamin C")) {
                    ((TextView) findViewById(R.id.vitamin_c_value)).setText(right.select("span.nfvitpct").first().ownText());
                } else if (right.text().contains("Iron")) {
                    ((TextView) findViewById(R.id.iron_value)).setText(right.select("span.nfvitpct").first().ownText());
                }

            }

            ((TextView) findViewById(R.id.ingred_allergen_list)).setText(Html.fromHtml(ingredAllergensText));
        }
    }
}