package com.phoneapp.phonepulse.models;

import com.phoneapp.phonepulse.request.CartItem;

import java.util.List;

public class Cart {

    private String userId;
    private List<CartItem> items;
    private String updatedAt;

    public Cart() {
    }

    public Cart(String userId, List<CartItem> items, String updatedAt) {
        this.userId = userId;
        this.items = items;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
