package com.knosseir.admin.bruinfeed;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.util.ArrayList;
import java.util.List;

public class DiningHallHoursActivity extends AppCompatActivity {

    private ArrayList hours = new ArrayList();

    private static final String breakfastLabel = "BREAKFAST";
    private static final String lunchLabel = "LUNCH";
    private static final String brunchLabel = "BRUNCH";
    private static final String dinnerLabel = "DINNER";
    private static final String lateNightLabel = "LATE_NIGHT";

    private RecyclerView recyclerView;
    private SimpleHoursAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    List<SimpleSectionedRecyclerViewAdapter.Section> sections = new ArrayList<>();
    SimpleSectionedRecyclerViewAdapter mSectionedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dining_hall_hours_activity);

        Spannable text = new SpannableString("Dining Hours");
        text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        setTitle(text);

        ArrayList<String> diningHallNames = getIntent().getStringArrayListExtra("diningHallNames");

        Intent intent = getIntent();

        recyclerView = findViewById(R.id.diningHallHoursRecyclerView);

        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new SimpleHoursAdapter(getBaseContext(), hours);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        for (String hall : diningHallNames) {
            sections.add(new SimpleSectionedRecyclerViewAdapter.Section(hours.size(), hall.toUpperCase()));
            String breakfastRange = intent.getStringExtra(breakfastLabel + "_open_" + hall) + " - " + intent.getStringExtra(breakfastLabel + "_close_" + hall);
            String lunchRange = intent.getStringExtra(lunchLabel + "_open_" + hall) + " - " + intent.getStringExtra(lunchLabel + "_close_" + hall);
            String brunchRange = intent.getStringExtra(brunchLabel + "_open_" + hall) + " - " + intent.getStringExtra(brunchLabel + "_close_" + hall);
            String dinnerRange = intent.getStringExtra(dinnerLabel + "_open_" + hall) + " - " + intent.getStringExtra(dinnerLabel + "_close_" + hall);
            String lateNightRange = intent.getStringExtra(lateNightLabel + "_open_" + hall) + " - " + intent.getStringExtra(lateNightLabel + "_close_" + hall);

            hours.add("Breakfast: " + ((breakfastRange.contains("closed")) ? breakfastRange.replace("-", "") : breakfastRange));
            hours.add("Lunch/Brunch : " + ((brunchRange.contains("closed")) ? lunchRange : brunchRange));
            hours.add("Dinner: " + ((dinnerRange.contains("closed")) ? dinnerRange.replace("-", "") : dinnerRange));
            hours.add("Late Night: " + ((lateNightRange.contains("closed")) ? lateNightRange.replace("-", "") : lateNightRange));
        }
        updateRecyclerView();
    }

    public void updateRecyclerView() {
        // Add adapter to the sectionAdapter
        SimpleSectionedRecyclerViewAdapter.Section[] array = new SimpleSectionedRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter = new SimpleSectionedRecyclerViewAdapter(getBaseContext(), R.layout.section, R.id.section_text, mAdapter);
        mSectionedAdapter.setSections(sections.toArray(array));

        // Update RecyclerView
        recyclerView.setAdapter(mSectionedAdapter);
        mAdapter.notifyDataSetChanged();
    }
}