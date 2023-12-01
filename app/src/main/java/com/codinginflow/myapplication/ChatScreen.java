package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.DashboardScreen.currentUser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatScreen extends AppCompatActivity {
    TextView nameView;
    UserDetails chatUser;
    ImageView backBtn;
    FirebaseHelper firebaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        getSupportActionBar().hide();

        Intent intent = getIntent();
        if (intent.hasExtra("user")) {
            UserDetails userDetails = (UserDetails) intent.getSerializableExtra("user");
            if(userDetails != null) {
                chatUser = userDetails;
            }
        }

        init();
    }

    void init() {
        try {
            nameView = findViewById(R.id.nameView);
            backBtn = findViewById(R.id.backBtn);
            firebaseHelper = new FirebaseHelper();

            firebaseHelper.sendMessage(currentUser.getUid(), new ChatMessage(chatUser.uuid, "Hello World", SystemClock.currentThreadTimeMillis()), chatUser.uuid);
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            nameView.setText(chatUser.userName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}