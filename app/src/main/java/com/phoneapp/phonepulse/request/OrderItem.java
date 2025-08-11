package com.phoneapp.phonepulse.request;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

public class OrderItem implements Parcelable {
    @SerializedName("_id") // hoặc "id" tùy API trả về
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

    public OrderItem(@NonNull String id, @NonNull String name, @NonNull String imageUrl,
                     int price, int quantity, @NonNull String variant,
                     @NonNull String productId, @NonNull String variantId) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.variant = variant;
        this.productId = productId;
        this.variantId = variantId;

        Log.d("OrderItem_Init", "Tạo OrderItem: " +
                "id=" + id +
                ", name=" + name +
                ", imageUrl=" + imageUrl +
                ", price=" + price +
                ", quantity=" + quantity +
                ", variant=" + variant +
                ", productId=" + productId +
                ", variantId=" + variantId);
    }

    public OrderItem(@NonNull String name, @NonNull String imageUrl,
                     int price, int quantity, @NonNull String variant,
                     @NonNull String productId, @NonNull String variantId) {
        this("", name, imageUrl, price, quantity, variant, productId, variantId);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getVariant() { return variant; }
    public String getProductId() { return productId; }
    public String getVariantId() { return variantId; }

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
