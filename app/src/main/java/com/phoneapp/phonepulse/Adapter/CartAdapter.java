package com.phoneapp.phonepulse.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64; // Thêm import Base64
import android.util.Log; // Thêm import Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.CartItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private static final String TAG = "CartAdapter"; // Thêm TAG cho Logcat

    private List<CartItem> cartItemList;
    private Context context;
    private OnCartItemActionListener listener;

    public interface OnCartItemActionListener {
        void onQuantityChange(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
        void onItemSelected(CartItem item, boolean isSelected);
    }

    public void setOnCartItemActionListener(OnCartItemActionListener listener) {
        this.listener = listener;
    }

    public CartAdapter(List<CartItem> cartItemList) {
        this.cartItemList = cartItemList;
    }

    public void setCartItemList(List<CartItem> newCartItemList) {
        this.cartItemList = newCartItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem currentItem = cartItemList.get(position);

        // --- 1. Tải ảnh sản phẩm ---
        // Lấy URL ảnh trực tiếp từ trường productImage trong CartItem
        String imageUrl = currentItem.getProductImage(); // <-- THAY ĐỔI Ở ĐÂY



        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                // Đây là URL web thông thường
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(holder.ivProductImage);

            } else if (imageUrl.startsWith("data:image/")) {
                // Đây là chuỗi Base64
                try {
                    String base64Image = imageUrl.substring(imageUrl.indexOf(",") + 1);
                    byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                    Glide.with(context)
                            .load(decodedString)
                            .placeholder(R.drawable.placeholder_product)
                            .error(R.drawable.placeholder_product)
                            .into(holder.ivProductImage);

                } catch (IllegalArgumentException e) {

                    holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
                }
            } else {
                // Các định dạng khác hoặc đường dẫn cục bộ không được hỗ trợ trực tiếp
                holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
                Log.w(TAG, "onBindViewHolder: Unrecognized image URL format for " + (currentItem.getProduct() != null ? currentItem.getProduct().getProductName() : "N/A") + ". Using placeholder. URL: " + imageUrl);
            }
        } else {
            holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
            Log.w(TAG, "onBindViewHolder: Image URL is null or empty for " + (currentItem.getProduct() != null ? currentItem.getProduct().getProductName() : "N/A") + ". Using placeholder.");
        }

        // 2. Tên sản phẩm
        holder.tvProductName.setText(currentItem.getProduct() != null ? currentItem.getProduct().getProductName() : "Sản phẩm không xác định");

        // 3. Chi tiết biến thể (Màu, Kích thước, Bộ nhớ, RAM)
        StringBuilder variantDetails = new StringBuilder();
        if (currentItem.getVariant() != null) {
            if (currentItem.getVariant().getColor() != null && !TextUtils.isEmpty(currentItem.getVariant().getColor().getColorName())) {
                variantDetails.append("Màu: ").append(currentItem.getVariant().getColor().getColorName());
            }
            if (currentItem.getVariant().getSize() != null) {
                if (!TextUtils.isEmpty(currentItem.getVariant().getSize().getSizeName())) {
                    if (variantDetails.length() > 0) variantDetails.append(", ");
                    variantDetails.append("Kích thước: ").append(currentItem.getVariant().getSize().getSizeName());
                }
                if (!TextUtils.isEmpty(currentItem.getVariant().getSize().getStorage())) {
                    if (variantDetails.length() > 0) variantDetails.append(", ");
                    variantDetails.append("Bộ nhớ: ").append(currentItem.getVariant().getSize().getStorage());
                }
                if (!TextUtils.isEmpty(currentItem.getVariant().getSize().getRam())) {
                    if (variantDetails.length() > 0) variantDetails.append(", ");
                    variantDetails.append("RAM: ").append(currentItem.getVariant().getSize().getRam());
                }
            }
        }

        if (variantDetails.length() > 0) {
            holder.tvVariantDetails.setText(variantDetails.toString());
            holder.tvVariantDetails.setVisibility(View.VISIBLE);
        } else {
            holder.tvVariantDetails.setVisibility(View.GONE);
        }

        // 4. Giá sản phẩm (giá của biến thể)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);
        holder.tvProductPrice.setText(currencyFormat.format(currentItem.getVariant() != null ? currentItem.getVariant().getPrice() : 0));

        // 5. Số lượng
        holder.tvQuantity.setText(String.valueOf(currentItem.getQuantity()));

        // 6. Xử lý sự kiện tăng/giảm số lượng
        holder.btnDecrease.setOnClickListener(v -> {
            if (listener != null) {
                int currentQuantity = currentItem.getQuantity();
                if (currentQuantity > 1) {
                    listener.onQuantityChange(currentItem, currentQuantity - 1);
                } else {
                    showRemoveConfirmationDialog(currentItem);
                }
            }
        });

        holder.btnIncrease.setOnClickListener(v -> {
            if (listener != null) {
                int currentQuantity = currentItem.getQuantity();
                listener.onQuantityChange(currentItem, currentQuantity + 1);
            }
        });
        holder.btnDelete.setOnClickListener(v -> showRemoveConfirmationDialog(currentItem));

    }

    @Override
    public int getItemCount() {
        return cartItemList != null ? cartItemList.size() : 0;
    }

    private void showRemoveConfirmationDialog(CartItem item) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm " + (item.getProduct() != null ? item.getProduct().getProductName() : "này") + " khỏi giỏ hàng không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    if (listener != null) {
                        listener.onRemoveItem(item);
                    }
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvVariantDetails;
        TextView tvProductPrice;
        ImageView btnDecrease;
        TextView tvQuantity;
        ImageView btnIncrease;
        ImageView btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvVariantDetails = itemView.findViewById(R.id.tv_variant_details);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}