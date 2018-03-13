package com.iteam.easyups.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iteam.easyups.R;
import com.iteam.easyups.utils.SoundMeter;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Math.log10;

/**
 * Created by Marianna on 28/02/2018.
 */

public class InformationActivity extends AppCompatActivity{
    private Context mContext;
    private TextView lightLevelTxt;
    private TextView soundLevelTxt;
    private final SoundMeter soundMeter = new SoundMeter();
    private  SensorManager mSensorManager;
    private  Sensor mLightSensor;
    private float mLightQuantity;
    private TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_layout);
        mContext = this;
        tabHost = (TabHost)findViewById(R.id.tab_informations);
        tabHost.setup();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        TabHost.TabSpec tabAbout = tabHost.newTabSpec("A propos");
        TabHost.TabSpec tabGeneral = tabHost.newTabSpec("Informations générales");

        tabAbout.setIndicator("A propos");
        tabAbout.setContent(R.id.a_propos_tab);

        tabGeneral.setIndicator("Informations générales");
        tabGeneral.setContent(R.id.informations_generales_tab);
        tabHost.addTab(tabAbout);
        tabHost.addTab(tabGeneral);


        requestMicrophone();

    }

    private void getLightLevel(){

        // Obtain references to the SensorManager and the Light Sensor
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if(mLightSensor != null){

            // Implement a listener to receive updates
            SensorEventListener listener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    mLightQuantity = event.values[0];
                    lightLevelTxt.setText(mLightQuantity + " lx");
                    Log.e("LIGHT SENSOR ", mLightQuantity+"");
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int i) {

                }
            };

            // Register the listener with the light sensor -- choosing
            // one of the SensorManager.SENSOR_DELAY_* constants.
            mSensorManager.registerListener(
                    listener, mLightSensor, SensorManager.SENSOR_DELAY_UI);

        }else{
            Log.e("LIGHT SENSOR ", "No light sensor found");
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    private void updateSoundLevel(){
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
                             /*   if(dB < -10){
                                    soundLevelImage.setImageResource(R.drawable.mouth4);
                                }else if(dB > -10 && dB <= -20){
                                    soundLevelImage.setImageResource(R.drawable.mouth3);
                                }else if(dB > -20 && dB <= -40){
                                    soundLevelImage.setImageResource(R.drawable.mouth2);
                                }else if(dB > -40){
                                    soundLevelImage.setImageResource(R.drawable.mouth1);
                                }*/

                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }

    void requestMicrophone() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
        }else{
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
            }else{
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
    public void onBackPressed(){
        this.soundMeter.stop();
        finish();
    }



}
