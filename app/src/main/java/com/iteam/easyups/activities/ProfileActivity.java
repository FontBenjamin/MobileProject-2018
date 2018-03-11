package com.iteam.easyups.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TabHost;
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
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Formation;
import com.iteam.easyups.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sara  on 08/03/2018.
 */

public class ProfileActivity extends AppCompatActivity {
    private static final int CHOOSE_IMAGE = 101;

    TextView textView;
    ImageView imageView;
    EditText nameText;
    private String timeTableUrl = "";
    Spinner EdtText, niveauText, intituleText, groupeText;
    private TabHost tabHost;

    Uri uriProfileImage;
    ProgressBar progressBar;
    ArrayAdapter<String> adapter;

    String profileImageUrl;

    FirebaseAuth auth;

    FirebaseDatabase database = DatabaseConnection.getDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile);
        auth = FirebaseAuth.getInstance();


        nameText = findViewById(R.id.pseudo);
        EdtText = (Spinner) findViewById(R.id.edtText);
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
                        // EdtText.setselec(user.EDT);
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
        String edt = EdtText.getSelectedItem().toString();

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

    public void getFormationByLevel() {
        database.getReference().child("easyups/formations/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final List<String> areas = new ArrayList<String>();

                for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                    areas.add(areaSnapshot.getKey());


                }
                adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, areas);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                EdtText.setAdapter(adapter);
                EdtText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here
                        String text = EdtText.getSelectedItem().toString();
                        initLevelSpinner(text);
                        //DataSnapshot data = (DataSnapshot)parentView.getItemAtPosition(position);
                        //updateFormationSpinner(intituleText, data);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public void initLevelSpinner(String text) {
        final String leval = text;
        database.getReference().child("easyups/formations/" + leval).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final List<String> niveau = new ArrayList<String>();

                for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                    niveau.add(areaSnapshot.getKey());


                }
                adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, niveau);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                niveauText.setAdapter(adapter);
                niveauText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here
                        String niveau = EdtText.getSelectedItem().toString();
                        // initformationSpinner(leval, niveau);
                        DataSnapshot data = (DataSnapshot)parentView.getItemAtPosition(position);
                        updateFormationSpinner(intituleText, data);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public void initformationSpinner(String text, String niveau) {
        final String leval = text;

        database.getReference().child("easyups/formations/" + leval + "/" + niveau + "/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final List<String> niveau = new ArrayList<String>();

                for (DataSnapshot areaSnapshot : dataSnapshot.getChildren()) {
                    Formation formation = areaSnapshot.getValue(Formation.class);

                    niveau.add(formation.name);

                }
                adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, niveau);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                intituleText.setAdapter(adapter);
                intituleText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                        // your code here
                        String niveau = EdtText.getSelectedItem().toString();
                        initformationSpinner(leval, niveau);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void updateFormationSpinner(final Spinner spinnerFormationTitle, DataSnapshot data) {
        List<Formation> formations = new ArrayList<>();
        for (DataSnapshot dataFormation : data.getChildren()) {
            formations.add(dataFormation.getValue(Formation.class));
        }

        ArrayAdapter<Formation> adapter = new ArrayAdapter<Formation>(getApplicationContext(),
                android.R.layout.simple_spinner_item, formations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerFormationTitle.setAdapter(adapter);
        spinnerFormationTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }


}