package com.phoneapp.phonepulse.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint; // Import this for strikethrough
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
    private static final String TAG = "ProductGridAdapter"; // For consistent logging

    private Context context;
    private List<Product> productList;
    private NumberFormat numberFormat;
    private ApiService apiService;

    public ProductGridAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
        this.numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        // Khởi tạo ApiService với token
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
            // Load Product Image (assuming product.getImageUrl() or similar exists)
            // If product.getCategory().getIcon() is truly the product image URL:
            Glide.with(context)
                    .load(product.getCategory() != null ? product.getCategory().getIcon() : "")
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(ivProductImage);

            // Set Product Name
            tvProductName.setText(product.getName());

            // Get actual price and discount from the product object
            double price = product.getPrice(); // Sử dụng giá thực tế từ Product model
            int discount = product.getDiscount(); // Sử dụng chiết khấu thực tế từ Product model

            if (discount > 0) {
                tvDiscountPercent.setVisibility(View.VISIBLE);
                tvOriginalPrice.setVisibility(View.VISIBLE);

                double discountedPrice = price * (100 - discount) / 100;

                tvOriginalPrice.setText("₫" + numberFormat.format(price));
                tvDiscountPrice.setText("₫" + numberFormat.format(discountedPrice));
                tvDiscountPercent.setText("-" + discount + "%");

                // Gạch ngang giá gốc
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvDiscountPercent.setVisibility(View.GONE);
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountPrice.setText("₫" + numberFormat.format(price));
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)); // Remove strikethrough if no discount
            }

            // Set Sold Count (using a random number for demo, replace with actual product.getSoldCount() if available)
            // Nếu Product có trường sold_count, bạn sẽ dùng: product.getSoldCount()
            int soldCount = (int) (Math.random() * 500);
            tvSold.setText("Đã bán " + soldCount);

            // Handle Product Click (Go to Product Detail)
            cardProduct.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra(Constants.PRODUCT_ID, product.getId());
                context.startActivity(intent);
            });

            // Handle Add to Cart Button Click
            btnAddToCart.setOnClickListener(v -> {
                // Check if user is logged in (token exists)
                String currentToken = Constants.getToken(context);
                if (currentToken == null || currentToken.isEmpty()) {
                    Toast.makeText(context, "Vui lòng đăng nhập để mua hàng", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Temporary empty variantId - update if your products have variants
                String variantId = "";
                // If you have a specific variant selected, set it here, e.g., product.getDefaultVariantId()
                // Or you would typically select a variant on the product detail page.

                // Create CartRequest object with product data
                CartRequest request = new CartRequest(product.getId(), variantId, 1); // Quantity is 1 for direct add

                // Make API call to add product to cart
                apiService.addToCart(currentToken, request).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Added product to cart: " + product.getName() + ", ID: " + product.getId());
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
                            Toast.makeText(context, "Lỗi thêm vào giỏ hàng: " + response.message(), Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                        Log.e(TAG, "Network error during add to cart: " + t.getMessage(), t); // Log full stack trace
                        Toast.makeText(context, "Lỗi kết nối mạng khi thêm giỏ hàng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    }
                });
            });
        }
    }
}