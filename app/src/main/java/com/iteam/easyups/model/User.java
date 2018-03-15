package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;

/**
 * Created by sara on 08/03/2018.
 */

public class User {


    private String name;
    private String timetableLink;
    private String formationName;
    private String groupName;

    @Exclude
    public String id;


    public User() {
        this.name = "";
        this.timetableLink = "";
    }

    public User(String name, String timetableLink) {
        this.name = name;
        this.timetableLink = timetableLink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimetableLink() {
        return timetableLink;
    }

    public void setTimetableLink(String timetableLink) {
        this.timetableLink = timetableLink;
    }

    public String getFormationName() {
        return formationName;
    }

    public void setFormationName(String formationName) {
        this.formationName = formationName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
