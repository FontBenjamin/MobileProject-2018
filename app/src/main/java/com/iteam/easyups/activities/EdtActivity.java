package com.iteam.easyups.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteam.easyups.R;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Formation;
import com.iteam.easyups.utils.HtmlParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Marianna on 23/02/2018.
 */

public class EdtActivity extends AppCompatActivity {

    private String[] edtMainPages = {"https://edt.univ-tlse3.fr/FSI/2017_2018/index.html", "https://edt.univ-tlse3.fr/F2SMH/2017_2018/index.html"};
    private WebView webview;
    private String testUrl = "https://edt.univ-tlse3.fr/FSI/2017_2018/L1/L1_SF/g253956.xml";
    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private final static String FORMATION_PATH = "easyups/formations/";
    private Context mContext;
    private Spinner spinnerButtons;

    private List<String> levels;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edt_layout);
       // webview = (WebView) findViewById(R.id.webview);
        spinnerButtons = (Spinner) findViewById(R.id.toggle_button);
        levels = new ArrayList<>();
        mContext = this;

        if(isNetworkAvailable()){
            saveAllFormation();
        }
        getFormationByLevel();
        //displayEdt(this);
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

   private void displayEdt(final Context context){
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(context, description, Toast.LENGTH_SHORT).show();

            }
            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });

        webview.loadUrl(testUrl);
    }




    public void getFormationByLevel(){
        database.getReference().child(FORMATION_PATH).child("FSI").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            levels.add(data.getKey());
                        }
                        addButtonToLayout();
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }

    private void addButtonToLayout() {


            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, levels);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerButtons.setAdapter(adapter);

    }


    public void saveAllFormation(){
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(FORMATION_PATH).exists()) {
                    new HtmlParser( database.getReference().child(FORMATION_PATH)).execute(edtMainPages);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
