package com.iteam.easyups.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.iteam.easyups.R;

/**
 * Created by Marianna on 27/02/2018.
 */

public class TimetableWebViewActivity extends AppCompatActivity {
    private Context mContext;
    private WebView webView;
    private ProgressBar progress;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview_layout);
        webView = (WebView) findViewById(R.id.webview);
        progress = findViewById(R.id.progressBarEDT);
        progress.bringToFront();
        text = findViewById(R.id.textViewEDT);
        text.bringToFront();
        mContext = this;
        Intent i = getIntent();
        displayEdt(i.getStringExtra("timeTableUrl"));
        webView.getSettings().setBuiltInZoomControls(true);
    }

    private void displayEdt(String timeTableUrl) {
        Log.e("TIMETABLE ", timeTableUrl);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(mContext, description, Toast.LENGTH_SHORT).show();

            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.INVISIBLE);
                text.setVisibility(View.INVISIBLE);
            }

        });

        webView.loadUrl(timeTableUrl);

    }


}
