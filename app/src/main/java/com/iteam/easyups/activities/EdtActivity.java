package com.iteam.easyups.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iteam.easyups.R;
import com.iteam.easyups.communication.FormationService;
import com.iteam.easyups.model.Formation;
import com.iteam.easyups.utils.HtmlParser;

import java.io.IOException;

/**
 * Created by Marianna on 23/02/2018.
 */

public class EdtActivity extends AppCompatActivity {

    private String[] edtMainPages = {"https://edt.univ-tlse3.fr/FSI/2017_2018/index.html", "https://edt.univ-tlse3.fr/F2SMH/2017_2018/index.html"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edt_layout);

        if(isNetworkAvailable()){
            new FormationService().saveAllFormation(edtMainPages);
        }

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
