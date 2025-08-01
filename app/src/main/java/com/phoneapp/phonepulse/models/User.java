package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class User {

    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String passwordHash;

    @SerializedName("avatar_url")
    private String avatar_url; // Đã đổi tên biến để khớp với tên phương thức

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("gender")
    private String gender;

    @SerializedName("birthday")
    private Date birthday;

    @SerializedName("role")
    private boolean isAdmin;

    @SerializedName("status")
    private boolean status;

    @SerializedName("is_verified")
    private boolean isVerified;

    @SerializedName("created_date")
    private Date createdDate;

    @SerializedName("modified_date")
    private Date modifiedDate;

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // Giữ nguyên tên phương thức theo yêu cầu
    public String getAvatar_url() {
        return avatar_url;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getGender() {
        return gender;
    }

    public Date getBirthday() {
        return birthday;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public boolean getStatus() {
        return status;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    // Giữ nguyên tên phương thức theo yêu cầu
    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public void setAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public void setVerified(boolean isVerified) {
        this.isVerified = isVerified;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}