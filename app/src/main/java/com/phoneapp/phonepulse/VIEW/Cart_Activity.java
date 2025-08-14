package com.phoneapp.phonepulse.VIEW;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.CartRequest;
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
    private LinearLayout bottomCheckoutBar;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private Button btnShopNow;
    private ProgressBar progressBar;

    private LinearLayout llClearCart;
    private CheckBox cbSelectAll;
    private TextView tvClearCart;

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
        bottomCheckoutBar = findViewById(R.id.bottom_checkout_bar);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnShopNow = findViewById(R.id.btn_shop_now);
        progressBar = findViewById(R.id.progressBar);

        // Ánh xạ các view mới
        llClearCart = findViewById(R.id.ll_clear_cart);
        cbSelectAll = findViewById(R.id.cb_select_all);
        tvClearCart = findViewById(R.id.tv_clear_cart);

        // Thiết lập toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Giỏ hàng của bạn");
        }

        // Khởi tạo RecyclerView
        currentCartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(currentCartItems);
        cartAdapter.setOnCartItemActionListener(this);
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        // Xử lý nút "Mua sắm ngay" (khi giỏ hàng trống)
        btnShopNow.setOnClickListener(v -> finish());

        // Xử lý nút "Thanh toán"
        btnCheckout.setOnClickListener(v -> {
            if (currentCartItems.isEmpty()) {
                Toast.makeText(Cart_Activity.this, "Giỏ hàng của bạn đang trống.", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(Cart_Activity.this, Oder_Activity.class);
            intent.putParcelableArrayListExtra("order_items", new ArrayList<>(orderItemList));
            intent.putExtra("total_price", totalPrice);
            startActivityForResult(intent, 1001);
        });

        // Xử lý khi nhấn vào LinearLayout "Xóa giỏ hàng"
        llClearCart.setOnClickListener(v -> {
            if (currentCartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đã trống.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Toggle checkbox và hiển thị dialog
            cbSelectAll.setChecked(!cbSelectAll.isChecked());
            if (cbSelectAll.isChecked()) {
                showClearCartConfirmationDialog();
            }
        });

        // Tải dữ liệu giỏ hàng khi activity được tạo
        fetchCartData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchCartData();
    }

    private void fetchCartData() {
        String token = Constants.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng.", Toast.LENGTH_LONG).show();
            showEmptyCartView(true);
            return;
        }

        apiService = RetrofitClient.getApiService(token);
        progressBar.setVisibility(View.VISIBLE);

        Call<ApiResponse<Cart>> call = apiService.getCart();
        call.enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess() && response.body().getData() != null) {
                    Cart cart = response.body().getData();
                    if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                        currentCartItems.clear();
                        currentCartItems.addAll(cart.getItems());
                        cartAdapter.setCartItemList(currentCartItems);
                        orderItemList.clear();
                        for (CartItem cartItem : currentCartItems) {
                            if (cartItem.getProduct() != null && cartItem.getVariant() != null) {
                                orderItemList.add(new OrderItem(
                                        cartItem.getProduct().getProductName(),
                                        cartItem.getProductImage(),
                                        (int) cartItem.getVariant().getPrice(),
                                        cartItem.getQuantity(),
                                        String.valueOf(cartItem.getVariant().getStockQuantity()),
                                        cartItem.getProduct().getId(),
                                        cartItem.getVariant().getId()
                                ));
                            }
                        }
                        showEmptyCartView(false);
                        updateTotalPrice();
                    } else {
                        showEmptyCartView(true);
                    }
                } else {
                    Log.e("Cart_Activity", "Lỗi khi lấy giỏ hàng: " + response.code() + " - " + (response.body() != null ? response.body().getMessage() : "Unknown error"));
                    showEmptyCartView(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Cart_Activity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyCartView(true);
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
            llClearCart.setVisibility(View.GONE);
            bottomCheckoutBar.setVisibility(View.GONE);
        } else {
            emptyCartView.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            llClearCart.setVisibility(View.VISIBLE);
            bottomCheckoutBar.setVisibility(View.VISIBLE);
        }
    }

    private void showClearCartConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giỏ hàng")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng không?")
                .setPositiveButton("Xóa", (dialog, which) -> removeAllItemsFromCart())
                .setNegativeButton("Hủy", (dialog, which) -> {
                    // Nếu hủy, bỏ chọn checkbox
                    cbSelectAll.setChecked(false);
                })
                .show();
    }

    private void removeAllItemsFromCart() {
        String token = Constants.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để xóa giỏ hàng.", Toast.LENGTH_LONG).show();
            return;
        }

        apiService = RetrofitClient.getApiService(token);
        progressBar.setVisibility(View.VISIBLE);

        // Lấy một bản sao của danh sách để tránh lỗi khi xóa trong vòng lặp
        List<CartItem> itemsToRemove = new ArrayList<>(currentCartItems);
        if (itemsToRemove.isEmpty()) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Tạo một Runnable để thực hiện xóa từng sản phẩm một
        // Đây là cách đơn giản để xử lý tuần tự, tránh trường hợp API bị quá tải
        Runnable removeNextItem = new Runnable() {
            int index = 0;
            @Override
            public void run() {
                if (index < itemsToRemove.size()) {
                    CartItem item = itemsToRemove.get(index);
                    if (item.getProduct() != null && item.getVariant() != null) {
                        CartRequest.RemoveCartItem request = new CartRequest.RemoveCartItem(
                                item.getProduct().getId(),
                                item.getVariant().getId()
                        );
                        apiService.removeFromCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                                Log.d("Cart_Activity", "Removed item " + (index + 1) + "/" + itemsToRemove.size() + ". Response: " + response.isSuccessful());
                                index++;
                                run(); // Gọi lại để xóa item tiếp theo
                            }
                            @Override
                            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                                Log.e("Cart_Activity", "Failed to remove item at index " + index, t);
                                index++;
                                run(); // Vẫn tiếp tục xóa các item khác
                            }
                        });
                    } else {
                        Log.e("Cart_Activity", "Invalid item data at index " + index);
                        index++;
                        run();
                    }
                } else {
                    // Khi đã xóa hết, tải lại giỏ hàng và ẩn ProgressBar
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Cart_Activity.this, "Đã xóa toàn bộ sản phẩm khỏi giỏ hàng.", Toast.LENGTH_SHORT).show();
                    fetchCartData();
                }
            }
        };

        removeNextItem.run(); // Bắt đầu quá trình xóa
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onQuantityChange(CartItem item, int newQuantity) {
        if (newQuantity <= 0) {
            showRemoveItemConfirmationDialog(item);
            return;
        }

        if (item.getProduct() == null || item.getVariant() == null) {
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
                    }
                    Toast.makeText(Cart_Activity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("Cart_Activity", "Update quantity API failed: " + response.code() + " - " + errorMsg);
                    fetchCartData();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Cart_Activity.this, "Lỗi mạng khi cập nhật số lượng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("Cart_Activity", "Update quantity network failure: ", t);
                fetchCartData();
            }
        });
    }

    private void showRemoveItemConfirmationDialog(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng không?")
                .setPositiveButton("Xóa", (dialog, which) -> onRemoveItem(item))
                .setNegativeButton("Hủy", (dialog, which) -> cartAdapter.notifyDataSetChanged())
                .show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            boolean orderSuccess = data.getBooleanExtra("order_success", false);
            if (orderSuccess) {
                // Tải lại giỏ hàng sau khi đặt hàng thành công để đảm bảo cập nhật UI
                fetchCartData();
            }
        }
    }
}