package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.ui.product.ProductDetailActivity;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {
    private Context context;
    private List<Product> productList;
    private NumberFormat numberFormat;

    public ProductGridAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.productList = newProducts;
        notifyDataSetChanged();
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private CardView cardProduct;
        private ImageView ivProductImage;
        private TextView tvProductName;
        private TextView tvOriginalPrice;
        private TextView tvDiscountPrice;
        private TextView tvDiscountPercent;
        private TextView tvSold;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            cardProduct = itemView.findViewById(R.id.card_product);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscountPrice = itemView.findViewById(R.id.tv_discount_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
            tvSold = itemView.findViewById(R.id.tv_sold);
        }

        public void bind(Product product) {
            // Load product image
            Glide.with(context)
                    .load(product.getProductImage().getImageUrl())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(ivProductImage);

            // Set product name
            tvProductName.setText(product.getName());

            // Calculate and display prices
            double originalPrice = product.getVariantId().getPrice();
            int discount = product.getDiscount();

            if (discount > 0) {
                // Show discount
                tvDiscountPercent.setVisibility(View.VISIBLE);
                tvOriginalPrice.setVisibility(View.VISIBLE);

                double discountedPrice = originalPrice * (100 - discount) / 100;

                tvOriginalPrice.setText("₫" + numberFormat.format(Math.round(originalPrice)));
                tvDiscountPrice.setText("₫" + numberFormat.format(Math.round(discountedPrice)));
                tvDiscountPercent.setText("-" + discount + "%");

                // Strike through original price
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                // No discount
                tvDiscountPercent.setVisibility(View.GONE);
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountPrice.setText("₫" + numberFormat.format(originalPrice));
            }

            // Show sold count (random for demo - replace with actual data)
            int soldCount = (int) (Math.random() * 1000);
            tvSold.setText("Đã bán " + soldCount);

            // Click listener
            cardProduct.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra(Constants.PRODUCT_ID, product.getId());
                context.startActivity(intent);
                //log product id
                Log.d("ProductGridAdapter", "Clicked product ID: " + product.getId());
            });
        }
    }
}