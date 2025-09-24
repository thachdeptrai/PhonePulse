package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

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

    @SerializedName("sold_count")
    private int soldCount;   // ✅ Thêm số lượng đã bán

    @SerializedName("price")
    private double price;

    @SerializedName("original_price")
    private Double originalPrice; // ✅ Giá gốc (có thể null)

    @SerializedName("discount_percent")
    private int discountPercent;  // ✅ % giảm giá

    @SerializedName("images")
    private List<String> images;  // ✅ Ảnh riêng cho biến thể

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("modified_date")
    private String modifiedDate;

    // --- Constructors ---
    public Variant() {}

    public Variant(String id, String productId, Color color, Size size,
                   int quantity, int soldCount, double price, Double originalPrice,
                   int discountPercent, List<String> images,
                   String createdDate, String modifiedDate) {
        this.id = id;
        this.productId = productId;
        this.color = color;
        this.size = size;
        this.quantity = quantity;
        this.soldCount = soldCount;
        this.price = price;
        this.originalPrice = originalPrice;
        this.discountPercent = discountPercent;
        this.images = images;
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

    public int getSoldCount() { return soldCount; }
    public void setSoldCount(int soldCount) { this.soldCount = soldCount; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Double getOriginalPrice() { return originalPrice; }
    public void setOriginalPrice(Double originalPrice) { this.originalPrice = originalPrice; }

    public int getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(int discountPercent) { this.discountPercent = discountPercent; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(String modifiedDate) { this.modifiedDate = modifiedDate; }

    // --- Stock Update Methods ---
    public boolean reduceQuantity(int amount) {
        if (amount <= 0) return false;
        if (quantity >= amount) {
            quantity -= amount;
            return true;
        }
        return false;
    }

    public boolean isInStock() {
        return quantity > 0;
    }
}
