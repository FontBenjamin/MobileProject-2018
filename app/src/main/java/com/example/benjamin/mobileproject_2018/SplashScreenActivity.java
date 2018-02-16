package com.example.benjamin.mobileproject_2018;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * this activity is here only to display the launchescreen of the application
 * and will launch the main activity right after its creation and destroy itself.
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
