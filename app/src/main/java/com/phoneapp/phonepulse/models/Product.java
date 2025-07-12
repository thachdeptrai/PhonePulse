package com.phoneapp.phonepulse.models;

import java.util.List;

public class Product {
    private int id;
    private String name;
    private int brand_id;
    private int price;
    private int discount;
    private String image_url;
    private String specs_json;
    private int stock;
    private String created_at; // có thể dùng Date

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getBrand_id() { return brand_id; }
    public void setBrand_id(int brand_id) { this.brand_id = brand_id; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }

    public String getImage_url() { return image_url; }
    public void setImage_url(String image_url) { this.image_url = image_url; }

    public String getSpecs_json() { return specs_json; }
    public void setSpecs_json(String specs_json) { this.specs_json = specs_json; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

