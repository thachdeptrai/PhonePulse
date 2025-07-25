package com.phoneapp.phonepulse.models;

public class User {
    private String id;
    private String name;
    private String email;
    private String password; // dùng để nhận nếu cần thiết (nhưng nên bỏ)
    private String avatar_url;
    private String phone;
    private String address;
    private String gender;
    private String birthday;

    private String googleId;
    private String facebookId;
    private String provider;

    private boolean role;        // true: admin, false: user
    private boolean status;
    private boolean is_verified;

    private String created_date;
    private String modified_date;

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getAvatar_url() { return avatar_url; }
    public void setAvatar_url(String avatar_url) { this.avatar_url = avatar_url; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getFacebookId() { return facebookId; }
    public void setFacebookId(String facebookId) { this.facebookId = facebookId; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public boolean isRole() { return role; }
    public void setRole(boolean role) { this.role = role; }

    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public boolean isIs_verified() { return is_verified; }
    public void setIs_verified(boolean is_verified) { this.is_verified = is_verified; }

    public String getCreated_date() { return created_date; }
    public void setCreated_date(String created_date) { this.created_date = created_date; }

    public String getModified_date() { return modified_date; }
    public void setModified_date(String modified_date) { this.modified_date = modified_date; }
}
