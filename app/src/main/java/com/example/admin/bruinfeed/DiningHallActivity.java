package com.example.admin.bruinfeed;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DiningHallActivity extends AppCompatActivity {

    private static final String DiningHallTag = "DiningHallActivity";

    String url, meal;
    int activityLevel = 0;
    ArrayList<String> menuItemNames = new ArrayList<>();
    TextView activityLeveLTextView;

    String selectedDiningHall;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ProgressBar activityLevelProgressBar;

    SwipeRefreshLayout menuRefresh;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.bottom_breakfast_button:
                    meal = "Breakfast";
                    break;
                case R.id.bottom_lunch_button:
                    meal = "Lunch";
                    break;
                case R.id.bottom_dinner_button:
                    meal = "Dinner";
                    break;
            }
            url = "http://menu.dining.ucla.edu/Menus/" + selectedDiningHall + "/" + meal;
            Log.e(DiningHallTag, url);

            menuRefresh.setRefreshing(true);
            DiningHallActivity.AsyncTaskRunner runner = new DiningHallActivity.AsyncTaskRunner();
            runner.execute(url);

            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dining_hall);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        recyclerView = (RecyclerView) findViewById(R.id.menuRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MenuAdapter(menuItemNames);
        recyclerView.setAdapter(mAdapter);

        menuRefresh = (SwipeRefreshLayout) findViewById(R.id.menuRefresh);

        activityLeveLTextView = (TextView) findViewById(R.id.activityLevelTextBox);

        // get current hour of day
        Calendar cal = Calendar.getInstance();
        int currentHour = cal.get(Calendar.HOUR_OF_DAY);

        // pick default meal based on time of day
        if (currentHour > 6 && currentHour < 11) {
            navigation.setSelectedItemId(R.id.bottom_breakfast_button);
            meal = "Breakfast";
        }
        else if (currentHour < 17) {
            navigation.setSelectedItemId(R.id.bottom_lunch_button);
            meal = "Lunch";
        }
        else {
            navigation.setSelectedItemId(R.id.bottom_dinner_button);
            meal = "Dinner";
        }

        // TODO: CHOOSE MEAL BASED ON DINING PERIODS FOR EACH DINING HALL

        selectedDiningHall = getIntent().getStringExtra("SelectedDiningHall");
        activityLevel = getIntent().getIntExtra("ActivityLevel", 0);
        activityLevelProgressBar = (ProgressBar) findViewById(R.id.activityLevel);

        url = "http://menu.dining.ucla.edu/Menus/" + selectedDiningHall + "/" + meal;
        setTitle(meal + " at " + selectedDiningHall);

        menuRefresh.setRefreshing(true);

        DiningHallActivity.AsyncTaskRunner runner = new DiningHallActivity.AsyncTaskRunner();
        runner.execute(url);

        menuRefresh.setColorSchemeColors(Color.rgb(244, 205, 65));

        // refreshes menu upon pull to refresh
        menuRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
                        asyncTaskRunner.execute(url);
                    }
                }
        );
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Document doc = Jsoup.connect(params[0]).timeout(10 * 1000).get();
                Elements links = doc.select("a.recipeLink");
                menuItemNames.clear();

                for (Element link : links) {
                    menuItemNames.add(link.ownText());      // TODO: CHECK THAT LINK HAS TEXT USING HASTEXT()
                }

                updateMenuRecyclerView();

                /*
                if (menuItemNames.isEmpty()) {
                    final Snackbar emptyMenuSnackbar = Snackbar.make(findViewById(R.id.menuGrid), R.string.empty_menu, Snackbar.LENGTH_INDEFINITE);
                    emptyMenuSnackbar.setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            emptyMenuSnackbar.dismiss();
                        }
                    });
                    emptyMenuSnackbar.show();
                }
                */

            } catch (SocketTimeoutException e) {    // TODO: CHECK NUMBER OF EXCEPTIONS OCCURRED
                Log.e(DiningHallTag, e.toString());
                updateMenuRecyclerView();
                reload(R.string.connection_timeout);
            } catch (IOException | IllegalArgumentException e) {
                Log.e(DiningHallTag, e.toString());
                updateMenuRecyclerView();
                reload(R.string.retry_connection);
            }

            return "success";
        }
    }

    public void updateMenuRecyclerView() {
        // update RecyclerView with menu and set action bar title on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                setTitle(meal + " at " + selectedDiningHall);
                if (activityLevel == 0) {
                    activityLeveLTextView.setText("Activity Level at " + selectedDiningHall + " is unavailable");
                }
                else {
                    activityLeveLTextView.setText("Activity Level at " + selectedDiningHall + " is " + activityLevel + "%");
                }
                activityLevelProgressBar.setProgress(activityLevel);
                menuRefresh.setRefreshing(false);
            }
        });
    }

    public void reload(int error) {
        Snackbar reloadSnackbar = Snackbar.make(findViewById(R.id.menuRecyclerView), error, Snackbar.LENGTH_INDEFINITE);
        reloadSnackbar.setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        menuRefresh.setRefreshing(true);
                        AsyncTaskRunner asyncTaskRunner = new AsyncTaskRunner();
                        asyncTaskRunner.execute(url);
                    }
                });
                reloadSnackbar.show();
    }

    public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder> {
        private List<String> values;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView header;
            TextView footer;
            View menuLayout;

            public ViewHolder(View v) {
                super(v);
                menuLayout = v;
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

        public MenuAdapter(List<String> data) {
            values = data;
        }

        @Override
        public MenuAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // create a new view
            LayoutInflater inflater = LayoutInflater.from(
                    parent.getContext());
            View v = inflater.inflate(R.layout.menu_row, parent, false);
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
