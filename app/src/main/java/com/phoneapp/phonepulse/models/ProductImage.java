package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class ProductImage {
    @SerializedName("_id")
    private String id; // Maps to MongoDB's _id for the image record

    @SerializedName("product_id")
    private String productId; // The ID of the product this image belongs to.
    // If you want the full Product object here, the backend needs to populate it.

    @SerializedName("image_url")
    private String imageUrl; // The URL of the image, where the image is actually hosted

    // --- Constructors (Optional, but good practice) ---
    public ProductImage() {
        // Default constructor required for Gson
    }

    public ProductImage(String id, String productId, String imageUrl) {
        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl;
    }

    // --- Getters and Setters ---
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}