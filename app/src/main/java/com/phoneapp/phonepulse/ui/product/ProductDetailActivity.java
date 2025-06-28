package com.phoneapp.phonepulse.ui.product;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    // UI Components
    private Toolbar toolbar;
    private ImageView ivProductImage;
    private TextView tvProductName;
    private TextView tvOriginalPrice;
    private TextView tvDiscountPrice;
    private TextView tvDiscountPercent;
    private TextView tvProductSpecs;
    private TextView tvStock;

    // Data
    private String productId;
    private Product currentProduct;
    private NumberFormat numberFormat;

    // API Service
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Get product ID from intent
        productId = getIntent().getStringExtra(Constants.PRODUCT_ID);
        // log
        Log.d(TAG, "Received product ID: " + productId);
        if (productId == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        initApiService();
        loadProductDetail();

        numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar_product_detail);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvDiscountPrice = findViewById(R.id.tv_discount_price);
        tvDiscountPercent = findViewById(R.id.tv_discount_percent);
        tvProductSpecs = findViewById(R.id.tv_product_specs);
        tvStock = findViewById(R.id.tv_stock);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Chi tiết sản phẩm");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void initApiService() {
        apiService = RetrofitClient.getApiService(null); // null because product detail does not require token
    }

    private void loadProductDetail() {
        Call<Product> call = apiService.getProductById(String.valueOf(productId));
        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    runOnUiThread(() -> displayProductDetail());
                    Log.d(TAG, "Product loaded: " + currentProduct.getName());
                } else {
                    Log.e(TAG, "Failed to load product: " + response.code());
                    runOnUiThread(() -> showError("Không thể tải thông tin sản phẩm"));
                }
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                runOnUiThread(() -> showError("Lỗi kết nối mạng"));
            }
        });
    }

    private void displayProductDetail() {
        if (currentProduct == null) return;

        // Load product image
        Glide.with(this)
                .load(currentProduct.getProductImage().getImageUrl())
                .placeholder(R.drawable.placeholder_product)
                .error(R.drawable.placeholder_product)
                .into(ivProductImage);

        // Set product name
        tvProductName.setText(currentProduct.getName());

        // Calculate and display prices
        double originalPrice = currentProduct.getVariantId().getPrice();
        int discount = currentProduct.getDiscount();

        if (discount > 0) {
            // Show discount
            tvDiscountPercent.setVisibility(TextView.VISIBLE);
            tvOriginalPrice.setVisibility(TextView.VISIBLE);

            double discountedPrice = originalPrice * (100 - discount) / 100;

            tvOriginalPrice.setText("₫" + numberFormat.format(originalPrice));
            tvDiscountPrice.setText("₫" + numberFormat.format(discountedPrice));
            tvDiscountPercent.setText("-" + discount + "%");

            // Strike through original price
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            // No discount
            tvDiscountPercent.setVisibility(TextView.GONE);
            tvOriginalPrice.setVisibility(TextView.GONE);
            tvDiscountPrice.setText("₫" + numberFormat.format(originalPrice));
        }

        // Display specifications
        if (currentProduct.getVariantId() != null ) {
            tvProductSpecs.setText(
                            "Dòng : " + currentProduct.getVariantId().getSizeId().getName() + "\n" +
                            "Màu sắc: " + currentProduct.getVariantId().getColorId().getName() + "\n" +
                            "Bộ nhớ: " + currentProduct.getVariantId().getSizeId().getStorage() + "\n");
        } else {
            tvProductSpecs.setText("Thông số kỹ thuật đang được cập nhật");
        }

        // Display stock
        int stock = currentProduct.getVariantId().getQuantity();
        if (stock > 0) {
            tvStock.setText("Còn " + stock + " sản phẩm");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStock.setText("Hết hàng");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }

        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentProduct.getName());
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}