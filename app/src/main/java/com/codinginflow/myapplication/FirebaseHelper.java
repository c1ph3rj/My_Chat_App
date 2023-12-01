package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.DashboardScreen.currentUser;
import static com.codinginflow.myapplication.MainActivity.currentUserDetails;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Lists;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
    private final CollectionReference usersCollection;
    private final DatabaseReference phoneNumbersRef;
    private final DatabaseReference messagesRef;
    List<String> usersInDb;

    public FirebaseHelper() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        phoneNumbersRef = database.getReference("phoneNumbers");
        usersCollection = db.collection("users");
        messagesRef = database.getReference("messages");
        usersInDb = new ArrayList<>();
    }

    public interface LoadMessagesCallBack {
        void onMessagedLoaded(ArrayList<UserDetails> userDetailsList);
    }


    public void fetchMessagesAndUserDetails(LoadMessagesCallBack callback) {
        // Query messages where currentUserUUID is in the array
        if (currentUserDetails == null) {
            getUserDetailsById(currentUser.getUid()).addOnCompleteListener(new OnCompleteListener<UserDetails>() {
                @Override
                public void onComplete(@NonNull Task<UserDetails> task) {
                    if (task.isSuccessful()) {
                        fetchMessages(callback);
                    }
                }
            });
        } else {
            fetchMessages(callback);
        }
    }

    private void fetchMessages(LoadMessagesCallBack callback) {
        usersCollection.document(currentUser.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<String> otherUserUUIDs = new ArrayList<>();
                UserDetails userDetails = task.getResult().toObject(UserDetails.class);
                // Extract otherUserUUIDs from the messages
                if (userDetails != null) {
                    if(userDetails.messages != null && !userDetails.messages.isEmpty()) {
                        for (String eachChatsId : userDetails.messages) {
                            String eachUUID = eachChatsId.replace(currentUser.getUid(), "");
                            otherUserUUIDs.add(eachUUID);
                        }

                        // Fetch user details for each otherUserUUID
                        fetchUserDetailsForUUIDs(otherUserUUIDs, callback, userDetails.messages);
                    }
                }
            } else {
                // Handle errors
            }
        });
    }

    private void fetchUserDetailsForUUIDs(ArrayList<String> userUUIDs, LoadMessagesCallBack callback, ArrayList<String> messages) {
        ArrayList<UserDetails> userDetailsList = new ArrayList<>();

        // Fetch user details for each userUUID
        for (String userUUID : userUUIDs) {
            usersCollection.document(userUUID).get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        UserDetails userDetails = document.toObject(UserDetails.class);
                        if (userDetails != null) {
                            userDetails.messageId = messages.get(userUUIDs.indexOf(userUUID));
                            userDetailsList.add(userDetails);
                        }
                    }
                } else {
                    userDetailsList.add(new UserDetails());
                    // Handle errors
                }

                // Check if all user details have been fetched
                if (userDetailsList.size() == userUUIDs.size()) {
                    callback.onMessagedLoaded(userDetailsList);
                }
            });
        }
    }

    void sendMessage(String senderId, ChatMessage chatMessage, String receiverId) {
        ArrayList<String> messages = currentUserDetails.messages;
        String messageId = senderId + receiverId;
        if (messages != null && !messages.isEmpty() &&messages.stream().allMatch((eachMessages) -> (eachMessages.replace(senderId, "").equals(receiverId)))) {
            for (String oldMessageId : messages) {
                String otherId = oldMessageId.replace(senderId, "");
                if (otherId.equals(receiverId)) {
                    messageId = messages.get(messages.indexOf(oldMessageId));
                }
            }
            messagesRef.child(messageId).child(String.valueOf(System.nanoTime())).setValue(chatMessage);
        } else {
            String finalMessageId = messageId;
            messagesRef.child(finalMessageId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        if (currentUser != null) {
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(currentUser.getUid())
                                    .update("messages", FieldValue.arrayUnion(finalMessageId))
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("FirebaseHelper", "Message reference added to user's document.");
                                        } else {
                                            Log.e("FirebaseHelper", "Error adding message reference to user's document: " + task.getException().getMessage());
                                        }
                                    });
                            FirebaseFirestore.getInstance()
                                    .collection("users")
                                    .document(receiverId)
                                    .update("messages", FieldValue.arrayUnion(finalMessageId))
                                    .addOnCompleteListener(task -> {
                                        if (task.isSuccessful()) {
                                            Log.d("FirebaseHelper", "Message reference added to user's document.");
                                        } else {
                                            Log.e("FirebaseHelper", "Error adding message reference to user's document: " + task.getException().getMessage());
                                        }
                                    });

                            messagesRef.child(finalMessageId).child(String.valueOf(System.nanoTime())).setValue(chatMessage);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    public Task<Void> storeUserDetails(String userId, String phoneNumber, String userName, String profilePic, String aboutDetails) {
        UserDetails user = new UserDetails();
        user.userName = userName;
        user.phoneNumber = phoneNumber;
        user.profilePic = profilePic;
        user.uuid = userId;
        user.aboutDetails = aboutDetails;

        DocumentReference userDocument = usersCollection.document(userId);

        return userDocument.set(user, SetOptions.merge());
    }

    public Task<Void> updateUserInfo(String userId, String userName, String aboutDetails) {
        DocumentReference userDocument = usersCollection.document(userId);

        Map<String, Object> updates = new HashMap<>();
        if (userName != null) {
            updates.put("userName", userName);
        }
        if (aboutDetails != null) {
            updates.put("aboutDetails", aboutDetails);
        }

        return userDocument.update(updates);
    }

    public Task<UserDetails> getUserDetailsById(String userId) {
        DocumentReference userDocument = usersCollection.document(userId);

        return userDocument.get()
                .continueWith(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            return document.toObject(UserDetails.class);
                        }
                    }
                    return null;
                });
    }

    public void storePhoneNumber(String userId, String phoneNumber) {
        phoneNumbersRef.child(phoneNumber).setValue(userId);
    }

    public interface UserDetailsCallback {
        void onUserDetailsFetched(List<UserDetails> userDetailsList);
    }

    public void checkAndStoreUserDetails(List<ContactDetails> contactDetailsList, UserDetailsCallback callback) {
        List<UserDetails> userDetailsList = new ArrayList<>();
        for (ContactDetails contactDetails : contactDetailsList) {
            String phoneNumber = sanitizePhoneNumber(contactDetails.mobileNumber.replace(" ", ""));
            if (!currentUser.getPhoneNumber().contains(phoneNumber)) {
                phoneNumbersRef.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String userId = dataSnapshot.getValue(String.class);
                            if (userId != null) {
                                if (!usersInDb.contains(phoneNumber)) {
                                    usersInDb.add(phoneNumber);
                                    fetchAndStoreUserDetails(userId, userDetailsList, callback);
                                }
                            } else if (contactDetailsList.indexOf(contactDetails) == contactDetailsList.size() - 1) {
                                if (usersInDb.size() == 0) {
                                    callback.onUserDetailsFetched(new ArrayList<>());
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("FireStoreHelper", "Error checking phone number: " + databaseError.getMessage());
                    }
                });
            }
        }
    }

    private void fetchAndStoreUserDetails(String userId, List<UserDetails> userDetailsList, UserDetailsCallback callback) {
        usersCollection.document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    UserDetails userDetails = document.toObject(UserDetails.class);
                    if (userDetails != null) {
                        userDetailsList.add(userDetails);
                    }
                } else {
                    Log.d("FireStoreHelper", "User details do not exist for userId: " + userId);
                }

                if (userDetailsList.size() == usersInDb.size()) {
                    callback.onUserDetailsFetched(userDetailsList);
                }
            } else {
                // Handle errors
                Log.e("FireStoreHelper", "Error fetching user details: " + task.getException().getMessage());
            }
        });
    }

    private String sanitizePhoneNumber(String phoneNumber) {
        phoneNumber = phoneNumber.replaceAll("[.#\\$\\[\\]]", "");
        phoneNumber = phoneNumber.replace("+91", "");
        return phoneNumber.startsWith("0") ? phoneNumber.substring(1) : phoneNumber;
    }

    public interface LoadChatsCallBack {
        void onChatsLoaded(List<ChatMessage> chatMessageList, boolean hasMore);
    }

    public void fetchChatsForMessageId(String messageId, int pageSize, LoadChatsCallBack callback) {
        DatabaseReference messageRef = messagesRef.child(messageId);

        Query query = messageRef.limitToLast(pageSize);

        query.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    DataSnapshot dataSnapshot = task.getResult();
                    if (dataSnapshot != null) {
                        List<ChatMessage> chatMessageList = new ArrayList<>();
                        for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                            ChatMessage chatMessage = chatSnapshot.getValue(ChatMessage.class);
                            if (chatMessage != null) {
                                chatMessageList.add(chatMessage);
                            }
                        }

                        // Check if there are more messages
                        boolean hasMore = chatMessageList.size() == pageSize;

                        // Remove the first item if it was included for querying purposes
                        if (hasMore) {
                            chatMessageList.remove(0);
                        }

                        callback.onChatsLoaded(chatMessageList, hasMore);
                    }
                } else {
                    // Handle errors
                    Log.e("FirebaseHelper", "Error fetching chats: " + task.getException().getMessage());
                }
            }
        });
    }

    public interface RealtimeDataListener<ChatMessage> {
        void onDataAdded(ChatMessage data);
        void onDataChanged(ChatMessage data);
        void onDataRemoved(ChatMessage data);
        void onError(DatabaseError databaseError);
    }

    public <T> void addRealtimeDataListener(String messageId, RealtimeDataListener<ChatMessage> listener) {
        messagesRef.child(messageId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<DataSnapshot> allDataSnapshots = Lists.newArrayList(dataSnapshot.getChildren());
                ChatMessage chatData = allDataSnapshots.get(allDataSnapshots.size() - 1).getValue(ChatMessage.class);
                listener.onDataAdded(chatData);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onError(databaseError);
            }
        });
    }
}
