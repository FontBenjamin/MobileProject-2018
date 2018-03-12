package com.iteam.easyups.model;

import com.google.firebase.database.Exclude;

/**
 * Created by sara on 08/03/2018.
 */

public class User {


    public String name;
    public String EDT;
    @Exclude
    public String id;


    public User() {

    }


    public User(String name, String EDT) {
        this.name = name;
        this.EDT = EDT;
    }


}
