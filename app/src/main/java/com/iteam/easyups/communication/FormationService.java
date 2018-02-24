package com.iteam.easyups.communication;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteam.easyups.model.Formation;
import com.iteam.easyups.utils.HtmlParser;

import org.jsoup.nodes.Element;

/**
 * Created by Marianna on 24/02/2018.
 */

public class FormationService {
    private FirebaseDatabase database = DatabaseConnection.getDatabase();
    private final static String FORMATION_PATH = "formations/";


    public void saveFormation(Formation formation){
        formation.id = database.getReference().child(FORMATION_PATH).push().getKey();
        database.getReference().child(FORMATION_PATH + formation.department).child(formation.id).setValue(formation);

    }

    public void saveAllFormation(final String[] edtMainPages){
        database.getReference().addListenerForSingleValueEvent(new ValueEventListener() {


            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(FORMATION_PATH).exists()) {
                    new HtmlParser().execute(edtMainPages);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }




}
