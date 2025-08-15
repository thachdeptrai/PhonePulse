package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Variant {

    @SerializedName("_id")
    private String id;

    @SerializedName("product_id")
    private String productId;

    @SerializedName("color_id")
    private Color color;

    @SerializedName("size_id")
    private Size size;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("price")
    private double price;

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("modified_date")
    private String modifiedDate;

    // --- Constructors ---
    public Variant() {
        // Constructor mặc định cho Gson
    }

    public Variant(int quantity) {
        this.quantity = quantity;
    }

    public Variant(String id, String productId, Color color, Size size,
                   int quantity, double price, String createdDate, String modifiedDate) {
        this.id = id;
        this.productId = productId;
        this.color = color;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
    }

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Color getColor() { return color; }
    public void setColor(Color color) { this.color = color; }

    public Size getSize() { return size; }
    public void setSize(Size size) { this.size = size; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(String modifiedDate) { this.modifiedDate = modifiedDate; }

    // --- Stock Update Methods ---
    /**
     * Giảm số lượng tồn kho.
     *
     * @param amount Số lượng cần giảm (phải > 0).
     * @return true nếu giảm thành công, false nếu không hợp lệ hoặc không đủ hàng.
     */
    public boolean reduceQuantity(int amount) {
        if (amount <= 0) {
            return false; // Số lượng giảm không hợp lệ
        }
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false; // Không đủ hàng
    }

    /**
     * Kiểm tra còn hàng hay không.
     * @return true nếu quantity > 0, false nếu hết hàng.
     */
    public boolean isInStock() {
        return quantity > 0;
    }
}
