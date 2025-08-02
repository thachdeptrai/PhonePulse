package com.phoneapp.phonepulse.request;

import android.os.Parcel;
import android.os.Parcelable;

public class OrderItem implements Parcelable {
    private String name;
    private String imageUrl;
    private int price;
    private int quantity;
    private String variant; // Tùy chọn, có thể là màu/kích thước

    public OrderItem(String name, String imageUrl, int price, int quantity, String variant) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.price = price;
        this.quantity = quantity;
        this.variant = variant;
    }

    // Constructor nếu không có variant
    public OrderItem(String name, String imageUrl, int price, int quantity) {
        this(name, imageUrl, price, quantity, null);
    }

    // Getters
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getVariant() { return variant; }

    // -------- Parcelable implementation --------
    protected OrderItem(Parcel in) {
        name = in.readString();
        imageUrl = in.readString();
        price = in.readInt();
        quantity = in.readInt();
        variant = in.readString();
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
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
