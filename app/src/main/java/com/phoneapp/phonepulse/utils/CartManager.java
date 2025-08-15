package com.phoneapp.phonepulse.utils;



import com.phoneapp.phonepulse.request.CartItem;

import java.util.ArrayList;
import java.util.Iterator; // Import Iterator
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    // Constructor riêng tư để đảm bảo Singleton pattern
    private CartManager() {
        cartItems = new ArrayList<>();
    }

    // Phương thức để lấy thể hiện duy nhất của CartManager
    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    /**
     * Đặt danh sách các CartItem vào CartManager.
     * Sử dụng khi tải giỏ hàng từ API.
     * @param items Danh sách CartItem mới.
     */
    public void setCartItems(List<CartItem> items) {
        // Tạo một bản sao để tránh tham chiếu trực tiếp, đảm bảo an toàn luồng
        this.cartItems = new ArrayList<>(items);
    }

    /**
     * Lấy toàn bộ danh sách CartItem hiện có trong giỏ hàng.
     * @return Danh sách các CartItem.
     */
    public List<CartItem> getCartItems() {
        // Trả về một bản sao để ngăn chặn sửa đổi trực tiếp bên ngoài
        return new ArrayList<>(cartItems);
    }

    /**
     * Tìm một CartItem theo ID biến thể (variantId).
     * @param variantId ID của biến thể cần tìm.
     * @return CartItem nếu tìm thấy, ngược lại trả về null.
     */
    public CartItem getItemByVariantId(String variantId) {
        for (CartItem item : cartItems) {
            // ✅ SỬA LỖI: So sánh variantId của Variant object, KHÔNG phải Variant object với String.
            if (item.getVariant() != null && item.getVariant().getId() != null && item.getVariant().getId().equals(variantId)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Xóa một CartItem cụ thể khỏi giỏ hàng theo ID biến thể.
     * @param variantId ID của biến thể của sản phẩm cần xóa.
     */
    public void removeItem(String variantId) {
        // Sử dụng Iterator để xóa an toàn trong khi lặp
        Iterator<CartItem> iterator = cartItems.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            // ✅ SỬA LỖI: So sánh variantId của Variant object
            if (item.getVariant() != null && item.getVariant().getId() != null && item.getVariant().getId().equals(variantId)) {
                iterator.remove(); // Xóa an toàn
                break; // Chỉ cần xóa một item khớp
            }
        }
    }

    /**
     * Cập nhật số lượng của một sản phẩm trong giỏ hàng theo ID biến thể.
     * @param variantId ID của biến thể của sản phẩm cần cập nhật.
     * @param quantity Số lượng mới.
     */
    public void updateQuantity(String variantId, int quantity) {
        for (CartItem item : cartItems) {
            // ✅ SỬA LỖI: So sánh variantId của Variant object
            if (item.getVariant() != null && item.getVariant().getId() != null && item.getVariant().getId().equals(variantId)) {
                item.setQuantity(quantity);
                break; // Cập nhật xong thì thoát vòng lặp
            }
        }
    }

    /**
     * Xóa toàn bộ sản phẩm khỏi giỏ hàng.
     */
    public void clearCart() {
        cartItems.clear();
    }
}