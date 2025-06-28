package com.phoneapp.phonepulse.models;

public class Favourite {
    private String _id;
    private String userId;
    private String productId;

    public String getId() { return _id; }
    public void setId(String _id) { this._id = _id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}

