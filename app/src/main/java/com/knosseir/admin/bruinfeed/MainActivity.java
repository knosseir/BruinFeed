package com.knosseir.admin.bruinfeed;

import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import io.fabric.sdk.android.Fabric;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String MainTag = "MainActivity";
    private static final String url = "http://menu.dining.ucla.edu/Menus";

    private ArrayList<String> diningHallNames = new ArrayList<>();
    private Map<String, Integer> activityLevelMap = new HashMap<>();

    private Map<String, Calendar> breakfastOpeningHours = new HashMap<>();
    private Map<String, Calendar> breakfastClosingHours = new HashMap<>();

    private Map<String, Calendar> lunchOpeningHours = new HashMap<>();
    private Map<String, Calendar> lunchClosingHours = new HashMap<>();

    private Map<String, Calendar> dinnerOpeningHours = new HashMap<>();
    private Map<String, Calendar> dinnerClosingHours = new HashMap<>();

    private SwipeRefreshLayout diningHallRefresh;
    ProgressDialog progress;

    Calendar currentTime = Calendar.getInstance();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private DatabaseHandler db = new DatabaseHandler(this);
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        diningHallRefresh = (SwipeRefreshLayout) findViewById(R.id.diningHallRefresh);
        setSupportActionBar(toolbar);

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    final Intent i = new Intent(MainActivity.this, IntroActivity.class);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(i);
                        }
                    });

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
            }
        });

        // Start the thread
        t.start();

        toolbar.setTitleTextColor(getResources().getColor(R.color.black));

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "App Open");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);

        recyclerView = (RecyclerView) findViewById(R.id.diningHallRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DiningHallAdapter(diningHallNames);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        // display loading spinner on initial boot up
        progress = new ProgressDialog(MainActivity.this);
        progress.setTitle("Loading...");
        progress.setMessage(getResources().getString(R.string.initial_boot_load_message));
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        // create JobScheduler to download and update future meal data every day
        // this will prevent long loading times in the future
        JobScheduler scheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(getPackageName(), JobSchedulerService.class.getName()));
        builder.setMinimumLatency(TimeUnit.HOURS.toMillis(1))   // job will happen a minimum of one hour after app has been launched
                .setOverrideDeadline(TimeUnit.DAYS.toMillis(1)) // job will override all other requirements if it has not been run for a day
                .setPersisted(true)     // persist job after device reboot
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)   // job will execute on any type of network connection
                .setRequiresDeviceIdle(true)   // job will execute only when device is idle to avoid modifying database while app is running
                .setRequiresCharging(false);   // job will execute whether or not device is charging due to low CPU/RAM footprint

        // builder.build() will return <= 0 if there was an issue in starting the job
        if (scheduler.schedule(builder.build()) <= 0) {
            Log.e(MainTag, "Job failed to start");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!isOnline()) {
            reload(R.string.no_internet);
            return;
        }

        updateDatabase();

        diningHallRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        // refreshes dining hall grid upon pull to refresh
        diningHallRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // determine whether or not new data must be loaded
                        updateDatabase();

                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "diningHallRefresh");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = new Intent(getBaseContext(), MealPeriodActivity.class);

        if (id == R.id.nav_breakfast) {
            intent.putExtra("selectedMeal", "Breakfast");

        } else if (id == R.id.nav_lunch) {
            intent.putExtra("selectedMeal", "Lunch");

        } else if (id == R.id.nav_dinner) {
            intent.putExtra("selectedMeal", "Dinner");

        } else if (id == R.id.nav_favorites) {
            intent = new Intent(getBaseContext(), FavoritesActivity.class);
        }

        startActivity(intent);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void updateDatabase() {
        String dateRange = getCachedDateRange(db);
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String currentDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date);

        AsyncTaskRunner runner = new AsyncTaskRunner();

        if (!dateRange.contains(currentDateString)) {
            db.clear();
            // show uncancellable progress bar while initial data load occurs
            progress.show();
            runner.execute(url, "true");
        } else {
            runner.execute(url, "false");
            diningHallRefresh.setRefreshing(true);
        }
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, Void> {

        @Override
        protected Void doInBackground(String... params) {
            try {
                // check for internet connectivity
                if (!isOnline()) {
                    reload(R.string.no_internet);
                    return null;
                }

                // connect to url, set 10 second timeout in case internet connection is slow
                Document doc = Jsoup.connect(params[0]).timeout(10 * 1000).get();

                if (params[1].equals("true")) {
                    Elements diningHalls = doc.select("h3");

                    diningHallNames.clear();

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
                } else {
                    diningHallNames.clear();

                    List<MealItem> allMealItems = db.getAllMealItems();

                    String diningHall = "";

                    for (MealItem mealItem : allMealItems) {
                        if (!mealItem.getHall().equals(diningHall)) {
                            diningHallNames.add(mealItem.getHall());
                            diningHall = mealItem.getHall();
                        }
                    }
                }

                getHours();

                getActivityLevels(doc);

                updateRecyclerView();

            } catch (SocketTimeoutException e) {
                Log.e(MainTag, e.toString());
                updateRecyclerView();
                reload(R.string.connection_timeout);
                return null;
            } catch (IOException | IllegalArgumentException e) {
                Log.e(MainTag, e.toString());
                updateRecyclerView();
                reload(R.string.retry_connection);
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progress.dismiss();
        }

        public boolean getMeals(String diningHall) {
            try {
                String[] meals = {"Breakfast", "Lunch", "Dinner"};

                Calendar calendar = Calendar.getInstance();

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

            } catch (SocketTimeoutException e) {
                Log.e(MainTag, e.toString());
                reload(R.string.connection_timeout);
                return false;
            } catch (IOException | IllegalArgumentException e) {
                Log.e(MainTag, e.toString());
                reload(R.string.retry_connection);
                return false;
            }
            return true;
        }

        public boolean getActivityLevels(Document doc) {
            Elements levels = doc.getElementsMatchingOwnText("Activity Level");

            for (int i = 0; i < levels.size(); i++) {
                activityLevelMap.put(diningHallNames.get(i), Integer.parseInt(levels.get(i).parent().ownText().replaceAll("[: %]", "")));
            }

            return true;
        }

        public boolean getHours() {
            try {
                Document hoursDoc = Jsoup.connect("http://menu.dining.ucla.edu/Hours").timeout(10 * 1000).get();
                Elements elements = hoursDoc.select("td.hours-head");
                Elements parents = new Elements();

                for (Element e : elements) {
                    parents.add(e.parent());
                }

                for (Element e : parents) {
                    String diningHall = e.select("span.hours-location").text();

                    Elements elems = e.select("span.hours-range");

                    for (Element f : elems) {
                        String meal = f.parent().className().replace("hours-open ", "");
                        String range = f.text();

                        String[] s = range.split(" - ");
                        Calendar openTime = Calendar.getInstance();
                        Calendar closeTime = Calendar.getInstance();

                        int year = currentTime.get(Calendar.YEAR);
                        int month = currentTime.get(Calendar.MONTH);
                        int day = currentTime.get(Calendar.DAY_OF_MONTH);

                        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");

                        Date openDate = dateFormat.parse(s[0]), closeDate = dateFormat.parse(s[1]);

                        int openHour = openDate.getHours(), closeHour = closeDate.getHours();
                        int openMinute = openDate.getMinutes(), closeMinute = closeDate.getMinutes();

                        openTime.set(year, month, day, openHour, openMinute);
                        closeTime.set(year, month, day, closeHour, closeMinute);

                        switch (meal) {
                            case ("Breakfast"):
                                breakfastOpeningHours.put(diningHall, openTime);
                                breakfastClosingHours.put(diningHall, closeTime);
                                break;
                            case ("Lunch"):
                                lunchOpeningHours.put(diningHall, openTime);
                                lunchClosingHours.put(diningHall, closeTime);
                                break;
                            case ("Dinner"):
                                dinnerOpeningHours.put(diningHall, openTime);
                                dinnerClosingHours.put(diningHall, closeTime);
                                break;
                            default:
                                return false;
                        }
                    }
                }

            } catch (IOException | ParseException e) {
                Log.e(MainTag, e.toString());
            }

            return true;
        }
    }

    public void updateRecyclerView() {
        // update dining hall RecyclerView with list of dining halls on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                diningHallRefresh.setRefreshing(false);
            }
        });
    }

    public String getCachedDateRange(DatabaseHandler db) {
        List<MealItem> allIMealtems = db.getAllMealItems();
        String dateRange = "";

        for (MealItem mealItem : allIMealtems) {
            if (!dateRange.contains(mealItem.getDate())) {
                dateRange += mealItem.getDate() + " ";
            }
        }
        return dateRange;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public void reload(int error) {
        Snackbar reloadSnackbar = Snackbar.make(findViewById(R.id.diningHallRecyclerView), error, Snackbar.LENGTH_INDEFINITE);
        reloadSnackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDatabase();
            }
        });
        reloadSnackbar.show();
    }

    public class DiningHallAdapter extends RecyclerView.Adapter<DiningHallAdapter.ViewHolder> {
        private List<String> values;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView header;
            TextView footer;
            View diningHallLayout;
            ImageButton infoButton;

            public ViewHolder(View v) {
                super(v);
                diningHallLayout = v;
                header = (TextView) v.findViewById(R.id.firstLine);
                footer = (TextView) v.findViewById(R.id.secondLine);
                infoButton = (ImageButton) v.findViewById(R.id.dining_hall_map_button);
            }
        }

        public void add(int position, String item) {
            values.add(position, item);
            notifyItemInserted(position);
        }

        public void remove(int position) {
            values.remove(position);
            notifyItemRemoved(position);
        }

        public String getItemAtPosition(int position) {
            return values.get(position);
        }

        public DiningHallAdapter(List<String> data) {
            values = data;
        }

        @Override
        public DiningHallAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            LayoutInflater inflater = LayoutInflater.from(
                    parent.getContext());
            View v = inflater.inflate(R.layout.dining_hall_row, parent, false);
            // set the view's size, margins, paddings and layout parameters
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            // get element from data set at this position
            // replace the contents of the view with that element
            try {
                final String diningHall = values.get(position);
                holder.header.setText(diningHall);

                View.OnClickListener diningHallListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object obj = getItemAtPosition(position);
                        Intent diningHallMenuIntent = new Intent(getBaseContext(), DiningHallActivity.class);
                        diningHallMenuIntent.putExtra("SelectedDiningHall", obj.toString());
                        diningHallMenuIntent.putExtra("ActivityLevel", activityLevelMap.get(obj.toString()));
                        startActivity(diningHallMenuIntent);
                    }
                };

                ((View) holder.header.getParent()).setOnClickListener(diningHallListener);
                ((View) holder.footer.getParent()).setOnClickListener(diningHallListener);

                holder.infoButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Object obj = getItemAtPosition(position);
                        Intent diningHallInfoIntent = new Intent(getBaseContext(), DiningHallInfoActivity.class);
                        diningHallInfoIntent.putExtra("SelectedDiningHall", obj.toString());
                        startActivity(diningHallInfoIntent);
                    }
                });

                Calendar cal = Calendar.getInstance();
                int currentHour = cal.get(Calendar.HOUR_OF_DAY);
                int currentMinute = cal.get(Calendar.MINUTE);

                int breakfastOpen = breakfastOpeningHours.get(diningHall).get(Calendar.HOUR_OF_DAY);
                int lunchOpen = lunchOpeningHours.get(diningHall).get(Calendar.HOUR_OF_DAY);
                int dinnerOpen = dinnerOpeningHours.get(diningHall).get(Calendar.HOUR_OF_DAY);

                int breakfastClose = breakfastClosingHours.get(diningHall).get(Calendar.HOUR_OF_DAY);
                int lunchClose = lunchClosingHours.get(diningHall).get(Calendar.HOUR_OF_DAY);
                int dinnerClose = dinnerClosingHours.get(diningHall).get(Calendar.HOUR_OF_DAY);

                holder.footer.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.Open));

                // dining hall hasn't opened for breakfast yet
                if (currentHour < breakfastOpen ||
                        (currentHour <= breakfastOpen && currentMinute < breakfastOpeningHours.get(diningHall).get(Calendar.MINUTE))) {
                    Calendar breakfastOpenCal = breakfastOpeningHours.get(diningHall);
                    String period = (breakfastOpenCal.get(Calendar.AM_PM) == 0) ? "AM" : "PM";
                    String minute = (breakfastOpenCal.get(Calendar.MINUTE) == 0) ? "00" : Integer.toString(breakfastOpenCal.get(Calendar.MINUTE));
                    holder.footer.setText("Opening for breakfast at " + breakfastOpenCal.get(Calendar.HOUR) + ":" + minute + " " + period);
                    holder.footer.setTextColor(Color.RED);
                }
                // dining hall is currently in breakfast phase
                else if (currentHour < breakfastClose ||
                        currentHour <= breakfastClose && currentMinute < breakfastClosingHours.get(diningHall).get(Calendar.MINUTE)) {
                    Calendar breakfastCloseCal = breakfastClosingHours.get(diningHall);
                    String period = (breakfastCloseCal.get(Calendar.AM_PM) == 0) ? "AM" : "PM";
                    String minute = (breakfastCloseCal.get(Calendar.MINUTE) == 0) ? "00" : Integer.toString(breakfastCloseCal.get(Calendar.MINUTE));
                    holder.footer.setText("Open for breakfast until " + breakfastCloseCal.get(Calendar.HOUR) + ":" + minute + " " + period);
                }
                // dining hall is between breakfast and lunch phases
                else if (currentHour < lunchOpen ||
                        currentHour <= lunchOpen && currentMinute < lunchOpeningHours.get(diningHall).get(Calendar.MINUTE)) {
                    Calendar lunchOpenCal = lunchOpeningHours.get(diningHall);
                    String period = (lunchOpenCal.get(Calendar.AM_PM) == 0) ? "AM" : "PM";
                    String minute = (lunchOpenCal.get(Calendar.MINUTE) == 0) ? "00" : Integer.toString(lunchOpenCal.get(Calendar.MINUTE));
                    holder.footer.setText("Opening for lunch at " + lunchOpenCal.get(Calendar.HOUR) + ":" + minute + " " + period);
                    holder.footer.setTextColor(Color.RED);
                }
                // dining hall is currently in lunch phase
                else if (currentHour < lunchClose ||
                        currentHour <= lunchClose && currentMinute < lunchClosingHours.get(diningHall).get(Calendar.MINUTE)) {
                    Calendar lunchCloseCal = lunchClosingHours.get(diningHall);
                    String period = (lunchCloseCal.get(Calendar.AM_PM) == 0) ? "AM" : "PM";
                    String minute = (lunchCloseCal.get(Calendar.MINUTE) == 0) ? "00" : Integer.toString(lunchCloseCal.get(Calendar.MINUTE));
                    holder.footer.setText("Open for lunch until " + lunchCloseCal.get(Calendar.HOUR) + ":" + minute + " " + period);
                }
                // dining hall is between lunch and dinner phases
                else if (currentHour < dinnerOpen ||
                        currentHour <= dinnerOpen && currentMinute < dinnerOpeningHours.get(diningHall).get(Calendar.MINUTE)) {
                    Calendar dinnerOpenCal = dinnerOpeningHours.get(diningHall);
                    String period = (dinnerOpenCal.get(Calendar.AM_PM) == 0) ? "AM" : "PM";
                    String minute = (dinnerOpenCal.get(Calendar.MINUTE) == 0) ? "00" : Integer.toString(dinnerOpenCal.get(Calendar.MINUTE));
                    holder.footer.setText("Opening for dinner at " + dinnerOpenCal.get(Calendar.HOUR) + ":" + minute + " " + period);
                    holder.footer.setTextColor(Color.RED);
                }
                // dining hall is currently in dinner phase
                else if (currentHour < dinnerClose ||
                        currentHour <= dinnerClose && currentMinute < dinnerClosingHours.get(diningHall).get(Calendar.MINUTE)) {
                    Calendar dinnerCloseCal = dinnerClosingHours.get(diningHall);
                    String period = (dinnerCloseCal.get(Calendar.AM_PM) == 0) ? "AM" : "PM";
                    String minute = (dinnerCloseCal.get(Calendar.MINUTE) == 0) ? "00" : Integer.toString(dinnerCloseCal.get(Calendar.MINUTE));
                    holder.footer.setText("Open for dinner until " + dinnerCloseCal.get(Calendar.HOUR) + ":" + minute + " " + period);
                }
                // dining hall has closed for dinner
                else if (currentHour > dinnerClose ||
                        currentHour >= dinnerClose && currentMinute > dinnerClosingHours.get(diningHall).get(Calendar.MINUTE)) {
                    Calendar dinnerCloseCal = dinnerClosingHours.get(diningHall);
                    String period = (dinnerCloseCal.get(Calendar.AM_PM) == 0) ? "AM" : "PM";
                    String minute = (dinnerCloseCal.get(Calendar.MINUTE) == 0) ? "00" : Integer.toString(dinnerCloseCal.get(Calendar.MINUTE));
                    holder.footer.setText("Closed for tonight at " + dinnerCloseCal.get(Calendar.HOUR) + ":" + minute + " " + period);
                    holder.footer.setTextColor(Color.RED);
                }
            } catch (Exception e) {
                Log.e(MainTag, e.toString());
            }
        }

        @Override
        public int getItemCount() {
            return values.size();
        }
    }
}
