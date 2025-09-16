package com.phoneapp.phonepulse.FRAGMENT.HISTORY_ORDER_FRAGMENT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.OrderAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse; // Keep this if other APIs use it
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.models.Variant; // This is the direct Variant model
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.utils.Constants;
import com.phoneapp.phonepulse.utils.OrderCanceledEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TatCaDonHang_FRAGMENT extends Fragment {

    private static final String TAG = "TatCaDonHang_FRAGMENT";

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;

    private TextView tvTotal, tvCancelled, tvShipping, tvCompleted, tvProcessing;

    private List<Order> currentOrders;
    private ApiService apiService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tatcadonhang_fragment, container, false);
        Log.d(TAG, "onCreateView: Fragment created.");

        String rawToken = Constants.getToken(requireContext());
        if (rawToken == null || rawToken.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy token. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onCreateView: Token is null or empty. Cannot initialize API service.");
            return view;
        }
        apiService = RetrofitClient.getApiService(rawToken);

        rvOrders = view.findViewById(R.id.rv_order_item);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        tvTotal = view.findViewById(R.id.tv_total_orders);
        tvCancelled = view.findViewById(R.id.tv_cancelled_orders);
        tvShipping = view.findViewById(R.id.tv_shipping_orders);
        tvCompleted = view.findViewById(R.id.tv_completed_orders);
        tvProcessing = view.findViewById(R.id.tv_processing_orders);

        handleBundleData();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void handleBundleData() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("order_items")) {
                ArrayList<OrderItem> orderItems = bundle.getParcelableArrayList("order_items");
                if (orderItems != null && !orderItems.isEmpty()) {
                    Order tempOrder = new Order();
                    tempOrder.setItems(orderItems);
                    List<Order> singleOrderList = new ArrayList<>();
                    singleOrderList.add(tempOrder);
                    setupOrders(singleOrderList);
                    return;
                }
            }
            if (bundle.containsKey("order_detail")) {
                Order order = (Order) bundle.getSerializable("order_detail");
                if (order != null) {
                    List<Order> orders = new ArrayList<>();
                    orders.add(order);
                    setupOrders(orders);
                    return;
                }
            }
        }
        fetchOrdersFromApi();
    }

    private void fetchOrdersFromApi() {
        String rawToken = Constants.getToken(requireContext());
        if (rawToken == null || rawToken.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy token. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService service = RetrofitClient.getApiService(rawToken);
        Call<ApiResponse<List<Order>>> call = service.getUserOrders();
        Log.d(TAG, "fetchOrdersFromApi: Fetching user orders from API.");

        call.enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Order>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Log.d(TAG, "onResponse: Successfully fetched " + apiResponse.getData().size() + " orders.");
                        setupOrders(apiResponse.getData());
                    } else {
                        Toast.makeText(getContext(), "Không có đơn hàng nào.", Toast.LENGTH_SHORT).show();
                        currentOrders = new ArrayList<>();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    currentOrders = new ArrayList<>();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                currentOrders = new ArrayList<>();
            }
        });
    }

    private void setupOrders(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            Toast.makeText(getContext(), "Danh sách đơn hàng trống.", Toast.LENGTH_SHORT).show();
            currentOrders = new ArrayList<>();
            if (orderAdapter != null) {
                orderAdapter = null;
                rvOrders.setAdapter(null);
            }
            tvTotal.setText("Tổng số đơn: 0");
            tvCancelled.setText("Đã hủy: 0");
            tvProcessing.setText("Đang xử lý: 0");
            tvShipping.setText("Đang giao: 0");
            tvCompleted.setText("Hoàn thành: 0");
            return;
        }

        this.currentOrders = orders;
        Log.d(TAG, "setupOrders: Displaying " + orders.size() + " orders.");
        int total = orders.size();
        int cancelled = 0, shipping = 0, completed = 0, processing = 0;

        for (Order order : orders) {
            String status = order.getStatus() != null ? order.getStatus().toLowerCase(Locale.ROOT) : "";
            String shippingStatus = order.getShippingStatus() != null ? order.getShippingStatus().toLowerCase(Locale.ROOT) : "";
            String paymentStatus = order.getPaymentStatus() != null ? order.getPaymentStatus().toLowerCase(Locale.ROOT) : "";

            if ("cancelled".equals(status)) {
                cancelled++;
            }
            else if (("pending".equals(status) || "confirmed".equals(status)) && "not_shipped".equals(shippingStatus)) {
                processing++;
            }
            else if ("confirmed".equals(status) && "shipping".equals(shippingStatus)) {
                shipping++;
            }
            else if ("confirmed".equals(status) && "shipped".equals(shippingStatus) && "paid".equals(paymentStatus)) {
                completed++;
            }
        }


        tvTotal.setText("Tổng số đơn: " + total);
        tvCancelled.setText("Đã hủy: " + cancelled);
        tvProcessing.setText("Đang xử lý: " + processing);
        tvShipping.setText("Đang giao: " + shipping);
        tvCompleted.setText("Hoàn thành: " + completed);

        orderAdapter = new OrderAdapter(getContext(), orders);
        rvOrders.setAdapter(orderAdapter);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderCanceledEvent(OrderCanceledEvent event) {
        Log.d(TAG, "onOrderCanceledEvent: Received cancel event for Order ID: " + event.getOrderId());
        cancelOrderApi(event.getOrderId(), event.getCancelReason());
    }

    private void cancelOrderApi(String orderId, String reason) {
        String token = Constants.getToken(requireContext());
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService service = RetrofitClient.getApiService(token);
        Call<ApiResponse> call = service.cancelOrder("Bearer " + token, orderId);
        Log.d(TAG, "cancelOrderApi: Attempting to cancel order with ID: " + orderId);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.i(TAG, "✅ Đơn hàng " + orderId + " đã hủy thành công trên server. Lý do: " + reason);
                    Toast.makeText(getContext(), "Đã hủy đơn: " + reason, Toast.LENGTH_SHORT).show();

                    Order canceledOrder = findOrderInCurrentList(orderId);
                    if (canceledOrder != null && canceledOrder.getItems() != null && !canceledOrder.getItems().isEmpty()) {
                        Log.d(TAG, "📦 Found " + canceledOrder.getItems().size() + " items in the canceled order. Proceeding to update stock on server.");
                        updateStockOnServer(canceledOrder.getItems());
                    } else {
                        Log.w(TAG, "⚠️ Canceled order not found locally or has no items. Cannot update stock. Refreshing order list.");
                        fetchOrdersFromApi();
                    }

                } else {
                    String errorMessage = "Hủy thất bại: " + (response.body() != null ? response.body().getMessage() : "Lỗi không xác định.");
                    Log.e(TAG, "❌ Lỗi khi hủy đơn hàng " + orderId + ". Mã lỗi: " + response.code() + ". Chi tiết: " + errorMessage);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "🌐 Lỗi mạng/API khi hủy đơn hàng " + orderId, t);
            }
        });
    }



    /**
     * Cập nhật tồn kho trên server cho từng sản phẩm trong đơn hàng đã hủy.
     * Phương thức này đảm bảo lấy số lượng tồn kho hiện tại từ server trước,
     * sau đó cộng thêm số lượng sản phẩm đã hủy và gửi lại tổng số lượng mới.
     *
     * @param items Danh sách các OrderItem từ đơn hàng đã bị hủy.
     */
    private void updateStockOnServer(List<OrderItem> items) {
        final CountDownLatch latch = new CountDownLatch(items.size());
        Log.d(TAG, "✨ Bắt đầu cập nhật tồn kho cho " + items.size() + " sản phẩm.");

        for (OrderItem item : items) {
            final String variantId = item.getVariantId(); // Đảm bảo final
            final String productId = item.getProductId(); // Đảm bảo final
            final int quantityToIncrease = item.getQuantity(); // Đảm bảo final

            if (variantId == null || variantId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
                Log.w(TAG, "⚠️ Bỏ qua cập nhật tồn kho: Thiếu variantId hoặc productId cho một sản phẩm.");
                latch.countDown();
                continue;
            }

            // --- BƯỚC 1: GỌI API ĐỂ LẤY THÔNG TIN BIẾN THỂ HIỆN TẠI TỪ SERVER ---
            // Yêu cầu API để lấy chi tiết biến thể bao gồm số lượng tồn kho hiện tại
            apiService.getVariantForProductById(productId, variantId)
                    .enqueue(new Callback<Variant>() { // <--- **Đã thay đổi ở đây để khớp với Call<Variant>**
                        @Override
                        public void onResponse(Call<Variant> call, Response<Variant> response) {
                            if (response.isSuccessful() && response.body() != null) { // body() sẽ là Variant, không phải ApiResponse
                                Variant currentVariant = response.body(); // Lấy trực tiếp đối tượng Variant
                                int currentServerQuantity = currentVariant.getQuantity(); // Số lượng tồn kho hiện tại trên server
                                int newTotalQuantity = currentServerQuantity + quantityToIncrease; // Tính toán tổng số lượng mới

                                Log.d(TAG, "🚀 Đã lấy tồn kho hiện tại từ server cho Variant ID: " + variantId + " là " + currentServerQuantity + ". Tính toán tồn kho mới: " + newTotalQuantity);

                                // --- BƯỚC 2: GỌI API ĐỂ CẬP NHẬT TỒN KHO VỚI SỐ LƯỢNG MỚI ĐÃ TÍNH TOÁN ---
                                // Tạo payload chỉ chứa số lượng mới (và có thể các trường khác nếu API PUT yêu cầu đầy đủ)
                                Variant updatedVariantPayload = new Variant();
                                // Quan trọng: Nếu API PUT yêu cầu tất cả các trường, hãy sao chép từ currentVariant
                                // Ví dụ: updatedVariantPayload.setId(currentVariant.getId());
                                // updatedVariantPayload.setName(currentVariant.getName());
                                // updatedVariantPayload.setPrice(currentVariant.getPrice());
                                // ... và các thuộc tính khác
                                updatedVariantPayload.setQuantity(newTotalQuantity); // Đặt số lượng LÀ TỔNG MỚI

                                apiService.updateVariantForProductById(productId, variantId, updatedVariantPayload)
                                        .enqueue(new Callback<ApiResponse<Variant>>() { // API update này vẫn là ApiResponse<Variant>
                                            @Override
                                            public void onResponse(Call<ApiResponse<Variant>> call, Response<ApiResponse<Variant>> response) {
                                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                                    Log.i(TAG, "✅ Tồn kho đã cập nhật thành công cho variant ID: " + variantId + ". Tồn kho mới trên server: " + (response.body().getData() != null ? response.body().getData().getQuantity() : "N/A"));
                                                } else {
                                                    String errorDetail = (response.body() != null ? response.body().getMessage() : "Không rõ lỗi.");
                                                    Log.e(TAG, "❌ Lỗi cập nhật tồn kho cho variant ID: " + variantId +
                                                            ". Mã lỗi: " + response.code() + ". Chi tiết: " + errorDetail + ". Payload: " + (call.request().body() != null ? call.request().body().toString() : "null"));
                                                }
                                                latch.countDown(); // Đảm bảo latch được giảm sau khi yêu cầu thứ 2 hoàn thành
                                            }

                                            @Override
                                            public void onFailure(Call<ApiResponse<Variant>> call, Throwable t) {
                                                Log.e(TAG, "🌐 Lỗi mạng/API khi gửi cập nhật tồn kho cho variant ID: " + variantId + ": " + t.getMessage(), t);
                                                latch.countDown(); // Đảm bảo latch được giảm ngay cả khi yêu cầu thứ 2 thất bại
                                            }
                                        });

                            } else {
                                // Nếu response.isSuccessful() là false hoặc body là null
                                // Hoặc nếu body có nhưng quantity là null hoặc không hợp lệ (tùy thuộc vào model Variant của bạn)
                                String errorMessage;
                                if (response.code() == 404) {
                                    errorMessage = "Không tìm thấy biến thể.";
                                } else {
                                    errorMessage = "Lỗi khi lấy dữ liệu tồn kho: " + response.code() + " - " + response.message();
                                    try {
                                        if (response.errorBody() != null) {
                                            errorMessage += " (" + response.errorBody().string() + ")";
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Lỗi đọc errorBody: " + e.getMessage());
                                    }
                                }
                                Log.e(TAG, "❌ Lỗi khi lấy tồn kho hiện tại cho variant ID: " + variantId + ". Chi tiết: " + errorMessage);
                                // Có thể thông báo cho người dùng ở đây nếu lỗi nghiêm trọng
                                // Toast.makeText(getContext(), "Không thể lấy tồn kho cho " + item.getName(), Toast.LENGTH_SHORT).show();
                                latch.countDown(); // Đảm bảo latch được giảm nếu yêu cầu đầu tiên thất bại
                            }
                        }

                        @Override
                        public void onFailure(Call<Variant> call, Throwable t) { // <--- **Đã thay đổi ở đây để khớp với Call<Variant>**
                            Log.e(TAG, "🌐 Lỗi mạng/API khi lấy tồn kho hiện tại cho variant ID: " + variantId + ": " + t.getMessage(), t);
                            // Có thể thông báo cho người dùng ở đây
                            // Toast.makeText(getContext(), "Lỗi kết nối khi lấy tồn kho cho " + item.getName(), Toast.LENGTH_SHORT).show();
                            latch.countDown(); // Đảm bảo latch được giảm ngay cả khi yêu cầu đầu tiên thất bại
                        }
                    });
        }

        // Luồng riêng để chờ tất cả các cập nhật tồn kho hoàn tất
        new Thread(() -> {
            try {
                latch.await(); // Chờ tất cả các latch.countDown() hoàn thành
                if (isAdded()) { // Đảm bảo Fragment vẫn còn gắn với Activity
                    Log.d(TAG, "🎉 Tất cả cập nhật tồn kho đã hoàn tất. Làm mới danh sách đơn hàng.");
                    requireActivity().runOnUiThread(this::fetchOrdersFromApi); // Cập nhật UI trên Main Thread
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Đặt lại cờ ngắt
                Log.e(TAG, "⚠️ Luồng bị gián đoạn khi chờ cập nhật tồn kho.", e);
            }
        }).start();
    }

    /**
     * Hàm phụ trợ để tìm đơn hàng trong danh sách hiện tại.
     */
    private Order findOrderInCurrentList(String orderId) {
        if (currentOrders == null) {
            return null;
        }
        for (Order order : currentOrders) {
            if (order.getId() != null && order.getId().equals(orderId)) {
                return order;
            }
        }
        return null;
    }
}