package com.phoneapp.phonepulse.request;

import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;

public class CartItem {

    private String productId;
    private String variantId;
    private int quantity;
    private Product product;   // Phần populate từ Backend (nếu có)
    private Variant variant;   // Thông tin chi tiết biến thể (nếu có)

    public CartItem() {
    }

    public CartItem(String productId, String variantId, int quantity) {
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getVariantId() {
        return variantId;
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }
}
