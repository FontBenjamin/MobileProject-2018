package com.iteam.easyups.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.iteam.easyups.R;
import com.iteam.easyups.communication.DatabaseConnection;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sara  on 08/03/2018.
 */

public class ProfileActivity extends AppCompatActivity {
    private static final int CHOOSE_IMAGE = 101;

    TextView textView;
    ImageView imageView;
    EditText EdtText,NameText;

    Uri uriProfileImage;
    ProgressBar progressBar;

    String profileImageUrl;

    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        auth = FirebaseAuth.getInstance();


        NameText = findViewById(R.id.pseudo);
        EdtText = findViewById(R.id.edtText);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressbar);
        //textView = findViewById(R.id.textVerified);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //showImageChooser();
            }
        });

        loadUserInformation();

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });
    }



    private void loadUserInformation() {
        final FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
           String id = user.getUid();


        }
    }

    private void saveUserInformation() {
        String displayName = NameText.getText().toString();

        if (displayName.isEmpty()) {
            NameText.setError("Nom obligatoire");
            NameText.requestFocus();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {

            String userId =user.getUid();
            FirebaseDatabase data = DatabaseConnection.getDatabase();
            DatabaseReference dataReference= data.getReference().child("easyups/Users").child(userId);

            String name = NameText.getText().toString();
            String edt = EdtText.getText().toString();


            Map newPost = new HashMap();
            newPost.put("name", name);
            newPost.put("EDT", edt);
            dataReference.setValue(newPost);
        }
    }
}