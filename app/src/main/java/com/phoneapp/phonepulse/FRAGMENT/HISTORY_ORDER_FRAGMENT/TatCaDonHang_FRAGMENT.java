package com.phoneapp.phonepulse.FRAGMENT.HISTORY_ORDER_FRAGMENT;

import android.os.Bundle;
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
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TatCaDonHang_FRAGMENT extends Fragment {

    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;

    // Thống kê
    private TextView tvTotal, tvCancelled, tvShipping, tvCompleted, tvProcessing;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tatcadonhang_fragment, container, false);

        // Ánh xạ view
        rvOrders = view.findViewById(R.id.rv_order_item);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        tvTotal = view.findViewById(R.id.tv_total_orders);
        tvCancelled = view.findViewById(R.id.tv_cancelled_orders);
        tvShipping = view.findViewById(R.id.tv_shipping_orders);
        tvCompleted = view.findViewById(R.id.tv_completed_orders);
        tvProcessing = view.findViewById(R.id.tv_processing_orders);

        // 1️⃣ Kiểm tra dữ liệu Bundle từ Activity
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
                    return view;
                }
            }

            if (bundle.containsKey("order_detail")) {
                Order order = (Order) bundle.getSerializable("order_detail");
                if (order != null) {
                    List<Order> orders = new ArrayList<>();
                    orders.add(order);
                    setupOrders(orders);
                    return view;
                }
            }
        }

        // 2️⃣ Nếu không có dữ liệu Bundle thì gọi API
        fetchOrdersFromApi();
        return view;
    }

    private void fetchOrdersFromApi() {
        String rawToken = Constants.getToken(requireContext());
        if (rawToken == null || rawToken.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy token. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService service = RetrofitClient.getApiService(rawToken);
        Call<ApiResponse<List<Order>>> call = service.getUserOrders();

        call.enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Order>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        setupOrders(apiResponse.getData());
                    } else {
                        Toast.makeText(getContext(), "Không có đơn hàng nào.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Gắn dữ liệu vào RecyclerView + update thống kê */
    private void setupOrders(List<Order> orders) {
        if (orders == null) return;

        // Đếm thống kê
        int total = orders.size();
        int cancelled = 0, shipping = 0, completed = 0, processing = 0;

        for (Order order : orders) {
            String status = order.getStatus() != null ? order.getStatus().toLowerCase(Locale.ROOT) : "";
            String shippingStatus = order.getShippingStatus() != null ? order.getShippingStatus().toLowerCase(Locale.ROOT) : "";

            // Ưu tiên kiểm tra status trước
            if ("cancelled".equals(status)) {
                cancelled++;
            } else if ("pending".equals(status)) {
                processing++;
            } else if ("shipping".equals(shippingStatus)) {
                shipping++;
            } else if ("shipped".equals(shippingStatus) || "completed".equals(status)) {
                completed++;
            }

        }

        // Update UI
        tvTotal.setText("Tổng số đơn: " + total);
        tvCancelled.setText("Đã hủy: " + cancelled);
        tvProcessing.setText("Đang xử lý: " + processing);
        tvShipping.setText("Đang giao: " + shipping);
        tvCompleted.setText("Hoàn thành: " + completed);

        // RecyclerView
        orderAdapter = new OrderAdapter(getContext(), orders);
        rvOrders.setAdapter(orderAdapter);
    }
}
