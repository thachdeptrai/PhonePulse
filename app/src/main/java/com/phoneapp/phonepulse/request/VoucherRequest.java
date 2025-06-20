package com.phoneapp.phonepulse.request;

public class VoucherRequest {
    private String code;
    private double total;

    public VoucherRequest() {}

    public VoucherRequest(String code, double total) {
        this.code = code;
        this.total = total;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}

