package com.phoneapp.phonepulse.utils;

import com.phoneapp.phonepulse.models.Variant;

public class CartUtils {

    /**
     * Kiểm tra xem biến thể sản phẩm còn hàng không.
     *
     * @param variant Biến thể cần kiểm tra
     * @return true nếu còn hàng, false nếu hết hàng
     */
    public static boolean isVariantInStock(Variant variant) {
        return variant != null && variant.getQuantity() > 0;
    }

    /**
     * Kiểm tra xem có thể thêm vào giỏ hàng với số lượng mong muốn không.
     *
     * @param variant Biến thể cần kiểm tra
     * @param desiredQuantity Số lượng người dùng muốn thêm vào giỏ
     * @return true nếu số lượng còn đủ, false nếu không đủ hoặc hết hàng
     */
    public static boolean canAddToCart(Variant variant, int desiredQuantity) {
        return variant != null && variant.getQuantity() >= desiredQuantity;
    }
}
