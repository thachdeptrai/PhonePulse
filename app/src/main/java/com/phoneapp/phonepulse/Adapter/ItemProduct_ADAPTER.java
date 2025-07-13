package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.ui.product.ProductDetailActivity; // Đảm bảo đúng package
import com.phoneapp.phonepulse.utils.Constants;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItemProduct_ADAPTER extends RecyclerView.Adapter<ItemProduct_ADAPTER.ProductViewHolder> {

    private static final String TAG = "ItemProduct_ADAPTER";

    private Context context;
    private List<Variant> variantList;

    public ItemProduct_ADAPTER(Context context, List<Variant> variantList) {
        this.context = context;
        this.variantList = variantList != null ? variantList : new ArrayList<>();
        Log.d(TAG, "Adapter initialized with " + this.variantList.size() + " variants.");
    }

    public void setData(List<Variant> newData) {
        this.variantList.clear();
        if (newData != null) {
            this.variantList.addAll(newData);
        }
        notifyDataSetChanged();
        Log.d(TAG, "setData: Adapter data updated. New size: " + variantList.size());
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false); // Changed to item_product
        Log.d(TAG, "onCreateViewHolder: New ViewHolder created from R.layout.item_product.");
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Variant variant = variantList.get(position);

        if (variant == null) {
            Log.w(TAG, "onBindViewHolder: Variant at position " + position + " is null.");
            return;
        }

        Log.d(TAG, "onBindViewHolder: Binding variant at position " + position + ": " + variant.getProductName());

        // 1. Set Product Name (lấy từ Variant)
        holder.tvProductName.setText(variant.getProductName() != null ? variant.getProductName() : "Tên không xác định");
        Log.d(TAG, "Product Name (from Variant): " + variant.getProductName());

        // 2. Set Product Image using Glide (lấy từ Variant)
        String imageUrl = variant.getImageUrl();

        // Kiểm tra ivProductImage trước khi sử dụng
        if (holder.ivProductImage != null) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(holder.ivProductImage);
                Log.d(TAG, "Variant Image URL: " + imageUrl);
            } else {
                holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
                Log.w(TAG, "Variant " + variant.getProductName() + " has no image URL. Using placeholder.");
            }
        } else {
            Log.e(TAG, "onBindViewHolder: ivProductImage is NULL for variant " + variant.getProductName() + ". Cannot set image.");
        }


        // 3. Handle Price (loại bỏ hoàn toàn logic discount)
        double price = variant.getPrice();
        if (holder.tvDiscountPrice != null) {
            holder.tvDiscountPrice.setText(formatCurrency(price));
            holder.tvDiscountPrice.setVisibility(View.VISIBLE);
        } else {
            Log.e(TAG, "onBindViewHolder: tvDiscountPrice is NULL for variant " + variant.getProductName() + ". Cannot set price.");
        }

        if (holder.tvOriginalPrice != null) {
            holder.tvOriginalPrice.setVisibility(View.GONE);
        }
        if (holder.tvDiscountPercent != null) {
            holder.tvDiscountPercent.setVisibility(View.GONE);
        }
        Log.d(TAG, "Displaying price (from Variant): " + price);

        // 4. Set Sold Count (vẫn ẩn)
        if (holder.tvSold != null) {
            holder.tvSold.setVisibility(View.GONE);
        }
        Log.d(TAG, "Sold count TextView is set to GONE.");

        // 5. Button Add to Cart Listener (Giữ nguyên)
        if (holder.btnAddToCart != null) {
            holder.btnAddToCart.setOnClickListener(v -> {
                Toast.makeText(context, "Thêm " + variant.getProductName() + " vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Add to cart button clicked for " + variant.getProductName());
            });
        } else {
            Log.e(TAG, "onBindViewHolder: btnAddToCart is NULL for variant " + variant.getProductName() + ". Cannot set click listener.");
        }


        // ======================================================================================
        // SỬA ĐỔI QUAN TRỌNG TẠI ĐÂY: Handle item click (cho chi tiết variant)
        // Đảm bảo truyền cả VARIANT_ID và PRODUCT_ID
        // ======================================================================================
        holder.itemView.setOnClickListener(v -> {
            String currentVariantId = variant.getId();
            String currentProductId = variant.getProductId(); // Lấy productId từ Variant object

            // Log giá trị ID trước khi truyền
            Log.d(TAG, "Item click: Attempting to open ProductDetailActivity.");
            Log.d(TAG, "Item click: Variant ID to pass: " + currentVariantId);
            Log.d(TAG, "Item click: Product ID to pass: " + currentProductId);


            if (currentVariantId != null && currentProductId != null) { // Đảm bảo cả hai ID không null
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra(Constants.VARIANT_ID, currentVariantId); // Truyền variant ID
                intent.putExtra(Constants.PRODUCT_ID, currentProductId); // TRUYỀN PRODUCT ID
                context.startActivity(intent);
                Log.d(TAG, "Item clicked: Opening ProductDetailActivity for Variant ID: " + currentVariantId + ", Product ID: " + currentProductId);
            } else {
                String missingId = (currentVariantId == null) ? "Variant ID" : "Product ID";
                Toast.makeText(context, "Không thể mở chi tiết sản phẩm: " + missingId + " không hợp lệ.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Item click failed: " + missingId + " is null for " + variant.getProductName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return variantList != null ? variantList.size() : 0;
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvDiscountPrice;
        TextView tvDiscountPercent;
        TextView tvOriginalPrice;
        TextView tvSold;
        Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            // Khởi tạo và kiểm tra từng View
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            if (ivProductImage == null) {
                Log.e(TAG, "ProductViewHolder: ivProductImage (R.id.iv_product_image) is NULL!");
            }

            tvProductName = itemView.findViewById(R.id.tv_product_name);
            if (tvProductName == null) {
                Log.e(TAG, "ProductViewHolder: tvProductName (R.id.tv_product_name) is NULL!");
            }

            tvDiscountPrice = itemView.findViewById(R.id.tv_discount_price);
            if (tvDiscountPrice == null) {
                Log.e(TAG, "ProductViewHolder: tvDiscountPrice (R.id.tv_discount_price) is NULL!");
            }

            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
            if (tvDiscountPercent == null) {
                Log.e(TAG, "ProductViewHolder: tvDiscountPercent (R.id.tv_discount_percent) is NULL!");
            }

            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            if (tvOriginalPrice == null) {
                Log.e(TAG, "ProductViewHolder: tvOriginalPrice (R.id.tv_original_price) is NULL!");
            }

            tvSold = itemView.findViewById(R.id.tv_sold);
            if (tvSold == null) {
                Log.e(TAG, "ProductViewHolder: tvSold (R.id.tv_sold) is NULL!");
            }

            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
            if (btnAddToCart == null) {
                Log.e(TAG, "ProductViewHolder: btnAddToCart (R.id.btn_add_to_cart) is NULL!");
            }

            Log.d(TAG, "ProductViewHolder initialized. All views checked for null.");
        }
    }

    private String formatCurrency(double amount) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String formatted = formatter.format(amount);
        formatted = formatted.replaceAll("(\\,|\\.)00(\\s*₫)?$", "$2");
        return formatted;
    }
}
