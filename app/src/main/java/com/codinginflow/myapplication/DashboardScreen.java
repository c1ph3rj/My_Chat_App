package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.MainActivity.currentUser;
import static com.codinginflow.myapplication.MainActivity.currentUserDetails;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class DashboardScreen extends AppCompatActivity implements FirebaseHelper.LoadMessagesCallBack {
    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 123;
    FloatingActionButton newMessagesView;
    ImageView userProfileView;
    ArrayList<UserDetails> listOfMessages;
    RecyclerView listOfMessagesView;
    String oldProfile;
    DatabaseHelper databaseHelper;
    FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_screen);
        getSupportActionBar().hide();

        init();
    }

    void init() {
        try {
            currentUser = FirebaseAuth.getInstance().getCurrentUser();
            databaseHelper = new DatabaseHelper(this);

            if (currentUser != null && currentUser.getDisplayName() == null) {
                Intent getUserDetailsIntent = new Intent(this, UserDetailsScreen.class);
                getUserDetailsIntent.putExtra("isFirstTime", true);
                startActivity(getUserDetailsIntent);
            }

            newMessagesView = findViewById(R.id.newMessagesView);
            userProfileView = findViewById(R.id.userProfileView);
            listOfMessagesView = findViewById(R.id.listOfMessagesView);
            listOfMessages = databaseHelper.getAllMessageDetails();

            try {
                Drawable myFabSrc = AppCompatResources.getDrawable(this, R.drawable.message_ic);
                Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
                willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                newMessagesView.setImageDrawable(willBeWhite);
            } catch (Exception e) {
                e.printStackTrace();
            }
            newMessagesView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (hasContactsPermission()) {
                        startActivity(new Intent(DashboardScreen.this, ContactsScreen.class));
                    } else {
                        requestContactsPermission();
                    }
                }
            });

            userProfileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(DashboardScreen.this, UserDetailsScreen.class));
                }
            });

            initListView();
            firebaseHelper = new FirebaseHelper();

            updateUserDetails();
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    updateUserDetails();
                    new Handler().postDelayed(this, 5000); // checking for every 5 sec
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void updateUserDetails() {
        try {
            firebaseHelper.fetchMessagesAndUserDetails(this);
            if (currentUserDetails != null) {
                if (currentUserDetails.profilePic != null && (oldProfile != null && !currentUserDetails.profilePic.equals(oldProfile)) && !currentUserDetails.profilePic.isEmpty()) {
                    Glide.with(this)
                            .load(currentUserDetails.profilePic)
                            .circleCrop()
                            .error(R.drawable.profile_ic)
                            .into(userProfileView);
                    oldProfile = currentUserDetails.profilePic;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to exit?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finishAffinity();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACTS_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startActivity(new Intent(DashboardScreen.this, ContactsScreen.class));
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                    Toast.makeText(this, "Contacts permission is required to proceed.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Contacts permission is required. Please enable it in the app settings.", Toast.LENGTH_LONG).show();
                    redirectToAppSettings();
                }
            }
        }
    }

    private boolean hasContactsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestContactsPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.READ_CONTACTS},
                CONTACTS_PERMISSION_REQUEST_CODE
        );
    }


    private void redirectToAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onMessagedLoaded(ArrayList<UserDetails> listOfMessages) {
        this.listOfMessages = listOfMessages;
        if (!databaseHelper.getAllMessageDetails().isEmpty()) {
            databaseHelper.deleteAllUserDetails();
        }
        databaseHelper.addListOfUserDetails(listOfMessages);
        initListView();
    }

    private void initListView() {
        try {
            ListOfMessagesAdapter listOfMessagesAdapter = new ListOfMessagesAdapter(listOfMessages, this);
            listOfMessagesView.setAdapter(listOfMessagesAdapter);
            listOfMessagesView.setLayoutManager(new LinearLayoutManager(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}