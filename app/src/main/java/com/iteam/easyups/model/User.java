package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;

/**
 * Created by sara on 08/03/2018.
 */

public class User {


    public String name;
    public String timetableLink;
    @Exclude
    public String id;


    public User() {

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
