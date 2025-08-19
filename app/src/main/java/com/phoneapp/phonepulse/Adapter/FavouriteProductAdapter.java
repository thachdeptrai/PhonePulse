package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FavouriteProductAdapter extends RecyclerView.Adapter<FavouriteProductAdapter.FavouriteViewHolder> {

    private List<Product> favouriteProducts;
    private final Context context;
    private final OnItemClickListener listener;
    private final NumberFormat numberFormat;

    public interface OnItemClickListener {
        // Updated to include variantId
        void onItemClick(Product product, String variantId);
    }

    public FavouriteProductAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        this.favouriteProducts = new ArrayList<>();
        this.listener = listener;
        this.numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        this.numberFormat.setMaximumFractionDigits(0); // Không hiển thị phần thập phân
    }

    public void setData(List<Product> products) {
        this.favouriteProducts.clear();
        if (products != null) {
            this.favouriteProducts.addAll(products);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FavouriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favourite_product, parent, false);
        return new FavouriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavouriteViewHolder holder, int position) {
        Product product = favouriteProducts.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return favouriteProducts.size();
    }

    class FavouriteViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        // TextView tvProductOriginalPrice; // Uncomment if you add this to item_favourite_product.xml
        // TextView tvDiscountPercent;    // Uncomment if you add this to item_favourite_product.xml

        public FavouriteViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_favourite_product_image);
            tvProductName = itemView.findViewById(R.id.tv_favourite_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_favourite_product_price);
            // tvProductOriginalPrice = itemView.findViewById(R.id.tv_favourite_product_original_price);
            // tvDiscountPercent = itemView.findViewById(R.id.tv_favourite_discount_percent);
        }

        public void bind(final Product product, final OnItemClickListener listener) {
            tvProductName.setText(product.getName());

            String firstVariantId = null;
            double priceFromVariant = 0;
            boolean hasVariants = product.getVariants() != null && !product.getVariants().isEmpty();

            if (hasVariants) {
                Variant firstVariant = product.getVariants().get(0);
                priceFromVariant = firstVariant.getPrice();
                firstVariantId = firstVariant.getId();
            }

            int discount = product.getDiscount();
            double priceToDisplay = priceFromVariant; // Default to price from the first variant

            // if (tvProductOriginalPrice != null) tvProductOriginalPrice.setVisibility(View.GONE); // Hide by default
            // if (tvDiscountPercent != null) tvDiscountPercent.setVisibility(View.GONE);       // Hide by default

            if (discount > 0 && hasVariants) {
                priceToDisplay = priceFromVariant * (100 - discount) / 100.0;
                // If you want to show original price and discount:
                // if (tvProductOriginalPrice != null) {
                //     tvProductOriginalPrice.setText(numberFormat.format(priceFromVariant));
                //     tvProductOriginalPrice.setPaintFlags(tvProductOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                //     tvProductOriginalPrice.setVisibility(View.VISIBLE);
                // }
                // if (tvDiscountPercent != null) {
                //     tvDiscountPercent.setText("-" + discount + "%");
                //     tvDiscountPercent.setVisibility(View.VISIBLE);
                // }
            } else if (hasVariants) {
                // No discount, display variant price directly
                priceToDisplay = priceFromVariant;
            } else {
                // No variants, or no discount.
                // Consider how to display price if no variants. For now, it will be 0 or based on priceFromVariant's default.
                // You might want to set a default text like "N/A"
            }

            if (hasVariants) {
                tvProductPrice.setText(numberFormat.format(priceToDisplay));
            } else {
                tvProductPrice.setText("N/A"); // Or some other placeholder if no variants
            }

            Glide.with(context)
                    .load(product.getImageUrlSafe())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(ivProductImage);

            final String finalFirstVariantId = firstVariantId;
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(product, finalFirstVariantId);
                }
            });
        }
    }
}
