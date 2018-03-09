package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;

/**
 * Created by sara on 08/03/2018.
 */

public class User {


    public  String PseudoName;
    public  String EDT;
    @Exclude
    public String id;

    public String getPseudoName() {
        return PseudoName;
    }

    public User(String pseudoName, String EDT) {
        PseudoName = pseudoName;
        this.EDT = EDT;
    }

    public void setPseudoName(String pseudoName) {
        PseudoName = pseudoName;
    }

    public String getEDT() {
        return EDT;
    }

    public void setEDT(String EDT) {
        this.EDT = EDT;
    }


}
