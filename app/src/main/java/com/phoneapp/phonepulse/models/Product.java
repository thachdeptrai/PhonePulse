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
    private Variant variantId;

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("modified_date")
    private String modifiedDate;

    @SerializedName("discount")
    private int discount = 0;

    @SerializedName("productimage_id")
    private ProductImage productImage;

    // ✅ Hàm an toàn để lấy image URL
    public String getImageUrlSafe() {
        if (productImage != null && productImage.getImageUrl() != null) {
            return productImage.getImageUrl();
        } else {
            return null; // hoặc return ảnh mặc định
        }
    }

    // Getters and Setters
    public Variant getVariantId() { return variantId; }
    public void setVariantId(Variant variantId) { this.variantId = variantId; }

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
