package com.codinginflow.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

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

            // Checking if the user logged in or not.
            Intent intent;
            if(firebaseAuth.getCurrentUser() != null) {
                // if the user logged in redirect the user to the dashboard screen.
                intent = new Intent(this, DashboardScreen.class);
            } else {
                // else redirect the user to the login screen.
                intent = new Intent(this, LoginScreen.class);
            }

            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}