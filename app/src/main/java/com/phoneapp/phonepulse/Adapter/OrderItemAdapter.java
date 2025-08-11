package com.phoneapp.phonepulse.Adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.OrderItem;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderViewHolder> {

    private final List<OrderItem> orderItems;
    private static final String TAG = "OrderItemAdapter";

    public OrderItemAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        Log.d(TAG, "Constructor - orderItems size = " + (orderItems != null ? orderItems.size() : 0));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder() called for viewType=" + viewType);
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder() position=" + position + " / adapterCount=" + getItemCount());

        if (orderItems == null) {
            Log.e(TAG, "orderItems is null -> nothing to bind");
            return;
        }
        if (position < 0 || position >= orderItems.size()) {
            Log.e(TAG, "Invalid position: " + position);
            return;
        }

        OrderItem item = orderItems.get(position);
        if (item == null) {
            Log.w(TAG, "OrderItem at position " + position + " is null");
            // reset UI to defaults
            holder.tvName.setText("Sản phẩm");
            holder.tvQuantity.setText("Số lượng: 0");
            holder.tvPrice.setText("Giá: 0 đ");
            holder.tvVariant.setVisibility(View.GONE);
            holder.ivImage.setImageResource(R.drawable.placeholder_product);
            return;
        }

        // pretty JSON debug
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(item);
            Log.d(TAG, "Raw OrderItem JSON at pos " + position + ":\n" + json);
        } catch (Exception e) {
            Log.w(TAG, "Gson serialization failed: " + e.getMessage(), e);
        }

        // Read fields safely
        String id = safeString(item.getId());
        String name = safeString(item.getName());
        String imageUrl = safeString(item.getImageUrl());
        int price = item.getPrice();
        int quantity = item.getQuantity();
        String variant = safeString(item.getVariant());
        String productId = safeString(item.getProductId());
        String variantId = safeString(item.getVariantId());

        // Detailed field log
        Log.d(TAG, String.format("Fields (pos=%d): id=%s, productId=%s, variantId=%s, name=%s, quantity=%d, price=%d, variant=%s, imageUrl=%s, objHash=%d",
                position, id, productId, variantId, name, quantity, price, variant, imageUrl, System.identityHashCode(item)));

        // Warnings for suspicious values
        if (TextUtils.isEmpty(name)) Log.w(TAG, "Name is empty/null for item at pos " + position);
        if (price <= 0) Log.w(TAG, "Price is zero or negative for item at pos " + position);
        if (quantity <= 0) Log.w(TAG, "Quantity is zero or negative for item at pos " + position);
        if (TextUtils.isEmpty(imageUrl)) Log.w(TAG, "ImageURL is empty/null for item at pos " + position);

        boolean imageLooksValid = imageUrl.startsWith("http://") || imageUrl.startsWith("https://");
        Log.d(TAG, "Image URL looks valid: " + imageLooksValid + " ('" + imageUrl + "')");

        // UI binding (safe)
        holder.tvName.setText(!TextUtils.isEmpty(name) ? name : "Sản phẩm");
        holder.tvQuantity.setText("Số lượng: " + quantity);

        int totalItemPrice = price * Math.max(0, quantity); // guard quantity negative
        holder.tvPrice.setText("Giá: " + formatCurrency(totalItemPrice));

        if (!TextUtils.isEmpty(variant)) {
            holder.tvVariant.setText("Phân loại: " + variant);
            holder.tvVariant.setVisibility(View.VISIBLE);
        } else {
            holder.tvVariant.setVisibility(View.GONE);
        }

        // Load image with Glide and detailed logs
        try {
            if (imageLooksValid) {
                Log.d(TAG, "Glide load (URL) for pos " + position + ": " + imageUrl);
                Glide.with(holder.itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(holder.ivImage);
            } else {
                Log.d(TAG, "Glide load (placeholder) for pos " + position + " because imageUrl invalid/empty");
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.placeholder_product)
                        .into(holder.ivImage);
            }
        } catch (Exception e) {
            Log.e(TAG, "Glide failed for pos " + position + ": " + e.getMessage(), e);
            holder.ivImage.setImageResource(R.drawable.placeholder_product);
        }
    }

    @Override
    public int getItemCount() {
        int cnt = (orderItems != null) ? orderItems.size() : 0;
        Log.v(TAG, "getItemCount() = " + cnt);
        return cnt;
    }

    @Override
    public void onViewRecycled(@NonNull OrderViewHolder holder) {
        super.onViewRecycled(holder);
        Log.d(TAG, "onViewRecycled() position=" + holder.getAdapterPosition() + " / viewHash=" + holder.hashCode());
    }

    @Override
    public void onViewAttachedToWindow(@NonNull OrderViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        Log.d(TAG, "onViewAttachedToWindow() position=" + holder.getAdapterPosition());
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull OrderViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        Log.d(TAG, "onViewDetachedFromWindow() position=" + holder.getAdapterPosition());
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvVariant, tvQuantity, tvPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvVariant = itemView.findViewById(R.id.tv_product_variant);
            tvQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }

    // Format tiền kiểu 1.500.000 đ
    private String formatCurrency(int amount) {
        try {
            return String.format("%,d đ", amount).replace(",", ".");
        } catch (Exception e) {
            Log.w(TAG, "formatCurrency failed for amount=" + amount + ": " + e.getMessage());
            return amount + " đ";
        }
    }

    private String safeString(String s) {
        return s != null ? s : "";
    }
}
