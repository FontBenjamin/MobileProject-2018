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

    private  String name;
    private  String timeTableLink;
    @Exclude
    private String id;
    private List<FormationGroup> groupsList;
    @Exclude
    private String department;
    @Exclude
    private String level;



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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimeTableLink() {
        return timeTableLink;
    }

    public void setTimeTableLink(String timeTableLink) {
        this.timeTableLink = timeTableLink;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FormationGroup> getGroupsList() {
        return groupsList;
    }

    public void setGroupsList(List<FormationGroup> groupsList) {
        this.groupsList = groupsList;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String toString(){
        return name;
    }


}
