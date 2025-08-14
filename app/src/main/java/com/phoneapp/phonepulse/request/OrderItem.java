package com.phoneapp.phonepulse.request;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class OrderItem implements Parcelable {

    @SerializedName("_id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("price")
    private int price;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("variant")
    private String variant;

    @SerializedName("productId")
    private String productId;

    @SerializedName("variantId")
    private String variantId;

    // Constructor mặc định cho Gson
    public OrderItem() {
    }

    // Constructor đầy đủ
    public OrderItem(String id, String name, String imageUrl,
                     int price, int quantity, String variant,
                     String productId, String variantId) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.variant = variant;
        this.productId = productId;
        this.variantId = variantId;
    }

    // Constructor không có id
    public OrderItem(String name, String imageUrl,
                     int price, int quantity, String variant,
                     String productId, String variantId) {
        this(null, name, imageUrl, price, quantity, variant, productId, variantId);
    }

    // --------- GETTERS & SETTERS ---------
    public String getId() {
        return id != null ? id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getVariant() {
        return variant != null ? variant : "";
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getProductId() {
        return productId != null ? productId : "";
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getVariantId() {
        return variantId != null ? variantId : "";
    }

    public void setVariantId(String variantId) {
        this.variantId = variantId;
    }

    // --------- PARCELABLE ---------
    protected OrderItem(Parcel in) {
        id = in.readString();
        name = in.readString();
        imageUrl = in.readString();
        price = in.readInt();
        quantity = in.readInt();
        variant = in.readString();
        productId = in.readString();
        variantId = in.readString();
    }

    public static final Creator<OrderItem> CREATOR = new Creator<OrderItem>() {
        @Override
        public OrderItem createFromParcel(Parcel in) {
            return new OrderItem(in);
        }

        @Override
        public OrderItem[] newArray(int size) {
            return new OrderItem[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
        dest.writeInt(price);
        dest.writeInt(quantity);
        dest.writeString(variant);
        dest.writeString(productId);
        dest.writeString(variantId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // --------- DEBUG ---------
    @NonNull
    @Override
    public String toString() {
        return "OrderItem{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", variant='" + variant + '\'' +
                ", productId='" + productId + '\'' +
                ", variantId='" + variantId + '\'' +
                '}';
    }
}
