package com.phoneapp.phonepulse.utils;

import android.util.Log;

import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.request.OrderItem;

import java.util.List;

/**
 * Quản lý tồn kho biến thể trong bối cảnh giỏ hàng/đơn hàng.
 */
public final class CartUtils {

    private static final String TAG = "CartUtils";

    private CartUtils() {}

    /** Biến thể còn hàng nếu khác null và quantity > 0 */
    public static boolean isVariantInStock(final Variant variant) {
        return variant != null && variant.getQuantity() > 0;
    }

    /** Có thể thêm vào giỏ nếu desiredQuantity > 0 và tồn kho đủ */
    public static boolean canAddToCart(final Variant variant, final int desiredQuantity) {
        return desiredQuantity > 0 && isVariantInStock(variant) && variant.getQuantity() >= desiredQuantity;
    }

    /**
     * Giảm tồn kho cho 1 biến thể.
     * Giữ nguyên chữ ký hàm cũ để không phải sửa code chỗ gọi.
     */
    public static boolean reduceStock(final Variant variant, final int quantityToReduce) {
        if (variant == null) {
            Log.w(TAG, "reduceStock: variant == null. Bỏ qua.");
            return false;
        }
        if (quantityToReduce <= 0) {
            Log.w(TAG, "reduceStock: Số lượng cần giảm không hợp lệ: " + quantityToReduce);
            return false;
        }

        // Dùng API an toàn trong Variant để tránh âm kho
        boolean ok = variant.reduceQuantity(quantityToReduce);
        String vid = variant.getId() == null ? "(null)" : variant.getId();

        if (ok) {
            Log.d(TAG, "reduceStock: -"+quantityToReduce+" -> tồn kho mới: "
                    + variant.getQuantity() + " (variantId=" + vid + ")");
            return true;
        } else {
            Log.w(TAG, "reduceStock: Không đủ hàng. Hiện có: "
                    + variant.getQuantity() + ", cần: " + quantityToReduce
                    + " (variantId=" + vid + ")");
            return false;
        }
    }

    /**
     * Giảm tồn kho theo variantId từ danh sách allVariants.
     * Dùng vòng for thường để tránh lỗi Stream trên Android cũ.
     */
    public static boolean reduceStockById(final List<Variant> allVariants,
                                          final String variantId,
                                          final int quantityToReduce) {
        if (allVariants == null || allVariants.isEmpty()) {
            Log.w(TAG, "reduceStockById: Danh sách biến thể rỗng hoặc null.");
            return false;
        }
        if (variantId == null || variantId.trim().isEmpty()) {
            Log.w(TAG, "reduceStockById: variantId null hoặc rỗng.");
            return false;
        }
        if (quantityToReduce <= 0) {
            Log.w(TAG, "reduceStockById: Số lượng cần giảm không hợp lệ: " + quantityToReduce);
            return false;
        }

        for (Variant v : allVariants) {
            if (v != null && variantId.equals(v.getId())) {
                return reduceStock(v, quantityToReduce);
            }
        }
        Log.w(TAG, "reduceStockById: Không tìm thấy biến thể với ID = " + variantId);
        return false;
    }

    /**
     * Giảm tồn kho hàng loạt sau khi đơn hàng tạo thành công.
     * Nhận trực tiếp danh sách OrderItem (đã có variantId & quantity).
     */
    public static void reduceStockAfterOrder(final List<Variant> allVariants,
                                             final List<OrderItem> orderedItems) {
        if (allVariants == null || allVariants.isEmpty()) {
            Log.w(TAG, "reduceStockAfterOrder: allVariants rỗng hoặc null.");
            return;
        }
        if (orderedItems == null || orderedItems.isEmpty()) {
            Log.w(TAG, "reduceStockAfterOrder: orderedItems rỗng hoặc null.");
            return;
        }

        for (OrderItem item : orderedItems) {
            if (item == null) continue;

            final String variantId = item.getVariantId();
            final int qty = item.getQuantity();

            if (variantId == null || variantId.trim().isEmpty()) {
                Log.w(TAG, "reduceStockAfterOrder: variantId trống cho sản phẩm: " + item.getName());
                continue;
            }
            if (qty <= 0) {
                Log.w(TAG, "reduceStockAfterOrder: quantity <= 0 cho sản phẩm: " + item.getName());
                continue;
            }

            boolean found = false;
            for (Variant v : allVariants) {
                if (v != null && variantId.equals(v.getId())) {
                    found = true;
                    boolean ok = reduceStock(v, qty);
                    if (!ok) {
                        Log.w(TAG, "reduceStockAfterOrder: Không đủ tồn kho cho " + item.getName()
                                + " (variantId=" + variantId + ", yêu cầu=" + qty + ")");
                    }
                    break;
                }
            }
            if (!found) {
                Log.w(TAG, "reduceStockAfterOrder: Không tìm thấy variantId=" + variantId
                        + " cho sản phẩm: " + item.getName());
            }
        }
    }
}
