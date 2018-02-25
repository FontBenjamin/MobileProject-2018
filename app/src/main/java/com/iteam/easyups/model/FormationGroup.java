package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Marianna on 24/02/2018.
 */
@IgnoreExtraProperties
public class FormationGroup {

    public  String name;
    public  String timeTableLink;
    @Exclude
    public String id;

    public FormationGroup(){

    }


    public FormationGroup(String name, String edtLink){
        this.name = name;
        this.timeTableLink = edtLink;

    }


}
