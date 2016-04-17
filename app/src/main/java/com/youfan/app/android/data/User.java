package com.youfan.app.android.data;

public class User {
    public String facebookId;
    public String facebookToken;
    public UserInfo userInfo;

    public User(){
    }

    public User(String token, String facebookId) {
        this.facebookToken = token;
        this.facebookId = facebookId;
    }
}


