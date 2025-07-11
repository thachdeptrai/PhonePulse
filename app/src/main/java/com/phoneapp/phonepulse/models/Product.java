package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;
import java.util.List; // Import List

public class Product {

    @SerializedName("_id")
    private String id;

    @SerializedName("product_name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("category_id")
    private Category category; // Assuming Category model exists and is correctly mapped

    @SerializedName("variants") // Phải khớp với tên trường 'variants' mà backend trả về
    private List<Variant> variants; // Bây giờ là một DANH SÁCH các biến thể

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("modified_date")
    private String modifiedDate;

    @SerializedName("discount")
    private Integer discount;

    @SerializedName("productimage_id") // Assuming productimage_id field holds a ProductImage object
    private ProductImage productImage; // Assuming ProductImage model exists

    // ===================== Getters - Setters ==================== //

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // Getter cho danh sách biến thể
    public List<Variant> getVariants() {
        return variants;
    }

    // Setter cho danh sách biến thể
    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public int getDiscount() {
        return discount != null ? discount : 0;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public ProductImage getProductImage() {
        return productImage;
    }

    public void setProductImage(ProductImage productImage) {
        this.productImage = productImage;
    }

    // ===================== Tiện ích ==================== //

    /**
     * Lấy giá bán từ Variant ĐẦU TIÊN trong danh sách, nếu không có biến thể trả về 0.
     * Lưu ý: Nếu sản phẩm có nhiều biến thể, phương thức này chỉ lấy giá của biến thể đầu tiên.
     */
    public double getPrice() {
        if (variants != null && !variants.isEmpty()) {
            return variants.get(0).getPrice(); // Lấy giá từ biến thể đầu tiên
        }
        return 0; // Trả về 0 nếu không có biến thể
    }

    /**
     * Lấy ID của Variant ĐẦU TIÊN để xử lý giỏ hàng, nếu không có biến thể trả về null.
     * Lưu ý: Nếu sản phẩm có nhiều biến thể, phương thức này chỉ lấy ID của biến thể đầu tiên.
     */
    public String getVariantId() {
        if (variants != null && !variants.isEmpty()) {
            return variants.get(0).getId(); // Lấy ID từ biến thể đầu tiên
        }
        return null; // Trả về null nếu không có biến thể
    }
}