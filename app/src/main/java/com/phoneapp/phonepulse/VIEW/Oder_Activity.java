package com.phoneapp.phonepulse.VIEW;

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
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.request.OrderRequest;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Oder_Activity extends AppCompatActivity {

    private static final String TAG = "Oder_Activity";

    // Thành phần UI
    private MaterialToolbar toolbar;
    private TextView tvFullName, tvPhoneNumber, tvShippingAddress;
    private Button btnChangeAddress, btnPlaceOrder;
    private RecyclerView rvCheckoutProducts;
    private EditText etOrderNote;
    private RadioGroup paymentMethodGroup;
    private RadioButton radioCod, radioMomo;
    private TextView tvSubtotal, tvDiscount, tvFinalPrice, tvTotalAmount;
    private TextView tvAddCoupon;

    // Dữ liệu
    private ArrayList<OrderItem> orderItemList;
    private ApiService apiService;
    private List<Variant> variantsInCart = new ArrayList<>(); // Lưu trữ các biến thể đã được tải để kiểm tra tồn kho

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_oder);

        initViews();
        setupToolbar();
        bindUserToUI();
        getIntentData(); // Lấy dữ liệu và kiểm tra ngay lập tức
        setupListeners();

        // Tải chi tiết biến thể từ API nếu có sản phẩm trong đơn hàng
        if (orderItemList != null && !orderItemList.isEmpty()) {
            loadVariantsInCart();
        } else {
            Log.w(TAG, "Không tìm thấy sản phẩm trong Intent để đặt hàng. Kết thúc Activity.");
            Toast.makeText(this, "Không có sản phẩm nào để đặt hàng. Vui lòng thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Khởi tạo tất cả các thành phần UI bằng cách tìm ID tương ứng của chúng.
     */
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
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscount = findViewById(R.id.tv_discount);
        tvFinalPrice = findViewById(R.id.tv_final_price);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvAddCoupon = findViewById(R.id.tv_add_coupon);
    }

    /**
     * Thiết lập Toolbar với tiêu đề và nút quay lại.
     */
    private void setupToolbar() {
        toolbar.setTitle("Thanh toán đơn hàng");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    /**
     * Lấy danh sách các sản phẩm trong đơn hàng từ Intent và kiểm tra giá.
     */
    private void getIntentData() {
        orderItemList = getIntent().getParcelableArrayListExtra("order_items");
        if (orderItemList != null && !orderItemList.isEmpty()) {
            Log.d(TAG, "Kiểm tra OrderItems từ Intent:");
            for (int i = 0; i < orderItemList.size(); i++) {
                OrderItem item = orderItemList.get(i);
                Log.d(TAG, String.format(Locale.getDefault(),
                        "  Item %d: Name=%s, Price=%d, Quantity=%d, ProductId=%s, VariantId=%s",
                        i, item.getName(), item.getPrice(), item.getQuantity(), item.getProductId(), item.getVariantId()));
                if (item.getPrice() <= 0) {
                    Log.e(TAG, "❌ CẢNH BÁO: OrderItem '" + item.getName() + "' có giá <= 0 từ Intent! Tổng giá có thể sai.");
                }
            }
        } else {
            Log.w(TAG, "orderItemList rỗng hoặc null từ Intent.");
        }
    }

    /**
     * Thiết lập các lắng nghe sự kiện cho các nút và các thành phần UI khác.
     */
    private void setupListeners() {
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        // btnChangeAddress.setOnClickListener(v -> handleChangeAddress()); // Uncomment nếu có chức năng này
    }

    /**
     * Tải thông tin chi tiết của các biến thể (variant) có trong giỏ hàng từ API.
     * Sử dụng CountDownLatch để đợi tất cả các yêu cầu API hoàn thành trước khi cập nhật UI.
     */
    private void loadVariantsInCart() {
        apiService = RetrofitClient.getApiService(Constants.getToken(this));
        final CountDownLatch latch = new CountDownLatch(orderItemList.size());
        variantsInCart.clear();

        for (OrderItem item : orderItemList) {
            final OrderItem currentItem = item;

            if (currentItem.getProductId() == null || currentItem.getVariantId() == null ||
                    currentItem.getProductId().isEmpty() || currentItem.getVariantId().isEmpty()) {
                Log.w(TAG, "Bỏ qua item '" + currentItem.getName() + "' do thiếu productId hoặc variantId. Giảm bộ đếm.");
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
                                    Log.d(TAG, "Đã tải biến thể: " + variant.getId() + " - Tồn kho: " + variant.getQuantity());
                                } else {
                                    Log.e(TAG, "Phản hồi API cho biến thể " + currentItem.getVariantId() + " là null.");
                                }
                            } else {
                                Log.e(TAG, "Lỗi khi lấy biến thể " + currentItem.getVariantId() + ". Mã lỗi: " + response.code() + ", Thông báo: " + response.message());
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Call<Variant> call, Throwable t) {
                            Log.e(TAG, "Lỗi mạng khi lấy biến thể " + currentItem.getVariantId() + ": " + t.getMessage(), t);
                            latch.countDown();
                        }
                    });
        }

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(this::updateUIWithCartItems);
                Log.d(TAG, "Tất cả các biến thể đã được tải. Cập nhật UI.");
            } catch (InterruptedException e) {
                Log.e(TAG, "Luồng bị gián đoạn khi chờ tải biến thể.", e);
                runOnUiThread(this::updateUIWithCartItems); // Vẫn cố gắng cập nhật UI
            }
        }).start();
    }

    /**
     * Cập nhật giao diện người dùng với các sản phẩm trong giỏ hàng và tính toán tổng số tiền.
     */
    private void updateUIWithCartItems() {
        OrderItemAdapter adapter = new OrderItemAdapter(orderItemList);
        rvCheckoutProducts.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutProducts.setAdapter(adapter);

        int subtotal = 0;
        for (OrderItem item : orderItemList) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        String formattedTotal = String.format(Locale.getDefault(), "%,d đ", subtotal).replace(",", ".");
        tvTotalAmount.setText(formattedTotal);
        tvSubtotal.setText(formattedTotal);
        tvFinalPrice.setText(formattedTotal);
        tvDiscount.setText("0 đ");
        Log.d(TAG, "UI đã được cập nhật. Tổng tiền hiển thị: " + formattedTotal);
    }

    /**
     * Xử lý quá trình đặt hàng.
     * Kiểm tra token, tồn kho, sau đó gửi yêu cầu tạo đơn hàng đến API.
     */
    private void placeOrder() {
        String token = Constants.getToken(Oder_Activity.this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Không có token, yêu cầu đăng nhập.");
            return;
        }

        if (orderItemList == null || orderItemList.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào để đặt hàng.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "orderItemList rỗng khi cố gắng đặt hàng.");
            return;
        }

        if (!checkStockBeforeOrder()) {
            Toast.makeText(this, "Một số sản phẩm không đủ tồn kho. Vui lòng kiểm tra lại giỏ hàng.", Toast.LENGTH_LONG).show();
            Log.w(TAG, "Kiểm tra tồn kho thất bại.");
            return;
        }

        String shippingAddress = tvShippingAddress.getText().toString().trim();
        if (shippingAddress.isEmpty() || shippingAddress.equals("Chưa có địa chỉ")) {
            Toast.makeText(this, "Vui lòng cập nhật địa chỉ giao hàng.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Địa chỉ giao hàng trống hoặc chưa cập nhật.");
            return;
        }

        String paymentMethod = radioCod.isChecked() ? "COD" : "MOMO";
        String note = etOrderNote.getText().toString().trim();
        int discount = 0;
        int finalPrice = extractPrice(tvFinalPrice.getText().toString());

        // Kiểm tra finalPrice có bị về 0 không trước khi gửi yêu cầu
        if (finalPrice <= 0) {
            Toast.makeText(this, "Tổng giá đơn hàng không hợp lệ. Vui lòng kiểm tra lại.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "LỖI: Tổng giá finalPrice là " + finalPrice + ". Không thể đặt hàng.");
            return;
        }

        OrderRequest request = new OrderRequest(orderItemList, discount, finalPrice, shippingAddress, paymentMethod, note);
        Log.d(TAG, "Gửi yêu cầu đặt hàng: " + request.toString());

        apiService = RetrofitClient.getApiService(token);
        apiService.createOrder(request).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Oder_Activity.this, "Đặt hàng thành công! Đơn hàng của bạn đang được xử lý.", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "Đặt hàng thành công. Mã đơn hàng: " + (response.body().getData() != null ? response.body().getData().getId() : "N/A"));

                    ArrayList<OrderItem> orderedItems = new ArrayList<>();
                    if (response.body().getData() != null && response.body().getData().getItems() != null) {
                        orderedItems.addAll(response.body().getData().getItems());
                        Log.d(TAG, "Kiểm tra OrderItems từ phản hồi API tạo đơn hàng:");
                        for (int i = 0; i < orderedItems.size(); i++) {
                            OrderItem item = orderedItems.get(i);
                            Log.d(TAG, String.format(Locale.getDefault(),
                                    "  API Response Item %d: Name=%s, Price=%d, Quantity=%d",
                                    i, item.getName(), item.getPrice(), item.getQuantity()));
                            if (item.getPrice() <= 0) {
                                Log.e(TAG, "❌ CẢNH BÁO: OrderItem '" + item.getName() + "' có giá <= 0 từ phản hồi API tạo đơn hàng! Vấn đề từ Server?");
                            }
                        }
                    } else {
                        Log.w(TAG, "Phản hồi API đặt hàng không chứa danh sách sản phẩm đã đặt.");
                    }
                    updateVariantStockOnServer(orderedItems);
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
     * Kiểm tra xem có đủ tồn kho cho tất cả các sản phẩm trong đơn hàng hay không.
     * @return true nếu đủ tồn kho, false nếu không.
     */
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
            Log.e(TAG, "Kiểm tra tồn kho thất bại: \n" + stockErrorMsg.toString());
        }
        return allInStock;
    }

    /**
     * Cập nhật số lượng tồn kho của các biến thể trên máy chủ sau khi đặt hàng thành công.
     * Phương thức này đảm bảo rằng giá sản phẩm không bị thay đổi.
     * @param orderedItems Danh sách các sản phẩm đã được đặt.
     */
    private void updateVariantStockOnServer(ArrayList<OrderItem> orderedItems) {
        if (orderedItems == null || orderedItems.isEmpty()) {
            Log.w(TAG, "Không có OrderedItems để cập nhật tồn kho. Chuyển hướng.");
            navigateToOrderHistory();
            return;
        }

        final CountDownLatch stockUpdateLatch = new CountDownLatch(orderedItems.size());
        Log.d(TAG, "Bắt đầu cập nhật tồn kho cho " + orderedItems.size() + " sản phẩm.");

        for (OrderItem item : orderedItems) {
            final OrderItem finalOrderItem = item;

            String variantId = finalOrderItem.getVariantId();
            String productId = finalOrderItem.getProductId();

            if (variantId == null || variantId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
                Log.w(TAG, "❌ Bỏ qua cập nhật tồn kho: Thiếu variantId hoặc productId cho sản phẩm: " + finalOrderItem.getName());
                stockUpdateLatch.countDown();
                continue;
            }

            Variant foundVariant = findVariantById(variantId, variantsInCart);
            if (foundVariant != null) {
                int currentQuantity = foundVariant.getQuantity();
                int quantityOrdered = finalOrderItem.getQuantity();
                int newQuantity = Math.max(currentQuantity - quantityOrdered, 0); // Đảm bảo số lượng không âm

                Log.d(TAG, String.format(Locale.getDefault(),
                        "📦 Chuẩn bị cập nhật tồn kho cho Variant ID: %s | Tồn kho cũ: %d | Số lượng đặt: %d | Tồn kho mới dự kiến: %d",
                        variantId, currentQuantity, quantityOrdered, newQuantity
                ));

                Variant updatedVariant = new Variant();
                updatedVariant.setId(variantId);
                updatedVariant.setQuantity(newQuantity);
                // CHÚ Ý: KHÔNG GÁN GIÁ (PRICE) VÀO updatedVariant. Giá không thay đổi!

                apiService.updateVariantForProductById(productId, variantId, updatedVariant)
                        .enqueue(new Callback<ApiResponse<Variant>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Variant>> call, Response<ApiResponse<Variant>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    Log.i(TAG, "✅ Tồn kho đã cập nhật thành công cho variant ID: " + variantId + ". Tồn kho mới trên server: " + response.body().getData().getQuantity());
                                } else {
                                    String errorDetail = (response.body() != null ? response.body().getMessage() : "Không rõ lỗi.");
                                    Log.e(TAG, "⚠️ Lỗi cập nhật tồn kho cho variant ID: " + variantId +
                                            ". Mã lỗi: " + response.code() + ". Chi tiết: " + errorDetail + ". Payload: " + call.request().body());
                                }
                                stockUpdateLatch.countDown();
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Variant>> call, Throwable t) {
                                Log.e(TAG, "🌐 Lỗi mạng/API khi cập nhật tồn kho cho variant ID: " + variantId + ": " + t.getMessage(), t);
                                stockUpdateLatch.countDown();
                            }
                        });
            } else {
                Log.e(TAG, "❌ Không tìm thấy biến thể ID: " + variantId + " trong danh sách biến thể đã tải cục bộ. Không thể cập nhật tồn kho.");
                stockUpdateLatch.countDown();
            }
        }

        new Thread(() -> {
            try {
                stockUpdateLatch.await();
                runOnUiThread(() -> {
                    Log.d(TAG, "✅ Tất cả cập nhật tồn kho đã hoàn tất. Bắt đầu xóa giỏ hàng.");
                    clearCartOnServer(); // Gọi phương thức xóa giỏ hàng
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "⚠️ Luồng bị gián đoạn khi chờ cập nhật tồn kho.", e);
                runOnUiThread(this::navigateToOrderHistory);
            }
        }).start();
    }

    private void clearCartOnServer() {
        if (orderItemList == null || orderItemList.isEmpty()) {
            Log.d(TAG, "Giỏ hàng đã rỗng. Chuyển hướng.");
            navigateToOrderHistory();
            return;
        }

        // Sử dụng CountDownLatch để đợi tất cả các yêu cầu xóa hoàn tất
        final CountDownLatch cartRemovalLatch = new CountDownLatch(orderItemList.size());
        Log.d(TAG, "Bắt đầu xóa " + orderItemList.size() + " sản phẩm khỏi giỏ hàng.");

        for (OrderItem item : orderItemList) {
            String productId = item.getProductId();   // ✅ Lấy productId từ OrderItem
            String variantId = item.getVariantId();

            // Kiểm tra tính hợp lệ của cả productId và variantId
            if (productId == null || productId.trim().isEmpty() ||
                    variantId == null || variantId.trim().isEmpty()) {
                Log.w(TAG, "❌ Bỏ qua xóa giỏ hàng: Thiếu productId hoặc variantId cho sản phẩm: " + item.getName());
                cartRemovalLatch.countDown(); // Giảm bộ đếm ngay lập tức nếu dữ liệu không hợp lệ
                continue;
            }

            // ✅ Tạo request với cả productId và variantId
            CartRequest.RemoveCartItem request = new CartRequest.RemoveCartItem(productId, variantId);

            apiService.removeFromCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.i(TAG, "✅ Đã xóa thành công sản phẩm với productId: " + productId + ", variantId: " + variantId + " khỏi giỏ hàng.");
                    } else {
                        String errorDetail = (response.body() != null ? response.body().getMessage() : "Không rõ lỗi.");
                        Log.e(TAG, "⚠️ Lỗi xóa sản phẩm khỏi giỏ hàng cho productId: " + productId + ", variantId: " + variantId +
                                ". Mã lỗi: " + response.code() + ". Chi tiết: " + errorDetail);
                    }
                    cartRemovalLatch.countDown(); // Giảm bộ đếm sau mỗi phản hồi API
                }

                @Override
                public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                    Log.e(TAG, "🌐 Lỗi mạng/API khi xóa sản phẩm khỏi giỏ hàng cho productId: " + productId + ", variantId: " + variantId + ": " + t.getMessage(), t);
                    cartRemovalLatch.countDown(); // Giảm bộ đếm ngay cả khi lỗi mạng
                }
            });
        }

        // Luồng chờ tất cả các yêu cầu xóa hoàn tất
        new Thread(() -> {
            try {
                cartRemovalLatch.await(); // Chờ cho đến khi tất cả các countDown() được gọi
                runOnUiThread(() -> {
                    Log.d(TAG, "✅ Tất cả các sản phẩm đã được xử lý xong. Chuyển hướng đến lịch sử đơn hàng.");
                    // Có thể cần tải lại giỏ hàng một lần nữa để đảm bảo UI trống
                    // fetchCartData();
                    navigateToOrderHistory();
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "⚠️ Luồng bị gián đoạn khi chờ xóa giỏ hàng.", e);
                // Nếu luồng bị gián đoạn, vẫn cố gắng chuyển hướng
                runOnUiThread(this::navigateToOrderHistory);
            }
        }).start();
    }


    /**
     * Tìm một biến thể (Variant) trong danh sách dựa trên ID của nó.
     * @param variantId ID của biến thể cần tìm.
     * @param variants Danh sách các biến thể để tìm kiếm.
     * @return Đối tượng Variant nếu tìm thấy, ngược lại trả về null.
     */
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

    /**
     * Phương thức này không còn xóa sản phẩm khỏi giỏ hàng.
     * Nó chỉ phục vụ mục đích log và chuyển hướng đến màn hình lịch sử đơn hàng.
     */
    private void clearCartAfterOrderSuccess() {
        Log.d(TAG, "Đã hoàn tất việc đặt hàng. Chuyển hướng mà không xóa giỏ hàng tại đây.");
        navigateToOrderHistory();
    }

    /**
     * Chuyển hướng người dùng đến màn hình lịch sử đơn hàng (DashBoar_Activity).
     * Đặt cờ Intent để xóa các activity trên stack và tạo một task mới.
     */
    private void navigateToOrderHistory() {
        Intent intent = new Intent(this, DashBoar_Activity.class);
        intent.putExtra("navigate_to_history", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Hiển thị thông tin người dùng (tên, số điện thoại, địa chỉ giao hàng) lên giao diện.
     * Lấy dữ liệu từ SharedPreferences.
     */
    private void bindUserToUI() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String fullname = preferences.getString("fullname", "");
        String phone = preferences.getString("phone", "");
        String address = preferences.getString("address", "");

        tvFullName.setText(!fullname.isEmpty() ? fullname : "Chưa có tên");
        tvPhoneNumber.setText(!phone.isEmpty() ? phone : "Chưa có số điện thoại");
        tvShippingAddress.setText(!address.isEmpty() ? address : "Chưa có địa chỉ");
        Log.d(TAG, "Thông tin người dùng: Tên=" + fullname + ", SĐT=" + phone + ", Địa chỉ=" + address);
    }

    /**
     * Trích xuất giá trị số nguyên từ một chuỗi giá tiền đã định dạng (ví dụ: "100.000 đ").
     * @param formattedPrice Chuỗi giá tiền đã định dạng.
     * @return Giá trị số nguyên của giá tiền, hoặc 0 nếu có lỗi trong quá trình chuyển đổi.
     */
    private int extractPrice(String formattedPrice) {
        try {
            Log.d(TAG, "Attempting to extract price from: '" + formattedPrice + "'");
            String cleanPriceString = formattedPrice.replace(".", "").replace("đ", "").replace(" ", "").trim();
            int price = Integer.parseInt(cleanPriceString);
            Log.d(TAG, "Successfully extracted price: " + price);
            return price;
        } catch (NumberFormatException e) {
            Log.e(TAG, "LỖI CHUYỂN ĐỔI SỐ: Không thể trích xuất giá từ chuỗi: '" + formattedPrice + "'. Trả về 0.", e);
            return 0;
        }
    }
}