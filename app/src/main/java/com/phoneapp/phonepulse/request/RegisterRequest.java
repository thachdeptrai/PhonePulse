package com.phoneapp.phonepulse.request;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("name")   // ✅ khớp với backend
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("password")
    private String password;

    @SerializedName("otp")   // ✅ thêm OTP để backend xác minh
    private String otp;

    public RegisterRequest() {}

    public RegisterRequest(String name, String email, String phone, String password, String otp) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.otp = otp;
    }

    // Getters và Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public String getOtp() {
        return otp;
    }
    public void setOtp(String otp) {
        this.otp = otp;
    }

    @Override
    public String toString() {
        return "RegisterRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", otp='" + otp + '\'' +
                ", password='[HIDDEN]'" +
                '}';
    }
}
