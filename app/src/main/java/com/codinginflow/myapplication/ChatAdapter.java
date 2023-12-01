package com.codinginflow.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<ChatMessage> {

    private Context context;
    private List<ChatMessage> messages;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ChatMessage message = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        }

        TextView senderTextView = convertView.findViewById(R.id.senderTextView);
        TextView messageTextView = convertView.findViewById(R.id.messageTextView);

        // Set sender and message text
        senderTextView.setText(message.getSenderId());
        messageTextView.setText(message.getMessage());

        return convertView;
    }
}

