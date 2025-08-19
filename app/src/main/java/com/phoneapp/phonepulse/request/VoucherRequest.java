package com.phoneapp.phonepulse.request;

public class VoucherRequest {
    private String code;
    private double orderTotal; // tổng tiền đơn hàng

    public VoucherRequest(String code, double orderTotal) {
        this.code = code;
        this.orderTotal = orderTotal;
    }

    public String getCode() {
        return code;
    }

    public double getOrderTotal() {
        return orderTotal;
    }
}

