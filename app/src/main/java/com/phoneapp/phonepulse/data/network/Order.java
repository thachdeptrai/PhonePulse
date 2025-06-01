package com.phoneapp.phonepulse.data.network;

public class Order {
    private int id;
    private int user_id;
    private int total_price;
    private String payment_method;
    private String address;
    private String status;
    private String created_at; // có thể dùng Date

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUser_id() { return user_id; }
    public void setUser_id(int user_id) { this.user_id = user_id; }

    public int getTotal_price() { return total_price; }
    public void setTotal_price(int total_price) { this.total_price = total_price; }

    public String getPayment_method() { return payment_method; }
    public void setPayment_method(String payment_method) { this.payment_method = payment_method; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }
}

