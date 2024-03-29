package com.codinginflow.myapplication;

import static com.codinginflow.myapplication.MainActivity.currentUser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends ArrayAdapter<ChatMessage> {

    private static final int VIEW_TYPE_SENDER = 0;
    private static final int VIEW_TYPE_RECEIVER = 1;
    private final Context context;
    private final List<ChatMessage> messages;
    private boolean isLoading = false;

    public ChatAdapter(Context context, List<ChatMessage> messages) {
        super(context, 0, messages);
        this.context = context;
        this.messages = messages;
    }

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyDataSetChanged();
    }

    public void addMessages(List<ChatMessage> newMessages) {
        messages.addAll(newMessages);
        notifyDataSetChanged();
        isLoading = false;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        int viewType = getItemViewType(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(
                    (viewType == VIEW_TYPE_SENDER) ?
                            R.layout.item_message_sender : R.layout.item_message_receiver, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.messageTextView = convertView.findViewById(R.id.messageTextView);
            viewHolder.dateTimeView = convertView.findViewById(R.id.timeDateView);
            viewHolder.userNameView = convertView.findViewById(R.id.userNameView);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (position == 0 && !isLoading) {
            // Trigger the loading of more messages
            // Implement your logic to load more messages here
            isLoading = true;
        }

        ChatMessage message = getItem(position);

        if (message != null) {
            viewHolder.messageTextView.setText(message.getMessage());
            if(message.senderName != null) {
                viewHolder.userNameView.setText(message.senderName);
                viewHolder.userNameView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.userNameView.setVisibility(View.GONE);
            }
            viewHolder.dateTimeView.setText(convertMillisToDateTime(message.timestamp));
        }


        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        return 2; // Two types: Sender and Receiver
    }

    @Nullable
    @Override
    public ChatMessage getItem(int position) {
        return messages.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = getItem(position);
        return (message != null && currentUser.getUid().equals(message.senderId)) ?
                VIEW_TYPE_SENDER : VIEW_TYPE_RECEIVER;
    }

    private static class ViewHolder {
        TextView messageTextView;
        TextView userNameView;
        TextView dateTimeView;
    }

    public static String convertMillisToDateTime(long currentTimeMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault());

        Date date = new Date(currentTimeMillis);

        return sdf.format(date);
    }
}
