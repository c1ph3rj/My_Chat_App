package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.LoginScreen.mobileNumber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginOtpScreen extends AppCompatActivity {
    PhoneAuthProvider.ForceResendingToken mResendToken;
    String mVerificationId;
    FirebaseAuth firebaseAuth;
    TextView userMobileNoView;
    EditText otpView;
    Button verifyBtn;
    ImageView backBtn;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(LoginOtpScreen.this, "Invalid Otp!", Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(LoginOtpScreen.this, "Too many Request please try again later!", Toast.LENGTH_SHORT).show();
            } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                Toast.makeText(LoginOtpScreen.this, "Captcha Failed!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginOtpScreen.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            }

            // Show a message and update the UI
        }

        @Override
        public void onCodeSent(@NonNull String verificationId,
                               @NonNull PhoneAuthProvider.ForceResendingToken token) {

            mVerificationId = verificationId;
            mResendToken = token;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_otp_screen);
        getSupportActionBar().hide();

        init();
    }

    void init() {
        try {
            firebaseAuth = FirebaseAuth.getInstance();

            PhoneAuthOptions options =
                    PhoneAuthOptions.newBuilder(firebaseAuth)
                            .setPhoneNumber("+91" + mobileNumber)
                            .setTimeout(60L, TimeUnit.SECONDS)
                            .setActivity(LoginOtpScreen.this)
                            .setCallbacks(mCallbacks)
                            .build();

            PhoneAuthProvider.verifyPhoneNumber(options);

            userMobileNoView = findViewById(R.id.userMobileNoView);
            verifyBtn = findViewById(R.id.verifyBtn);
            otpView = findViewById(R.id.otpView);
            backBtn = findViewById(R.id.backBtn);

            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            String userMobileNoText = "Please enter the otp sent to the +91" + mobileNumber;
            userMobileNoView.setText(userMobileNoText);

            verifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String otp = otpView.getText().toString();

                    if(otp.length() < 6) {
                        Toast.makeText(LoginOtpScreen.this, "Please enter valid otp to continue!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                    signInWithPhoneAuthCredential(credential);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            FirebaseUser user = task.getResult().getUser();
                            startActivity(new Intent(LoginOtpScreen.this, DashboardScreen.class));
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            Exception e = task.getException();
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(LoginOtpScreen.this, "Invalid Otp!", Toast.LENGTH_SHORT).show();
                            } else if (e instanceof FirebaseTooManyRequestsException) {
                                Toast.makeText(LoginOtpScreen.this, "Too many Request please try again later!", Toast.LENGTH_SHORT).show();
                            } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                                Toast.makeText(LoginOtpScreen.this, "Captcha Failed!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginOtpScreen.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }
}