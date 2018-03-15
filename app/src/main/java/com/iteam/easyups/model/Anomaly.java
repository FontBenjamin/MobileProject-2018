package com.iteam.easyups.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.ByteArrayOutputStream;

/**
 * Created by Marianna on 11/03/2018.
 */
@IgnoreExtraProperties
public class Anomaly {

    @Exclude
    private String id;
    private String encodedImage;
    private Criticality criticality;
    private double longitude;
    private double latitude;



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

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEncodedImage() {
        return encodedImage;
    }

    public void setEncodedImage(String encodedImage) {
        this.encodedImage = encodedImage;
    }

    public Criticality getCriticality() {
        return criticality;
    }

    public void setCriticality(Criticality criticality) {
        this.criticality = criticality;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
