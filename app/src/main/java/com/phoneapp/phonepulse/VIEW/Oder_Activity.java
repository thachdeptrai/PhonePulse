package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.models.Voucher;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.request.OrderRequest;
import com.phoneapp.phonepulse.ui.voucher.VoucherBottomSheet;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Oder_Activity extends AppCompatActivity {

    private static final String TAG = "Oder_Activity";

    private MaterialToolbar toolbar;
    private TextView tvFullName, tvPhoneNumber, tvShippingAddress;
    private Button btnChangeAddress, btnPlaceOrder;
    private RecyclerView rvCheckoutProducts;
    private EditText etOrderNote;
    private RadioGroup paymentMethodGroup;
    private RadioButton radioCod, radioMomo, radio_vnpay;
    private TextView tvSubtotal, tvDiscount, tvFinalPrice, tvTotalAmount;
    private TextView tvAddCoupon, tvSelectedCoupon;

    private ArrayList<OrderItem> orderItemList;
    private ApiService apiService;
    private List<Variant> variantsInCart = new ArrayList<>();
    private Voucher selectedVoucher;
    private int subtotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oder);

        // Xử lý Deep Link khi Activity được khởi tạo
        handleDeepLink(getIntent());

        apiService = RetrofitClient.getApiService(Constants.getToken(this));

        initViews();
        setupToolbar();
        bindUserToUI();
        getIntentData();
        setupListeners();

        if (orderItemList != null && !orderItemList.isEmpty()) {
            loadVariantsInCart();
        } else {
            finish();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        // Xử lý Deep Link khi Activity đã chạy
        handleDeepLink(intent);
    }

    /**
     * Phương thức xử lý đường dẫn Deep Link từ backend VNPay trả về.
     */
    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "phonepulse".equals(data.getScheme()) && "payment".equals(data.getHost())) {
            String status = data.getQueryParameter("status");
            String error = data.getQueryParameter("error");
            String orderId = data.getQueryParameter("orderId");

            Log.d(TAG, "Deep Link được nhận: status=" + status + ", error=" + error + ", orderId=" + orderId);

            if ("success".equals(status)) {
                // Thanh toán thành công
                Toast.makeText(this, "Thanh toán thành công! Đơn hàng của bạn đã được đặt.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Thanh toán VNPay thành công. Bắt đầu cập nhật tồn kho và xóa giỏ hàng.");
                // Sau khi thành công, gọi hàm cập nhật tồn kho và xóa giỏ hàng
                if (orderItemList != null && !orderItemList.isEmpty()) {
                    updateVariantStockOnServer(new ArrayList<>(orderItemList));
                }
            } else if ("failed".equals(status)) {
                // Thanh toán thất bại
                String errorMessage = "Thanh toán thất bại.";
                if (error != null) {
                    errorMessage += " Lỗi: " + error;
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Thanh toán VNPay thất bại. Lỗi: " + error);
            }
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
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
        radio_vnpay = findViewById(R.id.radio_vnpay);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscount = findViewById(R.id.tv_discount);
        tvFinalPrice = findViewById(R.id.tv_final_price);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvAddCoupon = findViewById(R.id.tv_add_coupon);
        tvSelectedCoupon = findViewById(R.id.tv_selected_coupon);
    }

    private void setupToolbar() {
        toolbar.setTitle("Thanh toán đơn hàng");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void getIntentData() {
        orderItemList = getIntent().getParcelableArrayListExtra("order_items");
        if (orderItemList != null && !orderItemList.isEmpty()) {
            Log.d(TAG, "Kiểm tra OrderItems từ Intent:");
            for (int i = 0; i < orderItemList.size(); i++) {
                OrderItem item = orderItemList.get(i);
                if (item.getPrice() <= 0) {
                    // Xử lý sản phẩm có giá không hợp lệ nếu cần
                }
            }
        }
    }

    private void setupListeners() {
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        if (tvAddCoupon != null) {
            tvAddCoupon.setOnClickListener(v -> fetchVouchers());
        }
    }

    private void loadVariantsInCart() {
        apiService = RetrofitClient.getApiService(Constants.getToken(this));
        final CountDownLatch latch = new CountDownLatch(orderItemList.size());
        variantsInCart.clear();

        for (OrderItem item : orderItemList) {
            final OrderItem currentItem = item;

            if (currentItem.getProductId() == null || currentItem.getVariantId() == null ||
                    currentItem.getProductId().isEmpty() || currentItem.getVariantId().isEmpty()) {
                latch.countDown();
                continue;
            }

            apiService.getVariantForProductById(currentItem.getProductId(), currentItem.getVariantId())
                    .enqueue(new Callback<Variant>() {
                        @Override
                        public void onResponse(Call<Variant> call, Response<Variant> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Variant variant = response.body();
                                if (variant != null) {
                                    variantsInCart.add(variant);
                                }
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Call<Variant> call, Throwable t) {
                            latch.countDown();
                        }
                    });
        }

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(this::updateUIWithCartItems);
            } catch (InterruptedException e) {
                runOnUiThread(this::updateUIWithCartItems);
            }
        }).start();
    }

    private void updateUIWithCartItems() {
        OrderItemAdapter adapter = new OrderItemAdapter(orderItemList);
        rvCheckoutProducts.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutProducts.setAdapter(adapter);

        subtotal = 0;
        for (OrderItem item : orderItemList) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        String formattedTotal = formatCurrency(subtotal);
        tvTotalAmount.setText(formattedTotal);
        tvSubtotal.setText(formattedTotal);
        tvFinalPrice.setText(formattedTotal);
        tvDiscount.setText(formatCurrency(0));
    }

    private void placeOrder() {
        String token = Constants.getToken(Oder_Activity.this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (orderItemList == null || orderItemList.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào để đặt hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkStockBeforeOrder()) {
            Toast.makeText(this, "Một số sản phẩm không đủ tồn kho. Vui lòng kiểm tra lại giỏ hàng.", Toast.LENGTH_LONG).show();
            return;
        }

        String shippingAddress = tvShippingAddress.getText().toString().trim();
        if (shippingAddress.isEmpty() || shippingAddress.equals("Chưa có địa chỉ")) {
            Toast.makeText(this, "Vui lòng cập nhật địa chỉ giao hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod;
        if (radioCod.isChecked()) {
            paymentMethod = "COD";
        } else if (radio_vnpay.isChecked()) {
            paymentMethod = "vnpay";
        } else {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán.", Toast.LENGTH_SHORT).show();
            return;
        }

        String note = etOrderNote.getText().toString().trim();
        int discount = calculateDiscount(subtotal, selectedVoucher);
        int finalPrice = subtotal - discount;

        if (finalPrice <= 0) {
            Toast.makeText(this, "Tổng giá đơn hàng không hợp lệ. Vui lòng kiểm tra lại.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "LỖI: Tổng giá finalPrice là " + finalPrice + ". Không thể đặt hàng.");
            return;
        }

        OrderRequest request = new OrderRequest(orderItemList, discount, finalPrice, shippingAddress, paymentMethod, note);
        Log.d(TAG, "Gửi yêu cầu đặt hàng: " + request.toString());

        apiService.createOrder(request).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    ApiResponse<Order> apiResponse = response.body();
                    Order createdOrder = apiResponse.getData();

                    if ("vnpay".equals(request.getPayment_method())) {
                        String paymentUrl = apiResponse.getPaymentUrl();
                        if (paymentUrl != null && !paymentUrl.isEmpty()) {
                            Log.i(TAG, "Đơn hàng đã được tạo thành công. Chuyển hướng đến cổng thanh toán VNPay: " + paymentUrl);

                            // Mở trình duyệt để thanh toán. LƯU Ý: Backend sẽ điều hướng
                            // người dùng quay lại ứng dụng qua Deep Link sau khi thanh toán xong.
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                            startActivity(browserIntent);
                            Toast.makeText(Oder_Activity.this, "Đang chuyển đến trang thanh toán...", Toast.LENGTH_LONG).show();
                        } else {
                            Log.e(TAG, "Không nhận được URL thanh toán từ server.");
                            Toast.makeText(Oder_Activity.this, "Lỗi: Không thể tạo link thanh toán VNPay.", Toast.LENGTH_LONG).show();
                        }
                    } else { // COD hoặc phương thức khác
                        Toast.makeText(Oder_Activity.this, "Đặt hàng thành công! Đơn hàng của bạn đang được xử lý.", Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "Đặt hàng thành công. Mã đơn hàng: " + (createdOrder != null ? createdOrder.getId() : "N/A"));

                        if (createdOrder != null && createdOrder.getItems() != null) {
                            updateVariantStockOnServer(new ArrayList<>(createdOrder.getItems()));
                        }
                    }
                } else {
                    String errorMsg = "Đặt hàng thất bại.";
                    int errorCode = response.code();
                    String responseBodyError = null;
                    try {
                        if (response.errorBody() != null) {
                            responseBodyError = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi đọc errorBody: " + e.getMessage());
                    }
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else if (responseBodyError != null && !responseBodyError.isEmpty()) {
                        errorMsg = "Lỗi từ server: " + responseBodyError;
                    }
                    Log.e(TAG, "Đặt hàng thất bại: " + errorMsg + ". Mã lỗi HTTP: " + errorCode);
                    Toast.makeText(Oder_Activity.this, "Đặt hàng thất bại: " + errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Log.e(TAG, "Lỗi mạng/API khi gọi createOrder: " + t.getMessage(), t);
                Toast.makeText(Oder_Activity.this, "Lỗi kết nối hoặc server không phản hồi. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * @apiNote Loại bỏ phương thức này vì backend đã xử lý việc xác minh trạng thái thanh toán.
     * Ứng dụng sẽ nhận kết quả cuối cùng qua tham số Deep Link.
     */
    // private void checkVnPayOrderStatus(String orderId) { /* Logic đã được di chuyển vào handleDeepLink */ }

    // Phương thức onResume không cần kiểm tra lại trạng thái
    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateFinalPrice() {
        int discount = calculateDiscount(subtotal, selectedVoucher);
        int finalPrice = subtotal - discount;
        if (finalPrice < 0) finalPrice = 0;

        if (discount > 0) {
            tvDiscount.setText("- " + formatCurrency(discount));
        } else {
            tvDiscount.setText(formatCurrency(0));
        }

        tvFinalPrice.setText(formatCurrency(finalPrice));
        tvTotalAmount.setText(formatCurrency(finalPrice));
    }

    private int calculateDiscount(int subtotal, Voucher voucher) {
        if (voucher == null) {
            return 0;
        }
        if (subtotal < voucher.getMinOrderValue()) {
            Toast.makeText(this,
                    "Đơn hàng cần tối thiểu " + formatCurrency((int) voucher.getMinOrderValue())
                            + " để áp dụng voucher này",
                    Toast.LENGTH_SHORT).show();
            return 0;
        }
        int discount = 0;
        switch (voucher.getDiscountType()) {
            case "percent":
                discount = (int) (subtotal * voucher.getDiscountValue() / 100);
                if (voucher.getMaxDiscount() > 0 && discount > voucher.getMaxDiscount()) {
                    discount = (int) voucher.getMaxDiscount();
                }
                break;
            case "amount":
                discount = (int) voucher.getDiscountValue();
                break;
        }
        return Math.max(discount, 0);
    }
    private String formatCurrency(int amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator(',');
        DecimalFormat decimalFormat = new DecimalFormat("#,###", symbols);
        return decimalFormat.format(amount) + " ₫";
    }



    private void fetchVouchers() {
        String token = Constants.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem mã giảm giá.", Toast.LENGTH_SHORT).show();
            return;
        }
        apiService.getVouchers("Bearer " + token).enqueue(new Callback<ApiResponse<List<Voucher>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Voucher>>> call, Response<ApiResponse<List<Voucher>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Voucher> vouchers = response.body().getData();
                    showVoucherBottomSheet(vouchers);
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Không thể tải mã giảm giá. Vui lòng thử lại.";
                    Toast.makeText(Oder_Activity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Voucher>>> call, Throwable t) {
                Toast.makeText(Oder_Activity.this, "Lỗi kết nối khi tải mã giảm giá.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showVoucherBottomSheet(List<Voucher> vouchers) {
        if (vouchers == null || vouchers.isEmpty()) {
            Toast.makeText(this, "Không có mã giảm giá nào hiện có.", Toast.LENGTH_SHORT).show();
            return;
        }

        VoucherBottomSheet bottomSheet = new VoucherBottomSheet(vouchers, selected -> {
            if (selected != null && subtotal < selected.getMinOrderValue()) {
                Toast.makeText(this, "Đơn hàng cần tối thiểu "
                        + formatCurrency((int) selected.getMinOrderValue())
                        + " để dùng voucher này", Toast.LENGTH_SHORT).show();
                return;
            }
            this.selectedVoucher = selected;
            if (tvSelectedCoupon != null) {
                tvSelectedCoupon.setText(selected != null ? selected.getCode() : "Không dùng mã");
            }
            updateFinalPrice();
        });
        bottomSheet.show(getSupportFragmentManager(), "VoucherBottomSheet");
    }

    private boolean checkStockBeforeOrder() {
        boolean allInStock = true;
        StringBuilder stockErrorMsg = new StringBuilder("Các sản phẩm sau không đủ tồn kho:\n");
        for (OrderItem item : orderItemList) {
            Variant variant = findVariantById(item.getVariantId(), variantsInCart);
            if (variant == null) {
                stockErrorMsg.append("- ").append(item.getName()).append(" (biến thể không tìm thấy)\n");
                allInStock = false;
            } else if (variant.getQuantity() < item.getQuantity()) {
                stockErrorMsg.append("- ").append(item.getName())
                        .append(" (Yêu cầu: ").append(item.getQuantity())
                        .append(", Tồn kho: ").append(variant.getQuantity()).append(")\n");
                allInStock = false;
            }
        }
        if (!allInStock) {
        }
        return allInStock;
    }

    public void updateVariantStockOnServer(ArrayList<OrderItem> orderedItems) {
        if (orderedItems == null || orderedItems.isEmpty()) {
            navigateToOrderHistory();
            return;
        }

        final CountDownLatch stockUpdateLatch = new CountDownLatch(orderedItems.size());

        for (OrderItem item : orderedItems) {
            final OrderItem finalOrderItem = item;

            String variantId = finalOrderItem.getVariantId();
            String productId = finalOrderItem.getProductId();

            if (variantId == null || variantId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
                stockUpdateLatch.countDown();
                continue;
            }

            Variant foundVariant = findVariantById(variantId, variantsInCart);
            if (foundVariant != null) {
                int currentQuantity = foundVariant.getQuantity();
                int quantityOrdered = finalOrderItem.getQuantity();
                int newQuantity = Math.max(currentQuantity - quantityOrdered, 0);
                Variant updatedVariant = new Variant();
                updatedVariant.setId(variantId);
                updatedVariant.setQuantity(newQuantity);

                apiService.updateVariantForProductById(productId, variantId, updatedVariant)
                        .enqueue(new Callback<ApiResponse<Variant>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Variant>> call, Response<ApiResponse<Variant>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                }
                                stockUpdateLatch.countDown();
                            }
                            @Override
                            public void onFailure(Call<ApiResponse<Variant>> call, Throwable t) {
                                stockUpdateLatch.countDown();
                            }
                        });
            } else {
                stockUpdateLatch.countDown();
            }
        }
        new Thread(() -> {
            try {
                stockUpdateLatch.await();
                runOnUiThread(() -> {
                    clearCartOnServer();
                });
            } catch (InterruptedException e) {
                runOnUiThread(this::navigateToOrderHistory);
            }
        }).start();
    }
    public void clearCartOnServer() {
        if (orderItemList == null || orderItemList.isEmpty()) {
            navigateToOrderHistory();
            return;
        }
        final CountDownLatch cartRemovalLatch = new CountDownLatch(orderItemList.size());

        for (OrderItem item : orderItemList) {
            String productId = item.getProductId();
            String variantId = item.getVariantId();

            if (productId == null || productId.trim().isEmpty() || variantId == null || variantId.trim().isEmpty()) {
                cartRemovalLatch.countDown();
                continue;
            }

            CartRequest.RemoveCartItem request = new CartRequest.RemoveCartItem(productId, variantId);
            apiService.removeFromCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    }
                    cartRemovalLatch.countDown();
                }
                @Override
                public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                    cartRemovalLatch.countDown();
                }
            });
        }

        new Thread(() -> {
            try {
                cartRemovalLatch.await();
                runOnUiThread(() -> {
                    navigateToOrderHistory();
                });
            } catch (InterruptedException e) {
                runOnUiThread(this::navigateToOrderHistory);
            }
        }).start();
    }
    private Variant findVariantById(String variantId, List<Variant> variants) {
        if (variants == null || variantId == null || variantId.isEmpty()) {
            return null;
        }
        for (Variant variant : variants) {
            if (variant != null && variant.getId() != null && variant.getId().equals(variantId)) {
                return variant;
            }
        }
        return null;
    }

    private void navigateToOrderHistory() {
        Intent intent = new Intent(this, DashBoar_Activity.class);
        intent.putExtra("navigate_to_history", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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
}