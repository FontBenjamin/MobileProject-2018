package com.iteam.easyups.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.iteam.easyups.R;
import com.iteam.easyups.communication.BDDRoutes;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Anomaly;
import com.iteam.easyups.model.Criticality;
import com.iteam.easyups.utils.AlertMessage;
import com.iteam.easyups.utils.Util;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Created by Marianna on 09/03/2018.
 */

public class IncidentActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private CropImageView mImageView;
    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private Button sendBtn;
    private double longitude;
    private double latitude;
    private LocationManager locationManager;
    private Context mContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissionCamera();
        setContentView(R.layout.incident_layout);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mContext = this;
        mImageView = findViewById(R.id.imagePicture);
        sendBtn = findViewById(R.id.buttonEnvoieAnomalie);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseCriticity(IncidentActivity.this.mImageView.getCroppedImage());
            }
        });


    }

    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            longitude = location.getLongitude();
            latitude = location.getLatitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };


    /**
     * Check the GPS permission
     */
    private void requestPermissionGPS() {
        if (!Util.requestPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            dispatchGPSEvent();
        }
    }

    /**
     * Check the Camera permission
     */
    private void requestPermissionCamera() {
        if (!Util.requestPermission(this,  Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            dispatchTakePictureIntent();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (Util.requestPermission(this,  Manifest.permission.ACCESS_FINE_LOCATION)) {
                dispatchGPSEvent();
            } else {
                finish();
            }
        } else {
            if (Util.requestPermission(this, Manifest.permission.CAMERA)) {
                dispatchTakePictureIntent();
            } else {
                finish();
            }
        }
    }

    /**
     * Get the localisation information
     */
    @SuppressLint("MissingPermission")
    private void dispatchGPSEvent() {
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
            requestPermissionGPS();
        }else{
            finish();
        }
    }

    /**
     * Insert the picture in db
     * @param imageBitmap The picture with the box of important element
     * @param criticality The level of criticality
     */
    private void savePicture(Bitmap imageBitmap, Criticality criticality) {
        Anomaly anomaly = new Anomaly(imageBitmap, criticality, longitude, latitude);
        anomaly.id = database.getReference().push().getKey();
        database.getReference().child(BDDRoutes.ANOMALY_PATH).child(anomaly.id).setValue(anomaly);
        finish();
    }

    /**
     * Popup to get the level of criticality
     * @param imageBitmap The taken picture
     */
    private void chooseCriticity(final Bitmap imageBitmap) {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder.setTitle("Choisir le niveau de criticité");
        LinearLayout layout = new LinearLayout(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.criticity_layout, null);
        final RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        RadioButton button;
        for (Criticality crit : Criticality.values()) {
            button = new RadioButton(this);
            button.setText(crit.getLabel());
            radioGroup.addView(button);
        }
        dialogBuilder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int WhichButton) {
                dialog.cancel();
            }
        });
        dialogBuilder.setPositiveButton("Envoyer", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //save in db
                int selectedId = radioGroup.getCheckedRadioButtonId();

                // find the radiobutton by returned id
                RadioButton radioButton = dialogView.findViewById(selectedId);

                Criticality crit = Criticality.COMFORT;
                if(radioButton == null){
                    Toast.makeText(IncidentActivity.this, "Veuillez selectionner un niveau de criticité", Toast.LENGTH_SHORT).show();
                }else {
                    switch (radioButton.getText().toString()) {
                        case "Confort":
                            crit = Criticality.COMFORT;
                            break;
                        case "Problème":
                            crit = Criticality.PROBLEM;
                            break;
                        case "Danger":
                            crit = Criticality.DANGER;
                            break;
                    }
                    if(Util.isNetworkAvailable(mContext)){
                        savePicture(imageBitmap, crit);
                    }else{
                        Util.displayErrorAlert(AlertMessage.ERROR_TYPE, AlertMessage.NETWORK_ERROR, mContext);
                    }

                }
            }
        });

        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

}