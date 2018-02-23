package com.iteam.easyups.activities;


import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.iteam.easyups.R;


/*https://www.simplifiedcoding.net/android-firebase-tutorial-1/
*https://github.com/probelalkhan/firebase-authentication-tutorial/blob/master/app/src/main/java/net/simplifiedlearning/firebaseauth/SignUpActivity.java
*https://www.youtube.com/watch?v=mF5MWLsb4cg
* https://www.youtube.com/watch?v=0NFwF7L-YA8
*
* */
public class SingupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    ProgressBar progressBar;
    EditText emailText,pswdText;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        progressBar = findViewById(R.id.progressbar);
        emailText = findViewById(R.id.textEmail);
        pswdText = findViewById(R.id.textPassword);
        auth = FirebaseAuth.getInstance();
    }



    private void registerUser() {
        String email = emailText.getText().toString().trim();
        String password = pswdText.getText().toString().trim();

        if (email.isEmpty()) {
            emailText.setError("Email is required");
            pswdText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("Please enter a valid email");
            emailText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            pswdText.setError("Password is required");
            pswdText.requestFocus();
            return;
        }

        if (password.length() < 6) {
            pswdText.setError("Minimum lenght of password should be 6");
            pswdText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    finish();
                    startActivity(new Intent(SingupActivity.this, SingInActivity.class));
                } else {

                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }



    }



