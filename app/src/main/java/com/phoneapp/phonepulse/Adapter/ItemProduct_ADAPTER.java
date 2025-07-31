package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.ProductGirdItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemProduct_ADAPTER extends RecyclerView.Adapter<ItemProduct_ADAPTER.ProductViewHolder> {

    private static final String TAG = "ItemProductAdapter";
    private List<ProductGirdItem> productList;
    private Context context;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onAddToCartClick(ProductGirdItem item);
        void onItemClick(ProductGirdItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public ItemProduct_ADAPTER(List<ProductGirdItem> productList) {
        this.productList = productList;
    }

    public void setData(List<ProductGirdItem> newList) {
        this.productList = newList;
        notifyDataSetChanged();
        Log.d(TAG, "setData: Updated list with size = " + newList.size());
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductGirdItem item = productList.get(position);
        Log.d(TAG, "Binding product at position " + position + ": " + item.getProduct_name());

        // --- Load Image ---
        String imageUrl = null;
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            imageUrl = item.getImages().get(0).getImageUrl();
        } else {
            imageUrl = item.getImage_url(); // fallback
        }

        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.ivProductImage);
                Log.d(TAG, "Loaded image from URL: " + imageUrl);
            } else if (imageUrl.startsWith("data:image/")) {
                try {
                    String base64 = imageUrl.substring(imageUrl.indexOf(",") + 1);
                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                    Glide.with(context)
                            .load(decoded)
                            .placeholder(R.drawable.placeholder_product)
                            .error(R.drawable.placeholder_product)
                            .into(holder.ivProductImage);
                    Log.d(TAG, "Loaded image from Base64.");
                } catch (Exception e) {
                    Log.e(TAG, "Error decoding Base64 image", e);
                    holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
                }
            } else if (imageUrl.startsWith("/uploads/")) {
                String localUrl = "http://10.0.2.2:5000" + imageUrl;
                Glide.with(context)
                        .load(localUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.ivProductImage);
                Log.d(TAG, "Loaded image from local uploads: " + localUrl);
            } else {
                Log.w(TAG, "Unknown image format: " + imageUrl);
                holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
            }
        } else {
            Log.w(TAG, "Image URL is empty");
            holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
        }

        // --- Product Name ---
        holder.tvProductName.setText(item.getProduct_name() != null ? item.getProduct_name() : "");

        // --- Size & Color ---
        boolean hasSize = !TextUtils.isEmpty(item.getSize_name());
        boolean hasColor = !TextUtils.isEmpty(item.getColor_name());

        holder.tvProductSize.setVisibility(hasSize ? View.VISIBLE : View.GONE);
        holder.tvProductSize.setText(hasSize ? item.getSize_name() : "");

        holder.tvProductColor.setVisibility(hasColor ? View.VISIBLE : View.GONE);
        holder.tvProductColor.setText(hasColor ? item.getColor_name() : "");

        holder.llSizeColorContainer.setVisibility((hasSize || hasColor) ? View.VISIBLE : View.GONE);

        // --- Price Formatting ---
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);
        holder.tvDiscountPrice.setText(nf.format(item.getPrice()));

        if (item.getDiscount_percent() > 0 && item.getOriginal_price() > item.getPrice()) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText(nf.format(item.getOriginal_price()));
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            holder.tvDiscountPercent.setVisibility(View.VISIBLE);
            holder.tvDiscountPercent.setText("-" + item.getDiscount_percent() + "%");
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvDiscountPercent.setVisibility(View.GONE);
        }

        // --- Sold Count ---
        if (item.getSold_count() >= 0) {
            holder.tvSold.setText("Đã bán " + item.getSold_count());
            holder.tvSold.setVisibility(View.VISIBLE);
        } else {
            holder.tvSold.setVisibility(View.INVISIBLE);
        }

        // --- Click Listeners ---
        holder.btnAddtoCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCartClick(item);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductSize, tvProductColor, tvDiscountPrice, tvOriginalPrice, tvDiscountPercent, tvSold;
        Button btnAddtoCart;
        LinearLayout llSizeColorContainer;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductSize = itemView.findViewById(R.id.tv_product_size);
            tvProductColor = itemView.findViewById(R.id.tv_product_color);
            tvDiscountPrice = itemView.findViewById(R.id.tv_discount_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
            tvSold = itemView.findViewById(R.id.tv_sold);
            btnAddtoCart = itemView.findViewById(R.id.btn_add_to_cart);
            llSizeColorContainer = itemView.findViewById(R.id.ll_size_color_container);
        }
    }
}
