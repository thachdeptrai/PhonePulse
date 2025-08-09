package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ProductImage implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("product_id")
    private String productId;

    @SerializedName("image_url")
    private String imageUrl;

    public ProductImage() {
    }

    public ProductImage(String id, String productId, String imageUrl) {
        this.id = id;
        this.productId = productId;
        this.imageUrl = imageUrl;
    }

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

    @Override
    public String toString() {
        return "ProductImage{" +
                "id='" + id + '\'' +
                ", productId='" + productId + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
