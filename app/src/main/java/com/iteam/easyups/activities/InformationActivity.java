package com.iteam.easyups.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private ToggleButton detectSoundLevelBtn;
    private TextView soundLevelTxt;
    private final SoundMeter soundMeter = new SoundMeter();
    private Handler mHandler = new Handler();
    private ImageView soundLevelImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information_layout);
        mContext = this;
        detectSoundLevelBtn = (ToggleButton) findViewById(R.id.detect_sound);
        detectSoundLevelBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    try {
                        soundMeter.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    soundLevelTxt.setText(" décibels");
                    soundLevelImage.setImageDrawable(null);
                    soundMeter.stop();
                }

            }
        });
        soundLevelTxt = (TextView) findViewById(R.id.sound_level);
        soundLevelImage = (ImageView) findViewById(R.id.image_sound);
        updateSoundLevel();
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
                                if(detectSoundLevelBtn.isChecked()){
                                    double dB = 20 * log10(soundMeter.getAmplitude() / 32767.0);
                                    soundLevelTxt.setText(dB + " décibels");
                                    if(dB < -10){
                                        soundLevelImage.setImageResource(R.drawable.mouth4);
                                    }else if(dB > -10 && dB <= -20){
                                        soundLevelImage.setImageResource(R.drawable.mouth3);
                                    }else if(dB > -20 && dB <= -40){
                                        soundLevelImage.setImageResource(R.drawable.mouth2);
                                    }else if(dB > -40){
                                        soundLevelImage.setImageResource(R.drawable.mouth1);
                                    }
                                }
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }






}
