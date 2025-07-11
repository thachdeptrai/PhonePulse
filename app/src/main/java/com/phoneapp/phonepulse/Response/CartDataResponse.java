package com.phoneapp.phonepulse.Response;

import com.google.gson.annotations.SerializedName;
import com.phoneapp.phonepulse.request.CartItem;

import java.util.List;

public class CartDataResponse {

    @SerializedName("_id")
    private String id;

    @SerializedName("user") // Tên trường này phải khớp với trường user ID trong backend Cart model
    private String userId;

    @SerializedName("items")
    private List<CartItem> items; // Đây là danh sách các sản phẩm trong giỏ hàng

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("__v")
    private int v;

    // Constructors (nếu cần)
    public CartDataResponse() {}

    // Getters
    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public int getV() {
        return v;
    }

    // Setters (nếu cần)
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setV(int v) {
        this.v = v;
    }
}
