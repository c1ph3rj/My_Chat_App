package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.MainActivity.currentUser;
import static com.codinginflow.myapplication.MainActivity.currentUserDetails;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class ChatScreen extends AppCompatActivity implements FirebaseHelper.RealtimeDataListener<ChatMessage> {
    TextView nameView;
    UserChat chatUser;
    ImageView backBtn;
    FirebaseHelper firebaseHelper;
    CardView sendBtn;
    EditText messageTextView;
    ListView chatMessagesView;
    ImageView userProfileView;
    ChatAdapter chatAdapter;
    String userName;
    String profilePic;
    boolean isUser1;
    String uuid;
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
            UserChat userDetails = (UserChat) intent.getSerializableExtra("user");
            if (userDetails != null) {
                chatUser = userDetails;
                if (chatUser.messageId != null) {
                    messageId = chatUser.messageId;
                } else {
                    messageId = firebaseHelper.generateChatId(currentUser.getUid(), uuid);
                    chatUser.messageId = messageId;
                }
                isUser1 = !currentUser.getUid().equals(chatUser.user1Id);
                if(isUser1) {
                    userName = chatUser.user1Name;
                    profilePic = chatUser.user1ProfilePicture;
                    uuid = chatUser.user1Id;
                } else {
                    userName = chatUser.user2Name;
                    profilePic = chatUser.user2ProfilePicture;
                    uuid = chatUser.user2Id;
                }
                firebaseHelper = new FirebaseHelper();

            }
        }

        init();
    }

    void init() {
        try {
            nameView = findViewById(R.id.nameView);
            backBtn = findViewById(R.id.backBtn);
            sendBtn = findViewById(R.id.sendBtn);
            userProfileView = findViewById(R.id.userProfileView);
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



            nameView.setText(userName);

            if(profilePic != null && !profilePic.isEmpty()) {
                Glide.with(this)
                        .load(profilePic)
                        .error(R.drawable.profile_ic)
                        .circleCrop()
                        .into(userProfileView);
            }

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
                    chatMessage.senderName = currentUserDetails.userName;
                    if (!isUser1) {
                        chatUser.isUser2Opened = false;
                    } else {
                        chatUser.isUser1Opened = false;
                    }
                    chatUser.updatedTimeChamp = String.valueOf(System.currentTimeMillis());
                    chatUser.lastMessage = chatMessage.message;
                    firebaseHelper.sendMessage(chatUser, chatMessage);
                    messageTextView.setText("");
                }
            });

//            fetchInitialMessages();
            firebaseHelper.addRealtimeDataListener(messageId, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchInitialMessages() {
        // Set a large timestamp to get the latest messages
        long lastTimestamp = Long.MAX_VALUE;

        firebaseHelper.fetchMessagesForMessageId(messageId, PAGE_SIZE, new FirebaseHelper.LoadChatsCallBack() {
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
    public void onDataRemoved(ChatMessage data) {

    }

    @Override
    public void onError(DatabaseError databaseError) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseHelper.updateModelIsOpened((!isUser1) ? "isUser1Opened" : "isUser2Opened", messageId);
    }
}