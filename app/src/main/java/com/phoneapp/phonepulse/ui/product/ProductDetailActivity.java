package com.phoneapp.phonepulse.ui.product;

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
import androidx.core.content.ContextCompat; // Thêm import này

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants; // <-- Đảm bảo import Constants

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
    private TextView tvProductName, tvOriginalPrice, tvDiscountPrice, tvDiscountPercent, tvStock;
    private TextView tvProductDescription; // TextView cho mô tả sản phẩm
    private TextView tvProductSpecs; // TextView cho thông số kỹ thuật chi tiết
    private LinearLayout llColorOptions, llStorageOptions; // Layouts cho lựa chọn biến thể (hiện tại là tĩnh)
    private com.google.android.material.button.MaterialButton btnAddToCart, btnBuyNow;

    // Data
    private String variantId;
    private String productId;
    private Product currentProduct; // Đối tượng Product chứa thông tin chung của sản phẩm
    private Variant displayedVariant; // Đối tượng Variant chứa thông tin biến thể cụ thể
    private NumberFormat numberFormat;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Lấy variantId và productId từ Intent (Sử dụng Constants keys)
        variantId = getIntent().getStringExtra(Constants.VARIANT_ID);
        productId = getIntent().getStringExtra(Constants.PRODUCT_ID);

        // Kiểm tra xem dữ liệu có bị null không
        if (variantId == null || productId == null) {
            Toast.makeText(this, "Không tìm thấy biến thể hoặc sản phẩm. Dữ liệu không hợp lệ.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "onCreate: Missing variantId or productId. variantId: " + variantId + ", productId: " + productId);
            finish(); // Đóng activity nếu không có dữ liệu cần thiết
            return;
        }

        Log.d(TAG, "onCreate: Variant ID received: " + variantId + ", Product ID received: " + productId);

        initViews();
        setupToolbar();
        // Lấy token để gọi API (nếu API yêu cầu xác thực)
        apiService = RetrofitClient.getApiService(Constants.getToken(this));
        numberFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN")); // Định dạng tiền tệ Việt Nam
        numberFormat.setMaximumFractionDigits(0); // Không hiển thị phần thập phân cho tiền tệ

        loadProductAndVariantDetails();
    }

    /**
     * Khởi tạo và ánh xạ các thành phần UI từ layout.
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar_product_detail);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        tvOriginalPrice = findViewById(R.id.tv_original_price);
        tvDiscountPrice = findViewById(R.id.tv_discount_price);
        tvDiscountPercent = findViewById(R.id.tv_discount_percent);
        tvProductDescription = findViewById(R.id.tv_product_description); // Ánh xạ TextView mô tả
        tvProductSpecs = findViewById(R.id.tv_product_specs); // Ánh xạ TextView thông số kỹ thuật
        tvStock = findViewById(R.id.tv_stock);
        btnAddToCart = findViewById(R.id.btn_add_to_cart);
        btnBuyNow = findViewById(R.id.btn_buy_now);

        llColorOptions = findViewById(R.id.ll_color_options);
        llStorageOptions = findViewById(R.id.ll_storage_options);

        // Đặt lắng nghe sự kiện click cho các nút
        btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                Toast.makeText(ProductDetailActivity.this, "Thêm " + currentProduct.getName() + " vào giỏ hàng", Toast.LENGTH_SHORT).show();
                // TODO: Triển khai logic thêm vào giỏ hàng
            } else {
                Toast.makeText(ProductDetailActivity.this, "Thông tin sản phẩm chưa tải xong.", Toast.LENGTH_SHORT).show();
            }
        });
        btnBuyNow.setOnClickListener(v -> {
            if (currentProduct != null) {
                Toast.makeText(ProductDetailActivity.this, "Mua ngay " + currentProduct.getName(), Toast.LENGTH_SHORT).show();
                // TODO: Triển khai logic mua ngay
            } else {
                Toast.makeText(ProductDetailActivity.this, "Thông tin sản phẩm chưa tải xong.", Toast.LENGTH_SHORT).show();
            }
        });

        Log.d(TAG, "initViews: Tất cả các views đã được khởi tạo.");
    }

    /**
     * Thiết lập Toolbar cho Activity, bao gồm nút quay lại.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Đang tải..."); // Tiêu đề tạm thời
        }
        // Xử lý sự kiện click cho nút quay lại trên Toolbar
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Log.d(TAG, "setupToolbar: Toolbar đã được cấu hình.");
    }

    /**
     * Tải chi tiết sản phẩm và chi tiết biến thể từ API.
     */
    private void loadProductAndVariantDetails() {
        loadProductDetails(); // Tải chi tiết sản phẩm
        loadVariantDetails(); // Tải chi tiết biến thể
    }

    /**
     * Lấy chi tiết sản phẩm (tên, hình ảnh, mô tả, giảm giá) bằng productId.
     */
    private void loadProductDetails() {
        Log.d(TAG, "loadProductDetails: Đang lấy chi tiết sản phẩm với ID: " + productId);
        apiService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentProduct = response.body();
                    Log.d(TAG, "onResponse: Tải chi tiết sản phẩm thành công: " + currentProduct.getName());
                    updateUIFromProduct(); // Cập nhật UI từ thông tin Product
                    // Nếu dữ liệu biến thể đã được tải, gọi lại displayPriceAndSpecs để đồng bộ giá/thông số
                    if (displayedVariant != null) {
                        displayPriceAndSpecs();
                    }
                } else {
                    String errorMsg = "Không thể tải chi tiết sản phẩm. Mã lỗi: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi phân tích body lỗi cho chi tiết sản phẩm", e);
                    }
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
     * Lấy chi tiết biến thể (giá, số lượng, màu sắc, kích thước) bằng productId và variantId.
     */
    private void loadVariantDetails() {
        Log.d(TAG, "loadVariantDetails: Đang lấy chi tiết biến thể cho Sản phẩm ID: " + productId + ", Biến thể ID: " + variantId);
        apiService.getVariantForProductById(productId, variantId).enqueue(new Callback<Variant>() {
            @Override
            public void onResponse(@NonNull Call<Variant> call, @NonNull Response<Variant> response) {
                if (response.isSuccessful() && response.body() != null) {
                    displayedVariant = response.body();
                    Log.d(TAG, "onResponse: Tải chi tiết biến thể thành công cho ID: " + displayedVariant.getId());
                    updateUIFromVariant(); // Cập nhật UI từ thông tin Variant
                    // Nếu dữ liệu sản phẩm đã được tải, gọi lại displayPriceAndSpecs để đồng bộ giá/thông số
                    if (currentProduct != null) {
                        displayPriceAndSpecs();
                    }
                } else {
                    String errorMsg = "Không thể tải chi tiết biến thể. Mã lỗi: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi phân tích body lỗi cho chi tiết biến thể", e);
                    }
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
     * Cập nhật các thành phần UI phụ thuộc vào đối tượng Product (tên, hình ảnh, mô tả).
     */
    private void updateUIFromProduct() {
        if (currentProduct == null) {
            Log.w(TAG, "updateUIFromProduct: currentProduct is null, không thể cập nhật UI.");
            return;
        }

        // Cập nhật tên sản phẩm và tiêu đề toolbar
        String productName = currentProduct.getName() != null ? currentProduct.getName() : "Tên sản phẩm không xác định";
        tvProductName.setText(productName);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(productName);
        }

        // Cập nhật hình ảnh sản phẩm
        String imageUrl = currentProduct.getImageUrlSafe(); // Sử dụng hàm an toàn đã có trong Product
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_product)
                    .error(R.drawable.placeholder_product)
                    .into(ivProductImage);
            Log.d(TAG, "updateUIFromProduct: URL hình ảnh sản phẩm: " + imageUrl);
        } else {
            ivProductImage.setImageResource(R.drawable.placeholder_product);
            Log.w(TAG, "updateUIFromProduct: Không tìm thấy URL hình ảnh cho sản phẩm: " + productName + ". Sử dụng ảnh placeholder.");
        }

        // Cập nhật mô tả sản phẩm
        if (currentProduct.getDescription() != null && !currentProduct.getDescription().isEmpty()) {
            tvProductDescription.setText(currentProduct.getDescription());
            tvProductDescription.setVisibility(View.VISIBLE);
        } else {
            tvProductDescription.setText("Chưa có mô tả chi tiết cho sản phẩm này.");
            tvProductDescription.setVisibility(View.VISIBLE); // Giữ hiển thị với văn bản mặc định
        }

        // --- CẬP NHẬT PHẦN THÔNG SỐ KỸ THUẬT (tvProductSpecs) ---
        // Dựa trên model Product hiện tại của bạn, không có trường 'specs' hay 'specsDetails'.
        // Bạn cần quyết định dữ liệu nào sẽ hiển thị ở đây.
        // Giả định: Backend của bạn có thể trả về thông số kỹ thuật dưới dạng một chuỗi trong `description`
        // HOẶC bạn cần thêm một trường `specs` vào model `Product` hoặc `Variant` của bạn.
        // TẠM THỜI: Để tránh lỗi, tôi sẽ đặt text mặc định.
        // HOẶC nếu bạn muốn hiển thị thông tin màu sắc/dung lượng ở đây, bạn có thể làm như sau:
        StringBuilder specsText = new StringBuilder();
        if (displayedVariant != null) {
            // Hiển thị thông tin từ biến thể nếu có
            if (displayedVariant.getColor() != null && displayedVariant.getColor().getColorName() != null) {
                specsText.append("Màu sắc: ").append(displayedVariant.getColor().getColorName()).append("\n");
            }
            if (displayedVariant.getSize() != null && displayedVariant.getSize().getStorage() != null) {
                specsText.append("Dung lượng: ").append(displayedVariant.getSize().getStorage()).append("\n");
            }
            // Thêm các thông số khác nếu có trong Variant model của bạn
        }

        // Nếu Product có trường `specsDetails` (như đã thảo luận trước đó), bạn sẽ dùng nó ở đây:
        // if (currentProduct.getSpecsDetails() != null && !currentProduct.getSpecsDetails().isEmpty()) {
        //     specsText.append(currentProduct.getSpecsDetails());
        // }

        if (specsText.length() > 0) {
            tvProductSpecs.setText(specsText.toString().trim());
            tvProductSpecs.setVisibility(View.VISIBLE);
        } else {
            tvProductSpecs.setText("Chưa có thông số kỹ thuật chi tiết.");
            tvProductSpecs.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Cập nhật các thành phần UI phụ thuộc vào đối tượng Variant (giá, số lượng, màu sắc, kích thước).
     */
    private void updateUIFromVariant() {
        if (displayedVariant == null) {
            Log.w(TAG, "updateUIFromVariant: displayedVariant is null, không thể cập nhật UI.");
            return;
        }
        // Gọi hàm hiển thị giá, giảm giá, và số lượng tồn kho
        displayPriceAndSpecs();

        // Cập nhật lại phần specs (tvProductSpecs) sau khi có dữ liệu variant
        updateUIFromProduct(); // Gọi lại hàm này để nó cũng cập nhật specs từ variant nếu cần
    }

    /**
     * Hiển thị giá, giảm giá, và số lượng tồn kho.
     * Hàm này kết hợp dữ liệu từ cả đối tượng Product và Variant.
     */
    private void displayPriceAndSpecs() {
        if (displayedVariant == null) {
            Log.w(TAG, "displayPriceAndSpecs: displayedVariant is null. Không thể hiển thị giá và thông số.");
            return;
        }

        double originalPrice = displayedVariant.getPrice();
        int discount = 0;
        if (currentProduct != null) {
            discount = currentProduct.getDiscount(); // Lấy giảm giá từ đối tượng Product
        }

        if (discount > 0) {
            tvDiscountPercent.setVisibility(View.VISIBLE);
            tvOriginalPrice.setVisibility(View.VISIBLE);
            double discountedPrice = originalPrice * (100 - discount) / 100.0;

            tvOriginalPrice.setText(numberFormat.format(originalPrice)); // Định dạng giá gốc
            tvDiscountPrice.setText(numberFormat.format(discountedPrice)); // Định dạng giá đã giảm
            tvDiscountPercent.setText("-" + discount + "%");

            // Áp dụng gạch ngang cho giá gốc
            tvOriginalPrice.setPaintFlags(tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            tvDiscountPercent.setVisibility(View.GONE);
            tvOriginalPrice.setVisibility(View.GONE);
            tvDiscountPrice.setText(numberFormat.format(originalPrice)); // Hiển thị giá không giảm
            tvOriginalPrice.setPaintFlags(0); // Bỏ gạch ngang nếu không có giảm giá
        }
        Log.d(TAG, "displayPriceAndSpecs: Giá đã cập nhật. Gốc: " + originalPrice + ", Giảm giá: " + discount);

        // Cập nhật trạng thái tồn kho
        int stock = displayedVariant.getQuantity();
        if (stock > 0) {
            tvStock.setText("Còn " + stock + " sản phẩm");
            tvStock.setTextColor(ContextCompat.getColor(this, R.color.green)); // Sử dụng ContextCompat
        } else {
            tvStock.setText("Hết hàng");
            tvStock.setTextColor(ContextCompat.getColor(this, R.color.error_red)); // Sử dụng ContextCompat
        }
        Log.d(TAG, "displayPriceAndSpecs: Tồn kho đã cập nhật: " + stock);
    }

    /**
     * Xử lý các sự kiện click menu item (ví dụ: nút quay lại trên toolbar).
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Quay lại khi nhấn nút quay lại
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Hiển thị tin nhắn Toast ngắn gọn cho các lỗi.
     * @param msg Tin nhắn để hiển thị.
     */
    private void showError(String msg) {
        if (!isFinishing()) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "showError: Hiển thị Toast: " + msg);
        }
    }
}