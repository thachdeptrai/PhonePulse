package com.phoneapp.phonepulse.Response;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    @SerializedName("paymentUrl")
    private String paymentUrl;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message, T data, String paymentUrl) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.paymentUrl = paymentUrl;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}