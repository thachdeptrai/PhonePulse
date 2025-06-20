package com.phoneapp.phonepulse.request;

public class OrderRequest {
    private String shippingAddress;
    private String phone;
    private String voucherCode; // Optional
    private String paymentMethod;

    public OrderRequest() {}

    public OrderRequest(String shippingAddress, String phone, String voucherCode, String paymentMethod) {
        this.shippingAddress = shippingAddress;
        this.phone = phone;
        this.voucherCode = voucherCode;
        this.paymentMethod = paymentMethod;
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
}

