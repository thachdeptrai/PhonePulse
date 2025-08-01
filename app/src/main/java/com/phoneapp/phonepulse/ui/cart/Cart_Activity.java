package com.phoneapp.phonepulse.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager; // Import FragmentManager
import androidx.fragment.app.FragmentTransaction; // Import FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.CartAdapter;
import com.phoneapp.phonepulse.FRAGMENT.CheckoutFragment;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.CartRequest; // Import CartRequest để sử dụng các nested class
import com.phoneapp.phonepulse.utils.Constants;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart_Activity extends AppCompatActivity implements CartAdapter.OnCartItemActionListener {

    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> currentCartItems;
    private LinearLayout emptyCartView;
    private LinearLayout cartActionBar;
    private LinearLayout bottomCheckoutBar;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private Button btnShopNow;
    private ProgressBar progressBar;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Ánh xạ View
        Toolbar toolbar = findViewById(R.id.toolbar_cart);
        rvCartItems = findViewById(R.id.rv_cart_items);
        emptyCartView = findViewById(R.id.empty_cart_view);
        cartActionBar = findViewById(R.id.cart_action_bar);
        bottomCheckoutBar = findViewById(R.id.bottom_checkout_bar);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnShopNow = findViewById(R.id.btn_shop_now);
        progressBar = findViewById(R.id.progressBar);

        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Giỏ hàng của bạn");
        }

        // Khởi tạo RecyclerView
        currentCartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(currentCartItems);
        cartAdapter.setOnCartItemActionListener(this); // Đặt listener cho Adapter
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        // Xử lý nút "Mua sắm ngay" (khi giỏ hàng trống)
        btnShopNow.setOnClickListener(v -> {
            finish();
        });

        // Xử lý nút "Thanh toán"
        btnCheckout.setOnClickListener(v -> {
            // Kiểm tra nếu giỏ hàng không rỗng trước khi chuyển màn hình
            if (currentCartItems != null && !currentCartItems.isEmpty()) {
                // Sử dụng FragmentManager để chuyển sang CheckoutFragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                // Khởi tạo CheckoutFragment và truyền dữ liệu cần thiết (ví dụ: danh sách sản phẩm)
                CheckoutFragment checkoutFragment = new CheckoutFragment();

                // Thay thế container hiện tại bằng CheckoutFragment
                fragmentTransaction.replace(R.id.fragment_container, checkoutFragment);

                // Thêm vào back stack để người dùng có thể quay lại giỏ hàng
                fragmentTransaction.addToBackStack(null);

                // Thực thi transaction
                fragmentTransaction.commit();

                // Ẩn thanh công cụ của giỏ hàng và thanh toán
                cartActionBar.setVisibility(View.GONE);
                bottomCheckoutBar.setVisibility(View.GONE);
            } else {
                Toast.makeText(this, "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            }
        });

        // Tải dữ liệu giỏ hàng khi activity được tạo
        fetchCartData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu giỏ hàng mỗi khi Activity trở lại foreground
        fetchCartData();
    }

    // ... (các phương thức khác: fetchCartData, updateTotalPrice, showEmptyCartView)
    private void fetchCartData() {
        String token = Constants.getToken(this); // Lấy token từ Constants

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng.", Toast.LENGTH_LONG).show();
            showEmptyCartView(true);
            return;
        }

        // Khởi tạo apiService với token hiện tại
        apiService = RetrofitClient.getApiService(token);

        progressBar.setVisibility(View.VISIBLE);
        Call<ApiResponse<Cart>> call = apiService.getCart(); // Gọi getCart() không tham số token
        call.enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Cart> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Cart cart = apiResponse.getData();
                        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                            currentCartItems.clear();
                            currentCartItems.addAll(cart.getItems());
                            cartAdapter.setCartItemList(currentCartItems);
                            showEmptyCartView(false);
                            updateTotalPrice();
                        } else {
                            currentCartItems.clear();
                            cartAdapter.setCartItemList(currentCartItems);
                            showEmptyCartView(true);
                            updateTotalPrice();
                        }
                    } else {
                        Toast.makeText(Cart_Activity.this, apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi khi lấy giỏ hàng", Toast.LENGTH_SHORT).show();
                        showEmptyCartView(true);
                        Log.e("Cart_Activity", "API Response: " + apiResponse.getMessage());
                    }
                } else {
                    String errorBodyString = "N/A";
                    try {
                        if (response.errorBody() != null) {
                            errorBodyString = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("Cart_Activity", "Error reading errorBody", e);
                    }
                    Toast.makeText(Cart_Activity.this, "Lỗi kết nối hoặc phản hồi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    showEmptyCartView(true);
                    Log.e("Cart_Activity", "HTTP Error: " + response.code() + " - " + response.message() + " - " + errorBodyString);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Cart_Activity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyCartView(true);
                Log.e("Cart_Activity", "Network Failure: " + t.getMessage(), t);
            }
        });
    }

    private void updateTotalPrice() {
        double total = 0;
        int totalItems = 0;
        for (CartItem item : currentCartItems) {
            if (item.getVariant() != null && item.getProduct() != null) {
                total += item.getVariant().getPrice() * item.getQuantity();
                totalItems += item.getQuantity();
            }
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);
        tvTotalPrice.setText(currencyFormat.format(total));
        btnCheckout.setText("Thanh toán (" + totalItems + ")");
    }

    private void showEmptyCartView(boolean isEmpty) {
        if (isEmpty) {
            emptyCartView.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            cartActionBar.setVisibility(View.GONE);
            bottomCheckoutBar.setVisibility(View.GONE);
        } else {
            emptyCartView.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            cartActionBar.setVisibility(View.VISIBLE);
            bottomCheckoutBar.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ... (Triển khai CartAdapter.OnCartItemActionListener không thay đổi)
    @Override
    public void onQuantityChange(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            cartAdapter.notifyDataSetChanged();
            return;
        }

        if (item.getProduct() == null || item.getVariant() == null) {
            Log.e("Cart_Activity", "Product or Variant data missing for item during quantity change.");
            Toast.makeText(this, "Không thể cập nhật: Dữ liệu sản phẩm bị thiếu.", Toast.LENGTH_SHORT).show();
            cartAdapter.notifyDataSetChanged();
            return;
        }

        String productId = item.getProduct().getId();
        String variantId = item.getVariant().getId();
        String token = Constants.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để cập nhật giỏ hàng.", Toast.LENGTH_LONG).show();
            cartAdapter.notifyDataSetChanged();
            return;
        }

        CartRequest.UpdateCartItem request = new CartRequest.UpdateCartItem(productId, variantId, newQuantity);
        apiService = RetrofitClient.getApiService(token);

        progressBar.setVisibility(View.VISIBLE);
        apiService.updateCartItem(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Cart_Activity.this, "Cập nhật số lượng thành công!", Toast.LENGTH_SHORT).show();
                    fetchCartData();
                } else {
                    String errorMsg = "Lỗi khi cập nhật số lượng.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            errorMsg += " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("Cart_Activity", "Error parsing error body for update quantity", e);
                        }
                    }
                    Toast.makeText(Cart_Activity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("Cart_Activity", "Update quantity API failed: " + response.code() + " - " + errorMsg);
                    cartAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Cart_Activity.this, "Lỗi mạng khi cập nhật số lượng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Cart_Activity", "Update quantity network failure: ", t);
                cartAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onRemoveItem(CartItem item) {
        if (item.getProduct() == null || item.getVariant() == null) {
            Log.e("Cart_Activity", "Product or Variant data missing for item during removal.");
            Toast.makeText(this, "Không thể xóa: Dữ liệu sản phẩm bị thiếu.", Toast.LENGTH_SHORT).show();
            return;
        }

        String productId = item.getProduct().getId();
        String variantId = item.getVariant().getId();
        String token = Constants.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để xóa sản phẩm khỏi giỏ hàng.", Toast.LENGTH_LONG).show();
            return;
        }

        CartRequest.RemoveCartItem request = new CartRequest.RemoveCartItem(productId, variantId);
        apiService = RetrofitClient.getApiService(token);

        progressBar.setVisibility(View.VISIBLE);
        apiService.removeFromCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Cart_Activity.this, "Xóa sản phẩm khỏi giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                    fetchCartData();
                } else {
                    String errorMsg = "Lỗi khi xóa sản phẩm khỏi giỏ hàng.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            errorMsg += " " + response.errorBody().string();
                        } catch (Exception e) {
                            Log.e("Cart_Activity", "Error parsing error body for remove item", e);
                        }
                    }
                    Toast.makeText(Cart_Activity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("Cart_Activity", "Remove item API failed: " + response.code() + " - " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Cart_Activity.this, "Lỗi mạng khi xóa sản phẩm: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Cart_Activity", "Remove item network failure: ", t);
            }
        });
    }

    @Override
    public void onItemSelected(CartItem item, boolean isSelected) {
        // Logic cho checkbox chọn item (nếu được triển khai)
    }
}