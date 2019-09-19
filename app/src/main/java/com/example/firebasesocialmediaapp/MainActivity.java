package com.example.firebasesocialmediaapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtUsername, edtPassword;
    private Button btnSignUp, btnSignIn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializing coz was returning null
        FirebaseApp.initializeApp(this);

        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignIn = findViewById(R.id.btnSignIn);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signUp();

            }
        });

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                signIn();
            }
        });



    }

    @Override
    protected void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){

            //transition to next activity
            transitionToSocialMediaActivity();

    }

    }

    private void signUp(){

        mAuth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    Toast.makeText(MainActivity.this, "Signing up successful", Toast.LENGTH_LONG).show();
                                   //storing username to database
                    //specifying child to the root of my database
                    //child is my_users and its child is unique id for my_users which have a child and  that child have value
                    FirebaseDatabase.getInstance().getReference().child("my_users")
                            .child(Objects.requireNonNull(task.getResult().getUser()).getUid())
                            .child("username").setValue(edtUsername.getText().toString());

                    //how to update users profile data in firebase
                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(edtUsername.getText().toString())
                            .build();

                    FirebaseAuth.getInstance().getCurrentUser().updateProfile(profileUpdates)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                       Toast.makeText(MainActivity.this, "Display name updated", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                                    transitionToSocialMediaActivity();

                } else {

                    Toast.makeText(MainActivity.this, "Signing up failed", Toast.LENGTH_LONG).show();

                }


            }
        });

    }

    private void signIn(){

        mAuth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){

                    Toast.makeText(MainActivity.this, "Signing in successful", Toast.LENGTH_LONG).show();
                        transitionToSocialMediaActivity();

                } else {

                    Toast.makeText(MainActivity.this, "Signing in failed", Toast.LENGTH_LONG).show();


                }

            }
        });

    }

    private void transitionToSocialMediaActivity(){

        Intent intent = new Intent(this, SocialMediaActivity.class);
        startActivity(intent);

    }

}
