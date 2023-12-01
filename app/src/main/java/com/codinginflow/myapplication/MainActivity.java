package com.codinginflow.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;
    static FirebaseUser currentUser;
    static UserDetails currentUserDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Hiding the app bar.
        getSupportActionBar().hide();

        init();
    }

    void init() {
        try {
            // Initializing the firebase.
            FirebaseApp.initializeApp(this);

            firebaseAuth = FirebaseAuth.getInstance();
            currentUser = firebaseAuth.getCurrentUser();

            // Delaying the user login check for 1000ms (1s) by using handler.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Checking if the user logged in or not.
                    Intent intent;
                    if (currentUser != null) {
                        // if the user logged in redirect the user to the dashboard screen.
                        FirebaseHelper firebaseHelper = new FirebaseHelper();
                        firebaseHelper.getUserDetailsById(currentUser.getUid()).
                                addOnCompleteListener(new OnCompleteListener<UserDetails>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UserDetails> task) {
                                        if (task.isSuccessful()) {
                                            currentUserDetails = task.getResult();
                                            firebaseHelper.addCurrentUserDetailsListener(new FirebaseHelper.OnCurrentUserDetailsChangedListener() {
                                                @Override
                                                public void onCurrentUserDetailsChanged(UserDetails updatedUserDetails) {
                                                    currentUserDetails = updatedUserDetails;
                                                }
                                            });

                                            startActivity(new Intent(MainActivity.this, DashboardScreen.class));
                                        }
                                    }
                                });
                    } else {
                        // else redirect the user to the login screen.
                        startActivity(new Intent(MainActivity.this, LoginScreen.class));
                    }
                }
            }, 500);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}