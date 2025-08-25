package com.phoneapp.phonepulse.request;

public class VerifyOtpRequest {
    private String email;
    private String code;

    public VerifyOtpRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    // Getter & Setter
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
