package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class ProductImage {
    @SerializedName("_id")
    private String _id;
    @SerializedName("product_id")
    private String productId;
    @SerializedName("image_url")
    private String imageUrl;

    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}

