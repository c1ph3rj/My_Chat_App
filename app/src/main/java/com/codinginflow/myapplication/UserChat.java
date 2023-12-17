package com.codinginflow.myapplication;

import java.io.Serializable;

public class UserChat implements Serializable{
    public String messageId;
    public String lastMessage;
    public String user1Id;
    public String user1Name;
    public boolean isUser1Opened;
    public String user1ProfilePicture;
    public String user2Id;
    public String user2Name;
    public boolean isUser2Opened;
    public String user2ProfilePicture;
    public String updatedTimeChamp;
    public boolean isExists;

    public UserChat() {

    }
}
