package com.iteam.easyups.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.iteam.easyups.utils.Util;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GeolocationActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private boolean waitThreadRunning = false;
    private Context context;
    private BluetoothServerSocket mServerSocket;
    // in `MainActivity` class...
    public static final String SERVICE_PINGPONG_NAME = "BLUETOOTH PINGPONG M2 DL";
    // we will derivate the UUID from the name of our service
    public static final UUID SERVICE_PINGPONG_UUID =
            UUID.nameUUIDFromBytes(SERVICE_PINGPONG_NAME.getBytes());
    private final List<Place> buildingList = new ArrayList<>();
    private final List<Place> amphitheaterList = new ArrayList<>();
    private final List<Place> poiList = new ArrayList<>();
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private GoogleMap map;

    private Spinner buildingSpinner;
    private Spinner amphitheaterSpinner;
    private Spinner poiSpinner;

    private FloatingActionButton removePoiButton;
    private FloatingActionButton bluetooth;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<BluetoothDevice> mVisibleDevices = new ArrayList<>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        // this method is called each time there is a new event on which we subscribed
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // get the BluetoothDevice object and its info from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // add it if not a duplicate
                if (!mVisibleDevices.contains(device)) {
                    mVisibleDevices.add(device);

                    // we will define this method later, create a stub for now
                }
            }
        }
    };
    private BluetoothDevice mmDevice;
    private Runnable mListenThread = new Runnable() {
        @Override
        public void run() {
            try {
                System.out.println("Waiting for a client connection...");

                // wait for a client...
                BluetoothSocket clientSocket = mServerSocket.accept();
                System.out.println(String.format("Connected to %s (%s)",
                        clientSocket.getRemoteDevice().getName(),
                        clientSocket.getRemoteDevice().getAddress()));

                // send a simple message and wait for a response
                Place placeToSend = ((Place)poiSpinner.getSelectedItem());
                String messageToSend = placeToSend.getName()+"|"+placeToSend.getLongitude()+"|"+placeToSend.getLatitude();
                clientSocket.getOutputStream().write((messageToSend+"\n").getBytes());
                String message = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())).readLine();
                System.out.println(String.format("Received message : %s", message));

                // the client socket should be closed now
                clientSocket.close();
                mServerSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error when waiting for connection!");
            }
            waitThreadRunning = false;
        }
    };
    private ArrayList<BluetoothDevice> arrayList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.activity_geolocation);

        // Bluetooth part
        ApplicationInfo applicationInfo = this.getApplicationInfo();
        final int stringId = applicationInfo.labelRes;
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
                final Button envoyer = dialog.findViewById(R.id.buttonEnvoie);
                final Button recevoir = dialog.findViewById(R.id.buttonRecevoir);
                final Spinner bluetoothSpinner = dialog.findViewById(R.id.spinnerBluetooth);
                final TextView spinnerText = dialog.findViewById(R.id.spinnerText);
                final Button bluetoothOK = dialog.findViewById(R.id.bluetoothOK);
                final TextView receptionText = dialog.findViewById(R.id.textViewReception);
                final ProgressBar receptionProgressBar = dialog.findViewById(R.id.progressBarReception);
                spinnerText.setVisibility(View.INVISIBLE);
                bluetoothOK.setVisibility(View.INVISIBLE);
                bluetoothSpinner.setVisibility(View.INVISIBLE);
                receptionText.setVisibility(View.INVISIBLE);
                receptionProgressBar.setVisibility(View.INVISIBLE);

                recevoir.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // recevoir le point d'interêt
                        envoyer.setEnabled(false);
                        // bluetooth is off, ask user to on it.
                        if (bluetoothAdapter == null) {
                            // device doesn't support bluetooth
                        }
                        else {
                            bluetoothSpinner.setVisibility(View.VISIBLE);
                            spinnerText.setVisibility(View.VISIBLE);
                            bluetoothOK.setVisibility(View.VISIBLE);

                            // bluetooth is off, ask user to on it.
                            if(!bluetoothAdapter.isEnabled()) {
                                Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableAdapter, 0);
                            }

                            // Do whatever you want to do with your bluetoothAdapter
                            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
                            if (bondedDevices.size() > 0) {
                                final ArrayList<BluetoothDevice> arrayList = new ArrayList<>();
                                for(BluetoothDevice b : bondedDevices){
                                    arrayList.add(b);
                                }
                                String[] strings = new String[arrayList.size()];
                                int i = 0;
                                for(BluetoothDevice b : arrayList){
                                    strings[i] = b.getName();
                                    i++;
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                                        GeolocationActivity.this, android.R.layout.simple_spinner_item, strings);
                                spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
                                bluetoothSpinner.setAdapter(spinnerArrayAdapter);
                                bluetoothOK.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mmDevice = arrayList.get(bluetoothSpinner.getSelectedItemPosition());
                                            // receive data here
                                            try {
                                                BluetoothSocket socket = mmDevice.createRfcommSocketToServiceRecord(GeolocationActivity.SERVICE_PINGPONG_UUID);
                                                socket.connect();

                                                String message = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                                                System.out.println("**********************************************");
                                                System.out.println("****************** MESSAGE *******************");
                                                System.out.println("**********************************************");
                                                System.out.println(message);
                                                System.out.println("**********************************************");
                                                Place placeToReceive = new Place();
                                                String[] splited = message.split("\\|+");
                                                placeToReceive.setName(splited[0]);
                                                placeToReceive.setLongitude(Double.parseDouble(splited[1]));
                                                placeToReceive.setLatitude(Double.parseDouble(splited[2]));
                                                // TODO save poi here
                                                System.out.println("Place : "+placeToReceive.getName()+" "+placeToReceive.getLongitude()+" "+placeToReceive.getLatitude());
                                                socket.getOutputStream().write("Hello back, dear Bluetooth server!\n".getBytes());
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                    }
                                });
                            }
                        }
                    }
                });

                envoyer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recevoir.setEnabled(false);

                        // bluetooth is off, ask user to on it.
                        if (bluetoothAdapter == null) {
                            // device doesn't support bluetooth
                        }
                        else {
                            bluetoothSpinner.setVisibility(View.VISIBLE);
                            spinnerText.setVisibility(View.VISIBLE);
                            bluetoothOK.setVisibility(View.VISIBLE);

                            // creating a bluetooth object
                            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                            if (adapter == null) {
                                // create a 'Toast', representing an ephemeral message to be displayed, and show it
                                Toast.makeText(GeolocationActivity.this, "Bluetooth not supported on this device!", Toast.LENGTH_LONG).show();
                                System.exit(RESULT_OK);
                            }
                            // ask to enable bluetooth if not enabled
                            if (!adapter.isEnabled()) {
                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableBtIntent, 1);
                            }
                            // register for broadcasts when a device is discovered
                            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                            registerReceiver(mReceiver, filter);

                            // get the adapter and keep its reference
                            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            mVisibleDevices.clear();
                            // start discovering the surrounding bluetooth devices
                            //misibleDevices = mBluetoothAdapter.getBondedDevices();//mBluetoothAdapter.startDiscovery();
                           // if (mVisibleDevices.size() > 0) {
                                //arrayList = new ArrayList<>();
                                for(BluetoothDevice b : mBluetoothAdapter.getBondedDevices()){
                                    mVisibleDevices.add(b);
                                }
                                String[] strings = new String[mVisibleDevices.size()];
                                int i = 0;
                                for(BluetoothDevice b : mVisibleDevices){
                                    strings[i] = b.getName();
                                    i++;
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                                        GeolocationActivity.this, android.R.layout.simple_spinner_item, strings);
                                spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
                                bluetoothSpinner.setAdapter(spinnerArrayAdapter);
                                bluetoothOK.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        receptionText.setVisibility(View.VISIBLE);
                                        receptionProgressBar.setVisibility(View.VISIBLE);
                                        bluetoothSpinner.setVisibility(View.INVISIBLE);
                                        spinnerText.setVisibility(View.INVISIBLE);
                                        bluetoothOK.setVisibility(View.INVISIBLE);
                                        mmDevice = mVisibleDevices.get(bluetoothSpinner.getSelectedItemPosition());


                                            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                            // still in the onCreate method
                                            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                                            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                                            startActivity(discoverableIntent);
                                        synchronized (this) {
                                            if (!waitThreadRunning) {
                                                try {
                                                    mServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
                                                            GeolocationActivity.SERVICE_PINGPONG_NAME,
                                                            GeolocationActivity.SERVICE_PINGPONG_UUID);

                                                    // the server thread, defined later, will be "mListenThread"
                                                    new Thread(mListenThread).start();
                                                    waitThreadRunning = true;

                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                    }}
                                });
                            }
                        //}
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

        removePoiButton = (FloatingActionButton) findViewById(R.id.remove_poi);

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

    /**
     * This method is execute when google map is ready
     * @param googleMap The map
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (!Util.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        else
        {
            map.setMyLocationEnabled(true);
        }

        // Open activity with camera on Paul Sabatier
        LatLng latLng = new LatLng(43.562038, 1.466371);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));

        createPoiOnMap();
    }

    /**
     * Recover buildings from database
     */
    private void buildingManagement()
    {
        DatabaseReference ref = database.getReference(BDDRoutes.BUILDING_PATH);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                buildingList.clear();
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

    /**
     * Add buildings on spinner
     */
    private void addBuildingsOnSpinner()
    {
        Place place = new Place();
        place.setName("Bâtiments");
        buildingList.add(0, place);

        PlaceSpinnerAdapter dataAdapter = new PlaceSpinnerAdapter(context, R.layout.support_simple_spinner_dropdown_item, buildingList);
        buildingSpinner.setAdapter(dataAdapter);
    }

    /**
     * Recover amphitheaters from database
     */
    private void amphitheaterManagement()
    {
        DatabaseReference ref = database.getReference(BDDRoutes.AMPHIS_PATH);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                amphitheaterList.clear();
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

    /**
     * Add amphitheaters on spinner
     */
    private void addAmphitheatersOnSpinner()
    {
        Place place = new Place();
        place.setName("Amphis");
        amphitheaterList.add(0, place);

        PlaceSpinnerAdapter dataAdapter = new PlaceSpinnerAdapter(context, R.layout.support_simple_spinner_dropdown_item, amphitheaterList);
        amphitheaterSpinner.setAdapter(dataAdapter);
    }

    /**
     * Recover points of interest from database
     */
    private void poiManagement()
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
            removePoiButton.setEnabled(false);
        }
    }

    /**
     * Add points of interest on spinner
     */
    private void addPoiOnSpinner()
    {
        Place place = new Place();
        place.setName("Points d'intérêt");
        poiList.add(0, place);

        PlaceSpinnerAdapter dataAdapter = new PlaceSpinnerAdapter(context, R.layout.support_simple_spinner_dropdown_item, poiList);
        poiSpinner.setAdapter(dataAdapter);

        if (poiList.size() == 1)
        {
            poiSpinner.setEnabled(false);
        }
        removePoiButton.setEnabled(false);
    }

    /**
     * Create a new point of interest on google map
     */
    private void createPoiOnMap()
    {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(final LatLng latLng) {
                final EditText poiEditText = new EditText(context);
                poiEditText.setHint("Nom");
                AlertDialog dialog = new AlertDialog.Builder(context)
                        .setTitle("Nouveau point d'intérêt")
                        .setView(poiEditText)
                        .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                map.clear();
                                String poiName = String.valueOf(poiEditText.getText());
                                Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title(poiName));
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

    /**
     * Save point of interest in the database
     * @param latLng Latitude and longitude of point of interest
     * @param poiName Name of point of interest
     */
    private void savePoi(LatLng latLng, String poiName)
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

    /**
     * Remove a point of interest from the database
     */
    private void removePoi()
    {
        removePoiButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (poiSpinner.getSelectedItemPosition() != 0)
                {
                    map.clear();
                    Place poi = (Place) poiSpinner.getSelectedItem();
                    final FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {
                        String userId = user.getUid();
                        database.getReference(BDDRoutes.USERS_PATH).child(userId).child("poi").child(poi.getId()).setValue(null);
                    }
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.buildings:
                map.clear();
                if (position != 0)
                {
                    Place building = (Place) parent.getItemAtPosition(position);
                    if (building != null) {
                        LatLng latLng = new LatLng(building.getLatitude(), building.getLongitude());
                        Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title("Bâtiment " + building.getName()));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        poiSpinner.setSelection(0);
                        amphitheaterSpinner.setSelection(0);
                        removePoiButton.setEnabled(false);
                    }
                }
                break;
            case R.id.amphitheaters:
                map.clear();
                if (position != 0)
                {
                    Place amphitheater = (Place) parent.getItemAtPosition(position);
                    if (amphitheater != null) {
                        LatLng latLng = new LatLng(amphitheater.getLatitude(), amphitheater.getLongitude());
                        Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title("Amphi " + amphitheater.getName()));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        buildingSpinner.setSelection(0);
                        poiSpinner.setSelection(0);
                        removePoiButton.setEnabled(false);
                    }
                }
                break;
            case R.id.poi:
                map.clear();
                if (position != 0)
                {
                    Place poi = (Place) parent.getItemAtPosition(position);
                    if (poi != null) {
                        LatLng latLng = new LatLng(poi.getLatitude(), poi.getLongitude());
                        Marker currentMarker = map.addMarker(new MarkerOptions().position(latLng).title(poi.getName()));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        buildingSpinner.setSelection(0);
                        amphitheaterSpinner.setSelection(0);
                        removePoiButton.setEnabled(true);
                    }
                }
                else
                {
                    removePoiButton.setEnabled(false);
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Util.requestPermission(this,  Manifest.permission.ACCESS_FINE_LOCATION)) {
            map.setMyLocationEnabled(true);
        }
    }
}
