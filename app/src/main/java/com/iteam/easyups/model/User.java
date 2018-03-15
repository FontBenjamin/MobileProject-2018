package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;

/**
 * Created by sara on 08/03/2018.
 */

public class User {


    public String name;
    public String timetableLink;
    public String formationName;
    public String groupName;

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

    public String getTimetableLink() {
        return timetableLink;
    }
}
