package com.iteam.easyups.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iteam.easyups.R;
import com.iteam.easyups.adapter.DataSnapshotSpinnerAdapter;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Formation;
import com.iteam.easyups.model.FormationGroup;
import com.iteam.easyups.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sara  on 08/03/2018.
 */

public class ProfileActivity extends AppCompatActivity {
    private static final int CHOOSE_IMAGE = 101;

    private TextView textView;
    private ImageView imageView;
    private EditText nameText;
    private String timeTableUrl = "";
    private Spinner edtText, niveauText, intituleText, groupeText;
    private Context mContext;
    private Uri uriProfileImage;
    private ProgressBar progressBar;
    private String profileImageUrl;
    private FirebaseAuth auth;
    private FirebaseDatabase database = DatabaseConnection.getDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        auth = FirebaseAuth.getInstance();
        mContext = this;

        nameText = findViewById(R.id.pseudo);
        edtText = (Spinner) findViewById(R.id.edtText);
        niveauText = (Spinner) findViewById(R.id.niveauText);
        intituleText = (Spinner) findViewById(R.id.intituleText);
        groupeText = (Spinner) findViewById(R.id.groupeText);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressbar);
        //textView = findViewById(R.id.textVerified);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });
        getFormationByLevel();
        //loadUserInformation();

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();
            }
        });


        initFormationSpinner();
    }

    private void initFormationSpinner(){
        edtText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                DataSnapshot data = (DataSnapshot)parentView.getItemAtPosition(position);
                updateLevelSpinner(data);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        niveauText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                DataSnapshot data = (DataSnapshot)parentView.getItemAtPosition(position);
                updateFormationSpinner(data);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        intituleText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                Formation formation = (Formation)parentView.getItemAtPosition(position);
                timeTableUrl = formation.timeTableLink;
                updateGroupSpinner(formation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        groupeText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(parentView.getItemAtPosition(position) instanceof FormationGroup) {
                    FormationGroup group = (FormationGroup) parentView.getItemAtPosition(position);
                    int index = timeTableUrl.lastIndexOf('/');
                    timeTableUrl = timeTableUrl.substring(0, index);
                    timeTableUrl += "/" + group.timeTableLink;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

    }


    private void loadUserInformation() {
        final FirebaseUser user = auth.getCurrentUser();

        if (user != null) {

            String id = user.getUid();
            FirebaseDatabase database = DatabaseConnection.getDatabase();
            DatabaseReference ref = database.getReference().child("easyups/Users").child(id);
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user.name != null) {
                        nameText.setText(user.name);
                        //getFormationByLevel();
                        // edtText.setselec(user.EDT);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });

        }
    }

    private void saveUserInformation() {
        String name = nameText.getText().toString();
        String edt = edtText.getSelectedItem().toString();

        if (name.isEmpty()) {
            nameText.setError("Nom est obligatoire");
            nameText.requestFocus();
            return;
        }


        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {

            String userId = user.getUid();
            FirebaseDatabase data = DatabaseConnection.getDatabase();
            DatabaseReference dataReference = data.getReference().child("easyups/Users").child(userId);
            getFormationByLevel();

            Map newPost = new HashMap();
            newPost.put("Name", name);
            newPost.put("EDT", edt);
            dataReference.setValue(newPost);
            uploadImageToFirebaseStorage();
        }


        if (user != null && profileImageUrl != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                imageView.setImageBitmap(bitmap);

                uploadImageToFirebaseStorage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage() {
        StorageReference profileImageRef =
                FirebaseStorage.getInstance().getReference("profilepics/" + System.currentTimeMillis() + ".jpg");

        if (uriProfileImage != null) {
            progressBar.setVisibility(View.VISIBLE);
            profileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                            profileImageUrl = taskSnapshot.getDownloadUrl().toString();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    public void getFormationByLevel(){
        database.getReference().child(TimetableActivity.FORMATION_PATH).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        List<DataSnapshot> levels =  new ArrayList<>();
                        for (DataSnapshot dataChild : dataSnapshot.getChildren()) {
                            levels.add(dataChild);
                        }
                        DataSnapshotSpinnerAdapter adapter = new DataSnapshotSpinnerAdapter(mContext,
                                android.R.layout.simple_spinner_item, levels);
                        edtText.setAdapter(adapter);
                        edtText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }

    public void updateLevelSpinner(DataSnapshot data){
        List<DataSnapshot> levels =  new ArrayList<>();
        for (DataSnapshot dataChild : data.getChildren()) {
            levels.add(dataChild);
        }
        DataSnapshotSpinnerAdapter adapter = new DataSnapshotSpinnerAdapter(mContext,
                android.R.layout.simple_spinner_item, levels);
        niveauText.setAdapter(adapter);
        niveauText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    }

    public void updateGroupSpinner(Formation formation){
        List<FormationGroup> formationGroup;
        ArrayAdapter<?> adapter;
        if(formation.groupsList == null){
            adapter =  new ArrayAdapter<String> (mContext,
                    android.R.layout.simple_spinner_item, Arrays.asList("Aucun groupe pour cette formation"));
        }else{
            formationGroup  =  formation.groupsList;
            adapter =  new ArrayAdapter<FormationGroup> (mContext,
                    android.R.layout.simple_spinner_item, formationGroup);
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        groupeText.setAdapter(adapter);
        groupeText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void updateFormationSpinner(DataSnapshot data) {
        List<Formation> formations = new ArrayList<>();
        for (DataSnapshot dataFormation : data.getChildren()) {
            formations.add(dataFormation.getValue(Formation.class));
        }

        ArrayAdapter<Formation> adapter = new ArrayAdapter<Formation>(getApplicationContext(),
                android.R.layout.simple_spinner_item, formations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        intituleText.setAdapter(adapter);
        intituleText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }


}