package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

/**
 * Created by Marianna on 24/02/2018.
 */
@IgnoreExtraProperties
public class FormationGroup {

    private String name;
    private String timeTableLink;
    @Exclude
    private String id;

    public FormationGroup(){

    }


    public FormationGroup(String name, String edtLink){
        this.name = name;
        this.timeTableLink = edtLink;

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

    @Override
    public String toString(){
        return name;
    }

}
