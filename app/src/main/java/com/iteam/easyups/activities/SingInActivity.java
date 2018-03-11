package com.iteam.easyups.activities;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.internal.FederatedSignInActivity;
import com.iteam.easyups.R;

public class SingInActivity extends AppCompatActivity implements View.OnClickListener {


    FirebaseAuth auth;
    EditText textEmail, textPassword;
    ProgressBar bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        auth = FirebaseAuth.getInstance();

        textEmail = (EditText) findViewById(R.id.textEmail);

        textPassword = (EditText) findViewById(R.id.textPassword);
        bar = (ProgressBar) findViewById(R.id.progressbar);

        findViewById(R.id.textSignup).setOnClickListener(this);
        findViewById(R.id.buttonLogin).setOnClickListener(this);

    }

    private void userAuth() {
        String email = textEmail.getText().toString().trim();
        String password = textPassword.getText().toString().trim();

        if (email.isEmpty()) {
            textEmail.setError("Email est obligatoire");
            textEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textEmail.setError("Veuillez entrer un email valide");
            textEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            textPassword.setError("Mot de passe est obligatoire");
            textPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            textPassword.setError("La longueur minimale du mot de passe est 6");
            textPassword.requestFocus();
            return;
        }

        bar.setVisibility(View.VISIBLE);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                bar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    finish();
                    Intent intent = new Intent(SingInActivity.this, SingupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.textSignup:
                finish();
                System.out.print("avant de passer");
                startActivity(new Intent(this, SingupActivity.class));
                System.out.print("passer");
                break;

            case R.id.buttonLogin:
                userAuth();
                break;
        }
    }


 }
