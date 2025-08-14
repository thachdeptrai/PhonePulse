package com.phoneapp.phonepulse.Adapter;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.OrderViewHolder> {

    private final List<OrderItem> orderItems;
    private static final String TAG = "OrderItemAdapter";

    public OrderItemAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
        Log.d(TAG, "✅ Constructor - orderItems size = " + (orderItems != null ? orderItems.size() : 0));
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "📦 onCreateViewHolder()");
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        if (orderItems == null || position < 0 || position >= orderItems.size()) return;

        OrderItem item = orderItems.get(position);
        if (item == null) {
            setDefaultUI(holder);
            return;
        }

        // Bind quantity tạm thời
        holder.tvQuantity.setText("Số lượng: " + Math.max(item.getQuantity(), 0));
        holder.tvPrice.setText("Giá: " + formatCurrency(Math.max(item.getPrice(), 0) * item.getQuantity()));

        // Nếu chưa có name hoặc imageUrl hoặc giá = 0 -> gọi API
        if (TextUtils.isEmpty(item.getName()) || TextUtils.isEmpty(item.getImageUrl()) || item.getPrice() <= 0) {
            String token = Constants.getToken(holder.itemView.getContext());
            ApiService service = RetrofitClient.getApiService(token);
            service.getProductById(item.getProductId()).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Product product = response.body();

                        // Set tên sản phẩm
                        item.setName(product.getName());
                        item.setImageUrl(product.getImageUrlSafe());

                        // Lấy variant theo variantId
                        String variantName = "";
                        int price = 0;
                        if (product.getVariants() != null) {
                            for (Variant v : product.getVariants()) {
                                if (v.getId().equals(item.getVariantId())) {
                                    variantName = v.getProductId();
                                    price = (int) v.getPrice();
                                    break;
                                }
                            }
                        }
                        item.setVariant(variantName);
                        item.setPrice(price);

                        // Cập nhật UI
                        holder.tvName.setText(item.getName());
                        holder.tvVariant.setVisibility(!TextUtils.isEmpty(variantName) ? View.VISIBLE : View.GONE);
                        holder.tvPrice.setText("Giá: " + formatCurrency(price * item.getQuantity()));
                        Glide.with(holder.itemView.getContext())
                                .load(item.getImageUrl())
                                .placeholder(R.drawable.placeholder_product)
                                .error(R.drawable.placeholder_product)
                                .into(holder.ivImage);
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    Log.e(TAG, "❌ Lỗi lấy sản phẩm: " + t.getMessage());
                }
            });
        } else {
            // Nếu đã có dữ liệu -> bind trực tiếp
            holder.tvName.setText(item.getName());
            holder.tvVariant.setVisibility(!TextUtils.isEmpty(item.getVariant()) ? View.VISIBLE : View.GONE);
            holder.tvPrice.setText("Giá: " + formatCurrency(item.getPrice() * item.getQuantity()));
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(holder.ivImage);
        }
    }


    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvVariant, tvQuantity, tvPrice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvVariant = itemView.findViewById(R.id.tv_product_variant);
            tvQuantity = itemView.findViewById(R.id.tv_product_quantity);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
        }
    }

    private String formatCurrency(int amount) {
        try {
            return String.format("%,d đ", amount).replace(",", ".");
        } catch (Exception e) {
            Log.w(TAG, "⚠ formatCurrency failed: " + e.getMessage());
            return amount + " đ";
        }
    }

    private void setDefaultUI(OrderViewHolder holder) {
        holder.tvName.setText("Sản phẩm");
        holder.tvQuantity.setText("Số lượng: 0");
        holder.tvPrice.setText("Giá: 0 đ");
        holder.tvVariant.setVisibility(View.GONE);
        holder.ivImage.setImageResource(R.drawable.placeholder_product);
    }
}
