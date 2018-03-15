package com.iteam.easyups.activities;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.iteam.easyups.R;
import com.iteam.easyups.communication.BDDRoutes;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.User;

/**
 * Created by sara  on 08/03/2018.
 */

public class UserInfoActivity extends AppCompatActivity {
    private FirebaseAuth auth;
    private TextView name, groupeText, formation;
    ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_infouser);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        ApplicationInfo applicationInfo = this.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String result = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : this.getString(stringId);
        this.setTitle(result);


        name = findViewById(R.id.textName);
        formation = findViewById(R.id.textFormationName);
        groupeText = findViewById(R.id.textGroupeName);
        progressBar = findViewById(R.id.progressbaruser);

        final FirebaseUser user = auth.getCurrentUser();
        String id = user.getUid();
        FirebaseDatabase database = DatabaseConnection.getDatabase();
        DatabaseReference ref = database.getReference().child(BDDRoutes.USERS_PATH).child(id);
        progressBar.setVisibility(View.VISIBLE);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                progressBar.setVisibility(View.GONE);
                name.setText(user.name);
                formation.setText(user.formationName);
                groupeText.setText(user.groupName);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(UserInfoActivity.this, ProfileActivity.class));

            }
        });
        findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                auth.signOut();
                startActivity(new Intent(UserInfoActivity.this, MainActivity.class));

            }
        });

    }
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, SingupActivity.class));
        }
    }


}