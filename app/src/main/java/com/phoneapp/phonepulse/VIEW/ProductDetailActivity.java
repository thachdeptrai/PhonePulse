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
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
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
            if (currentProduct == null) {
                Toast.makeText(ProductDetailActivity.this, "Thông tin sản phẩm chưa tải xong.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (displayedVariant == null) {
                Toast.makeText(ProductDetailActivity.this, "Vui lòng chọn biến thể trước khi thêm vào giỏ.", Toast.LENGTH_SHORT).show();
                return;
            }
            checkStockAndAddToCart(currentProduct.getId(), displayedVariant.getId(), 1);
        });

        btnBuyNow.setOnClickListener(v -> {
            if (currentProduct == null) {
                Toast.makeText(ProductDetailActivity.this, "Thông tin sản phẩm chưa tải xong.", Toast.LENGTH_SHORT).show();
                return;
            }
            // ✅ Chưa chọn biến thể thì không cho mua
            if (displayedVariant == null) {
                Toast.makeText(ProductDetailActivity.this, "Vui lòng chọn biến thể trước khi mua.", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ Kiểm tra tồn kho trước khi cho phép mua
            if (displayedVariant.getQuantity() <= 0) {
                Toast.makeText(ProductDetailActivity.this, "❌ Sản phẩm này đã hết hàng.", Toast.LENGTH_LONG).show();
                return;
            }

            // Ưu tiên ảnh từ variant, fallback ảnh product
            String imageUrl = (displayedVariant.getImages() != null
                    && !displayedVariant.getImages().isEmpty())
                    ? displayedVariant.getImages().get(0)
                    : currentProduct.getImageUrlSafe();

            OrderItem orderItem = new OrderItem(
                    null,                                   // id
                    currentProduct.getName(),               // name
                    imageUrl,                               // imageUrl
                    (int) displayedVariant.getPrice(),      // price
                    1,                                      // quantity
                    displayedVariant.getColor() != null ? displayedVariant.getColor().getColorName() : "",
                    currentProduct.getId(),                 // productId
                    displayedVariant.getId()                // variantId
            );

            ArrayList<OrderItem> orderItems = new ArrayList<>();
            orderItems.add(orderItem);

            double totalPrice = displayedVariant.getPrice();

            Intent intent = new Intent(ProductDetailActivity.this, Oder_Activity.class);
            intent.putParcelableArrayListExtra("order_items", orderItems);
            intent.putExtra("total_price", totalPrice);
            startActivity(intent);
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
        if (authToken == null || authToken.trim().isEmpty()) {
            Toast.makeText(ProductDetailActivity.this, "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_LONG).show();
            return;
        }

        // Tạo request body
        CartRequest.AddToCart request = new CartRequest.AddToCart(productId, variantId, quantity);

        // Gọi API
        apiService.addToCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Cart>> call,
                                   @NonNull Response<ApiResponse<Cart>> response) {
                if (response.isSuccessful()) {
                    ApiResponse<Cart> apiResponse = response.body();
                    if (apiResponse != null && apiResponse.isSuccess()) {
                        Toast.makeText(ProductDetailActivity.this, "✅ Thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "AddToCart success: " + apiResponse.getData());
                    } else {
                        String errorMsg = (apiResponse != null && apiResponse.getMessage() != null)
                                ? apiResponse.getMessage()
                                : "Không thể thêm sản phẩm vào giỏ hàng.";
                        Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        Log.e(TAG, "AddToCart failed - " + errorMsg);
                    }
                } else {
                    // Trường hợp response không thành công (400, 500,...)
                    String errorMsg = "Máy chủ trả về lỗi: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                    Toast.makeText(ProductDetailActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "AddToCart API error: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Cart>> call, @NonNull Throwable t) {
                Toast.makeText(ProductDetailActivity.this, "❌ Lỗi kết nối mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "AddToCart network failure", t);
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

                    if (currentProduct.getVariants() != null && !currentProduct.getVariants().isEmpty()) {
                        // Tìm biến thể ban đầu từ Intent
                        Variant initialVariant = null;
                        if (initialVariantIdFromIntent != null) {
                            for (Variant v : currentProduct.getVariants()) {
                                if (v.getId().equals(initialVariantIdFromIntent)) {
                                    initialVariant = v;
                                    break;
                                }
                            }
                        }
                        // Nếu không tìm thấy, mặc định lấy biến thể đầu tiên
                        if (initialVariant == null) {
                            initialVariant = currentProduct.getVariants().get(0);
                        }

                        // Khởi tạo adapter với danh sách biến thể
                        variantAdapter = new VariantAdapter(currentProduct.getVariants(), variant -> {
                            if (variant != null) {
                                loadVariantDetails(variant.getId());
                            } else {
                                // Xử lý khi người dùng bỏ chọn biến thể
                                displayedVariant = null;
                                updateUIFromUnselectedState();
                                updateLoadingState();
                            }
                        });
                        rvColorOptions.setAdapter(variantAdapter);

                        // Load thông tin của biến thể ban đầu
                        if (initialVariant != null) {
                            loadVariantDetails(initialVariant.getId());
                            // Cần một cách để cập nhật UI của Adapter
                            // Hiện tại Adapter đã có logic để cập nhật khi click
                            // Để chọn ban đầu, bạn có thể thêm một phương thức vào Adapter
                            // Ví dụ: variantAdapter.setSelectedVariant(initialVariant);
                        }
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

                apiService.getFavourites().enqueue(new Callback<ApiResponse<List<Favourite>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<List<Favourite>>> call,
                                           @NonNull Response<ApiResponse<List<Favourite>>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            List<Favourite> favouriteEntries = response.body().getData();
                            isFavourite = false;
                            if (favouriteEntries != null) {
                                for (Favourite favEntry : favouriteEntries) {
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
                            isFavourite = false;
                            Log.e(TAG, "getFavourites API error: " + response.code() +
                                    (response.body() != null ? " - " + response.body().getMessage() : " (No error body)"));
                        }
                        isLoadingInitialFavouriteStatus = false;
                        updateFavouriteIcon();
                        updateLoadingState();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<List<Favourite>>> call, @NonNull Throwable t) {
                        isFavourite = false;
                        isLoadingInitialFavouriteStatus = false;
                        updateFavouriteIcon();
                        updateLoadingState();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                isLoadingProduct = false;
                updateLoadingState();
                showError("Lỗi kết nối. Vui lòng thử lại.");
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
                                }
                            } catch (IOException e) {
                                Log.e(TAG, "Error parsing 400 error body", e);
                            }
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
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }

    private void updateUIFromUnselectedState() {
        tvProductSpecs.setText("Chưa chọn biến thể");
        tvDiscountPrice.setText("—");
        tvOriginalPrice.setText("—");
        tvOriginalPrice.setVisibility(View.GONE);
        tvDiscountPercent.setVisibility(View.GONE);
        tvStock.setText("Chưa chọn biến thể");
        tvStock.setTextColor(ContextCompat.getColor(this, android.R.color.black));
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
        if (displayedVariant == null) return;

        double originalPrice = displayedVariant.getPrice();
        int discount = (currentProduct != null) ? currentProduct.getDiscount() : 0;

        // ✅ Xử lý hiển thị giá và giảm giá
        if (discount > 0) {
            tvDiscountPercent.setVisibility(View.VISIBLE);
            tvOriginalPrice.setVisibility(View.VISIBLE);

            double discountedPrice = originalPrice * (100 - discount) / 100.0;

            tvOriginalPrice.setText(numberFormat.format(originalPrice) + "₫");
            tvDiscountPrice.setText(numberFormat.format(discountedPrice) + "₫");
            tvDiscountPercent.setText("-" + discount + "%");

            // gạch ngang giá gốc
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvDiscountPercent.setVisibility(View.GONE);
            tvOriginalPrice.setVisibility(View.GONE);

            tvDiscountPrice.setText(numberFormat.format(originalPrice) + "");

            // bỏ gạch ngang nếu có từ trước
            tvOriginalPrice.setPaintFlags(0);
        }

        // ✅ Xử lý tồn kho
        int stock = displayedVariant.getQuantity();
        if (stock > 0) {
            tvStock.setText("Còn " + stock + " sản phẩm");
            tvStock.setTextColor(ContextCompat.getColor(this, R.color.success_green));

            btnBuyNow.setEnabled(true);
            btnAddToCart.setEnabled(true);
        } else {
            tvStock.setText("Hết hàng");
            tvStock.setTextColor(ContextCompat.getColor(this, R.color.error_red));

            btnBuyNow.setEnabled(false);
            btnAddToCart.setEnabled(false);
        }

        // ✅ Xử lý thông số kỹ thuật
        StringBuilder specsText = new StringBuilder();

        if (displayedVariant.getColor() != null && displayedVariant.getColor().getColorName() != null) {
            specsText.append("Màu sắc: ").append(displayedVariant.getColor().getColorName()).append("\n");
        }
        if (displayedVariant.getSize() != null && displayedVariant.getSize().getStorage() != null) {
            specsText.append("Dung lượng: ").append(displayedVariant.getSize().getStorage()).append("\n");
        }

        if (specsText.length() > 0) {
            tvProductSpecs.setText(specsText.toString().trim());
        } else {
            tvProductSpecs.setText("Chưa có thông số chi tiết.");
        }
    }



}