package com.phoneapp.phonepulse.ui.ordercreation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.models.OrderItem;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.OrderRequest;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderCreationActivity extends AppCompatActivity {

    private EditText etShippingAddress, etPhone, etVoucherCode, etPaymentMethod, etNote;
    private Button btnSubmitOrder;
    private ProgressBar progressBar;
    private List<OrderItem> cartItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_creation);

        // Ánh xạ view
        etShippingAddress = findViewById(R.id.et_shipping_address);
        etPhone = findViewById(R.id.et_phone);
        etVoucherCode = findViewById(R.id.et_voucher_code);
        etPaymentMethod = findViewById(R.id.et_payment_method);
        etNote = findViewById(R.id.et_note); // Thêm EditText cho note
        btnSubmitOrder = findViewById(R.id.btn_submit_order);
        progressBar = findViewById(R.id.pb_loading);

        // Lấy giỏ hàng khi khởi động
        fetchCartItems();

        // Xử lý gửi đơn hàng
        btnSubmitOrder.setOnClickListener(v -> submitOrder());
    }

    private void fetchCartItems() {
        String token = getSavedToken();
        if (token == null) return;

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<List<CartItem>> call = apiService.getCart(token);
        call.enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CartItem> cartItemsResponse = response.body();
                    cartItems = new ArrayList<>();
                    for (CartItem cartItem : cartItemsResponse) {
                        OrderItem item = new OrderItem();
                        item.setProductId(cartItem.getProductId());
                        item.setVariantId(cartItem.getVariantId());
                        item.setQuantity(cartItem.getQuantity());
                        item.setId(cartItem.getId()); // Sử dụng _id từ CartItem
                        cartItems.add(item);
                    }
                } else {
                    Toast.makeText(OrderCreationActivity.this, "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                Toast.makeText(OrderCreationActivity.this, "Lỗi kết nối khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void submitOrder() {
        String token = getSavedToken();
        if (token == null) {
            Toast.makeText(this, "Không tìm thấy token. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        String shippingAddress = etShippingAddress.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String voucherCode = etVoucherCode.getText().toString().trim();
        String paymentMethod = etPaymentMethod.getText().toString().trim();
        String note = etNote.getText().toString().trim();

        if (shippingAddress.isEmpty() || phone.isEmpty() || paymentMethod.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cartItems == null || cartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng trống. Vui lòng thêm sản phẩm!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tính discount_amount và final_price
        int discountAmount = calculateDiscountAmount(voucherCode);
        int finalPrice = calculateFinalPrice(cartItems, discountAmount);

        // Tạo đối tượng OrderRequest
        OrderRequest orderRequest = new OrderRequest(shippingAddress, phone, voucherCode, paymentMethod, cartItems, discountAmount, finalPrice, note);

        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);
        btnSubmitOrder.setEnabled(false);

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<com.phoneapp.phonepulse.data.api.ApiResponse<Order>> call = apiService.createOrder(token, orderRequest);

        call.enqueue(new Callback<com.phoneapp.phonepulse.data.api.ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<com.phoneapp.phonepulse.data.api.ApiResponse<Order>> call, Response<com.phoneapp.phonepulse.data.api.ApiResponse<Order>> response) {
                progressBar.setVisibility(View.GONE);
                btnSubmitOrder.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    com.phoneapp.phonepulse.data.api.ApiResponse<Order> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Order order = apiResponse.getData();
                        Toast.makeText(OrderCreationActivity.this, "Đơn hàng đã được gửi thành công! ID: " + order.getId(), Toast.LENGTH_SHORT).show();
                        // Xóa dữ liệu sau khi gửi thành công
                        etShippingAddress.setText("");
                        etPhone.setText("");
                        etVoucherCode.setText("");
                        etPaymentMethod.setText("");
                        etNote.setText("");
                    } else {
                        Toast.makeText(OrderCreationActivity.this, "Thất bại: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(OrderCreationActivity.this, "Lỗi khi gửi đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<com.phoneapp.phonepulse.data.api.ApiResponse<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                btnSubmitOrder.setEnabled(true);
                Toast.makeText(OrderCreationActivity.this, "Không thể kết nối tới máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int calculateDiscountAmount(String voucherCode) {
        // Giả lập gọi API applyVoucher (chưa có)
        if (voucherCode != null && !voucherCode.isEmpty()) {
            // Logic giả lập: Giảm 50,000 nếu có voucher
            return 50000;
        }
        return 0;
    }

    private int calculateFinalPrice(List<OrderItem> items, int discountAmount) {
        int totalPrice = 0;
        if (items != null) {
            for (OrderItem item : items) {
                // Giả lập giá sản phẩm (cần lấy từ API product)
                int itemPrice = 150000; // Giá mẫu
                totalPrice += itemPrice * item.getQuantity();
            }
        }
        return Math.max(0, totalPrice - discountAmount); // Đảm bảo không âm
    }

    private String getSavedToken() {
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(Constants.TOKEN_KEY, null);
    }
}

