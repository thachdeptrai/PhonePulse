package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Size {
    @SerializedName("_id")
    private String id; // Maps to MongoDB's _id for the size record

    @SerializedName("size_name")
    private String sizeName; // The general size name, e.g., "M", "L", or "Standard"

    @SerializedName("storage")
    private String storage; // Storage capacity, e.g., "64GB", "128GB", "256GB"

    @SerializedName("created_date")
    private String createdDate; // Date the size record was created

    @SerializedName("modified_date")
    private String modifiedDate; // Date the size record was last modified

    // --- Constructors (Optional) ---
    public Size() {
        // Default constructor required for Gson
    }

    public Size(String id, String sizeName, String storage, String createdDate, String modifiedDate) {
        this.id = id;
        this.sizeName = sizeName;
        this.storage = storage;
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

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
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