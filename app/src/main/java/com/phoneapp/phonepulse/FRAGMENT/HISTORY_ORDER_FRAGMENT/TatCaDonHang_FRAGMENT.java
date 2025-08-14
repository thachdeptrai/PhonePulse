package com.phoneapp.phonepulse.FRAGMENT.HISTORY_ORDER_FRAGMENT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.OrderAdapter;
import com.phoneapp.phonepulse.Adapter.OrderItemAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TatCaDonHang_FRAGMENT extends Fragment {

    private static final String TAG = "TatCaDonHang_FRAGMENT";
    private RecyclerView rvOrderItem;
    private OrderAdapter orderAdapter;
    private OrderItemAdapter orderItemAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tatcadonhang_fragment, container, false);

        rvOrderItem = view.findViewById(R.id.rv_order_item);
        rvOrderItem.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1️⃣ Kiểm tra nếu Activity gửi dữ liệu sản phẩm vừa đặt
        Bundle bundle = getArguments();
        if (bundle != null) {
            if (bundle.containsKey("order_items")) {
                ArrayList<OrderItem> orderItems = bundle.getParcelableArrayList("order_items");
                if (orderItems != null && !orderItems.isEmpty()) {
                    Log.d(TAG, "Nhận được " + orderItems.size() + " sản phẩm từ Order");
                    orderItemAdapter = new OrderItemAdapter(orderItems);
                    rvOrderItem.setAdapter(orderItemAdapter);
                    return view;
                }
            }
            // Fallback: nhận từng Order (nếu Activity gửi)
            if (bundle.containsKey("order_detail")) {
                Order order = (Order) bundle.getSerializable("order_detail");
                if (order != null) {
                    List<Order> orders = new ArrayList<>();
                    orders.add(order);
                    orderAdapter = new OrderAdapter(getContext(), orders);
                    rvOrderItem.setAdapter(orderAdapter);
                    return view;
                }
            }
        }

        // 2️⃣ Nếu không có dữ liệu truyền sang thì gọi API
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
                    if (apiResponse.isSuccess()) {
                        List<Order> orders = apiResponse.getData();
                        orderAdapter = new OrderAdapter(getContext(), orders);
                        rvOrderItem.setAdapter(orderAdapter);
                    } else {
                        Toast.makeText(getContext(), "Không thể lấy danh sách đơn hàng", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Không thể lấy danh sách đơn hàng.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
