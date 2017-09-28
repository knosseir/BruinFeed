package com.knosseir.admin.bruinfeed;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class DiningHallHoursActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dining_hall_hours_activity);

        AsyncTaskRunner runner = new AsyncTaskRunner();
        runner.execute();
    }

    public class AsyncTaskRunner extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                Document doc = Jsoup.connect("http://menu.dining.ucla.edu/Hours").get();

                Element hoursTable = doc.select("table.hours-table").first();
                hoursTable.select("a").remove();

                return hoursTable.toString().replaceAll("\\∣", "");

            } catch (IOException e) {
                Log.e("DiningHallHoursActivity", e.toString());
                return "";
            }
        }

        @Override
        protected void onPostExecute(String source) {
            WebView hoursWebView = findViewById(R.id.hours_webview);
            hoursWebView.setHorizontalScrollBarEnabled(true);
            WebSettings webSettings = hoursWebView.getSettings();
            webSettings.setUserAgentString("Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
            webSettings.setJavaScriptEnabled(true);
//            webSettings.setDefaultFontSize(14);
            hoursWebView.loadDataWithBaseURL(null, source, "text/html", "UTF-8", null);
        }
    }
}
