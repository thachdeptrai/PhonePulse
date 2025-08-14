package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Variant {
    @SerializedName("_id")
    private String id; // ID của biến thể, tương ứng với _id trong MongoDB

    @SerializedName("product_id")
    private String productId; // ID của sản phẩm mà biến thể này thuộc về.
    // Nếu bạn populate Product ở backend, bạn có thể thay bằng `private Product product;`

    @SerializedName("color_id")
    private Color color; // Đối tượng Color. Backend nên populate để trả về toàn bộ thông tin màu sắc

    @SerializedName("size_id")
    private Size size; // Đối tượng Size. Backend nên populate để trả về toàn bộ thông tin kích thước/dung lượng

    @SerializedName("quantity")
    private int quantity; // Số lượng tồn kho của biến thể

    @SerializedName("price")
    private double price; // Giá của biến thể

    @SerializedName("created_date")
    private String createdDate; // Ngày tạo biến thể

    @SerializedName("modified_date")
    private String modifiedDate; // Ngày chỉnh sửa gần nhất

    // --- Constructors (tùy chọn) ---
    public Variant(int newQuantity) {
        // Constructor mặc định cần thiết cho Gson
    }

    // Constructor với tất cả các trường (tùy chọn)
    public Variant(String id, String productId, Color color, Size size, int quantity, double price, String createdDate, String modifiedDate) {
        this.id = id;
        this.productId = productId;
        this.color = color;
        this.size = size;
        this.quantity = quantity;
        this.price = price;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public Size getSize() {
        return size;
    }

    public void setSize(Size size) {
        this.size = size;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
