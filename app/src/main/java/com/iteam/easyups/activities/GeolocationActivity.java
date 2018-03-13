package com.iteam.easyups.activities;

import android.Manifest;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

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
import com.iteam.easyups.communication.BDDRoutes;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Place;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeolocationActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private Context context;

    private final List<Place> buildingList = new ArrayList<>();
    private final List<Place> amphitheaterList = new ArrayList<>();
    private final List<Place> poiList = new ArrayList<>();

    private GoogleMap map;

    private Spinner buildingSpinner;
    private Spinner amphitheaterSpinner;
    private Spinner poiSpinner;

    private FloatingActionButton removePoiButton;
    private FloatingActionButton bluetooth;
    final DataOutputStream[] os = new DataOutputStream[1];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_geolocation);
        ApplicationInfo applicationInfo = this.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String result = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : this.getString(stringId);
        this.setTitle(result);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        buildingSpinner = (Spinner) findViewById(R.id.buildings);
        bluetooth = findViewById(R.id.bluetooth);
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.bluetooth_question);
                dialog.setTitle("Bluetooth");

                // set the custom dialog components - text, image and button
                Button envoyer = dialog.findViewById(R.id.buttonEnvoie);
                Button recevoir = dialog.findViewById(R.id.buttonRecevoir);

                recevoir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // recevoir le point d'interêt
                    }
                });

                recevoir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // envoyer le point d'interêt
                    }
                });

                dialog.show();
            }
        });
        buildingSpinner.setOnItemSelectedListener(this);
        amphitheaterSpinner = (Spinner) findViewById(R.id.amphitheaters);
        amphitheaterSpinner.setOnItemSelectedListener(this);
        poiSpinner = (Spinner) findViewById(R.id.poi);
        poiSpinner.setOnItemSelectedListener(this);

        removePoiButton =  findViewById(R.id.remove_poi);

        buildingManagement();
        amphitheaterManagement();
        poiManagement();
        removePoi();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
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
        DatabaseReference ref = database.getReference(BDDRoutes.BUILDING_PATH);
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

    public void amphitheaterManagement()
    {
        DatabaseReference ref = database.getReference(BDDRoutes.AMPHIS_PATH);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Place>> genericTypeIndicator = new GenericTypeIndicator<List<Place>>(){};
                amphitheaterList.addAll(dataSnapshot.getValue(genericTypeIndicator));
                addAmphitheatersOnSpinner();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }


    public void addAmphitheatersOnSpinner()
    {
        Place place = new Place();
        place.setName("Amphis");
        amphitheaterList.add(0, place);

        PlaceSpinnerAdapter dataAdapter = new PlaceSpinnerAdapter(context, R.layout.support_simple_spinner_dropdown_item, amphitheaterList);
        amphitheaterSpinner.setAdapter(dataAdapter);
    }

    public void poiManagement()
    {
        final FirebaseUser user = auth.getCurrentUser();

        if (user != null)
        {
            String userId = user.getUid();

            DatabaseReference ref = database.getReference(BDDRoutes.USERS_PATH).child(userId).child("poi");
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
            FloatingActionButton button =  findViewById(R.id.remove_poi);
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
                                Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title(poiName));
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
            String key = database.getReference().child(BDDRoutes.USERS_PATH).child(userId).child("poi").push().getKey();
            database.getReference().child(BDDRoutes.USERS_PATH).child(userId).child("poi").child(key).child("name").setValue(poiName);
            database.getReference().child(BDDRoutes.USERS_PATH).child(userId).child("poi").child(key).child("longitude").setValue(latLng.longitude);
            database.getReference().child(BDDRoutes.USERS_PATH).child(userId).child("poi").child(key).child("latitude").setValue(latLng.latitude);
        }
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
                    database.getReference(BDDRoutes.USERS_PATH).child(userId).child("poi").child(poi.getId()).setValue(null);
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
                        LatLng latLng = new LatLng(building.getLatitude(), building.getLongitude());
                        map.clear();
                        Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title("Bâtiment " + building.getName()));
                        currentMarker.showInfoWindow();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        poiSpinner.setSelection(0);
                        amphitheaterSpinner.setSelection(0);
                        removePoiButton.setEnabled(false);
                    }
                }
                break;
            case R.id.amphitheaters:
                if (position != 0)
                {
                    Place amphitheater = (Place) parent.getItemAtPosition(position);
                    if (amphitheater != null) {
                        LatLng latLng = new LatLng(amphitheater.getLatitude(), amphitheater.getLongitude());
                        map.clear();
                        Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title("Amphi " + amphitheater.getName()));
                        currentMarker.showInfoWindow();
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        buildingSpinner.setSelection(0);
                        poiSpinner.setSelection(0);
                        removePoiButton.setEnabled(false);
                    }
                }
                break;
            case R.id.poi:
                if (position != 0)
                {
                    Place poi = (Place) parent.getItemAtPosition(position);
                    if (poi != null) {
                        LatLng latLng = new LatLng(poi.getLatitude(), poi.getLongitude());
                        map.clear();
                        Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title(poi.getName()));
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

public void sendDataViaBluetooth() {

    final BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

    BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String remoteDeviceName = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
            BluetoothDevice remoteDevice;

            remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            Toast.makeText(getApplicationContext(), "Discovered: " + remoteDeviceName + " address " + remoteDevice.getAddress(), Toast.LENGTH_SHORT).show();

            try {
                BluetoothDevice device = bluetooth.getRemoteDevice(remoteDevice.getAddress());

                Method m = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});

                BluetoothSocket clientSocket = (BluetoothSocket) m.invoke(device, 1);

                clientSocket.connect();

                os[0] = new DataOutputStream(clientSocket.getOutputStream());

                new ClientSock().start();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("BLUETOOTH", e.getMessage());
            }
        }
    };

    registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));

    bluetooth.enable();
    if (!bluetooth.isDiscovering()) {
        bluetooth.startDiscovery();
    }


}

    public class ClientSock extends Thread {
        public void run() {
            try {
                os[0].writeBytes(GeolocationActivity.this.poiSpinner.getSelectedItem().toString()); // anything you want
                os[0].flush();
            } catch (Exception e1) {
                e1.printStackTrace();
                return;
            }
        }
    }
}
