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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.phoneapp.phonepulse.Adapter.CartAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.VIEW.Oder_Activity;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.CartRequest; // Import CartRequest để sử dụng các nested class
import com.phoneapp.phonepulse.request.OrderItem;
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
    private List<OrderItem> orderItemList = new ArrayList<>();
    private double totalPrice = 0;



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
            Intent intent = new Intent(Cart_Activity.this, Oder_Activity.class);
            intent.putParcelableArrayListExtra("order_items", new ArrayList<>(orderItemList));
            intent.putExtra("total_price", totalPrice);
            startActivity(intent);
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

                            // 🔧 Chuẩn bị orderItemList
                            orderItemList.clear();
                            for (CartItem cartItem : currentCartItems) {
                                if (cartItem.getProduct() != null && cartItem.getVariant() != null) {
                                    String name = cartItem.getProduct().getProductName();
                                    String imageUrl = cartItem.getProductImage();
                                    int price = (int) cartItem.getVariant().getPrice();
                                    int quantity = cartItem.getQuantity();


                                    orderItemList.add(new OrderItem(name, imageUrl, price, quantity));
                                } else {
                                }
                            }

                            showEmptyCartView(false);
                            updateTotalPrice();
                        } else {
                            currentCartItems.clear();
                            cartAdapter.setCartItemList(currentCartItems);
                            orderItemList.clear();
                            showEmptyCartView(true);
                            updateTotalPrice();
                        }
                    } else {
                        Toast.makeText(Cart_Activity.this, apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi khi lấy giỏ hàng", Toast.LENGTH_SHORT).show();
                        showEmptyCartView(true);
                    }
                } else {
                    String errorBodyString = "N/A";
                    try {
                        if (response.errorBody() != null) {
                            errorBodyString = response.errorBody().string();
                        }
                    } catch (Exception e) {

                    }
                    Toast.makeText(Cart_Activity.this, "Lỗi kết nối hoặc phản hồi server: " + response.code(), Toast.LENGTH_SHORT).show();

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
        totalPrice = 0;
        int totalItems = 0;
        for (CartItem item : currentCartItems) {
            if (item.getVariant() != null && item.getProduct() != null) {
                totalPrice += item.getVariant().getPrice() * item.getQuantity();
                totalItems += item.getQuantity();
            }
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);
        tvTotalPrice.setText(currencyFormat.format(totalPrice));
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

    // --- Triển khai CartAdapter.OnCartItemActionListener ---
    @Override
    public void onQuantityChange(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            // Nếu số lượng là 0 hoặc âm, Adapter đã xử lý hiển thị dialog xóa.
            // Nếu người dùng xác nhận xóa, onRemoveItem sẽ được gọi.
            // Ở đây, chúng ta chỉ cần đảm bảo UI không hiển thị số lượng âm/0 nếu người dùng chưa xác nhận xóa.
            cartAdapter.notifyDataSetChanged(); // Đảm bảo UI khớp với dữ liệu gốc nếu không có hành động API
            return;
        }

        // Đảm bảo item.getProduct() và item.getVariant() không null
        if (item.getProduct() == null || item.getVariant() == null) {
            Toast.makeText(this, "Không thể cập nhật: Dữ liệu sản phẩm bị thiếu.", Toast.LENGTH_SHORT).show();
            cartAdapter.notifyDataSetChanged(); // Cập nhật lại UI để số lượng không bị thay đổi ảo
            return;
        }

        String productId = item.getProduct().getId();
        String variantId = item.getVariant().getId();
        String token = Constants.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để cập nhật giỏ hàng.", Toast.LENGTH_LONG).show();
            cartAdapter.notifyDataSetChanged(); // Cập nhật lại UI để số lượng không bị thay đổi ảo
            return;
        }

        // Tạo request body
        CartRequest.UpdateCartItem request = new CartRequest.UpdateCartItem(productId, variantId, newQuantity);

        // Đảm bảo apiService được khởi tạo với token hiện tại
        apiService = RetrofitClient.getApiService(token);

        progressBar.setVisibility(View.VISIBLE);
        apiService.updateCartItem(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Cart_Activity.this, "Cập nhật số lượng thành công!", Toast.LENGTH_SHORT).show();
                    // Gọi lại fetchCartData để tải lại toàn bộ giỏ hàng và cập nhật UI một cách nhất quán
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
                    cartAdapter.notifyDataSetChanged(); // Cập nhật lại UI để số lượng không bị thay đổi ảo nếu lỗi
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Cart_Activity.this, "Lỗi mạng khi cập nhật số lượng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Cart_Activity", "Update quantity network failure: ", t);
                cartAdapter.notifyDataSetChanged(); // Cập nhật lại UI để số lượng không bị thay đổi ảo nếu lỗi mạng
            }
        });
    }

    @Override
    public void onRemoveItem(CartItem item) {
        // Đảm bảo item.getProduct() và item.getVariant() không null
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

        // Tạo request body
        CartRequest.RemoveCartItem request = new CartRequest.RemoveCartItem(productId, variantId);

        // Đảm bảo apiService được khởi tạo với token hiện tại
        apiService = RetrofitClient.getApiService(token);

        progressBar.setVisibility(View.VISIBLE);
        apiService.removeFromCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Cart_Activity.this, "Xóa sản phẩm khỏi giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                    // Gọi lại fetchCartData để tải lại toàn bộ giỏ hàng và cập nhật UI một cách nhất quán
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