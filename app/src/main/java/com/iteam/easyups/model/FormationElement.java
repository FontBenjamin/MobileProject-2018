package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;

/**
 * Created by Marianna on 24/02/2018.
 */

public class FormationElement {

    public  String name;
    public  String timeTableLink;
    @Exclude
    public String id;

    public FormationElement (){


    }

    public FormationElement (String name, String timeTableLink){

        this.name = name;
        this.timeTableLink = timeTableLink;

    }
}
