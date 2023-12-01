package com.codinginflow.myapplication;

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
import java.util.List;

public class ListOfMessagesAdapter extends RecyclerView.Adapter<ListOfMessagesAdapter.UserViewHolder> {

    private final List<UserDetails> messagesList;
    private final Activity activity;

    public ListOfMessagesAdapter(List<UserDetails> messagesList, Activity activity) {
        this.messagesList = messagesList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item_details, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        UserDetails userDetails = messagesList.get(position);
        holder.userNameTextView.setText(userDetails.userName);

        holder.messageItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startChatIntent = new Intent(activity, ChatScreen.class);
                startChatIntent.putExtra("user", userDetails);
                activity.startActivity(startChatIntent);
            }
        });
        // Load profile picture using Picasso or any other image loading library
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView;
        ImageView profilePicImageView;
        LinearLayout messageItemView;

        public UserViewHolder(View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameView);
            profilePicImageView = itemView.findViewById(R.id.userProfileView);
            messageItemView = itemView.findViewById(R.id.messageItemView);
        }
    }
}

