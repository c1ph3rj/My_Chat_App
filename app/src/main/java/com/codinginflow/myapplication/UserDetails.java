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
    public ArrayList<String> messages;

    public UserDetails() {
    }
}
