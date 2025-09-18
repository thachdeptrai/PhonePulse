package com.phoneapp.phonepulse.Response;

import com.phoneapp.phonepulse.models.User;

public class RegisterResponse {
    private boolean success;
    private String message;
    private User data;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public User getData() {
        return data;
    }
}
