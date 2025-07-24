package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;
import com.phoneapp.phonepulse.request.CartItem;

import java.util.List; // Import cho List
import java.util.Date; // Import cho Date

public class Cart {
    @SerializedName("_id") // Tên trường trong JSON từ backend
    private String id; // ID của giỏ hàng (ObjectId trong MongoDB thường là String trong Java)

    @SerializedName("userId") // Tên trường trong JSON từ backend
    private String userId; // ID của người dùng (ObjectId)

    @SerializedName("items") // Tên trường trong JSON từ backend
    private List<CartItem> items; // Danh sách các CartItem

    @SerializedName("updatedAt") // Tên trường trong JSON từ backend
    private Date updatedAt; // Thời gian cập nhật

    // Constructor mặc định (cần cho Gson)
    public Cart() {
    }

    // Constructor đầy đủ (tùy chọn, nhưng hữu ích)
    public Cart(String id, String userId, List<CartItem> items, Date updatedAt) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.updatedAt = updatedAt;
    }

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

    public Date getUpdatedAt() {
        return updatedAt;
    }

    // Setters (nếu bạn cần thay đổi giá trị sau khi tạo đối tượng)
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Cart{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", items=" + items +
                ", updatedAt=" + updatedAt +
                '}';
    }
}