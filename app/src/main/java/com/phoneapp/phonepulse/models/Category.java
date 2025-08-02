package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class Category implements Serializable {
    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("icon")
    private String icon;

    @SerializedName("created_date")
    private Date createdDate;

    @SerializedName("modified_date")
    private Date modifiedDate;

    public Category(String id, String name, String icon, Date createdDate, Date modifiedDate) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    // Setters (Nếu cần)
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}