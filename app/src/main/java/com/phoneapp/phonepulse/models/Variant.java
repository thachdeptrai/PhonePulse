package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Variant {
    @SerializedName("_id")
    private String _id;
    @SerializedName("product_id")
    private String productId;
    @SerializedName("color_id")
    private Color colorId;
    @SerializedName("size_id")
    private Size sizeId;
    @SerializedName("price")
    private double price;
    @SerializedName("quantity")
    private int quantity;

    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public Color getColorId() { return colorId; }
    public void setColorId(Color color) { this.colorId = color; }

    public Size getSizeId() { return sizeId; }
    public void setSizeId(Size size) { this.sizeId = size; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
