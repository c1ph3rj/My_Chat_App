package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.DashboardScreen.currentUser;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class ChatScreen extends AppCompatActivity implements FirebaseHelper.RealtimeDataListener<ChatMessage> {
    TextView nameView;
    UserDetails chatUser;
    ImageView backBtn;
    FirebaseHelper firebaseHelper;
    CardView sendBtn;
    EditText messageTextView;
    ListView chatMessagesView;
    ChatAdapter chatAdapter;
    ArrayList<ChatMessage> chatMessages;
    boolean isPaginationEnabled;
    String messageId;
    int PAGE_SIZE = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        getSupportActionBar().hide();

        Intent intent = getIntent();
        if (intent.hasExtra("user")) {
            UserDetails userDetails = (UserDetails) intent.getSerializableExtra("user");
            if (userDetails != null) {
                chatUser = userDetails;
                if (chatUser.messageId != null) {
                    messageId = chatUser.messageId;
                } else {
                    messageId = currentUser.getUid() + chatUser.uuid;
                }
            }
        }

        init();
    }

    void init() {
        try {
            nameView = findViewById(R.id.nameView);
            backBtn = findViewById(R.id.backBtn);
            sendBtn = findViewById(R.id.sendBtn);
            messageTextView = findViewById(R.id.messageTextView);
            chatMessagesView = findViewById(R.id.chatMessagesView);
            chatAdapter = new ChatAdapter(this, new ArrayList<>());
            chatMessagesView.setAdapter(chatAdapter);

            firebaseHelper = new FirebaseHelper();
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });

            nameView.setText(chatUser.userName);

            sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String messageText = messageTextView.getText().toString().trim();
                    if (messageText.isEmpty()) {
                        return;
                    }

                    ChatMessage chatMessage = new ChatMessage();

                    chatMessage.message = messageText;
                    chatMessage.senderId = currentUser.getUid();
                    chatMessage.timestamp = System.currentTimeMillis();

                    firebaseHelper.sendMessage(currentUser.getUid(), chatMessage, chatUser);
                    messageTextView.setText("");
                }
            });

            fetchInitialMessages();
            firebaseHelper.addRealtimeDataListener(messageId, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchInitialMessages() {
        // Set a large timestamp to get the latest messages
        long lastTimestamp = Long.MAX_VALUE;

        firebaseHelper.fetchChatsForMessageId(messageId, PAGE_SIZE, new FirebaseHelper.LoadChatsCallBack() {
            @Override
            public void onChatsLoaded(List<ChatMessage> chatMessageList, boolean hasMore) {
                // Update your adapter or UI with the loaded messages
                chatAdapter.addMessages(chatMessageList);

                // Enable or disable pagination based on hasMore
                isPaginationEnabled = hasMore;
            }
        });
    }

//    private void loadMoreMessages() {
//        if (isPaginationEnabled) {
//            // Get the timestamp of the oldest message in your current list
//            long lastTimestamp = chatAdapter.getItem(0).getTimestamp();
//
//            // Fetch more messages with pagination
//            firebaseHelper.fetchChatsForMessageId(messageId, lastTimestamp, PAGE_SIZE, new FirebaseHelper.LoadChatsCallBack() {
//                @Override
//                public void onChatsLoaded(List<ChatMessage> chatMessageList, boolean hasMore) {
//                    // Update your adapter or UI with the loaded messages
//                    chatAdapter.addMessages(chatMessageList);
//
//                    // Enable or disable pagination based on hasMore
//                    isPaginationEnabled = hasMore;
//                }
//            });
//        }
//    }

    @Override
    public void onDataAdded(ChatMessage data) {
        chatAdapter.addMessage(data);
        chatMessagesView.setSelection(chatAdapter.getCount() - 1);
    }

    @Override
    public void onDataChanged(ChatMessage data) {

    }

    @Override
    public void onDataRemoved(ChatMessage data) {

    }

    @Override
    public void onError(DatabaseError databaseError) {

    }
}