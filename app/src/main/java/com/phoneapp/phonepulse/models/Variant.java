package com.phoneapp.phonepulse.models;

public class Variant {
    private String _id;
    private String productId;
    private String color;
    private String size;
    private double price;
    private int quantity;

    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
