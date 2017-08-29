package com.example.admin.bruinfeed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

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
import java.util.Map;
import java.util.Set;

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

    Calendar currentTime = Calendar.getInstance();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private DatabaseHandler db = new DatabaseHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        diningHallRefresh = (SwipeRefreshLayout) findViewById(R.id.diningHallRefresh);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView) findViewById(R.id.diningHallRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new DiningHallAdapter(diningHallNames);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

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

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(url);

        diningHallRefresh.setColorSchemeColors(getResources().getColor(R.color.colorPrimary));

        // refreshes dining hall grid upon pull to refresh
        diningHallRefresh.setRefreshing(true);
        diningHallRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // retrieve new data upon pull to refresh
                        AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
                        asyncTaskRunner.execute(url);
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_breakfast) {

        } else if (id == R.id.nav_lunch) {

        } else if (id == R.id.nav_dinner) {

        } else if (id == R.id.nav_favorites) {
            Intent favoritesIntent = new Intent(getBaseContext(), FavoritesActivity.class);
            startActivity(favoritesIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                if (!isOnline()) {
                    reload(R.string.no_internet);
                    return "No internet connection!";
                }

                Document doc = Jsoup.connect(params[0]).timeout(10 * 1000).get();

                Elements diningHalls = doc.select("h3");
                for (Element element : diningHalls)
                    diningHallNames.add(element.ownText());

                // remove duplicates from diningHallNames ArrayList
                Set<String> diningHallTemp = new LinkedHashSet<>(diningHallNames);
                diningHallNames.clear();
                diningHallNames.addAll(diningHallTemp);

                getActivityLevels(doc);

                getHours();

                updateRecyclerView();

                for (String diningHall : diningHallNames) {
                    getMeals(diningHall);
                }

            } catch (SocketTimeoutException e) {    // TODO: CHECK NUMBER OF EXCEPTIONS OCCURRED
                Log.e(MainTag, e.toString());
                updateRecyclerView();
                reload(R.string.connection_timeout);
            } catch (IOException | IllegalArgumentException e) {
                Log.e(MainTag, e.toString());
                updateRecyclerView();
                reload(R.string.retry_connection);
            }

            return "success";
        }

        public boolean getMeals(String diningHall) {
            try {
                String[] meals = { "Breakfast", "Lunch", "Dinner" };

                for (String meal : meals) {
                    Document doc = Jsoup.connect("http://menu.dining.ucla.edu/Menus/" + diningHall + "/" + meal).timeout(10 * 1000).get();
                    Elements links = doc.select("a.recipeLink, li.sect-item");

                    String section = "";

                    for (Element link : links) {
                        if (link.tagName().equals("a")) {
                            String description;
                            Element parent = link.parent().parent();
                            if (parent != null) {
                                Elements descriptionElement = parent.select("div.tt-description");
                                String mealUrl = link.attr("href");
                                if (descriptionElement.size() > 0 && descriptionElement.get(0) != null) {
                                    description = parent.select("div.tt-description").text();
                                    db.addMealItem(new MealItem(link.ownText(), description, mealUrl, diningHall, meal, section));
                                } else {
                                    db.addMealItem(new MealItem(link.ownText(), "No description available", mealUrl, diningHall, meal, section));
                                }
                            }
                        } else if (link.tagName().equals("li")) {
                            section = link.ownText();
                        }
                    }
                }

            } catch (SocketTimeoutException e) {    // TODO: CHECK NUMBER OF EXCEPTIONS OCCURRED
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

            //if (diningHallNames.size() != levels.size())
            //  return false;

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
                                Log.d("Breakfast at " + diningHall, openTime.get(Calendar.HOUR_OF_DAY) + ":" + openTime.get(Calendar.MINUTE) + " to " + closeTime.get(Calendar.HOUR_OF_DAY) + ":" + closeTime.get(Calendar.MINUTE));
                                break;
                            case ("Lunch"):
                                lunchOpeningHours.put(diningHall, openTime);
                                lunchClosingHours.put(diningHall, closeTime);
                                Log.d("Lunch at " + diningHall, openTime.get(Calendar.HOUR_OF_DAY) + ":" + openTime.get(Calendar.MINUTE) + " to " + closeTime.get(Calendar.HOUR_OF_DAY) + ":" + closeTime.get(Calendar.MINUTE));
                                break;
                            case ("Dinner"):
                                dinnerOpeningHours.put(diningHall, openTime);
                                dinnerClosingHours.put(diningHall, closeTime);
                                Log.d("Dinner at " + diningHall, openTime.get(Calendar.HOUR_OF_DAY) + ":" + openTime.get(Calendar.MINUTE) + " to " + closeTime.get(Calendar.HOUR_OF_DAY) + ":" + closeTime.get(Calendar.MINUTE));
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
                diningHallRefresh.setRefreshing(true);
                AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
                asyncTaskRunner.execute(url);
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

            public ViewHolder(View v) {
                super(v);
                diningHallLayout = v;
                header = (TextView) v.findViewById(R.id.firstLine);
                footer = (TextView) v.findViewById(R.id.secondLine);
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
            // - get element from dataset at this position
            // - replace the contents of the view with that element
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

            Calendar cal = Calendar.getInstance();
            int currentHour = cal.get(Calendar.HOUR_OF_DAY);

            if (currentHour < 9) {
                Calendar open = breakfastOpeningHours.get(diningHall), close = breakfastClosingHours.get(diningHall);

                if (open != null && close != null) {
                    int openHour24 = breakfastOpeningHours.get(diningHall).get(Calendar.HOUR_OF_DAY), closeHour24 = breakfastClosingHours.get(diningHall).get(Calendar.HOUR_OF_DAY);
                    int openHour = breakfastOpeningHours.get(diningHall).get(Calendar.HOUR), closeHour = breakfastClosingHours.get(diningHall).get(Calendar.HOUR);
                    int openMinute = breakfastOpeningHours.get(diningHall).get(Calendar.MINUTE), closeMinute = breakfastClosingHours.get(diningHall).get(Calendar.MINUTE);
                    int openPeriod = breakfastOpeningHours.get(diningHall).get(Calendar.AM_PM), closePeriod = breakfastClosingHours.get(diningHall).get(Calendar.AM_PM);

                    String openPeriodString = (openPeriod == 0) ? "AM" : "PM", closedPeriodString = (closePeriod == 0) ? "AM" : "PM";

                    if (currentHour < openHour24) {
                        if (openMinute == 0)
                            holder.footer.setText("Opening at " + openHour + " " + openPeriodString);
                        else
                            holder.footer.setText("Opening at " + openHour + ":" + openMinute + " " + openPeriodString);
                        holder.footer.setTextColor(Color.RED);
                    } else if (currentHour >= openHour24 && currentHour < closeHour24) {
                        if (closeMinute == 0)
                            holder.footer.setText("Open until " + closeHour + " " + closedPeriodString);
                        else
                            holder.footer.setText("Open until " + closeHour + ":" + closeMinute + " " + closedPeriodString);
                        holder.footer.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.Open));
                    }
                } else {
                    holder.footer.setText(R.string.breakfast_closed);
                    holder.footer.setTextColor(Color.RED);
                }
            } else if (currentHour < 17) {
                Calendar open = lunchOpeningHours.get(diningHall), close = lunchClosingHours.get(diningHall);

                if (open != null && close != null) {
                    int openHour24 = lunchOpeningHours.get(diningHall).get(Calendar.HOUR_OF_DAY), closeHour24 = lunchClosingHours.get(diningHall).get(Calendar.HOUR_OF_DAY);
                    int openHour = lunchOpeningHours.get(diningHall).get(Calendar.HOUR), closeHour = lunchClosingHours.get(diningHall).get(Calendar.HOUR);
                    int openMinute = lunchOpeningHours.get(diningHall).get(Calendar.MINUTE), closeMinute = lunchClosingHours.get(diningHall).get(Calendar.MINUTE);
                    int openPeriod = lunchOpeningHours.get(diningHall).get(Calendar.AM_PM), closePeriod = lunchClosingHours.get(diningHall).get(Calendar.AM_PM);

                    String openPeriodString = (openPeriod == 0) ? "AM" : "PM", closedPeriodString = (closePeriod == 0) ? "AM" : "PM";

                    if (currentHour < openHour24) {
                        if (openMinute == 0)
                            holder.footer.setText("Opening at " + openHour + " " + openPeriodString);
                        else
                            holder.footer.setText("Opening at " + openHour + ":" + openMinute + " " + openPeriodString);
                        holder.footer.setTextColor(Color.RED);
                    } else if (currentHour >= openHour24 && currentHour < closeHour24) {
                        if (closeMinute == 0)
                            holder.footer.setText("Open until " + closeHour + " " + closedPeriodString);
                        else
                            holder.footer.setText("Open until " + closeHour + ":" + closeMinute + " " + closedPeriodString);
                        holder.footer.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.Open));
                    }
                } else {
                    holder.footer.setText(R.string.lunch_closed);
                    holder.footer.setTextColor(Color.RED);
                }

            } else {
                Calendar open = dinnerOpeningHours.get(diningHall), close = dinnerClosingHours.get(diningHall);

                if (open != null && close != null) {
                    int openHour24 = dinnerOpeningHours.get(diningHall).get(Calendar.HOUR_OF_DAY), closeHour24 = dinnerClosingHours.get(diningHall).get(Calendar.HOUR_OF_DAY);
                    int openHour = dinnerOpeningHours.get(diningHall).get(Calendar.HOUR), closeHour = dinnerClosingHours.get(diningHall).get(Calendar.HOUR);
                    int openMinute = dinnerOpeningHours.get(diningHall).get(Calendar.MINUTE), closeMinute = dinnerClosingHours.get(diningHall).get(Calendar.MINUTE);
                    int openPeriod = dinnerOpeningHours.get(diningHall).get(Calendar.AM_PM), closePeriod = dinnerClosingHours.get(diningHall).get(Calendar.AM_PM);

                    String openPeriodString = (openPeriod == 0) ? "AM" : "PM", closedPeriodString = (closePeriod == 0) ? "AM" : "PM";

                    if (currentHour < openHour24) {
                        // TODO: USE CALENDAR HH:MM PRINTOUT
                        if (openMinute == 0)
                            holder.footer.setText("Opening at " + openHour + " " + openPeriodString);
                        else
                            holder.footer.setText("Opening at " + openHour + ":" + openMinute + " " + openPeriodString);
                        holder.footer.setTextColor(Color.RED);
                    } else if (currentHour >= openHour24 && currentHour < closeHour24) {
                        if (closeMinute == 0)
                            holder.footer.setText("Open until " + closeHour + " " + closedPeriodString);
                        else
                            holder.footer.setText("Open until " + closeHour + ":" + closeMinute + " " + closedPeriodString);
                        holder.footer.setTextColor(ContextCompat.getColor(getBaseContext(), R.color.Open));
                    } else if (currentHour >= closeHour24) {
                        if (closeMinute == 0)
                            holder.footer.setText("Closed at " + closeHour + " " + closedPeriodString);
                        else
                            holder.footer.setText("Closed at " + closeHour + ":" + closeMinute + " " + closedPeriodString);
                        holder.footer.setTextColor(Color.RED);
                    }

                } else {
                    holder.footer.setText(R.string.dinner_closed);
                    holder.footer.setTextColor(Color.RED);
                }
            }

        }

        @Override
        public int getItemCount() {
            return values.size();
        }
    }
}
