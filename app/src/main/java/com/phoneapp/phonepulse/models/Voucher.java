package com.phoneapp.phonepulse.models;

public class Voucher {
    private String _id;
    private String code;
    private String discount_type;   // "percent" hoặc "amount"
    private double discount_value;  // giá trị giảm
    private double min_order_value; // giá trị tối thiểu để áp dụng
    private double max_discount;    // số tiền giảm tối đa
    private String start_date;
    private String end_date;

    // Getter & Setter
    public String getId() {
        return _id;
    }

    public void setId(String _id) {
        this._id = _id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDiscountType() {
        return discount_type;
    }

    public void setDiscountType(String discount_type) {
        this.discount_type = discount_type;
    }

    public double getDiscountValue() {
        return discount_value;
    }

    public void setDiscountValue(double discount_value) {
        this.discount_value = discount_value;
    }

    public double getMinOrderValue() {
        return min_order_value;
    }

    public void setMinOrderValue(double min_order_value) {
        this.min_order_value = min_order_value;
    }

    public double getMaxDiscount() {
        return max_discount;
    }

    public void setMaxDiscount(double max_discount) {
        this.max_discount = max_discount;
    }

    public String getStartDate() {
        return start_date;
    }

    public void setStartDate(String start_date) {
        this.start_date = start_date;
    }

    public String getEndDate() {
        return end_date;
    }

    public void setEndDate(String end_date) {
        this.end_date = end_date;
    }
}
