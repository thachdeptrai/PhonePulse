package com.phoneapp.phonepulse.request;

import com.google.gson.annotations.SerializedName;

public class CartRequest {

    // Đây là lớp bao bọc chính, không có trường riêng của nó.
    // Nó chỉ dùng để chứa các lớp request con.

    /**
     * Request body cho API Thêm sản phẩm vào giỏ hàng (POST /api/cart)
     */
    public static class AddToCart {
        @SerializedName("productId")
        private String productId;
        @SerializedName("variantId")
        private String variantId;
        @SerializedName("quantity")
        private int quantity;

        public AddToCart(String productId, String variantId, int quantity) {
            this.productId = productId;
            this.variantId = variantId;
            this.quantity = quantity;
        }

        // Getters (Optional, but good practice)
        public String getProductId() { return productId; }
        public String getVariantId() { return variantId; }
        public int getQuantity() { return quantity; }
    }

    /**
     * Request body cho API Cập nhật số lượng sản phẩm trong giỏ hàng (PUT /api/cart)
     */
    public static class UpdateCartItem {
        @SerializedName("productId")
        private String productId;
        @SerializedName("variantId")
        private String variantId;
        @SerializedName("quantity") // New quantity
        private int quantity;

        public UpdateCartItem(String productId, String variantId, int quantity) {
            this.productId = productId;
            this.variantId = variantId;
            this.quantity = quantity;
        }

        // Getters
        public String getProductId() { return productId; }
        public String getVariantId() { return variantId; }
        public int getQuantity() { return quantity; }
    }

    /**
     * Request body cho API Xóa sản phẩm khỏi giỏ hàng (DELETE /api/cart)
     */
    public static class RemoveCartItem {
        @SerializedName("productId")
        private String productId;
        @SerializedName("variantId")
        private String variantId;

        public RemoveCartItem(String productId, String variantId) {
            this.productId = productId;
            this.variantId = variantId;
        }

        // Getters
        public String getProductId() { return productId; }
        public String getVariantId() { return variantId; }
    }
}