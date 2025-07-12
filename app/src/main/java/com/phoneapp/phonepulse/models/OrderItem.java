package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

// Lớp con OrderItem
public class OrderItem {
    @SerializedName("productId")
    private String productId;

    @SerializedName("variantId")
    private String variantId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("_id")
    private String id;

    // Getters and Setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getVariantId() { return variantId; }
    public void setVariantId(String variantId) { this.variantId = variantId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}
