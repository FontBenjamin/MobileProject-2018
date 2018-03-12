package com.iteam.easyups.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Created by Marianna on 11/03/2018.
 */
@IgnoreExtraProperties
public class Anomaly {

    @Exclude
    public String id;
    public String encodedImage;
    public Criticality criticality;
    public double longitude;
    public double latitude;



    public Anomaly (){

    }


    public Anomaly (Bitmap img, Criticality crit,double longitude, double latitude){
        this.encodedImage = encodeImage(img);
        this.criticality = crit;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    private String encodeImage(Bitmap img){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        img.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteFormat = stream.toByteArray();
        return Base64.encodeToString(byteFormat, Base64.NO_WRAP);
    }

    private Bitmap decodeImage(){
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }




}
