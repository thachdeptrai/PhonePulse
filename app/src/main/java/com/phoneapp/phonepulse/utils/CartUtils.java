package com.phoneapp.phonepulse.utils;

import android.util.Log;

import com.phoneapp.phonepulse.models.Variant;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

/**
 * Lớp tiện ích để quản lý các biến thể sản phẩm trong bối cảnh giỏ hàng.
 * Lớp này cung cấp các phương thức tĩnh để kiểm tra số lượng tồn kho
 * và cập nhật số lượng tồn kho cho các biến thể sản phẩm.
 */
public final class CartUtils {

    private static final String TAG = "CartUtils";

    // Constructor private để ngăn việc khởi tạo đối tượng của lớp tiện ích này.
    private CartUtils() {
    }

    /**
     * Kiểm tra xem một biến thể sản phẩm còn hàng hay không.
     *
     * @param variant Biến thể cần kiểm tra.
     * @return {@code true} nếu biến thể không null và số lượng lớn hơn 0,
     * {@code false} nếu không.
     */
    public static boolean isVariantInStock(final Variant variant) {
        return variant != null && variant.getQuantity() > 0;
    }

    /**
     * Kiểm tra xem có thể thêm một số lượng mong muốn của biến thể vào giỏ hàng hay không.
     *
     * @param variant         Biến thể cần kiểm tra.
     * @param desiredQuantity Số lượng người dùng muốn thêm. Phải lớn hơn 0.
     * @return {@code true} nếu biến thể không null, số lượng mong muốn là số dương
     * và số lượng tồn kho đủ; {@code false} nếu không.
     */
    public static boolean canAddToCart(final Variant variant, final int desiredQuantity) {
        // --- ĐÃ THAY ĐỔI LỖI Ở ĐÂY ---
        // Thêm điều kiện để số lượng mong muốn phải nhỏ hơn hoặc bằng số lượng tồn kho.
        // Ví dụ: tồn kho 10, chỉ cho phép thêm tối đa 10 sản phẩm vào giỏ hàng.
        return desiredQuantity > 0 && isVariantInStock(variant) && variant.getQuantity() >= desiredQuantity;
    }


    /**
     * Giảm số lượng tồn kho của một biến thể cụ thể.
     *
     * @param variant          Biến thể cần giảm số lượng tồn kho.
     * @param quantityToReduce Số lượng cần giảm. Phải là số dương.
     * @return {@code true} nếu giảm thành công, {@code false} nếu không
     * (ví dụ: biến thể null, số lượng không hợp lệ, hoặc không đủ hàng).
     */
    public static boolean reduceStock(final Variant variant, final int quantityToReduce) {
        if (variant == null) {
            Log.w(TAG, "reduceStock: Biến thể là null. Không thể giảm tồn kho.");
            return false;
        }

        if (quantityToReduce <= 0) {
            Log.w(TAG, "reduceStock: Số lượng cần giảm không hợp lệ: " + quantityToReduce);
            return false;
        }

        final int currentStock = variant.getQuantity();
        if (currentStock >= quantityToReduce) {
            final int newStock = currentStock - quantityToReduce;
            variant.setQuantity(newStock);

            Log.d(TAG, String.format("reduceStock: Đã giảm %d. Tồn kho mới: %d (ID Biến thể: %s)",
                    quantityToReduce, newStock, variant.getId()));
            return true;
        }

        Log.w(TAG, String.format("reduceStock: Không đủ hàng. Hiện có: %d, Cần: %d (ID Biến thể: %s)",
                currentStock, quantityToReduce, variant.getId()));
        return false;
    }

    /**
     * Giảm số lượng tồn kho của một biến thể bằng ID từ một danh sách.
     *
     * @param allVariants      Danh sách tất cả các biến thể.
     * @param variantId        ID của biến thể cần tìm và cập nhật.
     * @param quantityToReduce Số lượng cần giảm. Phải là số dương.
     * @return {@code true} nếu biến thể được tìm thấy và giảm thành công,
     * {@code false} nếu không (ví dụ: danh sách null, không tìm thấy biến thể, hoặc không đủ hàng).
     */
    public static boolean reduceStockById(final List<Variant> allVariants, final String variantId, final int quantityToReduce) {
        if (allVariants == null || allVariants.isEmpty()) {
            Log.w(TAG, "reduceStockById: Danh sách biến thể rỗng hoặc null.");
            return false;
        }
        if (variantId == null || variantId.trim().isEmpty()) {
            Log.w(TAG, "reduceStockById: ID biến thể là null hoặc rỗng.");
            return false;
        }
        if (quantityToReduce <= 0) {
            Log.w(TAG, "reduceStockById: Số lượng cần giảm không hợp lệ: " + quantityToReduce);
            return false;
        }

        // Sử dụng Stream để tìm kiếm và cập nhật một cách hiện đại, dễ đọc hơn.
        return allVariants.stream()
                .filter(v -> variantId.equals(v.getId()))
                .findFirst()
                .map(variant -> reduceStock(variant, quantityToReduce))
                .orElseGet(() -> {
                    Log.w(TAG, "reduceStockById: Không tìm thấy biến thể với ID = " + variantId);
                    return false;
                });
    }
}
