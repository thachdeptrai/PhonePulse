package com.phoneapp.phonepulse.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class Product {
    @SerializedName("_id")
    private String id;

    @SerializedName("product_name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName("category_id")
    private Category category;

    // Khi backend trả về list
    @SerializedName("variants")
    private List<Variant> variants;

    // Khi backend chỉ trả về 1 variant (object)
    @SerializedName("variant")
    private Variant variant;

    @SerializedName("created_date")
    private String createdDate;

    @SerializedName("modified_date")
    private String modifiedDate;

    @SerializedName("discount")
    private int discount = 0;

    @SerializedName("productImage")
    private ProductImage productImage;

    public Product() {}

    // --- Getters & Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public List<Variant> getVariants() {
        // Nếu API trả về object variant thì tự chuyển thành list 1 phần tử
        if ((variants == null || variants.isEmpty()) && variant != null) {
            variants = new ArrayList<>();
            variants.add(variant);
        }
        return variants;
    }
    public void setVariants(List<Variant> variants) { this.variants = variants; }

    public Variant getVariant() { return variant; }
    public void setVariant(Variant variant) { this.variant = variant; }

    public ProductImage getProductImage() { return productImage; }
    public void setProductImage(ProductImage productImage) { this.productImage = productImage; }

    public String getCreatedDate() { return createdDate; }
    public void setCreatedDate(String createdDate) { this.createdDate = createdDate; }

    public String getModifiedDate() { return modifiedDate; }
    public void setModifiedDate(String modifiedDate) { this.modifiedDate = modifiedDate; }

    public int getDiscount() { return discount; }
    public void setDiscount(int discount) { this.discount = discount; }

    // ✅ Lấy image url an toàn
    public String getImageUrlSafe() {
        return productImage != null ? productImage.getImageUrl() : null;
    }
}
