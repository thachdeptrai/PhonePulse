package com.phoneapp.phonepulse.request;

import com.phoneapp.phonepulse.data.network.Product;

public class CartItem {
    private String _id;
    private Product product;
    private int quantity;

    public CartItem() {}

    public CartItem(String _id, Product product, int quantity) {
        this._id = _id;
        this.product = product;
        this.quantity = quantity;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

