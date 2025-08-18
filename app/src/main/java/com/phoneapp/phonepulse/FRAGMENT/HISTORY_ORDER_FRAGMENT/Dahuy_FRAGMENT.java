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
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Dahuy_FRAGMENT extends Fragment {

    private static final String TAG = "Dahuy_FRAGMENT";
    private RecyclerView rvOrders;
    private OrderAdapter orderAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dahuy_fragment, container, false);

        rvOrders = view.findViewById(R.id.rv_order_item);
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        fetchCancelledOrdersFromApi();

        return view;
    }

    private void fetchCancelledOrdersFromApi() {
        String rawToken = Constants.getToken(requireContext());
        if (rawToken == null || rawToken.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy token. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Token rỗng hoặc không tồn tại.");
            return;
        }

        ApiService service = RetrofitClient.getApiService(rawToken);
        Call<ApiResponse<List<Order>>> call = service.getUserOrders();
        Log.d(TAG, "🚀 Gọi API getUserOrders...");

        call.enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Order>> apiResponse = response.body();

                    if (apiResponse.isSuccess()) {
                        List<Order> orders = apiResponse.getData();
                        if (orders != null && !orders.isEmpty()) {
                            // ✅ Lọc đơn hàng status = "cancelled"
                            List<Order> cancelledOrders = new ArrayList<>();
                            for (Order order : orders) {
                                String status = order.getStatus() != null
                                        ? order.getStatus().toLowerCase(Locale.ROOT)
                                        : "";

                                if ("cancelled".equals(status)) {
                                    cancelledOrders.add(order);
                                    Log.d(TAG, "🛑 Order Đã hủy: ID=" + order.getId()
                                            + ", Giá=" + order.getFinalPrice());
                                }
                            }

                            if (!cancelledOrders.isEmpty()) {
                                orderAdapter = new OrderAdapter(getContext(), cancelledOrders);
                                rvOrders.setAdapter(orderAdapter);
                            } else {
                                Toast.makeText(getContext(), "Không có đơn hàng đã hủy.", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "⚠ Không tìm thấy Order nào có status = 'cancelled'.");
                            }
                        } else {
                            Log.w(TAG, "⚠ API trả về danh sách Order rỗng.");
                            Toast.makeText(getContext(), "Không có đơn hàng nào.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "❌ API lỗi: " + apiResponse.getMessage());
                        Toast.makeText(getContext(), "Không thể lấy danh sách đơn hàng: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "❌ Phản hồi lỗi từ server. Code: " + response.code());
                    Toast.makeText(getContext(), "Không thể lấy danh sách đơn hàng. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                Log.e(TAG, "❌ Lỗi kết nối API: " + t.getMessage(), t);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
