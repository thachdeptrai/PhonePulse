package com.phoneapp.phonepulse.data.network;

public class UpdateProfileRequest {
    private String fullname;
    private String phone;
    private String avatar; // URL ảnh nếu có

    public UpdateProfileRequest(String fullname, String phone, String avatar) {
        this.fullname = fullname;
        this.phone = phone;
        this.avatar = avatar;
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
}
