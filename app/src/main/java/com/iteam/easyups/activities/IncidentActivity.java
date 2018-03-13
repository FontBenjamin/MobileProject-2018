package com.iteam.easyups.activities;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
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
import com.iteam.easyups.utils.GPSTracker;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.security.Permission;

/**
 * Created by Marianna on 09/03/2018.
 */

public class IncidentActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private CropImageView mImageView;
    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private Button buttonEnvoi;
    private double longitude;
    private double latitude;
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
    private LocationManager locationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionCamera();
        } else {
            dispatchTakePictureIntent();
        }
        setContentView(R.layout.incident_layout);

        mImageView = findViewById(R.id.imagePicture);
        buttonEnvoi = findViewById(R.id.buttonEnvoieAnomalie);
        buttonEnvoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseCriticity(IncidentActivity.this.mImageView.getCroppedImage());
            }
        });


    }


    void requestPermissionGPS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        } else {
            dispatchGPSEvent();
        }
    }


    void requestPermissionCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            dispatchTakePictureIntent();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                dispatchGPSEvent();
            } else {
                finish();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                finish();
            }
        }
    }

    private void dispatchGPSEvent() {
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        //}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionGPS();
            } else {
                dispatchGPSEvent();
            }
        }else{
            finish();
        }
    }


    private void savePicture(Bitmap imageBitmap, Criticality criticality) {
        Anomaly anomaly = new Anomaly(imageBitmap, criticality, longitude, latitude);
        anomaly.id = database.getReference().push().getKey();
        database.getReference().child(BDDRoutes.ANOMALY_PATH).child(anomaly.id).setValue(anomaly);
        finish();
    }

    public void chooseCriticity(final Bitmap imageBitmap) {
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

                Criticality c = Criticality.COMFORT;
                switch (radioButton.getText().toString()) {
                    case "Confort":
                        c = Criticality.COMFORT;
                        break;
                    case "Problème":
                        c = Criticality.PROBLEM;
                        break;
                    case "Danger":
                        c = Criticality.DANGER;
                        break;
                }
                savePicture(imageBitmap, c);
            }
        });

        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


}