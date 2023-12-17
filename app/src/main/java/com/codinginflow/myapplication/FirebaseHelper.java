package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.MainActivity.currentUser;
import static com.codinginflow.myapplication.MainActivity.currentUserDetails;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseHelper {
    private final CollectionReference usersCollection;
    private final DatabaseReference phoneNumbersRef;
    private final DatabaseReference chatsRef;
    private final StorageReference storageRef;
    private final DocumentReference currentUserDocRef;
    private final String USER_CHATS = "user_chats";
    List<String> usersInDb;

    public FirebaseHelper() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

        String USERS = "users";
        usersCollection = db.collection(USERS);
        String PHONE_NUMBER = "phoneNumbers";
        phoneNumbersRef = database.getReference(PHONE_NUMBER);
        String CHATS = "chats";
        chatsRef = database.getReference(CHATS);
        if (currentUser != null) {
            currentUserDocRef = usersCollection.document(currentUser.getUid());
            storageRef = firebaseStorage.getReference(currentUser.getUid());
        } else {
            currentUserDocRef = null;
            storageRef = null;
        }
        usersInDb = new ArrayList<>();
    }

    public Task<Uri> uploadProfilePic(Uri fileUri) {
        storageRef.child("profilePic/" + currentUser.getUid() + ".jpg");
        UploadTask uploadTask = storageRef.putFile(fileUri);
        return uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful() && task.getException() != null) {
                    throw task.getException();
                }

                return storageRef.getDownloadUrl();
            }
        });
    }

    private void addChatDetails(String userId, UserChat chatDetails) {
        usersCollection.document(userId)
                .collection(USER_CHATS)
                .document(chatDetails.messageId)
                .set(chatDetails)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            System.out.println("Chat added Successfully:" + chatDetails.messageId);
                        } else {
                            if (task.getException() != null) {
                                task.getException().printStackTrace();
                            }
                        }
                    }
                });
    }

    public void checkAndUpdateChatDetails(UserChat chatDetails, String userId) {
        usersCollection.document(userId)
                .collection(USER_CHATS)
                .document(chatDetails.messageId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot chatDetailsRef = task.getResult();
                            if (chatDetailsRef.exists()) {
                                updateChatDetails(userId, chatDetails);
                            } else {
                                addChatDetails(userId, chatDetails);
                            }
                        } else {
                            if (task.getException() != null) {
                                task.getException().printStackTrace();
                            }
                        }
                    }
                });
    }

    private void updateChatDetails(String userId, UserChat chatDetails) {
        usersCollection.document(userId)
                .collection(USER_CHATS)
                .document(chatDetails.messageId)
                .set(chatDetails, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            System.out.println("Chat Updated Successfully:" + chatDetails.messageId);
                        } else {
                            if (task.getException() != null) {
                                task.getException().printStackTrace();
                            }
                        }
                    }
                });
    }

    void sendMessage(UserChat chatDetails, ChatMessage chatMessage) {
        checkAndUpdateChatDetails(chatDetails, currentUser.getUid());
        checkAndUpdateChatDetails(chatDetails, removeCurrentUserFromChatId(chatDetails.messageId, currentUser.getUid()));
        chatsRef.child(chatDetails.messageId).child(String.valueOf(System.nanoTime())).setValue(chatMessage);
    }

    void updateModelIsOpened(String field, String messageId) {
        currentUserDocRef.collection(USER_CHATS)
                .document(messageId)
                .update(field, true);
    }


    private void fetchAndStoreUserDetails(List<UserDetails> userDetailsList, UserDetailsCallback callback) {
        ArrayList<String> chatUserIds = new ArrayList<>();
        currentUserDocRef.collection(USER_CHATS)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> chatUsersRef = task.getResult().getDocuments();
                            for (DocumentSnapshot eachChat : chatUsersRef) {
                                UserChat chatUser = eachChat.toObject(UserChat.class);
                                if (chatUser != null) {
                                    chatUserIds.add(removeCurrentUserFromChatId(chatUser.messageId, currentUser.getUid()));
                                }
                            }
                            for(String eachUser : usersInDb) {
                                usersCollection.whereEqualTo("phoneNumber", eachUser)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                List<DocumentSnapshot> userDetailsRef = task.getResult().getDocuments();
                                                if(!userDetailsRef.isEmpty()) {
                                                    UserDetails userDetails = userDetailsRef.get(0).toObject(UserDetails.class);
                                                    if (userDetails != null) {
                                                        if (currentUserDetails != null) {
                                                            userDetails.messageId = generateChatId(currentUserDetails.uuid, userDetails.uuid);
                                                            if(chatUserIds.contains(userDetails.uuid)) {
                                                                userDetails.isExists = true;
                                                            }
                                                            userDetailsList.add(userDetails);
                                                        }
                                                    }
                                                }
                                                if(usersInDb.get(usersInDb.size() - 1).equals(eachUser)) {
                                                    callback.onUserDetailsFetched(userDetailsList);
                                                }
                                            }
                                        });
                            }
                        } else {
                            if (task.getException() != null) {
                                task.getException().printStackTrace();
                            }
                        }
                    }
                });
    }

public com.google.firebase.firestore.Query getMessagesQuery() {
        return usersCollection.document(currentUser.getUid()).collection(USER_CHATS);
}

    public String generateChatId(String userId1, String userId2) {
        // Sort the user IDs to ensure consistency
        String smallerUserId = userId1.compareTo(userId2) < 0 ? userId1 : userId2;
        String largerUserId = userId1.compareTo(userId2) < 0 ? userId2 : userId1;

        // Concatenate the user IDs to create a unique chat ID
        return smallerUserId + "_" + largerUserId;
    }

    private String removeCurrentUserFromChatId(String chatId, String currentUserUid) {
        // Split the chat ID into user IDs using the underscore as a delimiter
        String[] userIds = chatId.split("_");

        if (userIds.length == 2) {
            // Return the other user's ID as the modified chat ID
            return userIds[0].equals(currentUserUid) ? userIds[1] : userIds[0];
        } else {
            // Handle invalid chat ID
            return chatId;
        }
    }

    public void updateMessageDetails(String userId, Map<String, Object> updateMap) {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        usersCollection.document(userId).set(updateMap, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Handle success
                                        System.out.println("Document updated successfully!");
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    System.err.println("Error updating document: " + e.getMessage());
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
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

    public Task<Void> updateUserInfo(String userId, String userName, String aboutDetails, String profileUrl) {
        DocumentReference userDocument = usersCollection.document(userId);

        Map<String, Object> updates = new HashMap<>();
        if (userName != null) {
            updates.put("userName", userName);
        }
        if (aboutDetails != null) {
            updates.put("aboutDetails", aboutDetails);
        }

        if (profileUrl != null) {
            updates.put("profilePic", profileUrl);
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

    public void checkAndStoreUserDetails(List<ContactDetails> contactDetailsList, UserDetailsCallback callback) {
        List<UserDetails> userDetailsList = new ArrayList<>();
        final int[] totalContactCalls = {0};
        final int[] totalResponseCalls = {0};
        for (ContactDetails contactDetails : contactDetailsList) {
            String phoneNumber = sanitizePhoneNumber(contactDetails.mobileNumber.replace(" ", ""));
            if (currentUser != null && currentUser.getPhoneNumber() != null && !currentUser.getPhoneNumber().contains(phoneNumber)) {
                totalContactCalls[0] +=1;
                phoneNumbersRef.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String userId = dataSnapshot.getValue(String.class);
                            if (userId != null) {
                                if (!usersInDb.contains(phoneNumber)) {
                                    usersInDb.add(phoneNumber);
                                }
                            }
                        }
                        totalResponseCalls[0] += 1;
                        if (totalContactCalls[0] == totalResponseCalls[0]) {
                            if (usersInDb.size() == 0) {
                                callback.onUserDetailsFetched(new ArrayList<>());
                            } else {
                                fetchAndStoreUserDetails(userDetailsList, callback);
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

    private String sanitizePhoneNumber(String phoneNumber) {
        // Replace "+91" with an empty string
        phoneNumber = phoneNumber.replace("+91", "");

        // Remove any remaining "+" signs
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");

        // If the phone number starts with "0", remove the leading "0"
        return (phoneNumber.startsWith("0") ? phoneNumber.substring(1) : phoneNumber).trim();
    }


    public void fetchMessagesForMessageId(String messageId, int pageSize, LoadChatsCallBack callback) {
        DatabaseReference messageRef = chatsRef.child(messageId);

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
                    if (task.getException() != null) {
                        // Handle errors
                        Log.e("FirebaseHelper", "Error fetching chats: " + task.getException().getMessage());
                    }
                }
            }
        });
    }

    public void addRealtimeDataListener(String messageId, RealtimeDataListener<ChatMessage> listener) {
        chatsRef.child(messageId).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                ChatMessage chatData = snapshot.getValue(ChatMessage.class);
                listener.onDataAdded(chatData);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                ChatMessage chatMessage = snapshot.getValue(ChatMessage.class);
                listener.onDataRemoved(chatMessage);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onError(error);
            }
        });
    }

    public void addCurrentUserDetailsListener(final OnCurrentUserDetailsChangedListener listener) {
        usersCollection.document(currentUser.getUid()).addSnapshotListener((documentSnapshot, e) -> {
            if (e != null) {
                // Handle errors
                System.err.println("Error listening for user details: " + e.getMessage());
                return;
            }

            if (documentSnapshot != null && documentSnapshot.exists()) {
                UserDetails currentUser = documentSnapshot.toObject(UserDetails.class);
                if (currentUser != null) {
                    listener.onCurrentUserDetailsChanged(currentUser);
                }
            }
        });
    }

    public interface UserDetailsCallback {
        void onUserDetailsFetched(List<UserDetails> userDetailsList);
    }

    public interface LoadChatsCallBack {
        void onChatsLoaded(List<ChatMessage> chatMessageList, boolean hasMore);
    }


    public interface UpdateUserChatsListener {
        void updatedChat(UserChat updatedChatDetails);
    }

    public interface RealtimeDataListener<ChatMessage> {
        void onDataAdded(ChatMessage data);

        void onDataRemoved(ChatMessage data);

        void onError(DatabaseError databaseError);
    }

    // Interface to define a callback for user details changes
    public interface OnCurrentUserDetailsChangedListener {
        void onCurrentUserDetailsChanged(UserDetails updatedUser);
    }
}
