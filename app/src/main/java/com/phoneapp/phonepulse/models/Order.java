package com.phoneapp.phonepulse.models;

import java.util.Date;
import java.util.List;

public class Order {
    private String _id;
    private String userId;
    private List<OrderItem> items;
    private double discount_amount;
    private double final_price;
    private String status;
    private String shipping_address;
    private String payment_method;
    private String payment_status;
    private String shipping_status;
    private String note;
    private Date shipping_date;
    private Date delivered_date;
    private Date created_date;
    private Date modified_date;

    // Cần tạo class OrderItem tương ứng với cấu trúc trong backend
    public static class OrderItem {
        private String productId;
        private String variantId;
        private int quantity;

        // Getters and Setters cho OrderItem
        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getVariantId() {
            return variantId;
        }

        public void setVariantId(String variantId) {
            this.variantId = variantId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    // Getters and Setters cho Order
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getDiscount_amount() {
        return discount_amount;
    }

    public void setDiscount_amount(double discount_amount) {
        this.discount_amount = discount_amount;
    }

    public double getFinal_price() {
        return final_price;
    }

    public void setFinal_price(double final_price) {
        this.final_price = final_price;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getPayment_status() {
        return payment_status;
    }

    public void setPayment_status(String payment_status) {
        this.payment_status = payment_status;
    }

    public String getShipping_status() {
        return shipping_status;
    }

    public void setShipping_status(String shipping_status) {
        this.shipping_status = shipping_status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getShipping_date() {
        return shipping_date;
    }

    public void setShipping_date(Date shipping_date) {
        this.shipping_date = shipping_date;
    }

    public Date getDelivered_date() {
        return delivered_date;
    }

    public void setDelivered_date(Date delivered_date) {
        this.delivered_date = delivered_date;
    }

    public Date getCreated_date() {
        return created_date;
    }

    public void setCreated_date(Date created_date) {
        this.created_date = created_date;
    }

    public Date getModified_date() {
        return modified_date;
    }

    public void setModified_date(Date modified_date) {
        this.modified_date = modified_date;
    }
}