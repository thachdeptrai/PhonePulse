package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Category {
    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("icon")
    private String icon;

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("modified_date")
    private String modifiedDate;
    // Getters and Setters
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
