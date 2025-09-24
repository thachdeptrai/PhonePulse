package com.phoneapp.phonepulse.Adapter;

import android.annotation.SuppressLint;
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
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant; // Đảm bảo đã import đúng Variant model
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
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (orderItems == null || position < 0 || position >= orderItems.size()) {
            return;
        }

        OrderItem item = orderItems.get(position);
        if (item == null) {
            setDefaultUI(holder);
            return;
        }




        // --- LUÔN LUÔN hiển thị giá và số lượng từ OrderItem đã có ---
        // Giả định `item.getPrice()` đã có giá đúng từ backend (giá lúc đặt hàng)
        holder.tvQuantity.setText("Số lượng: " + Math.max(item.getQuantity(), 0));
        holder.tvPrice.setText("Giá: " + formatCurrency(item.getPrice()));


        // --- Logic để cập nhật Tên sản phẩm, Ảnh và TÊN BIẾN THỂ (từ Color/Size) nếu chúng chưa có trong OrderItem ---
        // Và cập nhật giá từ Variant nếu price của OrderItem là 0
        boolean needsProductApiCall = TextUtils.isEmpty(item.getName()) ||
                TextUtils.isEmpty(item.getImageUrl()) ||
                TextUtils.isEmpty(item.getVariant()) ||
                item.getPrice() <= 0; // Vẫn cần kiểm tra để lấy giá nếu nó là 0

        if (needsProductApiCall) {
            String token = Constants.getToken(holder.itemView.getContext());
            if (token == null || token.isEmpty()) {
                setDefaultUI(holder);
                return;
            }

            ApiService service = RetrofitClient.getApiService(token);
            service.getProductById(item.getProductId()).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(Call<Product> call, Response<Product> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Product product = response.body();

                        // Cập nhật tên sản phẩm nếu OrderItem chưa có
                        if (TextUtils.isEmpty(item.getName())) {
                            item.setName(product.getName());
                            holder.tvName.setText(item.getName());
                            Log.d(TAG, "    Cập nhật tên sản phẩm từ API: " + item.getName());
                        } else {
                            // Nếu OrderItem đã có tên, hiển thị nó
                            holder.tvName.setText(item.getName());
                        }

                        // Cập nhật URL ảnh nếu OrderItem chưa có
                        if (TextUtils.isEmpty(item.getImageUrl())) {
                            item.setImageUrl(product.getImageUrlSafe());
                            Glide.with(holder.itemView.getContext())
                                    .load(item.getImageUrl())
                                    .placeholder(R.drawable.placeholder_product)
                                    .error(R.drawable.placeholder_product)
                                    .into(holder.ivImage);
                        } else {
                            // Nếu OrderItem đã có ảnh, tải nó
                            Glide.with(holder.itemView.getContext())
                                    .load(item.getImageUrl())
                                    .placeholder(R.drawable.placeholder_product)
                                    .error(R.drawable.placeholder_product)
                                    .into(holder.ivImage);
                        }

                        // --- Logic lấy TÊN BIẾN THỂ và GIÁ từ Variant ---
                        String displayVariantName = "";
                        double priceFromVariant = 0; // Giá sẽ lấy từ Variant

                        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
                            for (Variant v : product.getVariants()) {
                                if (v.getId().equals(item.getVariantId())) {
                                    // Xây dựng tên biến thể từ Color và Size
                                    String colorName = (v.getColor() != null) ? v.getColor().getColorName() : "";
                                    String sizeName = (v.getSize() != null) ? v.getSize().getSizeName() : "";

                                    if (!TextUtils.isEmpty(colorName) && !TextUtils.isEmpty(sizeName)) {
                                        displayVariantName = colorName + ", " + sizeName;
                                    } else if (!TextUtils.isEmpty(colorName)) {
                                        displayVariantName = colorName;
                                    } else if (!TextUtils.isEmpty(sizeName)) {
                                        displayVariantName = sizeName;
                                    }

                                    // Lấy giá từ Variant
                                    priceFromVariant = v.getPrice();
                                    break;
                                }
                            }
                        }

                        // Cập nhật trường 'variant' trong OrderItem nếu chưa có (để hiển thị và lưu tạm)
                        if (TextUtils.isEmpty(item.getVariant())) {
                            item.setVariant(displayVariantName);
                        }

                        // Hiển thị tên biến thể (ưu tiên từ OrderItem, nếu không thì dùng từ API fetch)
                        if (!TextUtils.isEmpty(item.getVariant())) {
                            holder.tvVariant.setText(item.getVariant());
                            holder.tvVariant.setVisibility(View.VISIBLE);
                        } else if (!TextUtils.isEmpty(displayVariantName)) {
                            holder.tvVariant.setText(displayVariantName);
                            holder.tvVariant.setVisibility(View.VISIBLE);
                        } else {
                            holder.tvVariant.setVisibility(View.GONE);
                        }


                        // QUAN TRỌNG: Cập nhật giá của OrderItem CHỈ KHI NÓ ĐANG LÀ 0 từ ban đầu.
                        // Và giá đó phải được lấy từ Variant.
                        if (item.getPrice() <= 0 && priceFromVariant > 0) {
                            item.setPrice((int)priceFromVariant); // Cập nhật giá của OrderItem
                            holder.tvPrice.setText("Giá: " + formatCurrency(item.getPrice())); // Cập nhật UI
                        } else if (item.getPrice() <= 0 && priceFromVariant <= 0) {
                            Log.w(TAG, "    Không thể lấy giá biến thể từ API để cập nhật OrderItem (giá biến thể cũng là 0 hoặc không tìm thấy).");
                            // Giữ nguyên giá 0 hoặc cập nhật UI để cảnh báo
                            holder.tvPrice.setText("Giá: N/A"); // Hoặc giữ nguyên 0 đ
                        }


                    } else {
                        String errorBody = "";
                        try {
                            if (response.errorBody() != null) {
                                errorBody = response.errorBody().string();
                            }
                        } catch (Exception e) {
                        }
                        setDefaultUI(holder);
                    }
                }

                @Override
                public void onFailure(Call<Product> call, Throwable t) {
                    setDefaultUI(holder);
                }
            });
        } else {
            // Nếu đã có dữ liệu đầy đủ trong OrderItem (tên, ảnh, biến thể và giá khác 0) thì bind trực tiếp
            holder.tvName.setText(item.getName());
            holder.tvVariant.setText(item.getVariant());
            holder.tvVariant.setVisibility(!TextUtils.isEmpty(item.getVariant()) ? View.VISIBLE : View.GONE);
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
            return amount + " đ";
        }
    }

    private void setDefaultUI(OrderViewHolder holder) {
        holder.tvName.setText("Sản phẩm không rõ");
        holder.tvQuantity.setText("Số lượng: 0");
        holder.tvPrice.setText("Giá: 0 đ");
        holder.tvVariant.setVisibility(View.GONE);
        holder.ivImage.setImageResource(R.drawable.placeholder_product);
    }
}