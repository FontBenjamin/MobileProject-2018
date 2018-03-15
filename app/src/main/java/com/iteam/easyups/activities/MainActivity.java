package com.iteam.easyups.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteam.easyups.R;
import com.iteam.easyups.communication.BDDRoutes;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.User;
import com.iteam.easyups.utils.Util;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth auth;
    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private WebView webView;
    private String twitterURL = "https://twitter.com/ut3paulsabatier";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.webView = (WebView) findViewById(R.id.webview);
        this.webView.setWebViewClient(new WebViewClient());
        this.webView.loadUrl(twitterURL);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_anomaly) {
            startActivity(new Intent(MainActivity.this, IncidentActivity.class));
        } else if (id == R.id.nav_edt) {
            if(Util.isNetworkAvailable(this)){
               getUserTimetable();
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

    /**
     * Load directly user timetable if he is loggued in
     * Otherwise, redirect to the formation choice
     */
    private void getUserTimetable(){
        final FirebaseUser firebaseUser = auth.getCurrentUser();
        //check user is logged in
        if (firebaseUser != null) {
            String userID = firebaseUser.getUid();
            database.getReference().child(BDDRoutes.USERS_PATH).child(userID).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            if(user.getTimetableLink()!= null){
                                Intent i = new Intent(MainActivity.this, TimetableWebViewActivity.class);
                                i.putExtra("timeTableUrl", user.getTimetableLink());
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.changeEdt:
                auth.signOut();
                return true;
            default:
                finish();
                return super.onOptionsItemSelected(item);
        }
    }
}
