package com.iteam.easyups.communication;

import com.google.firebase.database.FirebaseDatabase;

public class DatabaseConnection {

    private static FirebaseDatabase database;

    public static FirebaseDatabase getDatabase()
    {
       if (database == null)
       {
           database = FirebaseDatabase.getInstance();
       }
       return database;
    }

}
