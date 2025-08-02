package com.phoneapp.phonepulse.request;

import java.util.List;

public class OrderRequest {
    private List<OrderItem> items;
    private int discount_amount;
    private int final_price;
    private String shipping_address;
    private String payment_method;
    private String note;

    public OrderRequest() {}

    public OrderRequest(List<OrderItem> items, int discount_amount, int final_price,
                        String shipping_address, String payment_method, String note) {
        this.items = items;
        this.discount_amount = discount_amount;
        this.final_price = final_price;
        this.shipping_address = shipping_address;
        this.payment_method = payment_method;
        this.note = note;
    }

    // Getters and Setters
    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public int getDiscount_amount() {
        return discount_amount;
    }

    public void setDiscount_amount(int discount_amount) {
        this.discount_amount = discount_amount;
    }

    public int getFinal_price() {
        return final_price;
    }

    public void setFinal_price(int final_price) {
        this.final_price = final_price;
    }

    public String getShipping_address() {
        return shipping_address;
    }

    public void setShipping_address(String shipping_address) {
        this.shipping_address = shipping_address;
    }

    public String getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(String payment_method) {
        this.payment_method = payment_method;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
