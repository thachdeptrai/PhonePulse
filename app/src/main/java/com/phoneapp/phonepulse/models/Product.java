package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("_id")
    private String id;

    @SerializedName("product_name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("category_id")
    private Category category;

    @SerializedName("variant_id")
    private Variant variant;

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("modified_date")
    private String modifiedDate;

    @SerializedName("discount")
    private Integer discount;

    @SerializedName("productimage_id")
    private ProductImage productImage;

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

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
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
     * Lấy giá bán từ Variant, nếu chưa có Variant trả về 0
     */
    public double getPrice() {
        if (variant != null) {
            return variant.getPrice();
        }
        return 0;
    }

    /**
     * Lấy ID của Variant để xử lý giỏ hàng
     */
    public String getVariantId() {
        if (variant != null) {
            return variant.getId();
        }
        return null;
    }
}
