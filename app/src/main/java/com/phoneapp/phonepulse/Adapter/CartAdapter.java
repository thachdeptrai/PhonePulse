package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.CartDisplayItem;
// Xóa các import Product, ProductImage, Variant vì chúng đã nằm trong CartDisplayItem
// import com.phoneapp.phonepulse.models.Product;
// import com.phoneapp.phonepulse.models.ProductImage;
// import com.phoneapp.phonepulse.models.Variant;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private List<CartDisplayItem> cartItems; // <-- Thay đổi kiểu dữ liệu
    private final CartItemActionCallback callback;
    private final NumberFormat numberFormat;

    public interface CartItemActionCallback {
        // Thay đổi tham số sang CartDisplayItem
        void onQuantityChanged(CartDisplayItem item, int newQuantity);
        void onRemoveItem(CartDisplayItem item);
        void onCartTotalChanged();
    }

    public CartAdapter(Context context, List<CartDisplayItem> cartItems, CartItemActionCallback callback) { // <-- Thay đổi kiểu dữ liệu
        this.context = context;
        this.cartItems = cartItems;
        this.callback = callback;
        this.numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    public void setCartItems(List<CartDisplayItem> cartItems) { // <-- Thay đổi kiểu dữ liệu
        this.cartItems = cartItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        com.phoneapp.phonepulse.request.CartDisplayItem item = cartItems.get(position); // <-- Sử dụng CartDisplayItem

        // Hiển thị ảnh sản phẩm
        if (item.getProductImageLUrl() != null && !item.getProductImageLUrl().isEmpty()) {
            Glide.with(context)
                    .load(item.getProductImageLUrl()) // Lấy từ CartDisplayItem
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(holder.ivProductImage);
        } else {
            holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
        }

        // Hiển thị tên sản phẩm
        holder.tvProductName.setText(item.getProductName()); // Lấy từ CartDisplayItem

        // Hiển thị chi tiết biến thể và giá
        // ĐÃ SỬA: Gọi item.getVariantPrice() mà không có tham số
        holder.tvProductPrice.setText(numberFormat.format(item.getVariantPrice())); // Lấy giá hiển thị từ CartDisplayItem

        StringBuilder variantDetails = new StringBuilder();
        if (item.getVariantColor() != null && !item.getVariantColor().isEmpty()) {
            variantDetails.append("Màu: ").append(item.getVariantColor());
        }
        if (item.getVariantSize() != null && !item.getVariantSize().isEmpty()) {
            if (variantDetails.length() > 0) {
                variantDetails.append(", ");
            }
            variantDetails.append("Kích thước: ").append(item.getVariantSize());
        }

        if (variantDetails.length() > 0) {
            holder.tvVariantDetails.setVisibility(View.VISIBLE);
            holder.tvVariantDetails.setText(variantDetails.toString());
        } else {
            holder.tvVariantDetails.setVisibility(View.GONE);
        }

        // Hiển thị số lượng
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        // --- Xử lý sự kiện tăng/giảm số lượng ---
        holder.btnDecrease.setOnClickListener(v -> {
            int currentQuantity = item.getQuantity();
            if (currentQuantity > 1) {
                item.setQuantity(currentQuantity - 1); // Cập nhật số lượng trong đối tượng CartDisplayItem
                holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
                callback.onQuantityChanged(item, item.getQuantity()); // Thông báo cho ViewModel
            } else {
                showRemoveConfirmationDialog(item);
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1); // Cập nhật số lượng trong đối tượng CartDisplayItem
            holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
            callback.onQuantityChanged(item, item.getQuantity()); // Thông báo cho ViewModel
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvVariantDetails;
        TextView tvProductPrice;
        ImageView btnDecrease;
        TextView tvQuantity;
        ImageView btnIncrease;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvVariantDetails = itemView.findViewById(R.id.tv_variant_details);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
        }
    }

    private void showRemoveConfirmationDialog(final com.phoneapp.phonepulse.request.CartDisplayItem item) { // <-- Thay đổi tham số
        String productName = item.getProductName(); // Lấy tên từ CartDisplayItem
        if (productName == null || productName.isEmpty()) {
            productName = "sản phẩm này";
        }

        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa sản phẩm")
                .setMessage("Bạn có muốn xóa sản phẩm \"" + productName + "\" khỏi giỏ hàng không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    callback.onRemoveItem(item);
                    Toast.makeText(context, "Đang xóa sản phẩm...", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}