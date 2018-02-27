package com.iteam.easyups.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.Intents;
import com.iteam.easyups.R;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.BarcodeView;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class ScanActivity extends AppCompatActivity {

    /**
     * the decoding qrcode view
     */
    private DecoratedBarcodeView barcodeScannerView;

    /**
     * boolean used to determine if the decoding is good
     */
    private boolean isScanDone;

    /**
     * the previous result, used to only scan once the same code
     */
    private Result lastResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        barcodeScannerView = findViewById(R.id.barcodeScannerView);

        requestPermission();


        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                // we only scan if the result is the first one or it is different than the last one
                if(lastResult == null || (lastResult != null && !result.getResult().getText().equals(lastResult.getText()))) {
                    Toast.makeText(ScanActivity.this, result.getText().toString(), Toast.LENGTH_SHORT).show();
                    lastResult = result.getResult();
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeScanner();
    }

    protected void resumeScanner() {
        isScanDone = false;
        if (!barcodeScannerView.isActivated())
            barcodeScannerView.resume();
    }

    protected void pauseScanner() {
        barcodeScannerView.pause();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pauseScanner();
    }


    void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0 && grantResults.length < 1) {
            requestPermission();
        } else {
            barcodeScannerView.resume();
        }
    }
}
