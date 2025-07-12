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
import com.phoneapp.phonepulse.models.ProductImage;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    private Toolbar toolbar;
    private ImageView ivProductImage;
    private TextView tvProductName, tvOriginalPrice, tvDiscountPrice, tvDiscountPercent, tvProductSpecs, tvStock;

    private String productId;
    private Product currentProduct;
    private Variant defaultVariant;
    private NumberFormat numberFormat;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        productId = getIntent().getStringExtra(Constants.PRODUCT_ID);
        if (productId == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        apiService = RetrofitClient.getApiService(null);
        numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        loadProductDetail();
        loadProductImage();
        loadProductVariants();
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

    private void loadProductDetail() {
        apiService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    tvProductName.setText(currentProduct.getName());
                    if (getSupportActionBar() != null)
                        getSupportActionBar().setTitle(currentProduct.getName());
                } else showError("Không thể tải thông tin sản phẩm");
            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {
                showError("Lỗi kết nối mạng");
            }
        });
    }

    private void loadProductImage() {
        apiService.getProductImages(productId).enqueue(new Callback<List<ProductImage>>() {
            @Override
            public void onResponse(Call<List<ProductImage>> call, Response<List<ProductImage>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Glide.with(ProductDetailActivity.this)
                            .load(response.body().get(0).getImageUrl())
                            .placeholder(R.drawable.placeholder_product)
                            .into(ivProductImage);
                }
            }

            @Override
            public void onFailure(Call<List<ProductImage>> call, Throwable t) {
                Log.e(TAG, "Failed to load image: " + t.getMessage());
            }
        });
    }

    private void loadProductVariants() {
        apiService.getVariants(productId).enqueue(new Callback<List<Variant>>() {
            @Override
            public void onResponse(Call<List<Variant>> call, Response<List<Variant>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    defaultVariant = response.body().get(0);
                    displayPriceAndSpecs();
                }
            }

            @Override
            public void onFailure(Call<List<Variant>> call, Throwable t) {
                Log.e(TAG, "Failed to load variants: " + t.getMessage());
            }
        });
    }

    private void displayPriceAndSpecs() {
        if (defaultVariant == null) return;

        double originalPrice = defaultVariant.getPrice();
        int discount = currentProduct != null ? currentProduct.getDiscount() : 0;

        if (discount > 0) {
            tvDiscountPercent.setVisibility(TextView.VISIBLE);
            tvOriginalPrice.setVisibility(TextView.VISIBLE);
            double discountedPrice = originalPrice * (100 - discount) / 100;

            tvOriginalPrice.setText("₫" + numberFormat.format(originalPrice));
            tvDiscountPrice.setText("₫" + numberFormat.format(discountedPrice));
            tvDiscountPercent.setText("-" + discount + "%");

            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvDiscountPercent.setVisibility(TextView.GONE);
            tvOriginalPrice.setVisibility(TextView.GONE);
            tvDiscountPrice.setText("₫" + numberFormat.format(originalPrice));
        }

        tvProductSpecs.setText(
                "Dòng: " + defaultVariant.getSizeId().getName() + "\n" +
                        "Màu sắc: " + defaultVariant.getColorId().getName() + "\n" +
                        "Bộ nhớ: " + defaultVariant.getSizeId().getStorage()
        );

        int stock = defaultVariant.getQuantity();
        if (stock > 0) {
            tvStock.setText("Còn " + stock + " sản phẩm");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStock.setText("Hết hàng");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
