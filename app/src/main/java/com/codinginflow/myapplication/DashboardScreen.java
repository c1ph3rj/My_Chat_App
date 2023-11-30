package com.codinginflow.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardScreen extends AppCompatActivity {
    static FirebaseUser currentUser;
    Button signOutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_screen);

        signOutBtn = findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(DashboardScreen.this, MainActivity.class));
                finish();
            }
        });

        init();
    }

    void init() {
        try {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser != null && currentUser.getDisplayName() == null) {
                Intent getUserDetailsIntent = new Intent(this, UserDetailsScreen.class);
                getUserDetailsIntent.putExtra("isFirstTime", true);
                startActivity(getUserDetailsIntent);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        // Build the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to exit?");
        builder.setCancelable(false);

        // Set up the buttons
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If the user clicks "Yes", close the app
                finishAffinity();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // If the user clicks "No", close the dialog and stay in the app
                dialog.cancel();
            }
        });

        // Show the alert dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}