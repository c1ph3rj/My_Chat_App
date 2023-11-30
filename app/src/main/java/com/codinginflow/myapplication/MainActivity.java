package com.codinginflow.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth firebaseAuth;

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

            // Delaying the user login check for 1000ms (1s) by using handler.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Checking if the user logged in or not.
                    Intent intent;
                    if (firebaseAuth.getCurrentUser() != null) {
                        // if the user logged in redirect the user to the dashboard screen.
                        intent = new Intent(MainActivity.this, DashboardScreen.class);
                    } else {
                        // else redirect the user to the login screen.
                        intent = new Intent(MainActivity.this, LoginScreen.class);
                    }

                    startActivity(intent);
                }
            }, 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}