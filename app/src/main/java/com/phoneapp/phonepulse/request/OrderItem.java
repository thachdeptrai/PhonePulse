package com.phoneapp.phonepulse.request;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class OrderItem implements Parcelable {
    private final String name;
    private final String imageUrl;
    private final int price;
    private final int quantity;
    private final String variant;
    private final String productId;
    private final String variantId;

    // ✅ Constructor đầy đủ
    public OrderItem(@NonNull String name, @NonNull String imageUrl, int price, int quantity,
                     @NonNull String variant, @NonNull String productId, @NonNull String variantId) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.variant = variant;
        this.productId = productId;
        this.variantId = variantId;
    }

    // ✅ Getters
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getVariant() { return variant; }
    public String getProductId() { return productId; }
    public String getVariantId() { return variantId; }

    // -------- Parcelable implementation --------
    protected OrderItem(Parcel in) {
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
}
