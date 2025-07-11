// com.phoneapp.phonepulse.request.CartItem.java (or com.phoneapp.phonepulse.models.CartItem.java if it's a model)
package com.phoneapp.phonepulse.request; // Or adjust to models package if preferred

import com.google.gson.annotations.SerializedName;
import com.phoneapp.phonepulse.models.Product; // Assuming this model exists

public class CartItem {
    @SerializedName("_id")
    private String id; // ID of the cart item entry itself

    @SerializedName("product") // Assuming your backend embeds the full product object
    private Product product;

    @SerializedName("variant") // Assuming your backend embeds the full variant object
    private com.phoneapp.phonepulse.models.Variant variant; // Full Variant object

    @SerializedName("quantity")
    private int quantity;

    // You might also have a 'price' or 'totalPrice' here if the backend calculates it for the item
    // private double itemPrice;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public com.phoneapp.phonepulse.models.Variant getVariant() {
        return variant;
    }

    public void setVariant(com.phoneapp.phonepulse.models.Variant variant) {
        this.variant = variant;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Consider adding a method to get the effective price of this item
    public double getItemTotalPrice() {
        if (product != null && variant != null) {
            return variant.getPrice() * quantity;
        } else if (product != null) { // Fallback if variant isn't always present
            return product.getPrice() * quantity;
        }
        return 0.0;
    }
}