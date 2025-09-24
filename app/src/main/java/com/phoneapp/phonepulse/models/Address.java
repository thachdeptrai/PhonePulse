package com.phoneapp.phonepulse.models;

public class Address {
    private String fullName;
    private String phoneNumber;
    private String fullAddress;
    private boolean isSelected; // để check RadioButton

    // No-arg constructor (tốt cho Gson/serialization)
    public Address() {
        this.fullName = "";
        this.phoneNumber = "";
        this.fullAddress = "";
        this.isSelected = false;
    }

    // Existing constructor
    public Address(String fullName, String phoneNumber, String fullAddress) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.fullAddress = fullAddress;
        this.isSelected = false;
    }

    // Optional convenience constructor: chỉ address
    public Address(String fullAddress) {
        this.fullName = "";
        this.phoneNumber = "";
        this.fullAddress = fullAddress;
        this.isSelected = false;
    }

    // getters/setters...
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
