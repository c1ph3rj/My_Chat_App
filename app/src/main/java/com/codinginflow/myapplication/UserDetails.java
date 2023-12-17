package com.codinginflow.myapplication;

import java.io.Serializable;
import java.util.ArrayList;

public class UserDetails implements Serializable {
    public String userName;
    public String uuid;
    public String phoneNumber;
    public String profilePic;
    public String aboutDetails;
    public String messageId;
    public boolean isExists;
    public UserDetails() {
    }

    public UserDetails(String userName, String uuid, String phoneNumber, String profilePic, String aboutDetails, String messageId) {
        this.userName = userName;
        this.uuid = uuid;
        this.phoneNumber = phoneNumber;
        this.profilePic = profilePic;
        this.aboutDetails = aboutDetails;
        this.messageId = messageId;
    }
}
