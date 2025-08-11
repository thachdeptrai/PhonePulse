package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;
import com.phoneapp.phonepulse.request.OrderItem;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class Order implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("items")
    private List<OrderItem> items;  // ✅ Dùng class từ package request

    @SerializedName("discount_amount")
    private double discountAmount;

    @SerializedName("final_price")
    private double finalPrice;

    @SerializedName("status")
    private String status;

    @SerializedName("shipping_address")
    private String shippingAddress;

    @SerializedName("payment_method")
    private String paymentMethod;

    @SerializedName("payment_status")
    private String paymentStatus;

    @SerializedName("shipping_status")
    private String shippingStatus;

    @SerializedName("note")
    private String note;

    @SerializedName("shipping_date")
    private Date shippingDate;

    @SerializedName("delivered_date")
    private Date deliveredDate;

    @SerializedName("created_date")
    private Date createdDate;

    @SerializedName("modified_date")
    private Date modifiedDate;

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(double discountAmount) { this.discountAmount = discountAmount; }

    public double getFinalPrice() { return finalPrice; }
    public void setFinalPrice(double finalPrice) { this.finalPrice = finalPrice; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getShippingStatus() { return shippingStatus; }
    public void setShippingStatus(String shippingStatus) { this.shippingStatus = shippingStatus; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public Date getShippingDate() { return shippingDate; }
    public void setShippingDate(Date shippingDate) { this.shippingDate = shippingDate; }

    public Date getDeliveredDate() { return deliveredDate; }
    public void setDeliveredDate(Date deliveredDate) { this.deliveredDate = deliveredDate; }

    public Date getCreatedDate() { return createdDate; }
    public void setCreatedDate(Date createdDate) { this.createdDate = createdDate; }

    public Date getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(Date modifiedDate) { this.modifiedDate = modifiedDate; }
}
