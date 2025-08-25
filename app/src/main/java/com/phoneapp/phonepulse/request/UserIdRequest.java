package com.phoneapp.phonepulse.request;

public class UserIdRequest {
    private String userId;

    public UserIdRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
