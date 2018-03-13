package com.iteam.easyups.activities;

import android.content.Context;
import android.content.Intent;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ListView;

import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.daimajia.slider.library.Tricks.ViewPagerEx;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteam.easyups.R;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.User;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth auth;
    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.webView = (WebView) findViewById(R.id.webview);
        this.webView.setWebViewClient(new WebViewClient());
        this.webView.loadUrl("https://twitter.com/ut3paulsabatier");
        this.webView.getSettings().setJavaScriptEnabled(true);
        this.webView.getSettings().setDomStorageEnabled(true);

        ApplicationInfo applicationInfo = this.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String result = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : this.getString(stringId);
        this.setTitle(result);
        auth = FirebaseAuth.getInstance();



        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

   /** @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_anomaly) {
            startActivity(new Intent(MainActivity.this, IncidentActivity.class));
        } else if (id == R.id.nav_edt) {
            if(isNetworkAvailable()){
               getUserEdt();
            }else{
                startActivity(new Intent(MainActivity.this, TimetableActivity.class));
            }

        } else if (id == R.id.nav_geo) {
            Intent geolocation = new Intent(this, GeolocationActivity.class);
            startActivity(geolocation);
        } else if (id == R.id.nav_information) {
            startActivity(new Intent(MainActivity.this, InformationActivity.class));
        } else if (id == R.id.nav_params) {
            Intent signup = new Intent(this, SingupActivity.class);
            startActivity(signup);
        } else if (id == R.id.nav_qrcode) {
            Intent intent = new Intent(this, ScanActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void getUserEdt(){
        final FirebaseUser user = auth.getCurrentUser();
        //check user is logged in
        if (user != null) {
            String userID = user.getUid();
            database.getReference().child("easyups/Users").child(userID).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            //TODO remove when functionnal
                            user.setEDT("https://edt.univ-tlse3.fr/F2SMH/2017_2018/L1/L1_STAPS/g223424.xml");
                            if(user.getEDT() != null){
                                Intent i = new Intent(MainActivity.this, TimetableWebViewActivity.class);
                                i.putExtra("timeTableUrl", user.getEDT());
                                startActivity(i);
                            }else{
                                startActivity(new Intent(MainActivity.this, TimetableActivity.class));
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    }
            );

        }else{
            startActivity(new Intent(MainActivity.this, TimetableActivity.class));
        }
    }

}
