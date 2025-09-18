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
            // 🚨 Giữ trạng thái pending ngay cả khi đã thanh toán
            else if ("pending".equals(status)) {
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
        Log.d(TAG, "📌 Gửi yêu cầu hủy đơn hàng, ID: " + orderId);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Log.i(TAG, "✅ Đơn hàng " + orderId + " đã được hủy thành công. Lý do: " + reason);
                    Toast.makeText(getContext(), "Đã hủy đơn: " + reason, Toast.LENGTH_SHORT).show();

                    Order canceledOrder = findOrderInCurrentList(orderId);
                    if (canceledOrder != null && canceledOrder.getItems() != null && !canceledOrder.getItems().isEmpty()) {
                        Log.d(TAG, "📦 Đã tìm thấy " + canceledOrder.getItems().size() + " sản phẩm trong đơn hủy. Tiến hành cập nhật tồn kho trên server.");
                        updateStockOnServer(canceledOrder.getItems());
                    } else {
                        Log.w(TAG, "⚠️ Không tìm thấy đơn hàng trong danh sách hiện tại hoặc không có sản phẩm. Làm mới danh sách đơn.");
                        fetchOrdersFromApi();
                    }

                } else {
                    String errorMessage = "Hủy đơn thất bại: " + (response.body() != null ? response.body().getMessage() : "Lỗi không xác định.");
                    Log.e(TAG, "❌ Hủy đơn hàng " + orderId + " thất bại. Mã lỗi: " + response.code() + ". Chi tiết: " + errorMessage);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (!isAdded()) return;
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "🌐 Lỗi mạng khi hủy đơn hàng " + orderId + ": " + t.getMessage(), t);
            }
        });
    }


    /**
     * Cập nhật tồn kho trên server cho từng sản phẩm trong đơn hàng đã hủy.
     * Số lượng sẽ được cộng thêm đúng bằng số lượng khách đã đặt.
     *
     * @param items Danh sách OrderItem từ đơn hàng đã bị hủy.
     */
    private void updateStockOnServer(List<OrderItem> items) {
        final CountDownLatch latch = new CountDownLatch(items.size());
        Log.d(TAG, "✨ Bắt đầu cập nhật tồn kho cho " + items.size() + " sản phẩm.");

        for (OrderItem item : items) {
            final String variantId = item.getVariantId();
            final String productId = item.getProductId();
            final int quantityToIncrease = item.getQuantity();

            if (variantId == null || variantId.trim().isEmpty() ||
                    productId == null || productId.trim().isEmpty()) {
                Log.w(TAG, "⚠️ Bỏ qua: Thiếu variantId hoặc productId cho sản phẩm.");
                latch.countDown();
                continue;
            }

            // --- BƯỚC 1: Lấy số lượng tồn kho hiện tại ---
            apiService.getVariantForProductById(productId, variantId)
                    .enqueue(new Callback<Variant>() {
                        @Override
                        public void onResponse(Call<Variant> call, Response<Variant> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Variant currentVariant = response.body();
                                int currentServerQuantity = currentVariant.getQuantity();
                                int newTotalQuantity = currentServerQuantity + quantityToIncrease;

                                Log.d(TAG, "📦 Variant ID " + variantId +
                                        " tồn kho hiện tại: " + currentServerQuantity +
                                        " | hoàn trả thêm: " + quantityToIncrease +
                                        " | tồn kho mới = " + newTotalQuantity);

                                // --- BƯỚC 2: Gọi API update với số lượng mới ---
                                Variant updatedVariantPayload = new Variant();
                                updatedVariantPayload.setId(currentVariant.getId());
                                updatedVariantPayload.setQuantity(newTotalQuantity);

                                // Nếu API yêu cầu các field khác -> copy thêm:
                                updatedVariantPayload.setPrice(currentVariant.getPrice());
                                updatedVariantPayload.setColor(currentVariant.getColor());
                                updatedVariantPayload.setSize(currentVariant.getSize());

                                apiService.updateVariantForProductById(productId, variantId, updatedVariantPayload)
                                        .enqueue(new Callback<ApiResponse<Variant>>() {
                                            @Override
                                            public void onResponse(Call<ApiResponse<Variant>> call, Response<ApiResponse<Variant>> response) {
                                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                                    Log.i(TAG, "✅ Cập nhật thành công Variant ID: " + variantId +
                                                            ". Tồn kho mới: " + response.body().getData().getQuantity());
                                                } else {
                                                    String err = response.body() != null ? response.body().getMessage() : "Không rõ lỗi.";
                                                    Log.e(TAG, "❌ Lỗi update Variant ID: " + variantId +
                                                            " | Mã lỗi: " + response.code() +
                                                            " | Chi tiết: " + err);
                                                }
                                                latch.countDown();
                                            }

                                            @Override
                                            public void onFailure(Call<ApiResponse<Variant>> call, Throwable t) {
                                                Log.e(TAG, "🌐 Lỗi mạng khi update Variant ID: " + variantId, t);
                                                latch.countDown();
                                            }
                                        });

                            } else {
                                Log.e(TAG, "❌ Không lấy được tồn kho cho Variant ID: " + variantId +
                                        " | Mã lỗi: " + response.code());
                                latch.countDown();
                            }
                        }

                        @Override
                        public void onFailure(Call<Variant> call, Throwable t) {
                            Log.e(TAG, "🌐 Lỗi mạng khi lấy tồn kho Variant ID: " + variantId, t);
                            latch.countDown();
                        }
                    });
        }

        // --- BƯỚC 3: Sau khi tất cả cập nhật xong thì refresh ---
        new Thread(() -> {
            try {
                latch.await();
                if (isAdded()) {
                    Log.d(TAG, "🎉 Hoàn tất cập nhật tồn kho. Làm mới danh sách đơn.");
                    requireActivity().runOnUiThread(this::fetchOrdersFromApi);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Log.e(TAG, "⚠️ Luồng bị gián đoạn khi chờ cập nhật tồn kho.", e);
            }
        }).start();



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