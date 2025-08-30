    package com.phoneapp.phonepulse.VIEW;

    import android.content.Intent;
    import android.graphics.Paint;
    import android.os.Bundle;
    import android.util.Log;
    import android.view.MenuItem;
    import android.view.View;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.appcompat.app.AppCompatActivity;
    import androidx.appcompat.widget.Toolbar;
    import androidx.core.content.ContextCompat;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.google.android.material.button.MaterialButton;
    import com.phoneapp.phonepulse.Adapter.VariantAdapter;
    import com.phoneapp.phonepulse.R;
    import com.phoneapp.phonepulse.Response.ApiResponse;
    import com.phoneapp.phonepulse.data.api.ApiService;
    import com.phoneapp.phonepulse.data.api.RetrofitClient;
    import com.phoneapp.phonepulse.models.Cart;
    import com.phoneapp.phonepulse.models.Favourite;
    import com.phoneapp.phonepulse.models.Product;
    import com.phoneapp.phonepulse.models.Variant;
    import com.phoneapp.phonepulse.request.CartItem;
    import com.phoneapp.phonepulse.request.CartRequest;
    import com.phoneapp.phonepulse.request.FavouriteRequest;
    import com.phoneapp.phonepulse.utils.Constants;

    import java.io.IOException;
    import java.text.NumberFormat;
    import java.util.List;
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
        private TextView tvProductName, tvOriginalPrice, tvDiscountPrice, tvDiscountPercent, tvStock;
        private TextView tvProductDescription;
        private TextView tvProductSpecs;
        private RecyclerView rvColorOptions;
        private MaterialButton btnAddToCart, btnBuyNow;
        private ImageView iv_cart_icon;
        private ImageView ivFavourite;

        // Data
        private String initialVariantIdFromIntent;
        private String productIdFromIntent;
        private Product currentProduct;
        private Variant displayedVariant;
        private NumberFormat numberFormat;
        private ApiService apiService;
        private String authToken;
        private boolean isFavourite = false;

        private boolean isLoadingInitialFavouriteStatus = false;
        private boolean isTogglingFavourite = false;
        private boolean isLoadingProduct = false;
        private boolean isLoadingVariant = false;

        private VariantAdapter variantAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_product_detail);

            initialVariantIdFromIntent = getIntent().getStringExtra(Constants.VARIANT_ID);
            productIdFromIntent = getIntent().getStringExtra(Constants.PRODUCT_ID);
            authToken = Constants.getToken(this);

            if (productIdFromIntent == null) {
                Toast.makeText(this, "ID sản phẩm không hợp lệ.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            initViews();
            setupToolbar();
            apiService = RetrofitClient.getApiService(authToken);
            numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            numberFormat.setMaximumFractionDigits(0);

            loadProductDetailsAndThenVariant();
        }

        private void initViews() {
            toolbar = findViewById(R.id.toolbar_product_detail);
            ivProductImage = findViewById(R.id.iv_product_image);
            tvProductName = findViewById(R.id.tv_product_name);
            tvOriginalPrice = findViewById(R.id.tv_original_price);
            tvDiscountPrice = findViewById(R.id.tv_discount_price);
            tvDiscountPercent = findViewById(R.id.tv_discount_percent);
            tvProductDescription = findViewById(R.id.tv_product_description);
            tvProductSpecs = findViewById(R.id.tv_product_specs);
            tvStock = findViewById(R.id.tv_stock);
            btnAddToCart = findViewById(R.id.btn_add_to_cart);
            btnBuyNow = findViewById(R.id.btn_buy_now);
            iv_cart_icon = findViewById(R.id.iv_cart_icon);
            ivFavourite = findViewById(R.id.iv_favourite);
            rvColorOptions = findViewById(R.id.rv_color_options);

            rvColorOptions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

            ivFavourite.setEnabled(false);
            btnAddToCart.setEnabled(false);
            btnBuyNow.setEnabled(false);

            ivFavourite.setOnClickListener(v -> {
                if (isLoadingInitialFavouriteStatus || isLoadingProduct) {
                    Toast.makeText(ProductDetailActivity.this, "Đang tải dữ liệu...", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (isTogglingFavourite) {
                    Toast.makeText(ProductDetailActivity.this, "Đang xử lý yêu thích...", Toast.LENGTH_SHORT).show();
                    return;
                }
                toggleFavouriteStatus();
            });

            iv_cart_icon.setOnClickListener(view -> {
                Intent intent = new Intent(ProductDetailActivity.this, Cart_Activity.class);
                startActivity(intent);
            });

            btnAddToCart.setOnClickListener(v -> {
                if (currentProduct != null && displayedVariant != null) {
                    checkStockAndAddToCart(currentProduct.getId(), displayedVariant.getId(), 1);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Thông tin sản phẩm/biến thể chưa tải xong.", Toast.LENGTH_SHORT).show();
                }
            });

            btnBuyNow.setOnClickListener(v -> {
                if (currentProduct != null && displayedVariant != null) {
                    Toast.makeText(ProductDetailActivity.this, "Mua ngay " + currentProduct.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Thông tin sản phẩm/biến thể chưa tải xong.", Toast.LENGTH_SHORT).show();
                }
            });
        }



        private void checkStockAndAddToCart(String productId, String variantId, int addedQuantity) {
            // ... (existing checkStockAndAddToCart logic - looks fine)
            if (authToken == null || authToken.isEmpty()) {
                Toast.makeText(this, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_LONG).show();
                return;
            }
            apiService.getCart().enqueue(new Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(@NonNull Call<ApiResponse<Cart>> call, @NonNull Response<ApiResponse<Cart>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Cart cart = response.body().getData();
                        int existingQuantity = 0;
                        if (cart != null && cart.getItems() != null) {
                            for (CartItem item : cart.getItems()) {
                                if (item.getVariant() != null && variantId.equals(item.getVariant().getId())) {
                                    existingQuantity = item.getQuantity();
                                    break;
                                }
                            }
                        }
                        int finalExistingQuantity = existingQuantity;
                        apiService.getVariantForProductById(productId, variantId).enqueue(new Callback<Variant>() {
                            @Override
                            public void onResponse(@NonNull Call<Variant> call, @NonNull Response<Variant> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Variant variant = response.body();
                                    int stockQuantity = variant.getQuantity();
                                    int totalRequested = finalExistingQuantity + addedQuantity;
                                    if (totalRequested > stockQuantity) {
                                        Toast.makeText(ProductDetailActivity.this,
                                                "Không thể thêm. Số lượng vượt quá tồn kho (" + stockQuantity + "). Hiện có " + finalExistingQuantity + " trong giỏ.",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        callAddToCartApi(productId, variantId, addedQuantity);
                                    }
                                } else {
                                    Toast.makeText(ProductDetailActivity.this, "Không thể lấy thông tin tồn kho.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<Variant> call, @NonNull Throwable t) {
                                Toast.makeText(ProductDetailActivity.this, "Lỗi mạng khi kiểm tra tồn kho: " + t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Log.w(TAG, "Could not get current cart. Proceeding to check stock and add.");
                        apiService.getVariantForProductById(productId, variantId).enqueue(new Callback<Variant>() {
                            @Override
                            public void onResponse(@NonNull Call<Variant> call, @NonNull Response<Variant> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Variant variant = response.body();
                                    int stockQuantity = variant.getQuantity();
                                    if (addedQuantity > stockQuantity) {
                                        Toast.makeText(ProductDetailActivity.this,
                                                "Không thể thêm. Số lượng vượt quá tồn kho (" + stockQuantity + ").",
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        callAddToCartApi(productId, variantId, addedQuantity);
                                    }
                                } else {
                                    Toast.makeText(ProductDetailActivity.this, "Không thể lấy thông tin tồn kho.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(@NonNull Call<Variant> call, @NonNull Throwable t_inner) {
                                Toast.makeText(ProductDetailActivity.this, "Lỗi mạng khi kiểm tra tồn kho: " + t_inner.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ApiResponse<Cart>> call, @NonNull Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi mạng khi lấy giỏ hàng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Network failure getting cart. Proceeding to check stock and add.", t);
                    apiService.getVariantForProductById(productId, variantId).enqueue(new Callback<Variant>() {
                        @Override
                        public void onResponse(@NonNull Call<Variant> call, @NonNull Response<Variant> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Variant variant = response.body();
                                int stockQuantity = variant.getQuantity();
                                if (addedQuantity > stockQuantity) {
                                    Toast.makeText(ProductDetailActivity.this,
                                            "Không thể thêm. Số lượng vượt quá tồn kho (" + stockQuantity + ").",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    callAddToCartApi(productId, variantId, addedQuantity);
                                }
                            } else {
                                Toast.makeText(ProductDetailActivity.this, "Không thể lấy thông tin tồn kho.", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<Variant> call, @NonNull Throwable t_inner) {
                            Toast.makeText(ProductDetailActivity.this, "Lỗi mạng khi kiểm tra tồn kho: " + t_inner.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
        }
        private void callAddToCartApi(String productId, String variantId, int quantity) {
            // ... (existing addToCart logic - looks fine)
            if (authToken == null || authToken.isEmpty()) {
                Toast.makeText(ProductDetailActivity.this, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_LONG).show();
                return;
            }
            CartRequest.AddToCart request = new CartRequest.AddToCart(productId, variantId, quantity);
            Toast.makeText(ProductDetailActivity.this, "Đang thêm sản phẩm vào giỏ hàng...", Toast.LENGTH_SHORT).show();
            apiService.addToCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(@androidx.annotation.NonNull Call<ApiResponse<com.phoneapp.phonepulse.models.Cart>> call, @androidx.annotation.NonNull Response<ApiResponse<com.phoneapp.phonepulse.models.Cart>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(ProductDetailActivity.this, "Thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Product added to cart successfully. Current cart: " + response.body().getData());
                    } else {
                        String errorMsg = "Lỗi khi thêm vào giỏ hàng.";
                        if (response.body() != null && response.body().getMessage() != null) {
                            errorMsg = response.body().getMessage();
                        } else if (response.errorBody() != null) {
                            try {
                                errorMsg += " " + response.errorBody().string();
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error body for add to cart", e);
                            }
                        }
                        Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Add to cart API failed: " + response.code() + " - " + errorMsg);
                    }
                }
                @Override
                public void onFailure(@androidx.annotation.NonNull Call<ApiResponse<com.phoneapp.phonepulse.models.Cart>> call, @androidx.annotation.NonNull Throwable t) {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi mạng khi thêm vào giỏ hàng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Add to cart network failure: ", t);
                }
            });
        }

        private void setupToolbar() {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setTitle("Đang tải...");
            }
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        private void loadProductDetailsAndThenVariant() {
            isLoadingProduct = true;
            updateLoadingState();

            apiService.getProductById(productIdFromIntent).enqueue(new Callback<Product>() {
                @Override
                public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                    isLoadingProduct = false;
                    if (response.isSuccessful() && response.body() != null) {
                        currentProduct = response.body();
                        updateUIFromProduct();
                        checkIfProductIsFavourite();

                        // setup variant list
                        if (currentProduct.getVariants() != null && !currentProduct.getVariants().isEmpty()) {
                            variantAdapter = new VariantAdapter(currentProduct.getVariants(), variant -> {
                                loadVariantDetails(variant.getId());
                            });
                            rvColorOptions.setAdapter(variantAdapter);

                            String variantIdToLoad = initialVariantIdFromIntent;
                            if (variantIdToLoad == null) {
                                variantIdToLoad = currentProduct.getVariants().get(0).getId();
                            }
                            loadVariantDetails(variantIdToLoad);
                        } else {
                            showError("Sản phẩm này không có biến thể nào.");
                        }
                    } else {
                        showError("Không thể tải thông tin sản phẩm.");
                    }
                    updateLoadingState();
                }

                private void checkIfProductIsFavourite() {
                    if (productIdFromIntent == null || authToken == null || authToken.isEmpty() || currentProduct == null) {
                        updateFavouriteIcon();
                        ivFavourite.setEnabled(currentProduct != null); // Enable only if product is loaded
                        return;
                    }
                    isLoadingInitialFavouriteStatus = true;
                    updateLoadingState();

                    // SỬA Ở ĐÂY: Thay đổi Callback để sử dụng List<Favourite>
                    apiService.getFavourites().enqueue(new Callback<ApiResponse<List<Favourite>>>() {
                        @Override
                        public void onResponse(@NonNull Call<ApiResponse<List<Favourite>>> call, @NonNull Response<ApiResponse<List<Favourite>>> response) {
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                List<Favourite> favouriteEntries = response.body().getData(); // Danh sách các đối tượng Favourite
                                isFavourite = false;
                                if (favouriteEntries != null) {
                                    for (Favourite favEntry : favouriteEntries) {
                                        // Lấy đối tượng Product cơ bản từ favEntry
                                        Product productInFavList = favEntry.getProductDetails();
                                        if (productInFavList != null && productInFavList.getId() != null) {
                                            if (productInFavList.getId().equals(productIdFromIntent)) {
                                                isFavourite = true;
                                                break;
                                            }
                                        }
                                    }
                                }
                            } else {
                                isFavourite = false; // Mặc định là false nếu có lỗi hoặc không thành công
                                Log.e(TAG, "getFavourites API error: " + response.code() + (response.body() != null ? " - " + response.body().getMessage() : " (No error body)"));
                            }
                            isLoadingInitialFavouriteStatus = false;
                            updateFavouriteIcon();
                            updateLoadingState();
                        }

                        @Override
                        public void onFailure(@NonNull Call<ApiResponse<List<Favourite>>> call, @NonNull Throwable t) {
                            isFavourite = false; // Mặc định là false nếu có lỗi mạng
                            isLoadingInitialFavouriteStatus = false;
                            updateFavouriteIcon();
                            updateLoadingState();
                            Log.e(TAG, "getFavourites network failure: " + t.getMessage(), t);
                            // Có thể hiển thị Toast cho người dùng nếu cần
                            // Toast.makeText(ProductDetailActivity.this, "Lỗi kiểm tra yêu thích.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                    isLoadingProduct = false;
                    showError("Lỗi mạng khi tải thông tin sản phẩm.");
                    updateLoadingState();
                }
            });
        }
        private void toggleFavouriteStatus() {
            if (currentProduct == null || productIdFromIntent == null) {
                Toast.makeText(this, "Thông tin sản phẩm không đầy đủ.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (authToken == null || authToken.isEmpty()) {
                Toast.makeText(this, "Bạn cần đăng nhập.", Toast.LENGTH_SHORT).show();
                return;
            }

            isTogglingFavourite = true;
            updateLoadingState();
            FavouriteRequest request = new FavouriteRequest(productIdFromIntent);

            if (isFavourite) {
                apiService.removeFavourite(request).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            isFavourite = false;
                            Toast.makeText(ProductDetailActivity.this, "Đã xóa khỏi yêu thích.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProductDetailActivity.this, "Lỗi khi xóa khỏi yêu thích.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "removeFavourite API error: " + response.code() + (response.body() != null ? " - " + response.body().getMessage() : ""));
                        }
                        finishToggleFavourite();
                    }
                    @Override
                    public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                        Toast.makeText(ProductDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        finishToggleFavourite();
                    }
                });
            } else {
                apiService.addFavourite(request).enqueue(new Callback<ApiResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            isFavourite = true;
                            Toast.makeText(ProductDetailActivity.this, "Đã thêm vào yêu thích.", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMessage = "Lỗi khi thêm vào yêu thích.";
                            if (response.code() == 400) {
                                try {
                                    if (response.errorBody() != null) {
                                        String errorBodyString = response.errorBody().string();
                                        // Kiểm tra xem có phải lỗi "đã tồn tại" không, nếu có thì cập nhật isFavourite = true
                                        // Dựa trên response thực tế của bạn cho lỗi "đã tồn tại"
                                        // Ví dụ: if (errorBodyString.contains("already in favourites"))
                                        if (errorBodyString.contains("Sản phẩm đã có trong danh sách yêu thích")) { // Cập nhật theo message thực tế
                                            isFavourite = true; // Set isFavourite to true if it was a 'duplicate' error
                                            errorMessage = "Sản phẩm này đã ở trong danh sách yêu thích!";
                                        }
                                        Log.e(TAG, "addFavourite API error (400): " + errorBodyString);
                                    } else {
                                        Log.e(TAG, "addFavourite API error (400) with no error body.");
                                    }
                                } catch (IOException e) {
                                    Log.e(TAG, "Error parsing 400 error body", e);
                                }
                            } else {
                                Log.e(TAG, "addFavourite API error: " + response.code() + (response.body() != null ? " - " + response.body().getMessage() : ""));
                            }
                            Toast.makeText(ProductDetailActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                        finishToggleFavourite();
                    }
                    @Override
                    public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                        Toast.makeText(ProductDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        finishToggleFavourite();
                    }
                });
            }
        }
        private void finishToggleFavourite() {
            isTogglingFavourite = false;
            updateFavouriteIcon();
            updateLoadingState();
        }

        private void updateFavouriteIcon() {
            if (isFinishing()) return;
            if (isFavourite) {
                ivFavourite.setImageResource(R.drawable.ic_heart_filled);
                ivFavourite.setColorFilter(ContextCompat.getColor(this, R.color.red));
            } else {
                ivFavourite.setImageResource(R.drawable.ic_heart_outline);
                ivFavourite.clearColorFilter();
            }
        }
        private void showError(String s) {
        }

        private void loadVariantDetails(String variantIdToLoad) {
            if (variantIdToLoad == null) return;
            isLoadingVariant = true;
            updateLoadingState();

            apiService.getVariantForProductById(productIdFromIntent, variantIdToLoad).enqueue(new Callback<Variant>() {
                @Override
                public void onResponse(@NonNull Call<Variant> call, @NonNull Response<Variant> response) {
                    isLoadingVariant = false;
                    if (response.isSuccessful() && response.body() != null) {
                        displayedVariant = response.body();
                        updateUIFromVariant();
                    } else {
                        showError("Không thể tải thông tin biến thể.");
                    }
                    updateLoadingState();
                }

                private void showError(String s) {
                }

                @Override
                public void onFailure(@NonNull Call<Variant> call, @NonNull Throwable t) {
                    isLoadingVariant = false;
                    showError("Lỗi mạng khi tải thông tin biến thể.");
                    updateLoadingState();
                }
            });
        }

        private void updateLoadingState() {
            boolean isLoading = isLoadingProduct || isLoadingVariant || isLoadingInitialFavouriteStatus || isTogglingFavourite;
            boolean dataLoaded = currentProduct != null && displayedVariant != null;
            btnAddToCart.setEnabled(!isLoading && dataLoaded);
            btnBuyNow.setEnabled(!isLoading && dataLoaded);
            ivFavourite.setEnabled(!isLoadingProduct && !isLoadingInitialFavouriteStatus && !isTogglingFavourite && currentProduct != null);
        }

        private void updateUIFromProduct() {
            if (currentProduct == null) return;
            tvProductName.setText(currentProduct.getName());
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(currentProduct.getName());

            String imageUrl = currentProduct.getImageUrlSafe();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this).load(imageUrl).placeholder(R.drawable.placeholder_product).into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.placeholder_product);
            }

            tvProductDescription.setText(currentProduct.getDescription() != null ? currentProduct.getDescription() : "Chưa có mô tả.");
        }

        private void updateUIFromVariant() {
            if (displayedVariant == null) return;
            displayPriceAndSpecs();
        }

        private void displayPriceAndSpecs() {
            double originalPrice = displayedVariant.getPrice();
            int discount = (currentProduct != null) ? currentProduct.getDiscount() : 0;

            if (discount > 0) {
                tvDiscountPercent.setVisibility(View.VISIBLE);
                tvOriginalPrice.setVisibility(View.VISIBLE);
                double discountedPrice = originalPrice * (100 - discount) / 100.0;
                tvOriginalPrice.setText(numberFormat.format(originalPrice));
                tvDiscountPrice.setText(numberFormat.format(discountedPrice));
                tvDiscountPercent.setText("-" + discount + "%");
                tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                tvDiscountPercent.setVisibility(View.GONE);
                tvOriginalPrice.setVisibility(View.GONE);
                tvDiscountPrice.setText(numberFormat.format(originalPrice));
                tvOriginalPrice.setPaintFlags(0);
            }

            int stock = displayedVariant.getQuantity();
            tvStock.setText(stock > 0 ? "Còn " + stock + " sản phẩm" : "Hết hàng");
            tvStock.setTextColor(ContextCompat.getColor(this, stock > 0 ? R.color.success_green : R.color.error_red));

            StringBuilder specsText = new StringBuilder();
            if (displayedVariant.getColor() != null && displayedVariant.getColor().getColorName() != null) {
                specsText.append("Màu sắc: ").append(displayedVariant.getColor().getColorName()).append("\n");
            }
            if (displayedVariant.getSize() != null && displayedVariant.getSize().getStorage() != null) {
                specsText.append("Dung lượng: ").append(displayedVariant.getSize().getStorage()).append("\n");
            }
            tvProductSpecs.setText(specsText.length() > 0 ? specsText.toString().trim() : "Chưa có thông số chi tiết.");
        }

        // (Giữ nguyên checkIfProductIsFavourite, toggleFavouriteStatus, addToCart, checkStockAndAddToCart, updateFavouriteIcon, showError...)
        // ...
    }
