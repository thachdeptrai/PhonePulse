package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Base64;
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
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.request.ProductGirdItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemProduct_ADAPTER extends RecyclerView.Adapter<ItemProduct_ADAPTER.ProductViewHolder> {

    private final List<ProductGirdItem> productList;
    private final Context context;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onAddToCartClick(ProductGirdItem item);
        void onItemClick(ProductGirdItem item);
        void onVariantSelected(ProductGirdItem product, Variant variant);
    }

    public ItemProduct_ADAPTER(Context context, List<ProductGirdItem> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setOnProductActionListener(OnProductActionListener listener) {
        this.listener = listener;
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

        // --- Load hình ảnh ---
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

        // --- Tên sản phẩm ---
        holder.tvProductName.setText(item.getProduct_name() != null ? item.getProduct_name() : "");

// --- Variants ---
        List<Variant> variants = item.getVariants();
        if (variants != null && !variants.isEmpty()) {
            holder.rvProductVariants.setVisibility(View.VISIBLE);
            VariantAdapter variantAdapter = new VariantAdapter(variants,
                    variant -> {
                        if (listener != null) listener.onVariantSelected(item, variant);
                    });
            holder.rvProductVariants.setLayoutManager(
                    new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            holder.rvProductVariants.setAdapter(variantAdapter);
        } else {
            holder.rvProductVariants.setVisibility(View.GONE);
        }

// --- Giá & Giảm giá ---
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        nf.setMaximumFractionDigits(0);

// Giá khuyến mãi luôn hiển thị
        holder.tvDiscountPrice.setText(nf.format(item.getPrice()));

// Kiểm tra giảm giá
        if (item.getDiscount_percent() > 0 && item.getOriginal_price() > item.getPrice()) {
            holder.tvOriginalPrice.setText(nf.format(item.getOriginal_price()));
            holder.tvOriginalPrice.setPaintFlags(
                    holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);

            holder.tvDiscountPercent.setText("-" + item.getDiscount_percent() + "%");
            holder.tvDiscountPercent.setVisibility(View.VISIBLE);
        } else {
            holder.tvOriginalPrice.setText("");
            holder.tvOriginalPrice.setVisibility(View.INVISIBLE); // giữ layout không dịch

            holder.tvDiscountPercent.setText("");
            holder.tvDiscountPercent.setVisibility(View.INVISIBLE); // giữ layout không dịch
        }

// --- Đã bán ---
        holder.tvSold.setText("Đã bán " + item.getSold_count());
// luôn hiển thị, kể cả = 0
        holder.tvSold.setVisibility(View.VISIBLE);

// --- Click ---
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

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvDiscountPrice, tvOriginalPrice, tvDiscountPercent, tvSold;
        Button btnAddtoCart;
        RecyclerView rvProductVariants;

        ProductViewHolder(@NonNull View itemView) {
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
