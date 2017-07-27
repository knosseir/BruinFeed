package com.example.admin.bruinfeed;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String MainTag = "MainActivity";
    private static final String url = "http://menu.dining.ucla.edu/Menus";

    private ArrayList<String> diningHallNames = new ArrayList<>();
    public Map<String, Integer> activityLevelMap = new HashMap<>();
    private SwipeRefreshLayout diningHallRefresh;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute(url);

        diningHallRefresh.setColorSchemeColors(Color.rgb(244, 205, 65));

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
            // setContentView(R.layout.content_main);      // TODO: FIX UPPER BACK BUTTON BEHAVIOR (DON'T REFRESH ENTIRE MAINACTIVITY)
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

                updateRecyclerView();

                // ------------------------------------------------------- //

                /*
                Map<String, String> breakfastHours = new HashMap<>();
                Map<String, String> lunchHours = new HashMap<>();
                Map<String, String> dinnerHours = new HashMap<>();


                Document hoursDoc = Jsoup.connect("http://menu.dining.ucla.edu/Hours").timeout(10 * 1000).get();

                Elements diningHours = hoursDoc.select("span.hours-location");
                Elements bHours = hoursDoc.select("td.hours-head");
                Elements lHours = hoursDoc.select("td.hours-open Lunch");
                Elements dHours = hoursDoc.select("td.hours-open Dinner");

                Elements els = hoursDoc.select("td.hours-open Breakfast");
                Log.e("SIZE", bHours.size() + "");
                */

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

        public boolean getActivityLevels(Document doc) {
            Elements levels = doc.getElementsMatchingOwnText("Activity Level");

            Log.e("tag", levels.size() + "");

            //if (diningHallNames.size() != levels.size())
              //  return false;

            for (int i = 0; i < levels.size(); i++) {       // TODO: MAKE SURE ORDER IS THE SAME
                activityLevelMap.put(diningHallNames.get(i), Integer.parseInt(levels.get(i).parent().ownText().replaceAll("[: %]", "")));
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
            final String name = values.get(position);
            holder.header.setText(name);
            holder.header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Object obj = getItemAtPosition(position);
                    Intent diningHallMenuIntent = new Intent(getBaseContext(), DiningHallActivity.class);
                    diningHallMenuIntent.putExtra("SelectedDiningHall", obj.toString());
                    diningHallMenuIntent.putExtra("ActivityLevel", activityLevelMap.get(obj.toString()));
                    startActivity(diningHallMenuIntent);
                }
            });

            holder.footer.setText("Hours: " + name);
        }

        @Override
        public int getItemCount() {
            return values.size();
        }
    }
}
