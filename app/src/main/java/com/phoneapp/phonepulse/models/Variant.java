package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Variant {
    @SerializedName("_id")
    private String id;  // Đổi tên biến thành id cho thống nhất

    @SerializedName("product_id")
    private String productId;

    @SerializedName("color_id") // Đây là một đối tượng Color, không phải String
    private Color colorId;

    @SerializedName("size_id") // Đây là một đối tượng Size, không phải String
    private Size sizeId;

    @SerializedName("price")
    private double price;

    @SerializedName("quantity")
    private int quantity;

    public Variant() {
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

    public Color getColorId() {
        return colorId;
    }
    public void setColorId(Color colorId) {
        this.colorId = colorId;
    }

    public Size getSizeId() {
        return sizeId;
    }
    public void setSizeId(Size sizeId) {
        this.sizeId = sizeId;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}