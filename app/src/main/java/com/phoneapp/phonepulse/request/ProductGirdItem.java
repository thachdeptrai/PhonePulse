package com.phoneapp.phonepulse.request;

import com.google.gson.annotations.SerializedName;
import com.phoneapp.phonepulse.models.ProductImage;

import java.io.Serializable;
import java.util.List;

public class ProductGirdItem implements Serializable {

    @SerializedName("_id")
    private String _id;

    @SerializedName("variant_id")
    private String variant_id;

    @SerializedName("product_name")
    private String product_name;

    @SerializedName("image_url")
    private String image_url; // Vẫn giữ ảnh đại diện

    @SerializedName("price")
    private double price;

    private double original_price;
    private int discount_percent;
    private int sold_count;

    @SerializedName("size_name")
    private String size_name;

    @SerializedName("color_name")
    private String color_name;

    // ✅ THÊM: Danh sách ảnh
    @SerializedName("images")
    private List<ProductImage> images;
    @SerializedName("category_id")
    private String category_id;

    public ProductGirdItem() {
    }

    public ProductGirdItem(String _id, String variant_id, String product_name, String image_url,
                           double price, double original_price, int discount_percent, int sold_count,
                           String size_name, String color_name, List<ProductImage> images) {
        this._id = _id;
        this.variant_id = variant_id;
        this.product_name = product_name;
        this.image_url = image_url;
        this.price = price;
        this.original_price = original_price;
        this.discount_percent = discount_percent;
        this.sold_count = sold_count;
        this.size_name = size_name;
        this.color_name = color_name;
        this.images = images;
    }

    // --- Getters và Setters ---
    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getVariant_id() {
        return variant_id;
    }

    public String getCategory_id() {
        return category_id;
    }

    public void setCategory_id(String category_id) {
        this.category_id = category_id;
    }

    public void setVariant_id(String variant_id) {
        this.variant_id = variant_id;
    }

    public String getProduct_name() {
        return product_name;
    }

    public void setProduct_name(String product_name) {
        this.product_name = product_name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getOriginal_price() {
        return original_price;
    }

    public void setOriginal_price(double original_price) {
        this.original_price = original_price;
    }

    public int getDiscount_percent() {
        return discount_percent;
    }

    public void setDiscount_percent(int discount_percent) {
        this.discount_percent = discount_percent;
    }

    public int getSold_count() {
        return sold_count;
    }

    public void setSold_count(int sold_count) {
        this.sold_count = sold_count;
    }

    public String getSize_name() {
        return size_name;
    }

    public void setSize_name(String size_name) {
        this.size_name = size_name;
    }

    public String getColor_name() {
        return color_name;
    }

    public void setColor_name(String color_name) {
        this.color_name = color_name;
    }

    // ✅ Getter & Setter for list image
    public List<ProductImage> getImages() {
        return images;
    }

    public void setImages(List<ProductImage> images) {
        this.images = images;
    }
}
