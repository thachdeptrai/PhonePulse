package com.phoneapp.phonepulse.ui.product;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Product; // Keep Product model for discount and potentially product name/image
import com.phoneapp.phonepulse.models.Variant; // Main model for variant details
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.NumberFormat;
import java.util.Locale;

import io.reactivex.annotations.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {
    private static final String TAG = "ProductDetailActivity";

    // UI elements
    private Toolbar toolbar;
    private ImageView ivProductImage;
    private TextView tvProductName, tvOriginalPrice, tvDiscountPrice, tvDiscountPercent, tvProductSpecs, tvStock;
    private com.google.android.material.button.MaterialButton btnAddToCart, btnBuyNow; // Assuming MaterialButton from XML

    // Data
    private String variantId;
    private String productId; // New: To store the product ID associated with the variant
    private Product currentProduct; // Stores the Product object to get product name, image, discount
    private Variant displayedVariant; // Stores the Variant object for price, quantity, color, size
    private NumberFormat numberFormat;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Retrieve variantId and productId from the Intent
        variantId = getIntent().getStringExtra(Constants.VARIANT_ID);
        productId = getIntent().getStringExtra(Constants.PRODUCT_ID); // Get product ID from intent

        if (variantId == null || productId == null) {
            Toast.makeText(this, "Không tìm thấy biến thể hoặc sản phẩm. Dữ liệu không hợp lệ.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "onCreate: Missing variantId or productId. variantId: " + variantId + ", productId: " + productId);
            finish(); // Close activity if essential data is missing
            return;
        }

        Log.d(TAG, "onCreate: Variant ID received: " + variantId + ", Product ID received: " + productId);

        initViews(); // Initialize UI components
        setupToolbar(); // Setup the toolbar
        apiService = RetrofitClient.getApiService(null); // Initialize API service (token might be needed for some APIs)
        numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN")); // Setup currency formatter

        // Load all necessary data
        loadProductAndVariantDetails();
    }

    /**
     * Initializes all UI components by finding them by their IDs.
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar_product_detail);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvDiscountPrice = findViewById(R.id.tv_discount_price);
        tvDiscountPercent = findViewById(R.id.tv_discount_percent);
        tvProductSpecs = findViewById(R.id.tv_product_specs);
        tvStock = findViewById(R.id.tv_stock);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBuyNow = findViewById(R.id.btn_buy_now);

        // Set click listeners for buttons
        btnAddToCart.setOnClickListener(v -> Toast.makeText(ProductDetailActivity.this, "Thêm vào giỏ hàng", Toast.LENGTH_SHORT).show());
        btnBuyNow.setOnClickListener(v -> Toast.makeText(ProductDetailActivity.this, "Mua ngay", Toast.LENGTH_SHORT).show());

        Log.d(TAG, "initViews: All views initialized.");
    }

    /**
     * Sets up the toolbar for the activity.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Đang tải..."); // Temporary title
        }
        // Handle back button click
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Log.d(TAG, "setupToolbar: Toolbar configured.");
    }

    /**
     * Loads both Product details (for name, image, discount) and Variant details (for price, quantity, color, size).
     * This involves two separate API calls due to backend structure.
     */
    private void loadProductAndVariantDetails() {
        // First, load the Product details
        loadProductDetails();
        // Then, load the Variant details
        loadVariantDetails();
    }

    /**
     * Fetches the Product details (name, image URL, discount) using the productId.
     */
    private void loadProductDetails() {
        Log.d(TAG, "loadProductDetails: Fetching Product details for ID: " + productId);
        apiService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    Log.d(TAG, "onResponse: Successfully loaded Product details: " + currentProduct.getName());
                    // Update UI elements that depend on Product (name, image, discount)
                    updateUIFromProduct();
                    // If variant data is already loaded, update prices/specs
                    if (displayedVariant != null) {
                        displayPriceAndSpecs();
                    }
                } else {
                    String errorMsg = "Không thể tải chi tiết sản phẩm. Mã lỗi: " + response.code();
                    Log.e(TAG, "onResponse: " + errorMsg);
                    showError("Không thể tải thông tin sản phẩm.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Lỗi kết nối mạng khi tải sản phẩm: " + t.getMessage(), t);
                showError("Lỗi kết nối mạng khi tải thông tin sản phẩm.");
            }
        });
    }

    /**
     * Fetches the Variant details (price, quantity, color, size) using both productId and variantId.
     */
    private void loadVariantDetails() {
        Log.d(TAG, "loadVariantDetails: Fetching Variant details for Product ID: " + productId + ", Variant ID: " + variantId);
        // Call the API that requires both product ID and variant ID
        apiService.getVariantForProductById(productId, variantId).enqueue(new Callback<Variant>() {
            @Override
            public void onResponse(@NonNull Call<Variant> call, @NonNull Response<Variant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayedVariant = response.body();
                    Log.d(TAG, "onResponse: Successfully loaded Variant details for ID: " + displayedVariant.getId());
                    // Update UI elements that depend on Variant (price, quantity, color, size)
                    updateUIFromVariant();
                    // If product data is already loaded, update prices/specs
                    if (currentProduct != null) {
                        displayPriceAndSpecs();
                    }
                } else {
                    String errorMsg = "Không thể tải chi tiết biến thể. Mã lỗi: " + response.code();
                    Log.e(TAG, "onResponse: " + errorMsg);
                    showError("Không thể tải thông tin biến thể.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<Variant> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: Lỗi kết nối mạng khi tải biến thể: " + t.getMessage(), t);
                showError("Lỗi kết nối mạng khi tải thông tin biến thể.");
            }
        });
    }

    /**
     * Updates UI elements that primarily depend on the Product object (name, image).
     */
    private void updateUIFromProduct() {
        if (currentProduct == null) {
            Log.w(TAG, "updateUIFromProduct: currentProduct is null, cannot update UI.");
            return;
        }

        // Update product name
        tvProductName.setText(currentProduct.getName() != null ? currentProduct.getName() : "Tên sản phẩm không xác định");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(currentProduct.getName() != null ? currentProduct.getName() : "Chi tiết sản phẩm");
        }

        // Update product image
        String imageUrl = (currentProduct.getProductImage() != null && currentProduct.getProductImage().getImageUrl() != null) ?
                currentProduct.getProductImage().getImageUrl() : null;

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(ivProductImage);
            Log.d(TAG, "updateUIFromProduct: Product Image URL: " + imageUrl);
        } else {
            ivProductImage.setImageResource(R.drawable.placeholder_product);
            Log.w(TAG, "updateUIFromProduct: No image URL found for product: " + currentProduct.getName() + ". Using placeholder.");
        }
    }

    /**
     * Updates UI elements that primarily depend on the Variant object (price, quantity, color, size).
     * Also uses Product's discount if available.
     */
    private void updateUIFromVariant() {
        if (displayedVariant == null) {
            Log.w(TAG, "updateUIFromVariant: displayedVariant is null, cannot update UI.");
            return;
        }

        // Call displayPriceAndSpecs to handle price, discount, stock, and specs
        displayPriceAndSpecs();
    }

    /**
     * Displays price, discount, stock, and product specifications.
     * This method combines data from both Product and Variant objects.
     */
    private void displayPriceAndSpecs() {
        if (displayedVariant == null) {
            Log.w(TAG, "displayPriceAndSpecs: displayedVariant is null. Cannot display price and specs.");
            return;
        }

        double originalPrice = displayedVariant.getPrice();
        // Get discount from currentProduct if it's loaded, otherwise 0
        int discount = (currentProduct != null) ? currentProduct.getDiscount() : 0;

        if (discount > 0) {
            tvDiscountPercent.setVisibility(View.VISIBLE);
            tvOriginalPrice.setVisibility(View.VISIBLE);
            double discountedPrice = originalPrice * (100 - discount) / 100.0;

            tvOriginalPrice.setText("₫" + numberFormat.format(originalPrice));
            tvDiscountPrice.setText("₫" + numberFormat.format(discountedPrice));
            tvDiscountPercent.setText("-" + discount + "%");

            // Apply strike-through to original price
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvDiscountPercent.setVisibility(View.GONE);
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscountPrice.setText("₫" + numberFormat.format(originalPrice));
        }
        Log.d(TAG, "displayPriceAndSpecs: Price updated. Original: " + originalPrice + ", Discount: " + discount);

        // Update product specifications (Color and Size)
        String colorName = (displayedVariant.getColor() != null && displayedVariant.getColor().getName() != null) ? displayedVariant.getColor().getName() : "N/A";
        String sizeName = (displayedVariant.getSize() != null && displayedVariant.getSize().getName() != null) ? displayedVariant.getSize().getName() : "N/A";
        String storage = (displayedVariant.getSize() != null && displayedVariant.getSize().getStorage() != null) ? displayedVariant.getSize().getStorage() : "N/A";

        tvProductSpecs.setText(
                "Màu sắc: " + colorName + "\n" +
                        "Kích thước: " + sizeName + "\n" +
                        "Bộ nhớ: " + storage
        );
        Log.d(TAG, "displayPriceAndSpecs: Specs updated. Color: " + colorName + ", Size: " + sizeName + ", Storage: " + storage);


        // Update stock status
        int stock = displayedVariant.getQuantity();
        if (stock > 0) {
            tvStock.setText("Còn " + stock + " sản phẩm");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStock.setText("Hết hàng");
            tvStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
        Log.d(TAG, "displayPriceAndSpecs: Stock updated: " + stock);
    }

    /**
     * Displays a short Toast message for errors.
     * @param msg The message to display.
     */
    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        Log.e(TAG, "showError: Displaying Toast: " + msg);
    }
}
