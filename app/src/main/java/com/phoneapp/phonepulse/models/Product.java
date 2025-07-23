package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

import java.util.List; // Thêm import này cho List

public class Product {
    @SerializedName("_id")
    private String id;

    @SerializedName("product_name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("category_id") // Backend populate thành đối tượng Category
    private Category category;

    // THAY ĐỔI: Đây là mảng các biến thể mà backend trả về
    @SerializedName("variants") // Tên trường trong JSON là "variants"
    private List<Variant> variants; // Loại dữ liệu là List<Variant>

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("modified_date")
    private String modifiedDate;

    @SerializedName("discount")
    private int discount = 0;

    // THAY ĐỔI: Đổi tên trường và @SerializedName cho khớp với backend
    @SerializedName("productImage") // Tên trường trong JSON là "productImage"
    private ProductImage productImage;

    // Constructor mặc định (cần cho Gson)
    public Product() {
    }

    // Constructor đầy đủ (Tùy chọn, nhưng hữu ích)
    public Product(String id, String name, String description, Category category,
                   List<Variant> variants, String createdDate, String modifiedDate,
                   int discount, ProductImage productImage) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.variants = variants;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.discount = discount;
        this.productImage = productImage;
    }


    // ✅ Hàm an toàn để lấy image URL (vẫn giữ nguyên, rất tốt!)
    public String getImageUrlSafe() {
        if (productImage != null && productImage.getImageUrl() != null) {
            return productImage.getImageUrl();
        } else {
            // Có thể trả về một URL ảnh mặc định nếu không có ảnh nào
            return null;
        }
    }

    // --- Getters and Setters ---
    // Đảm bảo có getter/setter cho List<Variant>
    public List<Variant> getVariants() { return variants; }
    public void setVariants(List<Variant> variants) { this.variants = variants; }

    public ProductImage getProductImage() { return productImage; }
    public void setProductImage(ProductImage productImage) { this.productImage = productImage; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(String modifiedDate) { this.modifiedDate = modifiedDate; }

    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }
}