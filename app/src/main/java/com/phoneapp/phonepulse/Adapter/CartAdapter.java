package com.phoneapp.phonepulse.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.CartItem;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private static final String TAG = "CartAdapter";

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

        // --- 1. Ảnh sản phẩm ---
        String imageUrl = currentItem.getProductImage();
        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://")) {
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(holder.ivProductImage);

            } else if (imageUrl.startsWith("data:image/")) {
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
                holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
                Log.w(TAG, "Unrecognized image URL: " + imageUrl);
            }
        } else {
            holder.ivProductImage.setImageResource(R.drawable.placeholder_product);
            Log.w(TAG, "Image URL empty for item.");
        }

        // 2. Tên sản phẩm
        holder.tvProductName.setText(
                currentItem.getProduct() != null
                        ? currentItem.getProduct().getProductName()
                        : "Sản phẩm không xác định"
        );

        // 3. Biến thể (Màu, Kích thước, Bộ nhớ, RAM)
        StringBuilder variantDetails = new StringBuilder();
        if (currentItem.getVariant() != null) {
            if (currentItem.getVariant().getColor() != null &&
                    !TextUtils.isEmpty(currentItem.getVariant().getColor().getColorName())) {
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

        // 4. Giá sản phẩm (dùng formatCurrency chuẩn VNPay)
        int price = currentItem.getVariant() != null ? (int) currentItem.getVariant().getPrice() : 0;
        holder.tvProductPrice.setText(formatCurrency(price));

        // 5. Số lượng
        holder.tvQuantity.setText(String.valueOf(currentItem.getQuantity()));

        // 6. Sự kiện tăng/giảm số lượng
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
                int stock = currentItem.getVariant().getStockQuantity();

                if (currentQuantity < stock) {
                    listener.onQuantityChange(currentItem, currentQuantity + 1);
                } else {
                    Toast.makeText(context,
                            "Không thể tăng thêm. Đã đạt số lượng tồn kho tối đa.",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.btnDelete.setOnClickListener(v -> showRemoveConfirmationDialog(currentItem));
    }

    // ✅ Hàm format tiền chuẩn VNPay
    private String formatCurrency(int amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator(','); // dùng dấu phẩy ngăn cách nghìn
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        return decimalFormat.format(amount) + " ₫";
    }

    @Override
    public int getItemCount() {
        return cartItemList != null ? cartItemList.size() : 0;
    }

    private void showRemoveConfirmationDialog(CartItem item) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa sản phẩm " +
                        (item.getProduct() != null ? item.getProduct().getProductName() : "này") +
                        " khỏi giỏ hàng không?")
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
