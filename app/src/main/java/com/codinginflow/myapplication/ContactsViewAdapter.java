package com.codinginflow.myapplication;

// UserDetailsAdapter.java

import static com.codinginflow.myapplication.MainActivity.currentUser;
import static com.codinginflow.myapplication.MainActivity.currentUserDetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ContactsViewAdapter extends RecyclerView.Adapter<ContactsViewAdapter.ViewHolder> {

    private final List<UserDetails> userDetailsList;
    private final Activity activity;

    public ContactsViewAdapter(List<UserDetails> userDetailsList, Activity activity) {
        this.userDetailsList = userDetailsList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserDetails userDetails = userDetailsList.get(position);

        holder.usernameTextView.setText(userDetails.userName);
        holder.phoneNumberTextView.setText(userDetails.phoneNumber);

        if (userDetails.profilePic != null && !userDetails.profilePic.isEmpty()) {
            Glide.with((Context) activity)
                    .load(userDetails.profilePic)
                    .circleCrop()
                    .error(R.drawable.profile_ic)
                    .into(holder.userIcon);
        }
        holder.contactView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startChatIntent = new Intent(activity, ChatScreen.class);
                UserChat chatUser = new UserChat();
                chatUser.messageId = userDetails.messageId;
                chatUser.user1Name = currentUserDetails.userName;
                chatUser.user1Id = currentUserDetails.uuid;
                chatUser.user1ProfilePicture = currentUserDetails.profilePic;
                chatUser.user2Name = userDetails.userName;
                chatUser.user2Id = userDetails.uuid;
                chatUser.user2ProfilePicture = userDetails.profilePic;
                chatUser.isExists = userDetails.isExists;
                startChatIntent.putExtra("user", chatUser);
                activity.startActivity(startChatIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userDetailsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView;
        TextView phoneNumberTextView;
        ImageView userIcon;
        LinearLayout contactView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.userNameView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNoView);
            contactView = itemView.findViewById(R.id.contactView);
            userIcon = itemView.findViewById(R.id.userProfileView);
        }
    }
}
