package com.phoneapp.phonepulse.request;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

public class OrderItem implements Parcelable {

    @SerializedName("_id") // Nếu API yêu cầu "id" thì đổi lại
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

        Log.d("OrderItem_Init", toString());
    }

    // Constructor không có id
    public OrderItem(String name, String imageUrl,
                     int price, int quantity, String variant,
                     String productId, String variantId) {
        this(null, name, imageUrl, price, quantity, variant, productId, variantId);
    }

    // Getter
    public String getId() { return id; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public String getVariant() { return variant; }
    public String getProductId() { return productId; }
    public String getVariantId() { return variantId; }

    // Setter cho id để có thể gán lại khi cần
    public void setId(String id) { this.id = id; }

    // Hàm xóa id rỗng/null
    public void sanitize() {
        if (id != null && id.trim().isEmpty()) {
            Log.w("OrderItem_Sanitize", "Phát hiện _id rỗng, đặt lại thành null.");
            id = null;
        }
    }

    // Parcelable
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
