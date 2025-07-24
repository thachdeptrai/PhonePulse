package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Base64; // Import Base64
import android.util.Log; // Import Log
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
import com.bumptech.glide.load.engine.DiskCacheStrategy; // Import DiskCacheStrategy
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.ProductGirdItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemProduct_ADAPTER extends RecyclerView.Adapter<ItemProduct_ADAPTER.ProductViewHolder> {

    private static final String TAG = "ProductAdapter"; // Tag cho Logcat

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

    public void setData(List<ProductGirdItem> newProductList) {
        this.productList = newProductList;
        notifyDataSetChanged();
        Log.d(TAG, "setData: Product list updated. New size: " + newProductList.size());
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
        Log.d(TAG, "onCreateViewHolder: ViewHolder created.");
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductGirdItem currentItem = productList.get(position);
        Log.d(TAG, "onBindViewHolder: Binding item at position " + position + " - Product: " + currentItem.getProduct_name());

        // --- 1. Tải ảnh sản phẩm ---
        String imageUrl = currentItem.getImage_url();
        Log.d(TAG, "onBindViewHolder: Product Image URL for " + currentItem.getProduct_name() + ": " + imageUrl);

        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // Đây là URL web thông thường
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache ảnh để tải nhanh hơn
                        .into(holder.ivProductImage);
                Log.d(TAG, "onBindViewHolder: Glide loading URL image for " + currentItem.getProduct_name() + " from URL: " + imageUrl);
            } else if (imageUrl.startsWith("data:image/")) {
                // Đây là chuỗi Base64
                try {
                    // Tách phần Base64 ra khỏi tiền tố "data:image/jpeg;base64,"
                    String base64Image = imageUrl.substring(imageUrl.indexOf(",") + 1);
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Glide.with(context)
                            .load(decodedString) // Tải từ byte array
                            .placeholder(R.drawable.placeholder_product)
                            .error(R.drawable.placeholder_product)
                            .into(holder.ivProductImage);
                    Log.d(TAG, "onBindViewHolder: Glide loading Base64 image for " + currentItem.getProduct_name());
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "onBindViewHolder: Invalid Base64 string for " + currentItem.getProduct_name(), e);
                    holder.ivProductImage.setImageResource(R.drawable.placeholder_product); // Fallback nếu Base64 lỗi
                }
            } else {
                // Đây là một đường dẫn file cục bộ hoặc định dạng không xác định
                // Nếu bạn có ảnh cục bộ trong thư mục drawable, bạn có thể kiểm tra ở đây:
                // Ví dụ: if (imageUrl.equals("local_image_name")) { holder.ivProductImage.setImageResource(R.drawable.local_image_name); }
                // Hiện tại, chúng ta sẽ mặc định dùng placeholder nếu không phải URL hoặc Base64
                holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
                Log.w(TAG, "onBindViewHolder: Unrecognized image URL format for " + currentItem.getProduct_name() + ". Using placeholder. URL: " + imageUrl);
            }
        } else {
            // URL ảnh là null hoặc rỗng, dùng placeholder
            holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
            Log.w(TAG, "onBindViewHolder: Image URL is null or empty for " + currentItem.getProduct_name() + ". Using placeholder.");
        }

        // --- 2. Tên sản phẩm ---
        holder.tvProductName.setText(currentItem.getProduct_name() != null ? currentItem.getProduct_name() : "");
        Log.d(TAG, "onBindViewHolder: Product Name: " + holder.tvProductName.getText());

        // --- 3. Kích thước và Màu sắc ---
        boolean hasSize = currentItem.getSize_name() != null && !currentItem.getSize_name().isEmpty();
        boolean hasColor = currentItem.getColor_name() != null && !currentItem.getColor_name().isEmpty();

        Log.d(TAG, "onBindViewHolder: Product Size: " + currentItem.getSize_name() + ", Color: " + currentItem.getColor_name());

        if (hasSize) {
            holder.tvProductSize.setText(currentItem.getSize_name());
            holder.tvProductSize.setVisibility(View.VISIBLE);
        } else {
            holder.tvProductSize.setText("");
            holder.tvProductSize.setVisibility(View.GONE);
        }

        if (hasColor) {
            holder.tvProductColor.setText(currentItem.getColor_name());
            holder.tvProductColor.setVisibility(View.VISIBLE);
        } else {
            holder.tvProductColor.setText("");
            holder.tvProductColor.setVisibility(View.GONE);
        }

        if (!hasSize && !hasColor) {
            holder.llSizeColorContainer.setVisibility(View.GONE);
            Log.d(TAG, "onBindViewHolder: Hiding size/color container for " + currentItem.getProduct_name());
        } else {
            holder.llSizeColorContainer.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder: Showing size/color container for " + currentItem.getProduct_name());
        }

        // --- 4. Định dạng và hiển thị giá ---
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);

        holder.tvDiscountPrice.setText(currencyFormat.format(currentItem.getPrice()));
        Log.d(TAG, "onBindViewHolder: Discount Price: " + currentItem.getPrice());

        // --- 5. Giá gốc và Phần trăm giảm giá ---
        if (currentItem.getDiscount_percent() > 0 && currentItem.getOriginal_price() > currentItem.getPrice()) {
            holder.tvOriginalPrice.setText(currencyFormat.format(currentItem.getOriginal_price()));
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);

            holder.tvDiscountPercent.setText("-" + currentItem.getDiscount_percent() + "%");
            holder.tvDiscountPercent.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder: Original Price: " + currentItem.getOriginal_price() + ", Discount Percent: " + currentItem.getDiscount_percent());
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvDiscountPercent.setVisibility(View.GONE);
            Log.d(TAG, "onBindViewHolder: No discount for " + currentItem.getProduct_name());
        }

        // --- 6. Số lượng đã bán ---
        if (currentItem.getSold_count() >= 0) {
            holder.tvSold.setText("Đã bán " + currentItem.getSold_count());
            holder.tvSold.setVisibility(View.VISIBLE);
            Log.d(TAG, "onBindViewHolder: Sold Count: " + currentItem.getSold_count());
        } else {
            holder.tvSold.setText("");
            holder.tvSold.setVisibility(View.INVISIBLE);
            Log.d(TAG, "onBindViewHolder: Sold Count not available for " + currentItem.getProduct_name());
        }

        // --- 7. Xử lý sự kiện click cho nút "Thêm vào giỏ" ---
        holder.btnAddtoCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(currentItem);
                Log.d(TAG, "onBindViewHolder: Add to Cart button clicked for " + currentItem.getProduct_name());
            }
        });

        // --- 8. Xử lý sự kiện click cho toàn bộ item (CardView) ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentItem);
                Log.d(TAG, "onBindViewHolder: Item clicked for " + currentItem.getProduct_name());
            }
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        LinearLayout llSizeColorContainer;
        TextView tvProductSize;
        TextView tvProductColor;
        TextView tvDiscountPrice;
        TextView tvOriginalPrice;
        TextView tvDiscountPercent;
        TextView tvSold;
        Button btnAddtoCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            llSizeColorContainer = itemView.findViewById(R.id.ll_size_color_container);
            tvProductSize = itemView.findViewById(R.id.tv_product_size);
            tvProductColor = itemView.findViewById(R.id.tv_product_color);
            tvDiscountPrice = itemView.findViewById(R.id.tv_discount_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
            tvSold = itemView.findViewById(R.id.tv_sold);
            btnAddtoCart = itemView.findViewById(R.id.btn_add_to_cart);
            Log.d(TAG, "ProductViewHolder: Views initialized.");
        }
    }
}