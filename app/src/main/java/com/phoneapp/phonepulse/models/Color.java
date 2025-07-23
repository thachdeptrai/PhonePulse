package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Color {
    @SerializedName("_id")
    private String id; // Maps to MongoDB's _id

    @SerializedName("color_name")
    private String colorName; // The name of the color, e.g., "Red", "Blue"

    @SerializedName("created_date")
    private String createdDate; // Date the color record was created

    @SerializedName("modified_date")
    private String modifiedDate; // Date the color record was last modified

    // --- Constructors (Optional) ---
    public Color() {
        // Default constructor required for Gson
    }

    public Color(String id, String colorName, String createdDate, String modifiedDate) {
        this.id = id;
        this.colorName = colorName;
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

    public String getColorName() {
        return colorName;
    }

    public void setColorName(String colorName) {
        this.colorName = colorName;
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