package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by Marianna on 24/02/2018.
 */
@IgnoreExtraProperties
public class Formation   {

    public  String name;
    public  String timeTableLink;
    @Exclude
    public String id;
    public List<FormationGroup> groupsList;
    @Exclude
    public String department;
    @Exclude
    public String level;



    public Formation (){

    }


    public Formation (String name, String edtLink, List<FormationGroup> groups){
        this.name = name;
        this.timeTableLink = edtLink;
        this.groupsList = groups;
        initLevelAndDepartment();

    }

    private void initLevelAndDepartment() {
        department = "";
        level = "";
        try {
            URL url = new URL(timeTableLink);
            String[] urlSplit = url.getPath().split("/");
            if(urlSplit.length >= 1 ) {
                department = urlSplit[1];
            }
            if(urlSplit.length >= 3 ) {
                level =  urlSplit[3];
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString(){
        return name;
    }


}
