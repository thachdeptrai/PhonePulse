package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.ui.product.ProductDetailActivity;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductGridAdapter extends RecyclerView.Adapter<ProductGridAdapter.ProductViewHolder> {
    private static final String TAG = "ProductGridAdapter";

    private Context context;
    private List<Product> productList;
    private NumberFormat numberFormat;
    private ApiService apiService;

    public ProductGridAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        // Khởi tạo ApiService một lần trong constructor
        this.apiService = RetrofitClient.getApiService(Constants.getToken(context));
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_grid, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(productList.get(position));
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
        private Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);

            cardProduct = itemView.findViewById(R.id.card_product);
            ivProductImage = itemView.findViewById(R.id.iv_product_image);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            tvOriginalPrice = itemView.findViewById(R.id.tv_original_price);
            tvDiscountPrice = itemView.findViewById(R.id.tv_discount_price);
            tvDiscountPercent = itemView.findViewById(R.id.tv_discount_percent);
            tvSold = itemView.findViewById(R.id.tv_sold);
            btnAddToCart = itemView.findViewById(R.id.btn_add_to_cart);
        }

        public void bind(Product product) {

            Glide.with(context)
                    .load(product.getCategory() != null ? product.getCategory().getIcon() : "") // Xem xét lại nguồn ảnh này, có thể nên dùng ProductImage
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(ivProductImage);

            tvProductName.setText(product.getName());

            double price = product.getPrice(); // Bây giờ sẽ lấy giá từ biến thể đầu tiên
            int discount = product.getDiscount();

            if (discount > 0) {
                tvDiscountPercent.setVisibility(View.VISIBLE);
                tvOriginalPrice.setVisibility(View.VISIBLE);

                double discountedPrice = price * (100 - discount) / 100;

                tvOriginalPrice.setText("₫" + numberFormat.format(price));
                tvDiscountPrice.setText("₫" + numberFormat.format(discountedPrice));
                tvDiscountPercent.setText("-" + discount + "%");

                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvDiscountPercent.setVisibility(View.GONE);
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountPrice.setText("₫" + numberFormat.format(price));
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            int soldCount = (int) (Math.random() * 500); // Tạm thời
            tvSold.setText("Đã bán " + soldCount);

            cardProduct.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra(Constants.PRODUCT_ID, product.getId());
                context.startActivity(intent);
            });

            btnAddToCart.setOnClickListener(v -> {
                String currentToken = Constants.getToken(context);
                if (currentToken == null || currentToken.isEmpty()) {
                    Toast.makeText(context, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
                    return;
                }

                // LẤY variantId TỪ BIẾN THỂ ĐẦU TIÊN trong danh sách variants
                String variantId = product.getVariantId();

                // KIỂM TRA ĐỂ ĐẢM BẢO variantId KHÔNG NULL HOẶC RỖNG
                if (variantId == null || variantId.isEmpty()) {
                    Log.e(TAG, "Product " + product.getName() + " (ID: " + product.getId() + ") does not have a valid variantId from its variants list.");
                    Toast.makeText(context, "Không thể thêm sản phẩm này vào giỏ hàng vì không có biến thể khả dụng.", Toast.LENGTH_LONG).show();
                    return; // Ngăn không cho gửi request với variantId không hợp lệ
                }

                // Tạo CartRequest object với product data
                CartRequest request = new CartRequest(product.getId(), variantId, 1); // Quantity is 1 for direct add

                Log.d(TAG, "Sending to API: Product ID=" + product.getId() + ", Variant ID=" + variantId + ", Quantity=" + 1);

                // Make API call to add product to cart
                apiService.addToCart(currentToken, request).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Added product to cart: " + product.getName() + ", ID: " + product.getId() + ", Variant ID: " + variantId);
                        } else {
                            String errorBody = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading error body: " + e.getMessage());
                            }
                            Log.e(TAG, "Failed to add to cart. Code: " + response.code() + ", Message: " + response.message() + ", Error Body: " + errorBody);
                            Toast.makeText(context, "Lỗi thêm vào giỏ hàng: " + response.message() + " " + errorBody, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Log.e(TAG, "Network error during add to cart: " + t.getMessage(), t);
                        Toast.makeText(context, "Lỗi kết nối mạng khi thêm giỏ hàng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }
}