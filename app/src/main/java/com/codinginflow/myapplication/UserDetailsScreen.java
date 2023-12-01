package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.DashboardScreen.currentUser;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class UserDetailsScreen extends AppCompatActivity {
    EditText userNameView;
    EditText aboutView;
    ImageView profileView;
    Button updateProfileButton;
    ImageView backBtn;
    boolean isFirstTime;
    private long mLastClickTime = 0;

    UserDetails currentUserDetails = null;
    Button signOutBtn;

    FirebaseHelper firestoreHelper = new FirebaseHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details_screen);
        getSupportActionBar().hide();

        if (isFirstTime) {
            init();
        } else {
            firestoreHelper.getUserDetailsById(currentUser.getUid())
                    .addOnSuccessListener(userDetails -> {
                        if (userDetails != null) {
                            currentUserDetails = userDetails;
                        }
                        init();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(this, "Something went Wrong!", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    void init() {
        try {
            userNameView = findViewById(R.id.userNameView);
            aboutView = findViewById(R.id.aboutView);
            backBtn = findViewById(R.id.backBtn);
            profileView = findViewById(R.id.profileView);
            updateProfileButton = findViewById(R.id.updateBtn);
            signOutBtn = findViewById(R.id.signOutBtn);

            isFirstTime = getIntent().getBooleanExtra("isFirstTime", false);

            if (isFirstTime) {
                backBtn.setVisibility(View.GONE);
                signOutBtn.setVisibility(View.GONE);
            } else {
                signOutBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(UserDetailsScreen.this, MainActivity.class));
                        finish();
                    }
                });
                backBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });
                if(currentUserDetails != null) {
                    userNameView.setText(currentUserDetails.userName);
                    aboutView.setText(currentUserDetails.aboutDetails);
                }
            }

            try {
                if (currentUser.getDisplayName() != null) {
                    userNameView.setText(currentUser.getDisplayName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            updateProfileButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Delaying user click by 3 seconds.
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 3000) {
                        return;
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();

                    String userName = userNameView.getText().toString().trim();

                    if (userName.trim().isEmpty()) {
                        Toast.makeText(UserDetailsScreen.this, "Please enter your name to continue!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (userName.trim().length() < 3) {
                        if (userName.trim().isEmpty()) {
                            Toast.makeText(UserDetailsScreen.this, "Username should be more than 3 letters!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    String aboutDetails = aboutView.getText().toString().trim();
                    if (isFirstTime) {
                        if (aboutDetails == null) {
                            aboutDetails = "";
                        }
                        firestoreHelper.storeUserDetails(currentUser.getUid(), currentUser.getPhoneNumber().replace("+91", ""), userName, "", aboutDetails)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        updateUserName(userName);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UserDetailsScreen.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                        try {
                            firestoreHelper.storePhoneNumber(currentUser.getUid(), currentUser.getPhoneNumber().replace("+91", ""));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        firestoreHelper.updateUserInfo(currentUser.getUid(), userName, aboutDetails)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        updateUserName(userName);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(UserDetailsScreen.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUserName(String userName) {
        if (currentUser.getDisplayName() != null && currentUser.getDisplayName().equals(userName)) {
            finish();
        } else {
            UserProfileChangeRequest userProfileChangeRequest = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .build();

            currentUser.updateProfile(userProfileChangeRequest)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(UserDetailsScreen.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(UserDetailsScreen.this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        }
                    });
        }
    }

    @Override
    public void onBackPressed() {
        if (isFirstTime) {
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
        } else {
            finish();
        }
    }
}