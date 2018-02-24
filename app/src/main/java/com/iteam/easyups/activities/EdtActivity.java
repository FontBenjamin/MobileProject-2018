package com.iteam.easyups.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iteam.easyups.R;
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

            new HtmlParser().execute(edtMainPages);


    }
}
