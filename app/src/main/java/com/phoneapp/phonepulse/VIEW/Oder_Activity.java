package com.phoneapp.phonepulse.VIEW;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.phoneapp.phonepulse.Adapter.OrderItemAdapter;
import com.phoneapp.phonepulse.MainActivity;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.request.OrderRequest;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Oder_Activity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextView tvFullName, tvPhoneNumber, tvShippingAddress;
    private Button btnChangeAddress, btnPlaceOrder;
    private RecyclerView rvCheckoutProducts;
    private EditText etOrderNote;
    private RadioGroup paymentMethodGroup;
    private RadioButton radioCod, radioMomo;
    private TextView tvSubtotal, tvDiscount, tvFinalPrice, tvTotalAmount;
    private TextView tvAddCoupon;

    private ArrayList<OrderItem> orderItemList;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_oder);

        // Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Thanh toán đơn hàng");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Ánh xạ view
        tvFullName = findViewById(R.id.tv_full_name);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        tvShippingAddress = findViewById(R.id.tv_shipping_address);
        btnChangeAddress = findViewById(R.id.btn_change_address);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        rvCheckoutProducts = findViewById(R.id.rv_checkout_products);
        etOrderNote = findViewById(R.id.et_order_note);
        paymentMethodGroup = findViewById(R.id.payment_method_group);
        radioCod = findViewById(R.id.radio_cod);
        radioMomo = findViewById(R.id.radio_momo);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscount = findViewById(R.id.tv_discount);
        tvFinalPrice = findViewById(R.id.tv_final_price);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvAddCoupon = findViewById(R.id.tv_add_coupon);

        // Gán dữ liệu người dùng
        bindUserToUI();

        // Nhận sản phẩm từ giỏ hàng
        orderItemList = getIntent().getParcelableArrayListExtra("order_items");

        if (orderItemList != null && !orderItemList.isEmpty()) {
            OrderItemAdapter adapter = new OrderItemAdapter(orderItemList);
            rvCheckoutProducts.setLayoutManager(new LinearLayoutManager(this));
            rvCheckoutProducts.setAdapter(adapter);

            int total = 0;
            for (OrderItem item : orderItemList) {
                total += item.getPrice() * item.getQuantity();
            }

            String formattedTotal = String.format("%,d đ", total).replace(",", ".");
            tvTotalAmount.setText(formattedTotal);
            tvSubtotal.setText(formattedTotal);
            tvFinalPrice.setText(formattedTotal);
        } else {
            Log.w("OderActivity", "Không có sản phẩm trong đơn hàng.");
        }

        // Xử lý khi nhấn nút đặt hàng
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String token = Constants.getToken(Oder_Activity.this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        String shippingAddress = tvShippingAddress.getText().toString();
        String paymentMethod = radioCod.isChecked() ? "COD" : "MOMO";
        String note = etOrderNote.getText().toString();
        int discount = 0; // Tạm thời là 0
        int finalPrice = extractPrice(tvFinalPrice.getText().toString());

        if (orderItemList == null || orderItemList.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào để đặt hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo danh sách OrderItem mới để gửi đi
        ArrayList<OrderItem> validOrderItems = new ArrayList<>();
        for (OrderItem item : orderItemList) {
            validOrderItems.add(new OrderItem(
                    item.getName(),
                    item.getImageUrl(),
                    item.getPrice(),
                    item.getQuantity(),
                    item.getVariant(),
                    item.getProductId(),
                    item.getVariantId()
            ));
        }

        OrderRequest request = new OrderRequest(validOrderItems, discount, finalPrice, shippingAddress, paymentMethod, note);

        apiService = RetrofitClient.getApiService(token);
        apiService.createOrder(request).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Oder_Activity.this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

                    // Lấy danh sách các item vừa đặt
                    ArrayList<OrderItem> orderedItems = new ArrayList<>();
                    if (response.body().getData() != null && response.body().getData().getItems() != null) {
                        orderedItems.addAll(response.body().getData().getItems());
                    }

                    // Xóa giỏ hàng
                    clearCartAfterOrderSuccess();

                    // Truyền danh sách item này sang màn hình khác (ví dụ DashBoar_Activity)
                    Intent intent = new Intent(Oder_Activity.this, DashBoar_Activity.class);
                    intent.putExtra("navigate_to_history", true);
                    intent.putParcelableArrayListExtra("ordered_items", orderedItems); // <-- thêm dòng này
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                } else {
                    String errorMsg = "Đặt hàng thất bại.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Log.e("OrderDebug", "Đặt hàng thất bại: " + errorMsg);
                    Toast.makeText(Oder_Activity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Log.e("OrderDebug", "Lỗi mạng khi gọi API createOrder: " + t.getMessage(), t);
                Toast.makeText(Oder_Activity.this, "Lỗi kết nối. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void clearCartAfterOrderSuccess() {
        if (orderItemList == null || orderItemList.isEmpty()) {
            navigateToOrderHistory();
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(Constants.getToken(this));
        final CountDownLatch latch = new CountDownLatch(orderItemList.size());

        for (OrderItem orderItem : orderItemList) {
            CartRequest.RemoveCartItem removeItemRequest = new CartRequest.RemoveCartItem(
                    orderItem.getProductId(),
                    orderItem.getVariantId()
            );

            apiService.removeFromCart(removeItemRequest).enqueue(new Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                    if (!response.isSuccessful() || response.body() == null || !response.body().isSuccess()) {
                        Log.e("Oder_Activity", "Failed to remove an item after order: " + (response.body() != null ? response.body().getMessage() : "Unknown error"));
                    }
                    latch.countDown();
                }
                @Override
                public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                    Log.e("Oder_Activity", "Failed to remove an item after order due to network error", t);
                    latch.countDown();
                }
            });
        }

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(() -> {
                    Log.d("Oder_Activity", "Đã xóa toàn bộ sản phẩm khỏi giỏ hàng.");
                    navigateToOrderHistory();
                });
            } catch (InterruptedException e) {
                Log.e("Oder_Activity", "Lỗi khi chờ xóa giỏ hàng.", e);
                runOnUiThread(this::navigateToOrderHistory);
            }
        }).start();
    }

    private void navigateToOrderHistory() {
        // Tạo Intent để quay về MainActivity
        Intent intent = new Intent(this, DashBoar_Activity.class);
        // Thêm cờ để chỉ định rằng bạn muốn chuyển sang TatCaDonHang_FRAGMENT
        intent.putExtra("navigate_to_history", true);
        // Xóa tất cả các Activity khác trên stack và khởi chạy MainActivity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        // Kết thúc Activity hiện tại
        finish();
    }

    private void bindUserToUI() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String fullname = preferences.getString("fullname", "");
        String phone = preferences.getString("phone", "");
        String address = preferences.getString("address", "");

        tvFullName.setText(!fullname.isEmpty() ? fullname : "Chưa có tên");
        tvPhoneNumber.setText(!phone.isEmpty() ? phone : "Chưa có số điện thoại");
        tvShippingAddress.setText(!address.isEmpty() ? address : "Chưa có địa chỉ");
    }

    private int extractPrice(String formattedPrice) {
        try {
            return Integer.parseInt(formattedPrice.replace(".", "").replace("đ", "").replace(" ", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }
}