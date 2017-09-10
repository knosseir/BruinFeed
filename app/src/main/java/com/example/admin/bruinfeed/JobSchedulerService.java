package com.example.admin.bruinfeed;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * JobService to be scheduled by the JobScheduler.
 */

public class JobSchedulerService extends JobService {
    private static final String TAG = "SyncService";
    private static final String url = "http://menu.dining.ucla.edu/Menus";

    private ArrayList<String> diningHallNames = new ArrayList<>();
    private DatabaseHandler db = new DatabaseHandler(this);

    @Override
    public boolean onStartJob(final JobParameters params) {
        Log.d(TAG, "Job Service running");

        // clear out old database entries to save space
        db.drop();

        AsyncTaskRunner runner = new AsyncTaskRunner() {
            @Override
            protected void onPostExecute(Boolean success) {
                Log.d(TAG, "Job service completed with status: " + success);
                jobFinished(params, !success);
            }
        };
        runner.execute(url);
        return true;
    }

    @Override
    public boolean onStopJob(final JobParameters params) {
        // onStopJob() is called only if system stops job before it completes
        // clear database to prevent any data corruption issues
        db.drop();
        return false;
    }

    public class AsyncTaskRunner extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            try {
                // connect to url, set 10 second timeout in case internet connection is slow
                Document doc = Jsoup.connect(params[0]).timeout(10 * 1000).get();

                Elements diningHalls = doc.select("h3");
                for (Element element : diningHalls)
                    diningHallNames.add(element.ownText());

                // remove duplicates from diningHallNames ArrayList
                Set<String> diningHallTemp = new LinkedHashSet<>(diningHallNames);
                diningHallNames.clear();
                diningHallNames.addAll(diningHallTemp);

                // get meals from all dining halls
                for (String diningHall : diningHallNames) {
                    getMeals(diningHall);
                }

            } catch (Exception e) {
                // TODO: send error to developer, maybe?
                Log.e(TAG, e.toString());
                return false;
            }
            return true;
        }

        public boolean getMeals(String diningHall) {
            try {
                String[] meals = {"Breakfast", "Lunch", "Dinner"};

                Calendar calendar = Calendar.getInstance();

                for (int i = 0; i < 6; i++) {
                    Date date = calendar.getTime();
                    String dateString = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);

                    for (String meal : meals) {
                        Document doc = Jsoup.connect("http://menu.dining.ucla.edu/Menus/" + diningHall + "/" + dateString + "/" + meal).timeout(10 * 1000).get();
                        Elements links = doc.select("a.recipeLink, li.sect-item");

                        String section = "";

                        for (Element link : links) {
                            if (link.tagName().equals("a")) {
                                String description;
                                String descriptors = "";
                                Element parent = link.parent().parent();
                                if (parent != null) {
                                    Elements descriptionElement = parent.select("div.tt-description");
                                    Elements descriptorElement = parent.select("div.tt-prodwebcode");
                                    String mealUrl = link.attr("href");
                                    if (descriptionElement.size() > 0 && descriptionElement.get(0) != null) {
                                        description = parent.select("div.tt-description").text();
                                    } else {
                                        description = "No description available";
                                    }

                                    if (descriptorElement.size() > 0 && descriptorElement.get(0) != null) {
                                        for (Element e : descriptorElement) {
                                            descriptors += (e.ownText()) + ", ";
                                        }
                                    }

                                    // remove ending comma and any leading or ending spaces
                                    if (descriptors.length() > 0 && descriptors.charAt(descriptors.length() - 2) == ',') {
                                        descriptors = descriptors.substring(0, descriptors.length() - 2).trim();
                                    }

                                    // add meal item to local SQLite database for future access
                                    db.addMealItem(new MealItem(link.ownText(), description, mealUrl, diningHall, meal, section, descriptors, dateString));
                                }
                            } else if (link.tagName().equals("li")) {
                                section = link.ownText();
                            }
                        }
                    }
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

            } catch (Exception e) {
                // TODO: send error to developer, maybe?
                Log.e(TAG, e.toString());
                return false;
            }
            return true;
        }
    }
}