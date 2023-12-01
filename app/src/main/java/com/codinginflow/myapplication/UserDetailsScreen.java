package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.MainActivity.currentUser;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.IOException;

public class UserDetailsScreen extends AppCompatActivity {
    private static final int STORAGE_PERMISSION_SUCCESS_CODE = 123;
    EditText userNameView;
    EditText aboutView;
    ImageView profileView;
    Button updateProfileButton;
    ImageView backBtn;
    Uri pickedImageUri;
    boolean isFirstTime;
    private long mLastClickTime = 0;
    CustomProgressDialog progressDialog;

    UserDetails currentUserDetails = null;
    Button signOutBtn;
    FirebaseHelper firebaseHelper = new FirebaseHelper();
    ActivityResultLauncher<Intent> getPickedResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent intent = result.getData();
                if (intent != null) {
                    Uri fileLocation = intent.getData();
                    try {
                        Bitmap userPickedProfilePic = MediaStore.Images.Media.getBitmap(UserDetailsScreen.this.getContentResolver(), fileLocation);
                        Glide.with(UserDetailsScreen.this)
                                .load(userPickedProfilePic)
                                .circleCrop()
                                .error(R.drawable.profile_ic)
                                .into(profileView);
                        pickedImageUri = fileLocation;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details_screen);
        getSupportActionBar().hide();

        if (isFirstTime) {
            init();
        } else {
            firebaseHelper.getUserDetailsById(currentUser.getUid())
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
            progressDialog = new CustomProgressDialog(this);

            backBtn.setVisibility(View.GONE);
            signOutBtn.setVisibility(View.GONE);

            isFirstTime = getIntent().getBooleanExtra("isFirstTime", false);

            if (isFirstTime) {
                backBtn.setVisibility(View.GONE);
                signOutBtn.setVisibility(View.GONE);
            } else {
                signOutBtn.setVisibility(View.VISIBLE);
                backBtn.setVisibility(View.VISIBLE);
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
                try {
                    if (currentUserDetails != null) {
                        userNameView.setText(currentUserDetails.userName);
                        aboutView.setText(currentUserDetails.aboutDetails);
                        Glide.with(this)
                                .load(currentUserDetails.profilePic)
                                .circleCrop()
                                .error(R.drawable.profile_ic)
                                .into(profileView);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            profileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasStoragePermission()) {
                        Intent pickProfileImgIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        pickProfileImgIntent.setType("image/*");
                        getPickedResult.launch(pickProfileImgIntent);
                    } else {
                        requestStoragePermission();
                    }
                }
            });

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
                    progressDialog.show();
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
                    String finalAboutDetails = (aboutDetails != null) ? aboutDetails : "";
                    final String[] photoUrl = {""};
                    if (isFirstTime) {

                        if(pickedImageUri != null) {
                            Task<Uri> uploadProfilePic = firebaseHelper.uploadProfilePic(pickedImageUri);
                            uploadProfilePic.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()) {
                                        photoUrl[0] = task.getResult().toString();
                                        continueStoreUserDetails(userName, photoUrl[0], finalAboutDetails);
                                    } else {
                                        if(progressDialog != null && progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                        task.getException().printStackTrace();
                                    }
                                }
                            });
                        } else {
                            continueStoreUserDetails(userName, photoUrl[0], finalAboutDetails);
                        }

                    } else {
                        if(pickedImageUri != null) {
                            Task<Uri> uploadProfilePic = firebaseHelper.uploadProfilePic(pickedImageUri);
                            uploadProfilePic.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if(task.isSuccessful()) {
                                        photoUrl[0] = task.getResult().toString();
                                        continueUpdateUserDetails(userName, photoUrl[0], finalAboutDetails);
                                    } else {
                                        if(progressDialog != null && progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                        task.getException().printStackTrace();
                                    }
                                }
                            });
                        } else {
                            continueUpdateUserDetails(userName, photoUrl[0], finalAboutDetails);
                        }
                    }
                }
            });
        } catch (Exception e) {
            if(progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            e.printStackTrace();
        }
    }

    private void continueUpdateUserDetails(String userName, String profileUrl, String aboutDetails) {
        firebaseHelper.updateUserInfo(currentUser.getUid(), userName, aboutDetails, profileUrl)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        updateUserName(userName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(UserDetailsScreen.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void continueStoreUserDetails(String userName, String profileUrl, String aboutDetails) {
        firebaseHelper.storeUserDetails(currentUser.getUid(), currentUser.getPhoneNumber().replace("+91", ""), userName, profileUrl, aboutDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        updateUserName(userName);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        Toast.makeText(UserDetailsScreen.this, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        try {
            firebaseHelper.storePhoneNumber(currentUser.getUid(), currentUser.getPhoneNumber().replace("+91", ""));
        } catch (Exception e) {
            if(progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
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
                                if(progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_SUCCESS_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    requestPermissionAgain(permission);
                }
            }
        }
    }

    void requestPermissionAgain(String permission) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
            Toast.makeText(this, "Storage permission required to proceed.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Storage permission is required. Please enable it in the app settings.", Toast.LENGTH_LONG).show();
            redirectToAppSettings();
        }
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_SUCCESS_CODE
            );
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO},
                    STORAGE_PERMISSION_SUCCESS_CODE
            );
        }
    }

    private void redirectToAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}