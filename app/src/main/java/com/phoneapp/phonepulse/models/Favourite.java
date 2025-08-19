package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Favourite implements Serializable {

    @SerializedName("_id")
    private String id; // ID của Favourite document

    @SerializedName("userId")
    private String userId;

    // QUAN TRỌNG: productId giờ là một đối tượng Product
    @SerializedName("productId")
    private Product productDetails; // Đổi tên để rõ ràng hơn, hoặc giữ là 'product'

    @SerializedName("addedAt")
    private String addedAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Trả về đối tượng Product (chứa ID của Product và các thông tin cơ bản khác)
    public Product getProductDetails() {
        return productDetails;
    }

    public void setProductDetails(Product productDetails) {
        this.productDetails = productDetails;
    }

    public String getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(String addedAt) {
        this.addedAt = addedAt;
    }
}
