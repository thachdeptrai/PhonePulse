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
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.ProductGirdItem;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class SearchProductAdapter extends RecyclerView.Adapter<SearchProductAdapter.ProductViewHolder> {

    private static final String TAG = "SearchProductAdapter";
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

    public SearchProductAdapter(Context context, List<ProductGirdItem> productList) {
        this.context = context;
        this.productList = productList;
    }

    public void setData(List<ProductGirdItem> newList) {
        this.productList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_product, parent, false);
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
                    Log.e(TAG, "Error decoding Base64 image: " + e.getMessage());
                    holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
                }
            } else if (imageUrl.startsWith("/Uploads/")) {
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

        holder.btnAddToCart.setOnClickListener(v -> {
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
        TextView tvProductName, tvDiscountPrice, tvOriginalPrice, tvDiscountPercent, tvSold;
        Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_search_product_image);
            tvProductName = itemView.findViewById(R.id.tv_search_product_name);
            tvDiscountPrice = itemView.findViewById(R.id.tv_search_discount_price);
            tvOriginalPrice = itemView.findViewById(R.id.tv_search_original_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_search_discount_percent);
            tvSold = itemView.findViewById(R.id.tv_search_sold);
            btnAddToCart = itemView.findViewById(R.id.btn_search_add_to_cart);
        }
    }
}