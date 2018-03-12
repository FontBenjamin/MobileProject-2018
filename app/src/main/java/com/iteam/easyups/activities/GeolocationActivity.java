package com.iteam.easyups.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.iteam.easyups.R;
import com.iteam.easyups.adapter.PlaceSpinnerAdapter;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Amphitheater;
import com.iteam.easyups.model.Place;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeolocationActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private Context context;

    private final List<Place> buildingList = new ArrayList<>();
    private final List<Amphitheater> amphitheaterList = new ArrayList<>();
    private final List<Place> poiList = new ArrayList<>();

    private GoogleMap map;

    private Spinner buildingSpinner;
    private Spinner amphitheaterSpinner;
    private Spinner poiSpinner;

    private Button removePoiButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_geolocation);

        buildingSpinner = (Spinner) findViewById(R.id.buildings);
        buildingSpinner.setOnItemSelectedListener(this);
        amphitheaterSpinner = (Spinner) findViewById(R.id.amphitheaters);
        amphitheaterSpinner.setOnItemSelectedListener(this);
        poiSpinner = (Spinner) findViewById(R.id.poi);
        poiSpinner.setOnItemSelectedListener(this);

        removePoiButton = (Button) findViewById(R.id.remove_poi);

        buildingManagement();
        poiManagement();
        initializeAmphitheatersSpinner();
        removePoi();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Open activity with camera on Paul Sabatier
        LatLng latLng = new LatLng(43.562038, 1.466371);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }

        createPoiOnMap();
    }

    public void buildingManagement()
    {
        DatabaseReference ref = database.getReference("easyups/building");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Place>> genericTypeIndicator = new GenericTypeIndicator<List<Place>>(){};
                buildingList.addAll(dataSnapshot.getValue(genericTypeIndicator));
                addBuildingsOnSpinner();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    public void addBuildingsOnSpinner()
    {
        Place place = new Place();
        place.setName("Bâtiments");
        buildingList.add(0, place);

        PlaceSpinnerAdapter dataAdapter = new PlaceSpinnerAdapter(context, R.layout.support_simple_spinner_dropdown_item, buildingList);
        buildingSpinner.setAdapter(dataAdapter);
    }

    public void poiManagement()
    {
        final FirebaseUser user = auth.getCurrentUser();

        if (user != null)
        {
            String userId = user.getUid();

            DatabaseReference ref = database.getReference("easyups/Users").child(userId).child("poi");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    poiList.clear();
                    GenericTypeIndicator<Map<String, Place>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Place>>(){};
                    if (genericTypeIndicator != null)
                    {
                        Map<String, Place> poiAsMap = dataSnapshot.getValue(genericTypeIndicator);
                        if (poiAsMap != null)
                        {
                            for (Map.Entry<String, Place> place : poiAsMap.entrySet()) {
                                Place poi = place.getValue();
                                poi.setId(place.getKey());
                                poiList.add(poi);
                            }
                        }
                        else
                        {
                            removePoiButton.setEnabled(false);
                        }
                    }
                    addPoiOnSpinner();

                    if (map != null)
                    {

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }
        else
        {
            poiSpinner.setEnabled(false);
            Button button = (Button) findViewById(R.id.remove_poi);
            button.setEnabled(false);
        }
    }

    public void addPoiOnSpinner()
    {
        Place place = new Place();
        place.setName("Centres d'intérêt");
        poiList.add(0, place);

        if (poiList.size() == 1)
        {
            poiSpinner.setEnabled(false);
        }

        PlaceSpinnerAdapter dataAdapter = new PlaceSpinnerAdapter(context, R.layout.support_simple_spinner_dropdown_item, poiList);
        poiSpinner.setAdapter(dataAdapter);
    }

    public void initializeAmphitheatersSpinner()
    {
        List<String> amphitheatersNames = new ArrayList<>();
        amphitheatersNames.add("Amphis");

        ArrayAdapter<String> dataAdapter = getArrayAdapter(amphitheatersNames);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        amphitheaterSpinner.setEnabled(false);
        amphitheaterSpinner.setClickable(false);
        amphitheaterSpinner.setAdapter(dataAdapter);
    }

    public void createPoiOnMap()
    {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                final EditText poiEditText = new EditText(context);
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Nouveau point d'intérêt")
                        .setMessage("Nom du point d'intérêt :")
                        .setView(poiEditText)
                        .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                map.clear();
                                String poiName = String.valueOf(poiEditText.getText());
                                Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title("Point d'intérêt " + poiName));
                                currentMarker.showInfoWindow();
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                poiSpinner.setEnabled(true);
                                savePoi(latLng, poiName);
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .create();
                dialog.show();
            }
        });
    }

    public void savePoi(LatLng latLng, String poiName)
    {
        final FirebaseUser user = auth.getCurrentUser();

        if (user != null)
        {
            String userId = user.getUid();
            String key = database.getReference().child("easyups/Users").child(userId).child("poi").push().getKey();
            database.getReference().child("easyups/Users").child(userId).child("poi").child(key).child("name").setValue(poiName);
            database.getReference().child("easyups/Users").child(userId).child("poi").child(key).child("longitude").setValue(latLng.longitude);
            database.getReference().child("easyups/Users").child(userId).child("poi").child(key).child("latitude").setValue(latLng.latitude);
        }
    }

    public void loadAmphitheatersOnSpinner(final String buildingName)
    {
        if (!amphitheaterList.isEmpty())
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
                    amphitheaterList.addAll(dataSnapshot.getValue(genericTypeIndicator));
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
        amphitheaterSpinner.setEnabled(true);
        amphitheaterSpinner.setClickable(true);

        List<String> amphitheaterNames = new ArrayList<>();
        amphitheaterNames.add("Amphis");
        for (Amphitheater amphitheater : amphitheaterList)
        {
            if (amphitheater.getBuilding().equals(buildingName))
            {
                amphitheaterNames.add(amphitheater.getName());
            }
        }
        ArrayAdapter<String> dataAdapter = getArrayAdapter(amphitheaterNames);
        amphitheaterSpinner.setAdapter(dataAdapter);
    }

    public void removePoi()
    {
        removePoiButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                map.clear();
                Place poi = (Place) poiSpinner.getSelectedItem();
                final FirebaseUser user = auth.getCurrentUser();

                if (user != null) {
                    String userId = user.getUid();
                    database.getReference("easyups/Users").child(userId).child("poi").child(poi.getId()).setValue(null);
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.buildings:
                if (position != 0)
                {
                    Place building = (Place) parent.getItemAtPosition(position);
                    if (building != null) {
                        loadAmphitheatersOnSpinner(building.getName());
                        LatLng latLng = new LatLng(building.getLatitude(), building.getLongitude());
                        map.clear();
                        Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title("Bâtiment " + building.getName()));
                        currentMarker.showInfoWindow();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        poiSpinner.setSelection(0);
                        removePoiButton.setEnabled(false);
                    }
                }
                break;
            case R.id.amphitheaters:
                String amphitheaterName = parent.getItemAtPosition(position).toString();
                Amphitheater amphitheater = getAmphitheater(amphitheaterName);
                if (amphitheater != null) {
                    LatLng latLng = new LatLng(amphitheater.getLatitude(), amphitheater.getLongitude());
                    map.clear();
                    Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title("Amphi " + amphitheater.getName()));
                    currentMarker.showInfoWindow();
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                    poiSpinner.setSelection(0);
                    removePoiButton.setEnabled(false);
                }
                break;
            case R.id.poi:
                if (position != 0)
                {
                    Place poi = (Place) parent.getItemAtPosition(position);
                    if (poi != null) {
                        LatLng latLng = new LatLng(poi.getLatitude(), poi.getLongitude());
                        map.clear();
                        Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title("Point d'intêret " + poi.getName()));
                        currentMarker.showInfoWindow();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        buildingSpinner.setSelection(0);
                        amphitheaterSpinner.setSelection(0);
                        removePoiButton.setEnabled(true);
                    }
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public Place getBuilding(String name)
    {
        for (Place building : buildingList) {
            if (building.getName().equals(name))
            {
                return building;
            }
        }
        return null;
    }

    public Amphitheater getAmphitheater(String name)
    {
        for (Amphitheater amphitheater : amphitheaterList) {
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
