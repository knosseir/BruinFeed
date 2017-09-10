package com.example.admin.bruinfeed;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class DiningHallInfoActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_dining_hall_info);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String selectedDiningHall = getIntent().getStringExtra("SelectedDiningHall");
        setTitle(selectedDiningHall);

        String selectedDiningHallDescription = "No information available about " + selectedDiningHall;

        LatLng diningHallLocation;

        switch (selectedDiningHall) {
            case "Covel":
                diningHallLocation = new LatLng(34.073276, -118.449967);
                selectedDiningHallDescription = "Covel Dining is located in Covel Commons";
                break;
            case "Rieber":
                diningHallLocation = new LatLng(34.071742, -118.451445);
                selectedDiningHallDescription = "Rieber Dining is located in Rieber Hall";
                break;
            case "FEAST at Rieber":
                diningHallLocation = new LatLng(34.071700, -118.451340);
                selectedDiningHallDescription = "FEAST at Rieber is located in Rieber Hall";
                break;
            case "De Neve":
                diningHallLocation = new LatLng(34.070423, -118.450160);
                selectedDiningHallDescription = "De Neve Dining is located in De Neve Commons";
                break;
            case "Sproul":
                diningHallLocation = new LatLng(34.072104, -118.449712);
                selectedDiningHallDescription = "Sproul Dining is located in Carnesale Commons";
                break;
            default:
                diningHallLocation = new LatLng(34.068903, -118.445149);
        }

        googleMap.addMarker(new MarkerOptions().position(diningHallLocation)
                .title(selectedDiningHall));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(diningHallLocation));
        googleMap.setMinZoomPreference(17.0f);

        ((TextView) findViewById(R.id.dining_hall_description)).setText(selectedDiningHallDescription);
    }
}