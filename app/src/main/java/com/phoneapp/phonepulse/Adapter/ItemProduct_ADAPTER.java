package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Sử dụng Button thay vì MaterialButton nếu bạn không có thư viện Material Components
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.ProductGirdItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ItemProduct_ADAPTER extends RecyclerView.Adapter<ItemProduct_ADAPTER.ProductViewHolder> {

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
        ProductGirdItem currentItem = productList.get(position);

        // --- 1. Tải ảnh sản phẩm ---
        if (currentItem.getImage_url() != null && !currentItem.getImage_url().isEmpty()) {
            Glide.with(context)
                    .load(currentItem.getImage_url())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
        }

        // --- 2. Tên sản phẩm ---
        // Giữ nguyên setText, vì minLines đã xử lý khoảng trống
        holder.tvProductName.setText(currentItem.getProduct_name() != null ? currentItem.getProduct_name() : "");

        // --- 3. Kích thước và Màu sắc ---
        boolean hasSize = currentItem.getSize_name() != null && !currentItem.getSize_name().isEmpty();
        boolean hasColor = currentItem.getColor_name() != null && !currentItem.getColor_name().isEmpty();

        if (hasSize) {
            holder.tvProductSize.setText(currentItem.getSize_name());
            holder.tvProductSize.setVisibility(View.VISIBLE);
        } else {
            holder.tvProductSize.setText(""); // Đặt rỗng để không hiển thị văn bản cũ
            holder.tvProductSize.setVisibility(View.GONE);
        }

        if (hasColor) {
            holder.tvProductColor.setText(currentItem.getColor_name());
            holder.tvProductColor.setVisibility(View.VISIBLE);
        } else {
            holder.tvProductColor.setText(""); // Đặt rỗng
            holder.tvProductColor.setVisibility(View.GONE);
        }

        // Ẩn/hiện container cho size và color nếu cả hai đều không có dữ liệu
        if (!hasSize && !hasColor) {
            holder.llSizeColorContainer.setVisibility(View.GONE);
        } else {
            holder.llSizeColorContainer.setVisibility(View.VISIBLE);
        }

        // --- 4. Định dạng và hiển thị giá ---
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);

        holder.tvDiscountPrice.setText(currencyFormat.format(currentItem.getPrice()));

        // --- 5. Giá gốc và Phần trăm giảm giá ---
        if (currentItem.getDiscount_percent() > 0 && currentItem.getOriginal_price() > currentItem.getPrice()) {
            holder.tvOriginalPrice.setText(currencyFormat.format(currentItem.getOriginal_price()));
            holder.tvOriginalPrice.setPaintFlags(holder.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvOriginalPrice.setVisibility(View.VISIBLE);

            holder.tvDiscountPercent.setText("-" + currentItem.getDiscount_percent() + "%");
            holder.tvDiscountPercent.setVisibility(View.VISIBLE);
        } else {
            holder.tvOriginalPrice.setVisibility(View.GONE);
            holder.tvDiscountPercent.setVisibility(View.GONE);
        }

        // --- 6. Số lượng đã bán ---
        if (currentItem.getSold_count() >= 0) { // Kiểm tra giá trị hợp lệ
            holder.tvSold.setText("Đã bán " + currentItem.getSold_count());
            holder.tvSold.setVisibility(View.VISIBLE);
        } else {
            holder.tvSold.setText(""); // Đặt rỗng
            holder.tvSold.setVisibility(View.INVISIBLE); // Dùng INVISIBLE để giữ khoảng trống cho dòng này
        }


        // --- 7. Xử lý sự kiện click cho nút "Thêm vào giỏ" ---
        holder.btnAddtoCart.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAddToCartClick(currentItem);
            }
        });

        // --- 8. Xử lý sự kiện click cho toàn bộ item (CardView) ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(currentItem);
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
        Button btnAddtoCart; // Nếu dùng MaterialButton thì đổi thành MaterialButton

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
        }
    }
}