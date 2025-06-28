package com.phoneapp.phonepulse.data.network;

public class UpdateUserRequest {
    private String fullname;
    private String phone;
    private String avatar;
    private String role;        // "admin" hoặc "user"
    private boolean is_verified;
    private boolean status;

    public UpdateUserRequest(String fullname, String phone, String avatar, String role, boolean is_verified, boolean status) {
        this.fullname = fullname;
        this.phone = phone;
        this.avatar = avatar;
        this.role = role;
        this.is_verified = is_verified;
        this.status = status;
    }

    public String getFullname() {
        return fullname;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getRole() {
        return role;
    }

    public boolean isVerified() {
        return is_verified;
    }

    public boolean isStatus() {
        return status;
    }

}
