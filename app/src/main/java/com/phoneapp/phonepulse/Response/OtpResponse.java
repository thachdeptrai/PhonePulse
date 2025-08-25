package com.phoneapp.phonepulse.Response;

public class OtpResponse {
    private boolean success;
    private String message;
    private String otp; // nếu server trả về để test

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getOtp() {
        return otp;
    }
}
