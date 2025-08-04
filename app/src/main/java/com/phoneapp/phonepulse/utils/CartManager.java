package com.phoneapp.phonepulse.utils;


import com.phoneapp.phonepulse.request.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartManager {
    private static CartManager instance;
    private List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // Lưu danh sách giỏ hàng
    public void setCartItems(List<CartItem> items) {
        this.cartItems = items;
    }

    // Lấy toàn bộ giỏ hàng
    public List<CartItem> getCartItems() {
        return cartItems;
    }

    // Tìm item theo variantId
    public CartItem getItemByVariantId(String variantId) {
        for (CartItem item : cartItems) {
            if (item.getVariant().equals(variantId)) {
                return item;
            }
        }
        return null;
    }

    // Xoá 1 item khỏi giỏ hàng
    public void removeItem(String variantId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getVariant().equals(variantId)) {
                cartItems.remove(i);
                break;
            }
        }
    }

    // Cập nhật số lượng
    public void updateQuantity(String variantId, int quantity) {
        for (CartItem item : cartItems) {
            if (item.getVariant().equals(variantId)) {
                item.setQuantity(quantity);
                break;
            }
        }
    }

    // Xoá toàn bộ giỏ hàng
    public void clearCart() {
        cartItems.clear();
    }
}
