package com.codinginflow.myapplication;

import java.io.Serializable;

public class Message implements Serializable{
    public String messageId;
    public String lastMessage;
    public Message(String messageId, String lastMessage) {
        this.messageId = messageId;
        this.lastMessage = lastMessage;
    }

    public Message() {

    }
}
