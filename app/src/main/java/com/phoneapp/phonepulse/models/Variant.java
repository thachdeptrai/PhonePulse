package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

// Đảm bảo bạn đã định nghĩa Color và Size model nếu chúng được sử dụng trong Variants
// Ví dụ đơn giản cho Color và Size (nếu chúng chỉ có ID và Name)
/*
public class Color {
    @SerializedName("_id")
    private String id;
    @SerializedName("color_name") // Tên trường trong DB
    private String name;
    // Getters and Setters
}

public class Size {
    @SerializedName("_id")
    private String id;
    @SerializedName("size_name") // Tên trường trong DB
    private String name;
    // Getters and Setters
}
*/

public class Variant {
    @SerializedName("_id")
    private String id;

    // Giữ productId nếu bạn vẫn muốn biết variant này thuộc product nào
    @SerializedName("product_id")
    private String productId;

    // Các trường gốc của Variant
    // Đảm bảo Color và Size model của bạn khớp với cấu trúc JSON trả về.
    // Nếu backend trả về Color và Size là object, thì giữ nguyên
    // Nếu backend chỉ trả về ID của Color/Size, bạn có thể thay đổi kiểu dữ liệu thành String
    @SerializedName("color_id") // Hoặc "color" nếu backend populate toàn bộ object Color
    private Color color; // Đã đổi tên biến cho rõ ràng hơn (từ colorId sang color)

    @SerializedName("size_id")  // Hoặc "size" nếu backend populate toàn bộ object Size
    private Size size;   // Đã đổi tên biến cho rõ ràng hơn (từ sizeId sang size)

    @SerializedName("price")
    private double price;

    @SerializedName("quantity")
    private int quantity;

    // THÊM CÁC TRƯỜNG ĐƯỢC NHÚNG TỪ PRODUCT VÀ PRODUCTIMAGE (Aggregation từ Backend)
    // Tên trong @SerializedName PHẢI KHỚP VỚI TÊN TRƯỜNG BẠN ĐẶT TRONG $project CỦA AGGREGATION
    @SerializedName("product_name")
    private String productName;

    @SerializedName("image_url")
    private String imageUrl;

    // ===================== Constructors ==================== //
    public Variant() {
    }

    public Variant(String id, String productId, Color color, Size size, double price, int quantity, String productName, String imageUrl) {
        this.id = id;
        this.productId = productId;
        this.color = color;
        this.size = size;
        this.price = price;
        this.quantity = quantity;
        this.productName = productName;
        this.imageUrl = imageUrl;
    }

    // ===================== Getters - Setters ==================== //

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }

    // Đã đổi tên getter/setter cho khớp với tên biến 'color'
    public Color getColor() {
        return color;
    }
    public void setColor(Color color) {
        this.color = color;
    }

    // Đã đổi tên getter/setter cho khớp với tên biến 'size'
    public Size getSize() {
        return size;
    }
    public void setSize(Size size) {
        this.size = size;
    }

    public double getPrice() {
        return price;
    }
    public void setPrice(double price) {
        this.price = price;
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

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}