package com.phoneapp.phonepulse.request;

import com.phoneapp.phonepulse.models.OrderItem;

import java.util.List;

public class OrderRequest {
    private String shippingAddress;
    private String phone;
    private String voucherCode; // Optional, sẽ tính discount_amount
    private String paymentMethod;
    private List<OrderItem> items;
    private int discountAmount;
    private int finalPrice;
    private String note;

    public OrderRequest() {}

    public OrderRequest(String shippingAddress, String phone, String voucherCode, String paymentMethod, List<OrderItem> items, int discountAmount, int finalPrice, String note) {
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.voucherCode = voucherCode;
        this.paymentMethod = paymentMethod;
        this.items = items;
        this.discountAmount = discountAmount;
        this.finalPrice = finalPrice;
        this.note = note;
    }

    // Getters and Setters
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getVoucherCode() { return voucherCode; }
    public void setVoucherCode(String voucherCode) { this.voucherCode = voucherCode; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public int getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(int discountAmount) { this.discountAmount = discountAmount; }

    public int getFinalPrice() { return finalPrice; }
    public void setFinalPrice(int finalPrice) { this.finalPrice = finalPrice; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}

// Sử dụng OrderItem từ Order.java