package com.phoneapp.phonepulse.request;

public class OrderItem {
    private String name;
    private String imageUrl;
    private int price;
    private int quantity;

    public OrderItem(String name, String imageUrl, int price, int quantity) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }
}
