package com.phoneapp.phonepulse.data.network;

public class DeleteSelfRequest {
    private String password;

    public DeleteSelfRequest(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }
}
