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
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class GeolocationActivity extends AppCompatActivity implements OnMapReadyCallback, AdapterView.OnItemSelectedListener {

    private Context context;

    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothServerSocket bluetoothServerSocket;
    private BluetoothDevice bluetoothDevice;
    private BroadcastReceiver broadcastReceiver;
    private Runnable listenThread;
    private ArrayList<BluetoothDevice> visibleDevices = new ArrayList<>();

    private static final String SERVICE_PINGPONG_NAME = "BLUETOOTH PINGPONG EASY UPS";
    private static final UUID SERVICE_PINGPONG_UUID = UUID.nameUUIDFromBytes(SERVICE_PINGPONG_NAME.getBytes());

    private final List<Place> buildingList = new ArrayList<>();
    private final List<Place> amphitheaterList = new ArrayList<>();
    private final List<Place> poiList = new ArrayList<>();

    private GoogleMap map;

    private Spinner buildingSpinner;
    private Spinner amphitheaterSpinner;
    private Spinner poiSpinner;

    private FloatingActionButton removePoiButton;
    private FloatingActionButton bluetoothButton;

    private boolean waitThreadRunning = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_geolocation);

        buildingSpinner = (Spinner) findViewById(R.id.buildings);
        buildingSpinner.setOnItemSelectedListener(this);
        amphitheaterSpinner = (Spinner) findViewById(R.id.amphitheaters);
        amphitheaterSpinner.setOnItemSelectedListener(this);
        poiSpinner = (Spinner) findViewById(R.id.poi);
        poiSpinner.setOnItemSelectedListener(this);
        removePoiButton = (FloatingActionButton) findViewById(R.id.remove_poi);
        bluetoothButton = (FloatingActionButton) findViewById(R.id.bluetooth);

        initBluetoothButton();
        buildingManagement();
        amphitheaterManagement();
        poiManagement();
        removePoi();
        bluetoothManagement();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * This method is execute when google map is ready
     *
     * @param googleMap The map
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (!Util.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
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
    private void buildingManagement() {
        DatabaseReference ref = database.getReference(BDDRoutes.BUILDING_PATH);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                buildingList.clear();
                GenericTypeIndicator<List<Place>> genericTypeIndicator = new GenericTypeIndicator<List<Place>>() {
                };
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
    private void addBuildingsOnSpinner() {
        Place place = new Place();
        place.setName("Bâtiments");
        buildingList.add(0, place);

        PlaceSpinnerAdapter dataAdapter = new PlaceSpinnerAdapter(context, R.layout.support_simple_spinner_dropdown_item, buildingList);
        buildingSpinner.setAdapter(dataAdapter);
    }

    /**
     * Recover amphitheaters from database
     */
    private void amphitheaterManagement() {
        DatabaseReference ref = database.getReference(BDDRoutes.AMPHIS_PATH);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                amphitheaterList.clear();
                GenericTypeIndicator<List<Place>> genericTypeIndicator = new GenericTypeIndicator<List<Place>>() {
                };
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
    private void addAmphitheatersOnSpinner() {
        Place place = new Place();
        place.setName("Amphis");
        amphitheaterList.add(0, place);

        PlaceSpinnerAdapter dataAdapter = new PlaceSpinnerAdapter(context, R.layout.support_simple_spinner_dropdown_item, amphitheaterList);
        amphitheaterSpinner.setAdapter(dataAdapter);
    }

    /**
     * Recover points of interest from database
     */
    private void poiManagement() {
        final FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            String userId = user.getUid();

            DatabaseReference ref = database.getReference(BDDRoutes.USERS_PATH).child(userId).child("poi");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    poiList.clear();
                    GenericTypeIndicator<Map<String, Place>> genericTypeIndicator = new GenericTypeIndicator<Map<String, Place>>() {
                    };
                    if (genericTypeIndicator != null) {
                        Map<String, Place> poiAsMap = dataSnapshot.getValue(genericTypeIndicator);
                        if (poiAsMap != null) {
                            for (Map.Entry<String, Place> place : poiAsMap.entrySet()) {
                                Place poi = place.getValue();
                                poi.setId(place.getKey());
                                poiList.add(poi);
                            }
                        } else {
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
        } else {
            poiSpinner.setEnabled(false);
            removePoiButton.setEnabled(false);
        }
    }

    /**
     * Add points of interest on spinner
     */
    private void addPoiOnSpinner() {
        Place place = new Place();
        place.setName("Points d'intérêt");
        poiList.add(0, place);

        PlaceSpinnerAdapter dataAdapter = new PlaceSpinnerAdapter(context, R.layout.support_simple_spinner_dropdown_item, poiList);
        poiSpinner.setAdapter(dataAdapter);

        if (poiList.size() == 1) {
            poiSpinner.setEnabled(false);
        }
        removePoiButton.setEnabled(false);
    }

    /**
     * Create a new point of interest on google map
     */
    private void createPoiOnMap() {
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
                                map.addMarker(new MarkerOptions().position(latLng).title(poiName));
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
     *
     * @param latLng  Latitude and longitude of point of interest
     * @param poiName Name of point of interest
     */
    private void savePoi(LatLng latLng, String poiName) {
        final FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
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
    private void removePoi() {
        removePoiButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (poiSpinner.getSelectedItemPosition() != 0) {
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

    /**
     * Init the bluetooth button
     * Disable if user is not connected otherwise enable
     */
    private void initBluetoothButton() {
        final FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            bluetoothButton.setEnabled(true);
        } else {
            bluetoothButton.setEnabled(false);
        }
    }

    private void bluetoothManagement() {
        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.bluetooth_question);

                // set the custom dialog components - text, image and button
                final Button sendButton = dialog.findViewById(R.id.sendButton);
                final Button receiveButton = dialog.findViewById(R.id.receiveButton);
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

                receiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendButton.setEnabled(false);
                        sendButton.setBackgroundColor(Color.GRAY);

                        if (bluetoothAdapter != null) {
                            spinnerText.setText("Expéditeur");

                            bluetoothSpinner.setVisibility(View.VISIBLE);
                            spinnerText.setVisibility(View.VISIBLE);
                            bluetoothOK.setVisibility(View.VISIBLE);

                            // bluetooth is off, ask user to on it.
                            if (!bluetoothAdapter.isEnabled()) {
                                Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableAdapter, 0);
                            }

                            // Do whatever you want to do with your bluetoothAdapter
                            Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
                            if (bondedDevices.size() > 0) {
                                final ArrayList<BluetoothDevice> arrayList = new ArrayList<>();
                                for (BluetoothDevice b : bondedDevices) {
                                    arrayList.add(b);
                                }
                                String[] strings = new String[arrayList.size()];
                                int i = 0;
                                for (BluetoothDevice b : arrayList) {
                                    strings[i] = b.getName();
                                    i++;
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                                        GeolocationActivity.this, android.R.layout.simple_spinner_item, strings);
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                bluetoothSpinner.setAdapter(spinnerArrayAdapter);
                                bluetoothOK.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        receptionText.setVisibility(View.VISIBLE);
                                        receptionProgressBar.setVisibility(View.VISIBLE);
                                        bluetoothSpinner.setVisibility(View.INVISIBLE);
                                        spinnerText.setVisibility(View.INVISIBLE);
                                        bluetoothOK.setVisibility(View.INVISIBLE);
                                        ((TextView) dialog.findViewById(R.id.textViewReception)).setText("Réception du point d'intérêt...");
                                        bluetoothDevice = arrayList.get(bluetoothSpinner.getSelectedItemPosition());

                                        try {
                                            BluetoothSocket socket = bluetoothDevice.createRfcommSocketToServiceRecord(GeolocationActivity.SERVICE_PINGPONG_UUID);
                                            socket.connect();

                                            String message = new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine();
                                            String[] splited = message.split("\\|+");
                                            String poiName = splited[0];
                                            Double latitude = Double.parseDouble(splited[2]);
                                            Double longitude = Double.parseDouble(splited[1]);

                                            dialog.dismiss();
                                            Toast.makeText(context, "Le point d'intérét " + poiName + " a bien été reçu !", Toast.LENGTH_LONG).show();

                                            LatLng latLng = new LatLng(latitude, longitude);
                                            map.clear();
                                            map.addMarker(new MarkerOptions().position(latLng).title(poiName));
                                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                            buildingSpinner.setSelection(0);
                                            amphitheaterSpinner.setSelection(0);

                                            poiSpinner.setEnabled(true);
                                            savePoi(latLng, poiName);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    }
                });

                sendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        spinnerText.setText("Destinataire");

                        if (poiSpinner.getSelectedItemPosition() == 0) {
                            dialog.dismiss();
                            int duration = Toast.LENGTH_LONG;
                            Toast toast = Toast.makeText(context, "Créer ou sélectionner un point d'intérêt à envoyer", duration);
                            toast.show();
                        } else {

                            receiveButton.setEnabled(false);
                            receiveButton.setBackgroundColor(Color.GRAY);
                            // bluetooth is off, ask user to on it.
                            if (bluetoothAdapter != null) {
                                bluetoothSpinner.setVisibility(View.VISIBLE);
                                spinnerText.setVisibility(View.VISIBLE);
                                bluetoothOK.setVisibility(View.VISIBLE);

                                // creating a bluetooth object
                                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                                if (adapter == null) {
                                    Toast.makeText(GeolocationActivity.this, "Le bluetooth n'est pas supporté par votre appareil", Toast.LENGTH_LONG).show();
                                    System.exit(RESULT_OK);
                                }

                                // ask to enable bluetooth if not enabled
                                if (!adapter.isEnabled()) {
                                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                    startActivityForResult(enableBtIntent, 1);
                                }
                                // register for broadcasts when a device is discovered
                                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);

                                registerReceiver(getBroadcastReceiver(), filter);

                                // get the adapter and keep its reference
                                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                visibleDevices.clear();

                                // start discovering the surrounding bluetooth devices
                                for (BluetoothDevice b : bluetoothAdapter.getBondedDevices()) {
                                    visibleDevices.add(b);
                                }
                                String[] strings = new String[visibleDevices.size()];
                                int i = 0;
                                for (BluetoothDevice b : visibleDevices) {
                                    strings[i] = b.getName();
                                    i++;
                                }
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                                        GeolocationActivity.this, android.R.layout.simple_spinner_item, strings);
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                bluetoothSpinner.setAdapter(spinnerArrayAdapter);
                                bluetoothOK.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        receptionText.setVisibility(View.VISIBLE);
                                        receptionProgressBar.setVisibility(View.VISIBLE);
                                        bluetoothSpinner.setVisibility(View.INVISIBLE);
                                        spinnerText.setVisibility(View.INVISIBLE);
                                        bluetoothOK.setVisibility(View.INVISIBLE);
                                        ((TextView) dialog.findViewById(R.id.textViewReception)).setText("Envoie du point d'intérêt...");

                                        bluetoothDevice = visibleDevices.get(bluetoothSpinner.getSelectedItemPosition());

                                        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                        // still in the onCreate method
                                        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                                        startActivity(discoverableIntent);
                                        synchronized (this) {
                                            if (!waitThreadRunning) {
                                                try {
                                                    bluetoothServerSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(
                                                            GeolocationActivity.SERVICE_PINGPONG_NAME,
                                                            GeolocationActivity.SERVICE_PINGPONG_UUID);
                                                    new Thread(getListenThread()).start();
                                                    waitThreadRunning = true;
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                });
                            }
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    public Runnable getListenThread() {
        if (listenThread == null){
            listenThread = new Runnable() {
                @Override
                public void run() {
                    try {
                        // wait for a client...
                        BluetoothSocket clientSocket = bluetoothServerSocket.accept();

                        // send a simple message and wait for a response
                        Place placeToSend = ((Place) poiSpinner.getSelectedItem());
                        String messageToSend = placeToSend.getName() + "|" + placeToSend.getLongitude() + "|" + placeToSend.getLatitude();
                        clientSocket.getOutputStream().write((messageToSend + "\n").getBytes());
                        String message = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())).readLine();

                        // the client socket should be closed now
                        clientSocket.close();
                        bluetoothServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    waitThreadRunning = false;
                }
            };
        }
        return listenThread;
    }

    public BroadcastReceiver getBroadcastReceiver()
    {
        if (broadcastReceiver == null)
        {
            broadcastReceiver = new BroadcastReceiver() {
                // this method is called each time there is a new event on which we subscribed
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                        // get the BluetoothDevice object and its info from the Intent
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                        // add it if not a duplicate
                        if (!visibleDevices.contains(device)) {
                            visibleDevices.add(device);
                        }
                    }
                }
            };
        }
        return broadcastReceiver;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.buildings:
                if (position != 0) {
                    map.clear();
                    Place building = (Place) parent.getItemAtPosition(position);
                    if (building != null) {
                        LatLng latLng = new LatLng(building.getLatitude(), building.getLongitude());
                        map.addMarker(new MarkerOptions().position(latLng).title("Bâtiment " + building.getName()));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        poiSpinner.setSelection(0);
                        amphitheaterSpinner.setSelection(0);
                        removePoiButton.setEnabled(false);
                    }
                }
                break;
            case R.id.amphitheaters:
                if (position != 0) {
                    map.clear();
                    Place amphitheater = (Place) parent.getItemAtPosition(position);
                    if (amphitheater != null) {
                        LatLng latLng = new LatLng(amphitheater.getLatitude(), amphitheater.getLongitude());
                        map.addMarker(new MarkerOptions().position(latLng).title("Amphi " + amphitheater.getName()));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        buildingSpinner.setSelection(0);
                        poiSpinner.setSelection(0);
                        removePoiButton.setEnabled(false);
                    }
                }
                break;
            case R.id.poi:
                if (position != 0) {
                    map.clear();
                    Place poi = (Place) parent.getItemAtPosition(position);
                    if (poi != null) {
                        LatLng latLng = new LatLng(poi.getLatitude(), poi.getLongitude());
                        map.addMarker(new MarkerOptions().position(latLng).title(poi.getName()));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));

                        buildingSpinner.setSelection(0);
                        amphitheaterSpinner.setSelection(0);
                        removePoiButton.setEnabled(true);
                    }
                } else {
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
        if (Util.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            map.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
