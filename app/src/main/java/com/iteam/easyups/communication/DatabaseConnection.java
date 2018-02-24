package com.iteam.easyups.communication;

import com.google.firebase.database.FirebaseDatabase;

public class DatabaseConnection {

    private static FirebaseDatabase mDatabase;

    public static FirebaseDatabase getDatabase()
    {
        if (mDatabase == null)
        {
            mDatabase = FirebaseDatabase.getInstance();
        }
        return mDatabase;
    }

}