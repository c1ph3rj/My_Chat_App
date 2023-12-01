package com.codinginflow.myapplication;

public class ChatMessage {
    public String senderId;
    public String message;
    public long timestamp;

    public ChatMessage() {
        // Default constructor required for Firebase
    }

    public ChatMessage(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
