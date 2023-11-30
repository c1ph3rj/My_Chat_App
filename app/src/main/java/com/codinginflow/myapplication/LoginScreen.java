package com.codinginflow.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginScreen extends AppCompatActivity {
    TextView mobileNoView;
    Button sendOtpBtn;
    static String mobileNumber;


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

}