package com.phoneapp.phonepulse.request;

import com.google.gson.annotations.SerializedName;

public class MomoData {
    @SerializedName("momoPayUrl")
    private String momoPayUrl;

    @SerializedName("qrCodeUrl")
    private String qrCodeUrl;

    public String getMomoPayUrl() {
        return momoPayUrl;
    }

    public void setMomoPayUrl(String momoPayUrl) {
        this.momoPayUrl = momoPayUrl;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void setQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }
}
