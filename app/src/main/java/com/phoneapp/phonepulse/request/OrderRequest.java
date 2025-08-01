package com.phoneapp.phonepulse.request;

import com.phoneapp.phonepulse.models.Order;

import java.util.List;

public class OrderRequest {
    private String shippingAddress;
    private String phone;
    private String voucherCode;
    private String paymentMethod;
    private String note;
    private double finalPrice;
    private List<Order.OrderItem> cartItems; // Thêm trường danh sách sản phẩm

    public OrderRequest() {
    }

    public OrderRequest(String shippingAddress, String phone, String voucherCode, String paymentMethod, String note, double finalPrice, List<Order.OrderItem> cartItems) {
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.voucherCode = voucherCode;
        this.paymentMethod = paymentMethod;
        this.note = note;
        this.finalPrice = finalPrice;
        this.cartItems = cartItems;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public List<Order.OrderItem> getCartItems() {
        return cartItems;
    }

    public void setCartItems(List<Order.OrderItem> cartItems) {
        this.cartItems = cartItems;
    }
}