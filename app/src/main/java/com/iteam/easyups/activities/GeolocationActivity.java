package com.iteam.easyups.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.iteam.easyups.R;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Amphitheater;
import com.iteam.easyups.model.Building;

import java.util.ArrayList;
import java.util.List;

public class GeolocationActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private GoogleMap map;
    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private final List<Building> buildings = new ArrayList<>();
    private final List<Amphitheater> amphitheaters = new ArrayList<>();
    private LatLngBounds UPS = new LatLngBounds(
            new LatLng(43.560352, 1.470266), new LatLng(43.562691, 1.469347));

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocation);

        initializeAmphitheatersSpinner();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        DatabaseReference ref = database.getReference("easyups/building");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Building>> genericTypeIndicator = new GenericTypeIndicator<List<Building>>(){};
                buildings.addAll(dataSnapshot.getValue(genericTypeIndicator));

                addBuildingsOnSpinner();

                for (Building building : buildings) {
                    LatLng latLng = new LatLng(building.getLatitude(), building.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
                }

                map.setLatLngBoundsForCameraTarget(UPS);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public void initializeAmphitheatersSpinner()
    {
        Spinner spinner = (Spinner) findViewById(R.id.amphitheaters);
        spinner.setOnItemSelectedListener(this);

        List<String> amphitheatersNames = new ArrayList<>();
        amphitheatersNames.add("Sélectionner un amphi");
        ArrayAdapter<String> dataAdapter = getArrayAdapter(amphitheatersNames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setEnabled(false);
        spinner.setClickable(false);
        spinner.setAdapter(dataAdapter);
    }

    public void loadAmphitheatersOnSpinner(final String buildingName)
    {
        if (!amphitheaters.isEmpty())
        {
            addAmphitheatersOnSpinner(buildingName);
        }
        else
        {
            DatabaseReference ref = database.getReference("easyups/amphitheater");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<Amphitheater>> genericTypeIndicator = new GenericTypeIndicator<List<Amphitheater>>(){};
                    amphitheaters.addAll(dataSnapshot.getValue(genericTypeIndicator));
                    addAmphitheatersOnSpinner(buildingName);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }
    }

    public void addAmphitheatersOnSpinner(String buildingName)
    {
        Spinner spinner = (Spinner) findViewById(R.id.amphitheaters);
        spinner.setOnItemSelectedListener(this);
        spinner.setEnabled(true);
        spinner.setClickable(true);

        List<String> amphitheaterNames = new ArrayList<>();
        amphitheaterNames.add("Sélectionner un amphi");
        for (Amphitheater amphitheater : amphitheaters)
        {
            if (amphitheater.getBuilding().equals(buildingName))
            {
                amphitheaterNames.add(amphitheater.getName());
            }
        }
        ArrayAdapter<String> dataAdapter = getArrayAdapter(amphitheaterNames);
        spinner.setAdapter(dataAdapter);
    }

    public void addBuildingsOnSpinner()
    {
        Spinner spinner = (Spinner) findViewById(R.id.buildings);
        spinner.setOnItemSelectedListener(this);

        List<String> buildingNames = new ArrayList<>();
        buildingNames.add("Sélectionner un bâtiment");
        for (Building building : buildings) {
            buildingNames.add(building.getName());
        }

        ArrayAdapter<String> dataAdapter = getArrayAdapter(buildingNames);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.buildings:
                String name = parent.getItemAtPosition(position).toString();
                Building building = getBuilding(name);
                if (building != null) {
                    loadAmphitheatersOnSpinner(building.getName());
                    LatLng latLng = new LatLng(building.getLatitude(), building.getLongitude());
                    map.clear();
                    Marker marker = map.addMarker(new MarkerOptions().position(latLng).title("Bâtiment " + building.getName()));
                    marker.showInfoWindow();
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
                }
                break;
            case R.id.amphitheaters:
                String amphitheaterName = parent.getItemAtPosition(position).toString();
                Amphitheater amphitheater = getAmphitheater(amphitheaterName);
                if (amphitheater != null) {
                    LatLng latLng = new LatLng(amphitheater.getLatitude(), amphitheater.getLongitude());
                    map.clear();
                    Marker marker = map.addMarker(new MarkerOptions().position(latLng).title("Amphi " + amphitheater.getName()));
                    marker.showInfoWindow();
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public Building getBuilding(String name)
    {
        for (Building building : buildings) {
            if (building.getName().equals(name))
            {
                return building;
            }
        }
        return null;
    }

    public Amphitheater getAmphitheater(String name)
    {
        for (Amphitheater amphitheater : amphitheaters) {
            if (amphitheater.getName().equals(name))
            {
                return amphitheater;
            }
        }
        return null;
    }

    public ArrayAdapter<String> getArrayAdapter(List<String> names)
    {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, names){
            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if(position == 0){
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                }
                else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return dataAdapter;
    }

}
