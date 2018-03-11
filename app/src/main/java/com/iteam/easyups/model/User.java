package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;

/**
 * Created by sara on 08/03/2018.
 */

public class User {


    public String pseudoName;
    public String edt;
    @Exclude
    public String id;

    public String getPseudoName() {
        return pseudoName;
    }

    public User() {

    }

    public User(String pseudoName, String edt) {
        this.pseudoName = pseudoName;
        this.edt = edt;
    }

    public void setPseudoName(String pseudoName) {
        this.pseudoName = pseudoName;
    }

    public String getEDT() {
        return edt;
    }

    public void setEDT(String edt) {
        this.edt = edt;
    }


}
