package com.phoneapp.phonepulse.utils;

import android.util.Log;

import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.request.OrderItem;

import java.util.List;

/**
 * Quản lý tồn kho biến thể trong bối cảnh giỏ hàng/đơn hàng.
 * Lớp tiện ích (utility class) này cung cấp các phương thức để kiểm tra, giảm và tăng tồn kho
 * của các biến thể sản phẩm trong bộ nhớ cục bộ của ứng dụng.
 * Việc cập nhật tồn kho trên máy chủ (backend) cần được thực hiện thông qua các API call riêng biệt.
 */
public final class CartUtils {

    private static final String TAG = "CartUtils";

    // Private constructor để ngăn việc tạo instance của lớp utility này
    private CartUtils() {}

    /**
     * Kiểm tra xem một biến thể có còn hàng hay không.
     * Biến thể còn hàng nếu đối tượng không null và số lượng tồn kho lớn hơn 0.
     *
     * @param variant Đối tượng Variant cần kiểm tra.
     * @return true nếu biến thể còn hàng, false nếu null hoặc hết hàng.
     */
    public static boolean isVariantInStock(final Variant variant) {
        return variant != null && variant.getQuantity() > 0;
    }

    /**
     * Kiểm tra xem có thể thêm một số lượng mong muốn của biến thể vào giỏ hàng hay không.
     * Có thể thêm vào giỏ nếu số lượng mong muốn lớn hơn 0 và tồn kho hiện có đủ.
     *
     * @param variant Đối tượng Variant cần kiểm tra.
     * @param desiredQuantity Số lượng mong muốn thêm vào giỏ hàng.
     * @return true nếu có thể thêm vào giỏ, false nếu không hợp lệ hoặc không đủ tồn kho.
     */
    public static boolean canAddToCart(final Variant variant, final int desiredQuantity) {
        return desiredQuantity > 0 && isVariantInStock(variant) && variant.getQuantity() >= desiredQuantity;
    }

    /**
     * Giảm số lượng tồn kho cho một biến thể cụ thể.
     * Phương thức này sử dụng hàm `reduceQuantity` an toàn của đối tượng `Variant` để đảm bảo
     * tồn kho không bị âm và xử lý các trường hợp không hợp lệ.
     *
     * @param variant Biến thể cần giảm tồn kho.
     * @param quantityToReduce Số lượng cần giảm. Phải là số dương.
     * @return true nếu tồn kho được giảm thành công, false nếu biến thể null, số lượng không hợp lệ, hoặc không đủ hàng.
     */
    public static boolean reduceStock(final Variant variant, final int quantityToReduce) {
        if (variant == null) {
            Log.w(TAG, "reduceStock: variant == null. Bỏ qua thao tác giảm tồn kho.");
            return false;
        }
        if (quantityToReduce <= 0) {
            Log.w(TAG, "reduceStock: Số lượng cần giảm không hợp lệ: " + quantityToReduce + ". Phải lớn hơn 0.");
            return false;
        }

        // Sử dụng phương thức an toàn của Variant để giảm số lượng tồn kho
        boolean ok = variant.reduceQuantity(quantityToReduce);
        String vid = variant.getId() == null ? "(null)" : variant.getId();

        if (ok) {
            Log.d(TAG, "reduceStock: Đã giảm -" + quantityToReduce + " -> tồn kho mới: "
                    + variant.getQuantity() + " (variantId=" + vid + ")");
            return true;
        } else {
            Log.w(TAG, "reduceStock: Không đủ hàng để giảm. Hiện có: "
                    + variant.getQuantity() + ", cần giảm: " + quantityToReduce
                    + " (variantId=" + vid + ")");
            return false;
        }
    }

    /**
     * Giảm tồn kho cho một biến thể dựa trên ID của nó trong danh sách tất cả các biến thể.
     * Phương pháp này sử dụng vòng lặp thông thường để tương thích với các phiên bản Android cũ hơn
     * và tránh các vấn đề tiềm ẩn với Stream API.
     *
     * @param allVariants Danh sách tất cả các biến thể sản phẩm có sẵn trong hệ thống.
     * @param variantId ID của biến thể cần giảm tồn kho.
     * @param quantityToReduce Số lượng tồn kho cần giảm.
     * @return true nếu tìm thấy biến thể và giảm tồn kho thành công, false nếu không tìm thấy hoặc lỗi.
     */
    public static boolean reduceStockById(final List<Variant> allVariants,
                                          final String variantId,
                                          final int quantityToReduce) {
        if (allVariants == null || allVariants.isEmpty()) {
            Log.w(TAG, "reduceStockById: Danh sách 'allVariants' rỗng hoặc null. Không thể giảm tồn kho.");
            return false;
        }
        if (variantId == null || variantId.trim().isEmpty()) {
            Log.w(TAG, "reduceStockById: 'variantId' null hoặc rỗng. Không thể xác định biến thể.");
            return false;
        }
        if (quantityToReduce <= 0) {
            Log.w(TAG, "reduceStockById: Số lượng cần giảm không hợp lệ: " + quantityToReduce + ". Phải lớn hơn 0.");
            return false;
        }

        for (Variant v : allVariants) {
            // Đảm bảo đối tượng biến thể không null trước khi truy cập ID
            if (v != null && variantId.equals(v.getId())) {
                return reduceStock(v, quantityToReduce); // Gọi lại phương thức giảm tồn kho chính
            }
        }
        Log.w(TAG, "reduceStockById: Không tìm thấy biến thể với ID = " + variantId + " trong danh sách.");
        return false;
    }

    /**
     * Giảm tồn kho hàng loạt cho các sản phẩm sau khi một đơn hàng được tạo thành công.
     * Phương thức này duyệt qua danh sách các mặt hàng đã đặt và giảm tồn kho tương ứng
     * trong danh sách `allVariants` cục bộ.
     *
     * @param allVariants Danh sách tất cả các biến thể sản phẩm hiện có trong hệ thống.
     * @param orderedItems Danh sách các OrderItem từ đơn hàng đã được tạo.
     */
    public static void reduceStockAfterOrder(final List<Variant> allVariants,
                                             final List<OrderItem> orderedItems) {
        if (allVariants == null || allVariants.isEmpty()) {
            Log.w(TAG, "reduceStockAfterOrder: 'allVariants' rỗng hoặc null. Không thể giảm tồn kho.");
            return;
        }
        if (orderedItems == null || orderedItems.isEmpty()) {
            Log.w(TAG, "reduceStockAfterOrder: 'orderedItems' rỗng hoặc null. Không có mặt hàng để giảm tồn kho.");
            return;
        }

        for (OrderItem item : orderedItems) {
            if (item == null) {
                Log.w(TAG, "reduceStockAfterOrder: OrderItem null. Bỏ qua.");
                continue;
            }

            final String variantId = item.getVariantId();
            final int qty = item.getQuantity();

            // Kiểm tra tính hợp lệ của dữ liệu OrderItem
            if (variantId == null || variantId.trim().isEmpty()) {
                Log.w(TAG, "reduceStockAfterOrder: variantId trống cho sản phẩm: " + item.getName() + ". Bỏ qua.");
                continue;
            }
            if (qty <= 0) {
                Log.w(TAG, "reduceStockAfterOrder: quantity <= 0 cho sản phẩm: " + item.getName() + ". Bỏ qua.");
                continue;
            }

            boolean found = false;
            for (Variant v : allVariants) {
                if (v != null && variantId.equals(v.getId())) {
                    found = true;
                    boolean ok = reduceStock(v, qty); // Gọi phương thức giảm tồn kho cho từng biến thể
                    if (!ok) {
                        Log.w(TAG, "reduceStockAfterOrder: Không đủ tồn kho cho " + item.getName()
                                + " (variantId=" + variantId + ", yêu cầu=" + qty + ")");
                    }
                    break; // Đã tìm thấy và xử lý biến thể, chuyển sang OrderItem tiếp theo
                }
            }
            if (!found) {
                Log.w(TAG, "reduceStockAfterOrder: Không tìm thấy variantId=" + variantId
                        + " cho sản phẩm: " + item.getName() + " trong danh sách 'allVariants'.");
            }
        }
    }




    /**
     * Tăng tồn kho hàng loạt khi đơn hàng bị hủy.
     * Phương thức này cập nhật số lượng tồn kho của các biến thể tương ứng
     * trong danh sách `allVariants` cục bộ, dựa trên các mặt hàng trong đơn hàng đã hủy.
     *
     * @param allVariants Danh sách tất cả các biến thể sản phẩm hiện có trong hệ thống. Đây là danh sách được cập nhật trong bộ nhớ.
     * @param canceledOrderItems Danh sách các OrderItem từ đơn hàng đã bị hủy.
     */
    public static void increaseStockAfterOrderCancel(final List<Variant> allVariants,
                                                     final List<OrderItem> canceledOrderItems) {
        // 1. Kiểm tra đầu vào hợp lệ để tránh lỗi NullPointerException
        if (allVariants == null || allVariants.isEmpty()) {
            Log.w(TAG, "increaseStockAfterOrderCancel: Danh sách 'allVariants' rỗng hoặc null. Không thể tăng tồn kho cục bộ.");
            return;
        }
        if (canceledOrderItems == null || canceledOrderItems.isEmpty()) {
            Log.w(TAG, "increaseStockAfterOrderCancel: Danh sách 'canceledOrderItems' rỗng hoặc null. Không có mặt hàng nào để tăng tồn kho.");
            return;
        }

        // 2. Duyệt qua từng mặt hàng trong đơn hàng đã hủy
        for (OrderItem item : canceledOrderItems) {
            // Bỏ qua nếu OrderItem bị null
            if (item == null) {
                Log.w(TAG, "increaseStockAfterOrderCancel: Một OrderItem trong danh sách là null. Bỏ qua.");
                continue;
            }

            final String variantId = item.getVariantId();
            final int qtyToIncrease = item.getQuantity(); // Số lượng cần tăng chính là số lượng đã đặt

            // 3. Kiểm tra tính hợp lệ của dữ liệu OrderItem
            if (variantId == null || variantId.trim().isEmpty()) {
                Log.w(TAG, "increaseStockAfterOrderCancel: variantId trống cho sản phẩm: '" + item.getName() + "'. Không thể xác định biến thể để tăng tồn kho. Bỏ qua.");
                continue;
            }
            if (qtyToIncrease <= 0) {
                Log.w(TAG, "increaseStockAfterOrderCancel: Số lượng cần tăng (quantity) <= 0 cho sản phẩm: '" + item.getName() + "'. Không thể tăng tồn kho với số lượng không hợp lệ. Bỏ qua.");
                continue;
            }

            // 4. Tìm biến thể tương ứng trong danh sách 'allVariants'
            boolean found = false;
            for (Variant v : allVariants) {
                // Đảm bảo đối tượng biến thể không null và ID khớp
                if (v != null && variantId.equals(v.getId())) {
                    found = true;
                    // 5. Tăng số lượng tồn kho cục bộ
                    // Vì lớp Variant không có phương thức addQuantity() riêng,
                    // chúng ta thực hiện việc tính toán và set lại số lượng.
                    int currentQuantity = v.getQuantity();
                    v.setQuantity(currentQuantity + qtyToIncrease);

                    Log.d(TAG, "increaseStockAfterOrderCancel: Đã tăng tồn kho cục bộ cho variantId=" + variantId
                            + " thêm +" + qtyToIncrease + ". Tồn kho mới: " + v.getQuantity());
                    break; // Đã tìm thấy và cập nhật biến thể, chuyển sang OrderItem tiếp theo
                }
            }

            // 6. Ghi log nếu không tìm thấy biến thể
            if (!found) {
                Log.w(TAG, "increaseStockAfterOrderCancel: Không tìm thấy variantId=" + variantId
                        + " trong danh sách 'allVariants' để tăng tồn kho cho sản phẩm: '" + item.getName() + "'.");
            }
        }
    }
}