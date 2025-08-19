package com.phoneapp.phonepulse.utils; // Tạo một package mới nếu cần

public class OrderCanceledEvent {
    private final String orderId;
    private final String cancelReason;

    public OrderCanceledEvent(String orderId, String cancelReason) {
        this.orderId = orderId;
        this.cancelReason = cancelReason;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getCancelReason() {
        return cancelReason;
    }
}