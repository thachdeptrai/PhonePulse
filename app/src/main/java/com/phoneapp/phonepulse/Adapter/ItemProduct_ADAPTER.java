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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.request.ProductGirdItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemProduct_ADAPTER extends RecyclerView.Adapter<ItemProduct_ADAPTER.ProductViewHolder> {

    private final List<ProductGirdItem> productList;
    private final Context context;
    private OnProductActionListener onProductActionListener;

    public interface OnProductActionListener {
        void onAddToCartClick(ProductGirdItem item);
        void onItemClick(ProductGirdItem item);
        void onVariantSelected(ProductGirdItem productItem, Variant selectedVariant);
    }

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.onProductActionListener = listener;
    }

    public ItemProduct_ADAPTER(Context context, List<ProductGirdItem> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductGirdItem item = productList.get(position);

        // --- Load Image ---
        String imageUrl = null;
        if (item.getImages() != null && !item.getImages().isEmpty()) {
            imageUrl = item.getImages().get(0).getImageUrl();
        } else {
            imageUrl = item.getImage_url();
        }

        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.ivProductImage);

            } else if (imageUrl.startsWith("data:image/")) {
                try {
                    String base64 = imageUrl.substring(imageUrl.indexOf(",") + 1);
                    byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                    Glide.with(context)
                            .load(decoded)
                            .placeholder(R.drawable.placeholder_product)
                            .error(R.drawable.placeholder_product)
                            .into(holder.ivProductImage);
                } catch (Exception e) {
                    holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
                }

            } else if (imageUrl.startsWith("/uploads/")) {
                String localUrl = "http://10.24.60.244:5000" + imageUrl;
                Glide.with(context)
                        .load(localUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.ivProductImage);

            } else {
                holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
            }
        } else {
            holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
        }

        // --- Product Name ---
        holder.tvProductName.setText(item.getProduct_name() != null ? item.getProduct_name() : "");

        // --- Variants RecyclerView ---
        List<Variant> variants = item.getVariants();
        if (variants != null && !variants.isEmpty()) {
            holder.rvProductVariants.setVisibility(View.VISIBLE);
            VariantAdapter variantAdapter = new VariantAdapter(
                    variants,
                    selectedVariant -> {
                        if (onProductActionListener != null) {
                            onProductActionListener.onVariantSelected(item, selectedVariant);
                        }
                    });
            holder.rvProductVariants.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.rvProductVariants.setAdapter(variantAdapter);
        } else {
            holder.rvProductVariants.setVisibility(View.GONE);
        }

        // --- Price & Discount ---
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);

        holder.tvDiscountPrice.setText(nf.format(item.getPrice()));

        if (item.getDiscount_percent() > 0 && item.getOriginal_price() > item.getPrice()) {
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);
            holder.tvOriginalPrice.setText(nf.format(item.getOriginal_price()));
            holder.tvOriginalPrice.setPaintFlags(
                    holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

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
            if (onProductActionListener != null) {
                onProductActionListener.onAddToCartClick(item);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (onProductActionListener != null) {
                onProductActionListener.onItemClick(item);
            }
        });
        Log.d("API_DATA", "Product: " + new Gson().toJson(productList));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvDiscountPrice, tvOriginalPrice, tvDiscountPercent, tvSold;
        Button btnAddtoCart;
        RecyclerView rvProductVariants;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvDiscountPrice = itemView.findViewById(R.id.tv_discount_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
            tvSold = itemView.findViewById(R.id.tv_sold);
            btnAddtoCart = itemView.findViewById(R.id.btn_add_to_cart);
            rvProductVariants = itemView.findViewById(R.id.rv_product_variants);
        }
    }
}
