package com.iteam.easyups.activities;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.iteam.easyups.R;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.utils.AlertMessage;
import com.iteam.easyups.utils.Util;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
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
    private TextView textQRCode;
    private ImageView imageQRCode;
    private ProgressBar progressBar;
    private TextView textViewWaitScan;
    private Context mContext;
    private FirebaseDatabase database = DatabaseConnection.getDatabase();

     /**
     * the previous result, used to only scan once the same code
     */
    private Result lastResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        mContext = this;
        imageQRCode = findViewById(R.id.imageViewQRCode);
        textQRCode = findViewById(R.id.textViewQRCode);
        progressBar = findViewById(R.id.progressBarWaitScan);
        textViewWaitScan = findViewById(R.id.textViewWaitScan);
        barcodeScannerView = findViewById(R.id.barcodeScannerView);

        textQRCode.setVisibility(View.INVISIBLE);
        imageQRCode.setVisibility(View.INVISIBLE);

        requestPermission();

        barcodeScannerView.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(final BarcodeResult result) {
                // we only scan if the result is the first one or it is different than the last one
                if(lastResult == null || (lastResult != null && !result.getResult().getText().equals(lastResult.getText()))) {
                    lastResult = result.getResult();
                        if(Util.isNetworkAvailable(mContext)){
                            // analyse the content
                            try {
                            database.getReference().child(result.getText()).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                                // for (final DataSnapshot data : dataSnapshot.getChildren()) {
                                                // we print the object, image or text
                                                String textData = dataSnapshot.getValue().toString();
                                                // if the string is an image
                                                if (textData.substring(0, 6).equals("image/")) {
                                                    try {
                                                        byte[] encodeByte = Base64.decode(textData.substring(6, textData.length()), Base64.DEFAULT);
                                                        Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
                                                        textQRCode.setVisibility(View.INVISIBLE);
                                                        textViewWaitScan.setVisibility(View.INVISIBLE);
                                                        progressBar.setVisibility(View.INVISIBLE);
                                                        imageQRCode.setVisibility(View.VISIBLE);
                                                        imageQRCode.setImageBitmap(bitmap);
                                                    } catch (Exception e) {
                                                        Util.displayErrorAlert(AlertMessage.ERROR_TYPE, AlertMessage.IMAGE_PARSING_ERROR, mContext);
                                                    }
                                                } else {
                                                    imageQRCode.setVisibility(View.INVISIBLE);
                                                    textViewWaitScan.setVisibility(View.INVISIBLE);
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    textQRCode.setVisibility(View.VISIBLE);
                                                    textQRCode.setText(textData);
                                                }
                                            
                                        }
                                        //}
                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    }

                            );
                        }catch(Exception e){
                                imageQRCode.setVisibility(View.INVISIBLE);
                                textViewWaitScan.setVisibility(View.INVISIBLE);
                                progressBar.setVisibility(View.INVISIBLE);
                                textQRCode.setVisibility(View.VISIBLE);
                                textQRCode.setText("Erreur de scan : ce QR code n'est pas relié à la base de données de l'université.");
                            }
                        }
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

    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    void requestPermission() {
        if (!Util.requestPermission(this, Manifest.permission.CAMERA)) {
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
