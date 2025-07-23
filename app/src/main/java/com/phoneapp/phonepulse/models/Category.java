package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("_id")
    private String id; // Maps to MongoDB's _id

    @SerializedName("name")
    private String name; // Category name

    @SerializedName("icon")
    private String icon; // URL or name of the icon

    @SerializedName("created_date")
    private String createdDate; // Creation date

    @SerializedName("modified_date")
    private String modifiedDate; // Last modified date

    // --- Constructors (Optional, but good practice) ---
    public Category() {
        // Default constructor required for Gson
    }

    public Category(String id, String name, String icon, String createdDate, String modifiedDate) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    // --- Getters and Setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}