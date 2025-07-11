// com.phoneapp.phonepulse.request.CartItem.java
package com.phoneapp.phonepulse.request;

import com.google.gson.annotations.SerializedName;
import com.phoneapp.phonepulse.models.Product; // Assuming this model exists
import com.phoneapp.phonepulse.models.Variant; // Assuming this model exists, adjust package if different

public class CartItem {
    @SerializedName("_id")
    private String id; // ID of the cart item entry itself

    @SerializedName("product") // Assuming your backend embeds the full product object
    private Product product;

    @SerializedName("variant") // Assuming your backend embeds the full variant object
    private Variant variant; // Full Variant object

    @SerializedName("quantity")
    private int quantity;

    // --- Getters and Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // --- NEW: Methods to get Product ID and Variant ID from the embedded objects ---
    // Make sure your Product and Variant models have a getId() method that returns their _id
    public String getProductId() {
        return (product != null) ? product.getId() : null;
    }

    public String getVariantId() {
        return (variant != null) ? variant.getId() : null;
    }

    // Phương thức tiện ích để tính tổng giá của mục (từ CartItem ban đầu)
    // Phương thức này có thể bị ảnh hưởng nếu giá được fetch riêng.
    // Bạn nên tính tổng giá trong CartDisplayItem sau khi có đủ thông tin.
    // Nếu bạn muốn giữ lại logic này ở đây, hãy đảm bảo Variant object có price.
    public double getItemTotalPrice() {
        if (variant != null) {
            return variant.getPrice() * quantity;
        }
        // Fallback to product price if variant is null but product exists and has a price.
        // This is less accurate for variants but prevents 0.0 if variant is missing.
        else if (product != null) {
            return product.getPrice() * quantity;
        }
        return 0.0; // Trả về 0 nếu không có thông tin giá
    }
}