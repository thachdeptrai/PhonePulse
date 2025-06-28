package com.phoneapp.phonepulse.request;

public class FavouriteRequest {
    private String productId;

    public FavouriteRequest() {}

    public FavouriteRequest(String productId) {
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
}

