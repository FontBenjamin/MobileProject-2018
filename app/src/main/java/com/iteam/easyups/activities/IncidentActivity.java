package com.iteam.easyups.activities;


import android.Manifest;
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
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.firebase.database.FirebaseDatabase;
import com.iteam.easyups.R;
import com.iteam.easyups.communication.BDDRoutes;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Anomaly;
import com.iteam.easyups.model.Criticality;
import com.iteam.easyups.utils.GPSTracker;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * Created by Marianna on 09/03/2018.
 */

public class IncidentActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private CropImageView mImageView;
    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private Button buttonEnvoi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.incident_layout);

        mImageView = findViewById(R.id.imagePicture);
        buttonEnvoi = findViewById(R.id.buttonEnvoieAnomalie);
        buttonEnvoi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseCriticity(IncidentActivity.this.mImageView.getCroppedImage());
            }
        });
        dispatchTakePictureIntent();

    }



    private void configureButton() {
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
        }
    }


    private void savePicture(Bitmap imageBitmap, Criticality criticality) {
        Anomaly anomaly = new Anomaly(imageBitmap, criticality);
        anomaly.id = database.getReference().push().getKey();
        database.getReference().child(BDDRoutes.ANOMALY_PATH).child(anomaly.id).setValue(anomaly);
    }

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    /**
     * @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
     * super.onRequestPermissionsResult(requestCode, permissions, grantResults);
     * if (requestCode == 0 && grantResults.length < 1) {
     * requestPermission();
     * } else {
     * finish();
     * }
     * }
     */

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
                LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
                }
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 10, locationListener);
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();
                savePicture(imageBitmap, c);
                //finish();
            }
        });

        dialogBuilder.setView(dialogView);
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }


}