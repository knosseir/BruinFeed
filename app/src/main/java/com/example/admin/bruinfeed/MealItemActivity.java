package com.example.admin.bruinfeed;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MealItemActivity extends AppCompatActivity {

    String name, description, url;
    MealItem test;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_item);

        name = getIntent().getStringExtra("Name");
        description = getIntent().getStringExtra("Description");
        url = getIntent().getStringExtra("url");

        setTitle(name);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(url);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.meal_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorite:
                addFavorite(new MealItem(name, description, url));
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    public void addFavorite(MealItem mealItem) {
        DatabaseHandler db = new DatabaseHandler(this);
        db.addMealItem(mealItem);
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