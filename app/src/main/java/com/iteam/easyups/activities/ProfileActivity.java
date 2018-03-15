package com.iteam.easyups.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.iteam.easyups.R;
import com.iteam.easyups.adapter.DataSnapshotSpinnerAdapter;
import com.iteam.easyups.communication.BDDRoutes;
import com.iteam.easyups.communication.DatabaseConnection;
import com.iteam.easyups.model.Formation;
import com.iteam.easyups.model.FormationGroup;
import com.iteam.easyups.model.User;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sara  on 08/03/2018.
 */

public class ProfileActivity extends AppCompatActivity {
    private static final int CHOOSE_IMAGE = 101;

    private ImageView imageView;
    private EditText nameText;
    private String timeTableUrl = "";
    private Spinner edtText, niveauText, intituleText, groupeText;
    private Context mContext;
    private Uri uriProfileImage;
    private ProgressBar progressBar;
    private String profileImageUrl;
    private FirebaseAuth auth;
    private Formation formation;
    private FormationGroup formationGroup;
    private FirebaseDatabase database = DatabaseConnection.getDatabase();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.profile);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        ApplicationInfo applicationInfo = this.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        String result = stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : this.getString(stringId);
        this.setTitle(result);


        mContext = this;
        nameText = findViewById(R.id.pseudo);
        edtText = (Spinner) findViewById(R.id.edtText);
        niveauText = (Spinner) findViewById(R.id.niveauText);
        intituleText = (Spinner) findViewById(R.id.intituleText);
        groupeText = (Spinner) findViewById(R.id.groupeText);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressbar);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });
        getFormationByLevel();
        initFormationSpinner();

        findViewById(R.id.buttonSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveUserInformation();

            }
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (auth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, SingupActivity.class));
        }
    }


    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }

    private void initFormationSpinner() {
        edtText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                DataSnapshot data = (DataSnapshot) parentView.getItemAtPosition(position);
                updateLevelSpinner(data);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //TODO
            }

        });

        niveauText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                DataSnapshot data = (DataSnapshot) parentView.getItemAtPosition(position);
                updateFormationSpinner(data);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //TODO
            }

        });

        intituleText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                formation = (Formation) parentView.getItemAtPosition(position);
                timeTableUrl = formation.getTimeTableLink();
                updateGroupSpinner(formation);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //TODO
            }

        });

        groupeText.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (parentView.getItemAtPosition(position) instanceof FormationGroup) {
                    formationGroup = (FormationGroup) parentView.getItemAtPosition(position);
                    int index = timeTableUrl.lastIndexOf('/');
                    timeTableUrl = timeTableUrl.substring(0, index);
                    timeTableUrl += "/" + formationGroup.getTimeTableLink();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                //TODO
            }

        });

    }

    /**
     * Save the user profile in db
     */
    private void saveUserInformation() {
        final String name = nameText.getText().toString();
        FirebaseUser user = auth.getCurrentUser();

        if (name.isEmpty()) {
            nameText.setError("Nom est obligatoire");
            nameText.requestFocus();
        }else if (user != null) {
            final String userId = user.getUid();

            database.getReference().child(BDDRoutes.USERS_PATH).child(userId).addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            user.setName(name);
                            user.setFormationName(formation.getName());
                            user.setGroupName(formationGroup.getName());
                            user.setTimetableLink(timeTableUrl);
                            database.getReference().child(BDDRoutes.USERS_PATH).child(userId).setValue(user);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    }
            );

            uploadImageToFirebaseStorage();

            if(profileImageUrl != null){
                UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(Uri.parse(profileImageUrl))
                        .build();


                user.updateProfile(profile)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Profil mis à jour", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
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


    private void getFormationByLevel() {
        database.getReference().child(BDDRoutes.FORMATION_PATH).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        List<DataSnapshot> levels = new ArrayList<>();
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

    private void updateLevelSpinner(DataSnapshot data) {
        List<DataSnapshot> levels = new ArrayList<>();
        for (DataSnapshot dataChild : data.getChildren()) {
            levels.add(dataChild);
        }
        DataSnapshotSpinnerAdapter adapter = new DataSnapshotSpinnerAdapter(mContext,
                android.R.layout.simple_spinner_item, levels);
        niveauText.setAdapter(adapter);
        niveauText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

    }

    private void updateGroupSpinner(Formation formation) {
        List<FormationGroup> formationGroup;
        ArrayAdapter<?> adapter;
        if (formation.getGroupsList() == null) {
            adapter = new ArrayAdapter<String>(mContext,
                    android.R.layout.simple_spinner_item, Arrays.asList("Aucun groupe pour cette formation"));
        } else {
            formationGroup = formation.getGroupsList();
            adapter = new ArrayAdapter<FormationGroup>(mContext,
                    android.R.layout.simple_spinner_item, formationGroup);
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        groupeText.setAdapter(adapter);
        groupeText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private void updateFormationSpinner(DataSnapshot data) {
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
        startActivityForResult(Intent.createChooser(intent, "Sélectionnez l'image du profil"), CHOOSE_IMAGE);
    }


}