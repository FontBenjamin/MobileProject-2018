package com.iteam.easyups.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TabHost;
import android.widget.TextView;

import com.iteam.easyups.R;
import com.iteam.easyups.utils.AlertMessage;
import com.iteam.easyups.utils.SoundMeter;
import com.iteam.easyups.utils.Util;

import java.io.IOException;

import static java.lang.Math.log10;

/**
 * Created by Marianna on 28/02/2018.
 */

public class InformationActivity extends AppCompatActivity {
    private Context mContext;
    private TextView lightLevelTxt;
    private TextView soundLevelTxt;
    private final SoundMeter soundMeter = new SoundMeter();
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private float mLightQuantity;
    private TabHost tabHost;

    private String tabAboutTitle = "A propos";
    private String tabGeneralTitle = "Informations générales";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_layout);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mContext = this;
        tabHost = (TabHost) findViewById(R.id.tab_informations);
        tabHost.setup();

        TabHost.TabSpec tabAbout = tabHost.newTabSpec(tabAboutTitle);
        TabHost.TabSpec tabGeneral = tabHost.newTabSpec(tabGeneralTitle);

        tabAbout.setIndicator(tabAboutTitle);
        tabAbout.setContent(R.id.a_propos_tab);

        tabGeneral.setIndicator(tabGeneralTitle);
        tabGeneral.setContent(R.id.informations_generales_tab);
        tabHost.addTab(tabAbout);
        tabHost.addTab(tabGeneral);

        requestMicrophone();

    }

    /**
     * Get light using SensorManage Sensor.TYPE_LIGHT and display it
     */
    private void getLightLevel() {

        // Obtain references to the SensorManager and the Light Sensor
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (mLightSensor != null) {

            // Implement a listener to receive updates
            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    mLightQuantity = event.values[0];
                    lightLevelTxt.setText(mLightQuantity + " lx");
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };

            // Register the listener with the light sensor -- choosing
            // one of the SensorManager.SENSOR_DELAY_* constants.
            mSensorManager.registerListener(
                    listener, mLightSensor, SensorManager.SENSOR_DELAY_UI);

        } else {
            Util.displayErrorAlert(AlertMessage.LIGHT_ERROR_TYPE, AlertMessage.LIGHT_SENSOR_ERROR, this);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }

    /**
     * Get sound using microphone and display it
     */
    private void updateSoundLevel() {
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                double dB = 20 * log10(soundMeter.getAmplitude() / 32767.0);
                                soundLevelTxt.setText(dB + " décibels");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }


    private void requestMicrophone() {
        if (!Util.requestPermission(this, Manifest.permission.RECORD_AUDIO)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        } else {
            try {
                soundMeter.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            soundLevelTxt = (TextView) findViewById(R.id.sound_level);
            lightLevelTxt = (TextView) findViewById(R.id.light_level);
            updateSoundLevel();
            getLightLevel();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                finish();
            } else {
                try {
                    soundMeter.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                soundLevelTxt = (TextView) findViewById(R.id.sound_level);
                lightLevelTxt = (TextView) findViewById(R.id.light_level);
                updateSoundLevel();
                getLightLevel();
            }
        }
    }

    @Override
    public void onBackPressed() {
        this.soundMeter.stop();
        finish();
    }


}
