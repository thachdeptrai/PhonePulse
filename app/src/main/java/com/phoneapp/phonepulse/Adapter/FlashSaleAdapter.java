package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.ui.product.ProductDetailActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FlashSaleAdapter extends RecyclerView.Adapter<FlashSaleAdapter.FlashSaleViewHolder> {

    private final Context context;
    private List<Product> flashSaleProducts;
    private final NumberFormat numberFormat;

    public FlashSaleAdapter(Context context, List<Product> flashSaleProducts) {
        this.context = context;
        this.flashSaleProducts = flashSaleProducts;
        this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public FlashSaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flash_sale, parent, false);
        return new FlashSaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashSaleViewHolder holder, int position) {
        Product product = flashSaleProducts.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return flashSaleProducts != null ? flashSaleProducts.size() : 0;
    }

    public void updateData(List<Product> newList) {
        this.flashSaleProducts = newList;
        notifyDataSetChanged();
    }

    class FlashSaleViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardFlashSale;
        private final ImageView ivFlashSaleImage;
        private final TextView tvFlashSalePrice, tvFlashSaleDiscount, tvSoldCount;
        private final ProgressBar progressSold;

        public FlashSaleViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFlashSale = itemView.findViewById(R.id.card_flash_sale);
            ivFlashSaleImage = itemView.findViewById(R.id.iv_flash_sale_image);
            tvFlashSalePrice = itemView.findViewById(R.id.tv_flash_sale_price);
            tvFlashSaleDiscount = itemView.findViewById(R.id.tv_flash_sale_discount);
            progressSold = itemView.findViewById(R.id.progress_sold);
            tvSoldCount = itemView.findViewById(R.id.tv_sold_count);
        }

        public void bind(Product product) {

            // Load image
            if (product.getProductImage() != null) {
                Glide.with(context)
                        .load(product.getProductImage().getImageUrl())
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(ivFlashSaleImage);
            }

            // Giá và giảm giá
            int discountPercent = product.getDiscount();
            double price = 0;
            int stock = 0;

            Variant variant = (Variant) product.getVariants();
            if (variant != null) {
                price = variant.getPrice();
                stock = variant.getQuantity();
            }

            double discountedPrice = price * (100 - discountPercent) / 100;

            tvFlashSalePrice.setText("₫" + numberFormat.format(discountedPrice));
            tvFlashSaleDiscount.setText("-" + discountPercent + "%");

            // Tiến trình đã bán
            int soldCount = (int) (Math.random() * stock * 0.7); // Dữ liệu demo
            int progress = stock > 0 ? (soldCount * 100) / stock : 0;

            progressSold.setProgress(progress);
            tvSoldCount.setText("Đã bán " + soldCount);

            // Click sang Product Detail
            cardFlashSale.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product_id", product.getId());
                context.startActivity(intent);
            });
        }
    }
}
