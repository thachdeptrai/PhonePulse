package com.phoneapp.phonepulse.request;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

// Lớp CartItem chính
public class CartItem implements Serializable {
    @SerializedName("_id")
    private String id;

    @SerializedName("productId")
    private ProductInCart product;

    @SerializedName("variantId")
    private VariantInCart variant;

    @SerializedName("quantity")
    private int quantity;

    // THÊM TRƯỜNG NÀY ĐỂ ÁNH XẠ ẢNH TỪ BACKEND
    @SerializedName("productImage") // Tên trường trong JSON từ backend
    private String productImage; // Kiểu String vì backend đang trả về URL trực tiếp

    private boolean isSelected;

    public CartItem() {
    }

    // Cập nhật constructor nếu bạn sử dụng constructor đầy đủ
    public CartItem(String id, ProductInCart product, VariantInCart variant, int quantity, String productImage) {
        this.id = id;
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        this.productImage = productImage; // Gán giá trị ảnh
        this.isSelected = false;
    }

    // Getters
    public String getId() { return id; }
    public ProductInCart getProduct() { return product; }
    public VariantInCart getVariant() { return variant; }
    public int getQuantity() { return quantity; }
    public boolean isSelected() { return isSelected; }
    public String getProductImage() { return productImage; } // THÊM GETTER NÀY

    // Setters
    public void setId(String id) { this.id = id; }
    public void setProduct(ProductInCart product) { this.product = product; }
    public void setVariant(VariantInCart variant) { this.variant = variant; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setSelected(boolean selected) { isSelected = selected; }
    public void setProductImage(String productImage) { this.productImage = productImage; } // THÊM SETTER NÀY

    @Override
    public String toString() {
        return "CartItem{" +
                "id='" + id + '\'' +
                ", product=" + product +
                ", variant=" + variant +
                ", quantity=" + quantity +
                ", productImage='" + (productImage != null ? productImage.substring(0, Math.min(productImage.length(), 50)) + "..." : "null") + '\'' + // Rút gọn log Base64
                ", isSelected=" + isSelected +
                '}';
    }

    // --- Nested Classes (giữ nguyên các lớp này) ---

    // Lớp ProductInCart
    public static class ProductInCart implements Serializable { // Thêm implements Serializable
        @SerializedName("_id")
        private String id;
        @SerializedName("product_name")
        private String productName;
        @SerializedName("original_price")
        private double originalPrice;
        @SerializedName("discount_percent")
        private double discountPercent;


        public ProductInCart() {}
        public ProductInCart(String id, String productName, double originalPrice, double discountPercent /*, List<ProductImageInCart> productImages*/) {
            this.id = id;
            this.productName = productName;
            this.originalPrice = originalPrice;
            this.discountPercent = discountPercent;
            // this.productImages = productImages;
        }

        public String getId() { return id; }
        public String getProductName() { return productName; }
        public double getOriginalPrice() { return originalPrice; }
        public double getDiscountPercent() { return discountPercent; }
        // public List<ProductImageInCart> getProductImages() { return productImages; } // Bỏ getter này nếu bỏ trường

        public void setId(String id) { this.id = id; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setOriginalPrice(double originalPrice) { this.originalPrice = originalPrice; }
        public void setDiscountPercent(double discountPercent) { this.discountPercent = discountPercent; }
        // public void setProductImages(List<ProductImageInCart> productImages) { this.productImages = productImages; } // Bỏ setter này nếu bỏ trường

        @Override
        public String toString() {
            return "ProductInCart{" +
                    "id='" + id + '\'' +
                    ", productName='" + productName + '\'' +
                    ", originalPrice=" + originalPrice +
                    ", discountPercent=" + discountPercent +
                    // ", productImages=" + productImages + // Bỏ nếu bỏ trường
                    '}';
        }
    }

    // Lớp ProductImageInCart (Bạn có thể giữ lại nếu cần nó cho các ảnh phụ khác, nhưng không dùng cho ảnh chính của CartItem nữa)
    public static class ProductImageInCart implements Serializable { // Thêm implements Serializable
        @SerializedName("_id")
        private String id;
        @SerializedName("image_url")
        private String imageUrl;
        @SerializedName("is_thumbnail")
        private boolean isThumbnail;

        public ProductImageInCart() {}
        public ProductImageInCart(String id, String imageUrl, boolean isThumbnail) {
            this.id = id;
            this.imageUrl = imageUrl;
            this.isThumbnail = isThumbnail;
        }

        public String getId() { return id; }
        public String getImageUrl() { return imageUrl; }
        public boolean isThumbnail() { return isThumbnail; }

        public void setId(String id) { this.id = id; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public void setThumbnail(boolean thumbnail) { isThumbnail = thumbnail; }

        @Override
        public String toString() {
            return "ProductImageInCart{" +
                    "id='" + id + '\'' +
                    ", imageUrl='" + imageUrl + '\'' +
                    ", isThumbnail=" + isThumbnail +
                    '}';
        }
    }

    // Lớp VariantInCart
    public static class VariantInCart implements Serializable { // Thêm implements Serializable
        @SerializedName("_id")
        private String id;
        @SerializedName("price")
        private double price;
        @SerializedName("quantity")
        private int stockQuantity;
        @SerializedName("color_id")
        private ColorInCart color;
        @SerializedName("size_id")
        private SizeInCart size;

        public VariantInCart() {}
        public VariantInCart(String id, double price, int stockQuantity, ColorInCart color, SizeInCart size) {
            this.id = id;
            this.price = price;
            this.stockQuantity = stockQuantity;
            this.color = color;
            this.size = size;
        }

        public String getId() { return id; }
        public double getPrice() { return price; }
        public int getStockQuantity() { return stockQuantity; }
        public ColorInCart getColor() { return color; }
        public SizeInCart getSize() { return size; }

        public void setId(String id) { this.id = id; }
        public void setPrice(double price) { this.price = price; }
        public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
        public void setColor(ColorInCart color) { this.color = color; }
        public void setSize(SizeInCart size) { this.size = size; }

        @Override
        public String toString() {
            return "VariantInCart{" +
                    "id='" + id + '\'' +
                    ", price=" + price +
                    ", stockQuantity=" + stockQuantity +
                    ", color=" + color +
                    ", size=" + size +
                    '}';
        }
    }

    // Lớp ColorInCart
    public static class ColorInCart implements Serializable { // Thêm implements Serializable
        @SerializedName("_id")
        private String id;
        @SerializedName("color_name")
        private String colorName;

        public ColorInCart() {}
        public ColorInCart(String id, String colorName) {
            this.id = id;
            this.colorName = colorName;
        }

        public String getId() { return id; }
        public String getColorName() { return colorName; }

        public void setId(String id) { this.id = id; }
        public void setColorName(String colorName) { this.colorName = colorName; }

        @Override
        public String toString() {
            return "ColorInCart{" +
                    "id='" + id + '\'' +
                    ", colorName='" + colorName + '\'' +
                    '}';
        }
    }

    // Lớp SizeInCart
    public static class SizeInCart implements Serializable { // Thêm implements Serializable
        @SerializedName("_id")
        private String id;
        @SerializedName("size_name")
        private String sizeName;
        @SerializedName("storage")
        private String storage;
        @SerializedName("ram")
        private String ram;

        public SizeInCart() {}
        public SizeInCart(String id, String sizeName, String storage, String ram) {
            this.id = id;
            this.sizeName = sizeName;
            this.storage = storage;
            this.ram = ram;
        }

        public String getId() { return id; }
        public String getSizeName() { return sizeName; }
        public String getStorage() { return storage; }
        public String getRam() { return ram; }

        public void setId(String id) { this.id = id; }
        public void setSizeName(String sizeName) { this.sizeName = sizeName; }
        public void setStorage(String storage) { this.storage = storage; }
        public void setRam(String ram) { this.ram = ram; }

        @Override
        public String toString() {
            return "SizeInCart{" +
                    "id='" + id + '\'' +
                    ", sizeName='" + sizeName + '\'' +
                    ", storage='" + storage + '\'' +
                    ", ram='" + ram + '\'' +
                    '}';
        }
    }
}