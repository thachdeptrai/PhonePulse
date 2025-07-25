package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class Order {
    @SerializedName("_id")
    private String id;

    @SerializedName("userId")
    private String userId;

    @SerializedName("items")
    private List<OrderItem> items;

    @SerializedName("discount_amount")
    private int discountAmount;

    @SerializedName("final_price")
    private int finalPrice;

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

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public int getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(int discountAmount) { this.discountAmount = discountAmount; }

    public int getFinalPrice() { return finalPrice; }
    public void setFinalPrice(int finalPrice) { this.finalPrice = finalPrice; }

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

