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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants;


import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private List<CartItem> cartItems;
    private final ApiService apiService;
    private final UpdateCartCallback callback;
    private final NumberFormat numberFormat;

    public interface UpdateCartCallback {
        void updateTotal(List<CartItem> updatedItems);
    }

    public CartAdapter(Context context, List<CartItem> cartItems, UpdateCartCallback callback) {
        this.context = context;
        this.cartItems = cartItems;
        this.callback = callback;
        this.apiService = RetrofitClient.getApiService(Constants.getToken(context));
        this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public void updateCartItems(List<CartItem> updatedItems) {
        this.cartItems = updatedItems;
        notifyDataSetChanged();
        refreshTotal();
    }

    private void refreshTotal() {
        callback.updateTotal(cartItems);
    }

    class CartViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivProductImage, btnDecrease, btnIncrease, btnDelete;
        private final TextView tvProductName, tvProductPrice, tvQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvProductPrice = itemView.findViewById(R.id.tv_product_price);
            tvQuantity = itemView.findViewById(R.id.tv_quantity);
            btnDecrease = itemView.findViewById(R.id.btn_decrease);
            btnIncrease = itemView.findViewById(R.id.btn_increase);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        public void bind(CartItem item) {
            if (item.getProduct() != null) {
                tvProductName.setText(item.getProduct().getName());
                tvProductPrice.setText("₫" + numberFormat.format(item.getProduct().getPrice()));

                Glide.with(context)
                        .load(item.getProduct().getCategory() != null ? item.getProduct().getCategory().getIcon() : "")
                        .placeholder(R.drawable.placeholder_product)
                        .error(R.drawable.placeholder_product)
                        .into(ivProductImage);
            }

            tvQuantity.setText(String.valueOf(item.getQuantity()));

            btnDecrease.setOnClickListener(v -> {
                if (item.getQuantity() > 1) {
                    updateQuantity(item, item.getQuantity() - 1);
                }
            });

            btnIncrease.setOnClickListener(v -> {
                updateQuantity(item, item.getQuantity() + 1);
            });

            btnDelete.setOnClickListener(v -> {
                removeFromCart(item);
            });
        }

        private void updateQuantity(CartItem item, int newQuantity) {
            CartRequest request = new CartRequest(item.getProduct().getId(), item.getProduct().getVariantId(), newQuantity);
            apiService.updateCart(Constants.getToken(context), request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        item.setQuantity(newQuantity);
                        notifyItemChanged(getAdapterPosition());
                        refreshTotal();
                    } else {
                        Toast.makeText(context, "Lỗi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("CartAdapter", t.getMessage());
                }
            });
        }

        private void removeFromCart(CartItem item) {
            CartRequest request = new CartRequest(item.getProduct().getId(), item.getProduct().getVariantId(), item.getQuantity());
            apiService.removeFromCart(Constants.getToken(context), request).enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        int position = getAdapterPosition();
                        cartItems.remove(position);
                        notifyItemRemoved(position);
                        refreshTotal();
                        Toast.makeText(context, "Đã xóa sản phẩm khỏi giỏ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "Lỗi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Toast.makeText(context, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
