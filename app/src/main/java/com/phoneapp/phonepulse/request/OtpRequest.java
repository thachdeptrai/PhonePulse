package com.phoneapp.phonepulse.request;

public class OtpRequest {
    private String email;

    public OtpRequest(String email) {
        this.email = email;
    }

    // Getter
    public String getEmail() {
        return email;
    }
}
