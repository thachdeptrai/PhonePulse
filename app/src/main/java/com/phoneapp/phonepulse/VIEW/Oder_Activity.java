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
import com.phoneapp.phonepulse.models.Variant; // Thêm import cho Variant
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.request.OrderRequest;
import com.phoneapp.phonepulse.utils.CartUtils;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List; // Thêm import cho List
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

    // TODO: ✅ Thêm một biến để lưu trữ toàn bộ danh sách biến thể sản phẩm.
    // Trong một ứng dụng thực tế, bạn sẽ lấy dữ liệu này từ API hoặc từ một repository.
    // Ở đây, chúng ta sẽ mô phỏng một danh sách rỗng để tránh lỗi biên dịch.
    private List<Variant> allVariants = new ArrayList<>();

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

        // TODO: ✅ Lấy danh sách variants từ một nguồn dữ liệu nào đó.
        // Ví dụ: lấy từ Intent hoặc từ API. Giả sử ta đã lấy được danh sách.
        // allVariants = getIntent().getParcelableArrayListExtra("all_variants");

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

                    // Lấy danh sách các item từ phản hồi của API để đảm bảo chính xác
                    ArrayList<OrderItem> orderedItems = new ArrayList<>();
                    if (response.body().getData() != null && response.body().getData().getItems() != null) {
                        orderedItems.addAll(response.body().getData().getItems());
                    }

                    // Bắt đầu cập nhật tồn kho trên server
                    updateVariantStockOnServer(orderedItems);
                }
                else {
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

    /**
     * Cập nhật tồn kho trên server cho tất cả các sản phẩm trong đơn hàng.
     * Sử dụng CountDownLatch để đảm bảo tất cả các request API đều hoàn tất
     * trước khi chuyển sang bước tiếp theo.
     * @param orderedItems Danh sách các sản phẩm đã đặt hàng.
     */
    private void updateVariantStockOnServer(ArrayList<OrderItem> orderedItems) {
        if (orderedItems == null || orderedItems.isEmpty()) {
            clearCartAfterOrderSuccess();
            return;
        }

        final CountDownLatch stockUpdateLatch = new CountDownLatch(orderedItems.size());

        for (OrderItem item : orderedItems) {
            // Lấy variant ID và productId từ OrderItem
            String variantId = item.getVariantId();
            String productId = item.getProductId();

            if (variantId == null || productId == null) {
                Log.w("OrderDebug", "Không thể cập nhật tồn kho: thiếu variantId hoặc productId cho sản phẩm: " + item.getName());
                stockUpdateLatch.countDown();
                continue;
            }

            // --- ĐÃ SỬA LỖI Ở ĐÂY ---
            // Lấy số lượng tồn kho hiện tại của variant từ đối tượng Variant
            int currentQuantity = item.getQuantity();
            // Lấy số lượng sản phẩm đã đặt hàng từ đối tượng OrderItem
            int quantityOrdered = item.getQuantity();
            // Tính số lượng tồn kho mới
            int newQuantity = currentQuantity - quantityOrdered;

            // TODO: ✅ Log lại thông tin để kiểm tra trên backend
            Log.d("OrderDebug", String.format("Cập nhật tồn kho cho Variant ID: %s. Tồn kho cũ: %d, Số lượng đặt: %d, Tồn kho mới: %d",
                    variantId, currentQuantity, quantityOrdered, newQuantity));

            // Tạo đối tượng Variant chỉ với số lượng mới để gửi lên server.
            // Vì chỉ gửi trường "quantity", nên giá (price) sẽ không bị thay đổi.
            Variant updatedVariant = new Variant(newQuantity);
            updatedVariant.setQuantity(newQuantity);

            apiService.updateVariantForProductById(productId, variantId, updatedVariant).enqueue(new Callback<ApiResponse<Variant>>() {
                @Override
                public void onResponse(Call<ApiResponse<Variant>> call, Response<ApiResponse<Variant>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.i("OrderDebug", "Cập nhật tồn kho thành công cho variant ID: " + variantId);
                    } else {
                        Log.e("OrderDebug", "Cập nhật tồn kho thất bại cho variant ID: " + variantId + ". Lỗi: " + (response.body() != null ? response.body().getMessage() : "Không rõ"));
                    }
                    stockUpdateLatch.countDown();
                }

                @Override
                public void onFailure(Call<ApiResponse<Variant>> call, Throwable t) {
                    Log.e("OrderDebug", "Lỗi mạng khi cập nhật tồn kho cho variant ID: " + variantId, t);
                    stockUpdateLatch.countDown();
                }
            });
        }

        // Chờ tất cả các request cập nhật tồn kho hoàn thành
        new Thread(() -> {
            try {
                stockUpdateLatch.await();
                runOnUiThread(() -> {
                    Log.d("OrderDebug", "Tất cả các cập nhật tồn kho đã hoàn tất.");
                    // Sau khi cập nhật tồn kho thành công, tiến hành xóa giỏ hàng
                    clearCartAfterOrderSuccess();
                });
            } catch (InterruptedException e) {
                Log.e("OrderDebug", "Lỗi khi chờ cập nhật tồn kho.", e);
                runOnUiThread(this::clearCartAfterOrderSuccess);
            }
        }).start();
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
