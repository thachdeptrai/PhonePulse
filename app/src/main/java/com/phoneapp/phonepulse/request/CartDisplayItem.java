package com.phoneapp.phonepulse.request;

// Bạn có thể không cần import Product và Variant ở đây nếu bạn chỉ lưu trữ các ID và thông tin đã được làm phẳng
// import com.phoneapp.phonepulse.models.Product;
// import com.phoneapp.phonepulse.models.Variant;

public class CartDisplayItem {

    private String cartItemId; // ID của mục trong giỏ hàng (từ CartItem ban đầu)
    private String productId;
    private String variantId;
    private int quantity;

    // Thông tin Product đầy đủ, được lấy từ API getProduct
    private String productName;
    private String productImageLUrl; // URL ảnh chính của sản phẩm

    // Thông tin Variant đầy đủ, được lấy từ API getVariant
    private String variantColor;
    private String variantSize;
    private double variantPrice; // Giá của biến thể cụ thể

    // Constructor để khởi tạo từ CartItem ban đầu
    public CartDisplayItem(CartItem cartItem) {
        this.cartItemId = cartItem.getId();
        this.productId = cartItem.getProductId(); // Giả sử CartItem đã được sửa để có productId
        this.variantId = cartItem.getVariantId(); // Giả sử CartItem đã được sửa để có variantId
        this.quantity = cartItem.getQuantity();
        // Các trường khác sẽ được set sau khi fetch từ API trong ViewModel
    }

    // Constructor đầy đủ để thuận tiện trong ViewModel sau khi fetch dữ liệu
    public CartDisplayItem(String cartItemId, String productId, String variantId, int quantity,
                           String productName, String productImageLUrl,
                           String variantColor, String variantSize, double variantPrice) {
        this.cartItemId = cartItemId;
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.productName = productName;
        this.productImageLUrl = productImageLUrl;
        this.variantColor = variantColor;
        this.variantSize = variantSize;
        this.variantPrice = variantPrice;
    }

    // --- Getters and Setters cho tất cả các trường ---

    public String getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(String cartItemId) {
        this.cartItemId = cartItemId;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImageLUrl() {
        return productImageLUrl;
    }

    public void setProductImageLUrl(String productImageLUrl) {
        this.productImageLUrl = productImageLUrl;
    }

    public String getVariantColor() {
        return variantColor;
    }

    public void setVariantColor(String variantColor) {
        this.variantColor = variantColor;
    }

    public String getVariantSize() {
        return variantSize;
    }

    public void setVariantSize(String variantSize) {
        this.variantSize = variantSize;
    }

    // CHỈNH SỬA Ở ĐÂY: Phương thức getVariantPrice() không cần tham số
    public double getVariantPrice() {
        return variantPrice;
    }

    public void setVariantPrice(double variantPrice) {
        this.variantPrice = variantPrice;
    }

    // Phương thức tiện ích để tính tổng giá của một mục (số lượng * giá biến thể)
    public double getItemTotalPrice() {
        return variantPrice * quantity;
    }
}