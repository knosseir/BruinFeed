package com.example.admin.bruinfeed;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    final String url = "http://menu.dining.ucla.edu/Menus";

    Set<Element> diningHallLinks = new HashSet<>();
    ArrayList<String> diningHallNames = new ArrayList<>();

    ArrayAdapter<String> gridViewArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        GridView diningHallGrid = (GridView) findViewById(R.id.diningHallGrid);
        setSupportActionBar(toolbar);

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
        runner.execute("hello");

        gridViewArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, diningHallNames);
        diningHallGrid.setAdapter(gridViewArrayAdapter);

        diningHallGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object obj = parent.getItemAtPosition(position);
                Toast.makeText(getBaseContext(), obj.toString(), Toast.LENGTH_SHORT).show();

                // launch menu based on the dining hall selected
                Intent diningHallMenuIntent = new Intent(getBaseContext(), DiningHallActivity.class);
                diningHallMenuIntent.putExtra("SelectedDiningHall", obj.toString());
                startActivity(diningHallMenuIntent);
            }
        });
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

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
            try {
                Document doc = Jsoup.connect(url).get();
                Elements links = doc.select("a[href]");

                for (Element link : links) {
                    Log.d("link titles", link.attr("href"));
                    String name = link.attr("href");
                    if (name.contains("/Menus/") && !name.contains("Breakfast")
                            && !name.contains("Lunch") && !name.contains("Dinner") && !name.contains("Full Menu")) {       // TODO: FIND BETTER WAY TO FILTER OUT BREAKFAST, LUNCH, AND DINNER LINKS FROM DINING HALL LIST
                        diningHallLinks.add(link);
                        diningHallNames.add(name);
                    }
                }

                // remove duplicates from diningHallNames ArrayList
                Set<String> diningHallTemp = new HashSet<>();
                diningHallTemp.addAll(diningHallNames);
                diningHallNames.clear();
                diningHallNames.addAll(diningHallTemp);

                /*
                // remove "/Menus/" from dining hall names
                for (String s : diningHallNames) {
                    s.replace("/Menus/", "");
                    Log.e("names", s);
                }
                */

                // update GridView with list of dining halls on UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gridViewArrayAdapter.notifyDataSetChanged();
                    }
                });

            } catch (IOException | IllegalArgumentException e) {    // TODO: CHECK NUMBER OF EXCEPTIONS OCCURRED
                Log.e("error", "This should never happen");
                Snackbar reloadSnackbar = Snackbar.make(findViewById(R.id.diningHallGrid), R.string.retry_connection, Snackbar.LENGTH_INDEFINITE);
                reloadSnackbar.setAction(R.string.reconnecting, new ReconnectListener());
                reloadSnackbar.show();
            }

            return "success";
        }
    }

    public class ReconnectListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent reconnectionIntent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(reconnectionIntent);
        }
    }
}
