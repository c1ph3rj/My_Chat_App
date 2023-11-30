package com.codinginflow.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginScreen extends AppCompatActivity {
    static String mobileNumber;
    TextView mobileNoView;
    Button sendOtpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        getSupportActionBar().hide();

        init();
    }

    void init() {
        try {
            sendOtpBtn = findViewById(R.id.sendOtpBtn);
            mobileNoView = findViewById(R.id.mobileNoView);

            sendOtpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mobileNo = mobileNoView.getText().toString();

                    if (mobileNo.length() < 10) {
                        Toast.makeText(LoginScreen.this, "Please enter valid phone number to continue!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mobileNumber = mobileNo;
                    startActivity(new Intent(LoginScreen.this, LoginOtpScreen.class));
                }
            });

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