package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.MainActivity.currentUser;
import static com.codinginflow.myapplication.MainActivity.currentUserDetails;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class DashboardScreen extends AppCompatActivity implements FirebaseHelper.OnCurrentUserDetailsChangedListener {
    private static final int CONTACTS_PERMISSION_REQUEST_CODE = 123;
    FloatingActionButton newMessagesView;
    ImageView userProfileView;
    ArrayList<UserChat> chats;
    RecyclerView listOfMessagesView;
    String oldProfile;
    DatabaseHelper databaseHelper;
    FirebaseHelper firebaseHelper;
        FirestoreRecyclerAdapter<UserChat, UserViewHolder> firebaseRecyclerAdapter;
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
//            listOfMessages = databaseHelper.getAllMessageDetails();

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

            firebaseHelper = new FirebaseHelper();

            firebaseHelper.addCurrentUserDetailsListener(this);
            initListView();

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


    private void initListView() {
        try {
//            chatsAdapter = new ChatsAdapter(new ArrayList<>(), this);


            FirestoreRecyclerOptions<UserChat> firebaseRecyclerOptions = new FirestoreRecyclerOptions.Builder<UserChat>()
                    .setQuery(firebaseHelper.getMessagesQuery(), UserChat.class)
                    .setLifecycleOwner(this)
                    .build();
            firebaseRecyclerAdapter = new FirestoreRecyclerAdapter<UserChat, UserViewHolder>(firebaseRecyclerOptions) {


                @Override
                protected void onBindViewHolder(@NonNull UserViewHolder holder, int position, @NonNull UserChat model) {

                    String userName;
                    String profilePic;
                    boolean isUser1;
                    String uuid;

                    isUser1 = currentUser.getUid().equals(model.user1Id);
                    if(!isUser1) {
                        userName = model.user1Name;
                        profilePic = model.user1ProfilePicture;
                        uuid = model.user1Id;
                    } else {
                        userName = model.user2Name;
                        profilePic = model.user2ProfilePicture;
                        uuid = model.user2Id;
                    }

                    holder.userNameTextView.setText(userName);
                    if (profilePic != null && !profilePic.isEmpty()) {
                        Glide.with((Context) DashboardScreen.this)
                                .load(profilePic)
                                .circleCrop()
                                .error(R.drawable.profile_ic)
                                .into(holder.profilePicImageView);
                    }

                    if((isUser1) ? model.isUser1Opened : model.isUser2Opened) {
                        holder.isClickedView.setVisibility(View.GONE);
                    } else {
                        holder.isClickedView.setVisibility(View.VISIBLE);
                    }

                    holder.messageItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            firebaseHelper.updateModelIsOpened((isUser1) ? "isUser1Opened" : "isUser2Opened", model.messageId);
                            Intent startChatIntent = new Intent(DashboardScreen.this, ChatScreen.class);
                            startChatIntent.putExtra("user", model);
                            DashboardScreen.this.startActivity(startChatIntent);
                        }
                    });

                    holder.lastMessageView.setText(model.lastMessage);
                }

                @NonNull
                @Override
                public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_details, parent, false);
                    return new UserViewHolder(view);
                }


            };

            listOfMessagesView.setLayoutManager(new LinearLayoutManager(this));
            listOfMessagesView.setAdapter(firebaseRecyclerAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCurrentUserDetailsChanged(UserDetails updatedUser) {
        currentUserDetails = updatedUser;
        initUserDetails();
    }

    private void initUserDetails() {
        try {
            Glide.with(this)
                    .load(currentUserDetails.profilePic)
                    .circleCrop()
                    .error(R.drawable.profile_ic)
                    .into(userProfileView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        ImageView profilePicImageView;
        LinearLayout messageItemView;
        TextView lastMessageView;
        CardView isClickedView;

        public UserViewHolder(View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameView);
            profilePicImageView = itemView.findViewById(R.id.userProfileView);
            messageItemView = itemView.findViewById(R.id.messageItemView);
            lastMessageView = itemView.findViewById(R.id.lastMessageView);
            isClickedView = itemView.findViewById(R.id.isClicked);
            isClickedView.setVisibility(View.GONE);
        }
    }
}