package com.codinginflow.myapplication;

import java.io.Serializable;
import java.util.ArrayList;

public class UserDetails implements Serializable {
    public String userName;
    public String uuid;
    public String phoneNumber;
    public String profilePic;
    public String aboutDetails;
    public ArrayList<String> messages = new ArrayList<>();
    public UserDetails() {
    }
}
