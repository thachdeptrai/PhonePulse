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
import com.phoneapp.phonepulse.Adapter.OrderItemAdapter; // Giữ lại nếu bạn có OrderItemAdapter riêng cho việc hiển thị order_items trực tiếp
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Order; // Import Order model
import com.phoneapp.phonepulse.request.OrderItem; // Import OrderItem request model
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TatCaDonHang_FRAGMENT extends Fragment {

    private static final String TAG = "TatCaDonHang_FRAGMENT";
    private RecyclerView rvOrders; // Đổi tên để rõ ràng hơn: đây là RecyclerView cho danh sách các Đơn hàng
    private OrderAdapter orderAdapter;
    // private OrderItemAdapter orderItemAdapter; // Có thể không cần nếu OrderAdapter đã đủ để hiển thị chi tiết Order

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tatcadonhang_fragment, container, false);

        rvOrders = view.findViewById(R.id.rv_order_item); // Giữ nguyên ID layout nếu không muốn sửa layout
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));

        // 1️⃣ Kiểm tra nếu Activity gửi dữ liệu sản phẩm vừa đặt qua Bundle (ví dụ: sau khi đặt hàng thành công)
        Bundle bundle = getArguments();
        if (bundle != null) {
            // Trường hợp nhận danh sách OrderItem trực tiếp (ví dụ: để hiển thị chi tiết một đơn hàng ngay sau khi tạo)
            if (bundle.containsKey("order_items")) {
                ArrayList<OrderItem> orderItems = bundle.getParcelableArrayList("order_items");
                if (orderItems != null && !orderItems.isEmpty()) {
                    Log.d(TAG, "🟢 Nhận được " + orderItems.size() + " sản phẩm từ Bundle (order_items) để hiển thị chi tiết OrderItem.");
                    for (OrderItem item : orderItems) {
                        Log.d(TAG, "    Bundle OrderItem: " + item.toString());
                    }
                    // Nếu bạn muốn hiển thị một danh sách các OrderItem riêng lẻ mà không bọc trong Order,
                    // thì bạn sẽ cần một OrderItemAdapter cho trường hợp này.
                    // Nếu không, bạn cần tạo một Order giả để OrderAdapter có thể xử lý.
                    // Ví dụ: Tạo một Order giả chứa các OrderItem này
                    Order tempOrder = new Order();
                    tempOrder.setItems(orderItems);
                    // Có thể set các thông tin khác cho tempOrder nếu cần
                    List<Order> singleOrderList = new ArrayList<>();
                    singleOrderList.add(tempOrder);

                    orderAdapter = new OrderAdapter(getContext(), singleOrderList);
                    rvOrders.setAdapter(orderAdapter);
                    return view; // Trả về view nếu có dữ liệu từ Bundle
                }
            }
            // Trường hợp nhận một đối tượng Order đầy đủ qua Bundle (ví dụ: xem chi tiết một đơn hàng đã có)
            if (bundle.containsKey("order_detail")) {
                Order order = (Order) bundle.getSerializable("order_detail");
                if (order != null) {
                    Log.d(TAG, "🟢 Nhận được một Order từ Bundle (order_detail): ID " + order.getId() + ", Final Price: " + order.getFinalPrice());
                    if (order.getItems() != null) {
                        Log.d(TAG, "    Số lượng OrderItems trong Order từ Bundle: " + order.getItems().size());
                        for (OrderItem item : order.getItems()) {
                            Log.d(TAG, "      Bundle OrderItem (từ order_detail): " + item.toString());
                        }
                    } else {
                        Log.w(TAG, "    Order từ Bundle không có OrderItems.");
                    }
                    List<Order> orders = new ArrayList<>();
                    orders.add(order);
                    orderAdapter = new OrderAdapter(getContext(), orders);
                    rvOrders.setAdapter(orderAdapter);
                    return view; // Trả về view nếu có dữ liệu từ Bundle
                }
            }
        }

        // 2️⃣ Nếu không có dữ liệu truyền sang qua Bundle, thì gọi API để lấy danh sách tất cả đơn hàng
        Log.d(TAG, "🟡 Không có dữ liệu Order từ Bundle. Bắt đầu gọi API.");
        fetchOrdersFromApi();

        return view;
    }

    private void fetchOrdersFromApi() {
        String rawToken = Constants.getToken(requireContext());
        if (rawToken == null || rawToken.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy token. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "❌ Lỗi: Token rỗng hoặc không tồn tại.");
            return;
        }

        ApiService service = RetrofitClient.getApiService(rawToken);
        Call<ApiResponse<List<Order>>> call = service.getUserOrders();
        Log.d(TAG, "🚀 Đang gọi API getUserOrders...");

        call.enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Order>> apiResponse = response.body();
                    Log.d(TAG, "✅ API Response: Thành công = " + apiResponse.isSuccess() + ", Message = " + apiResponse.getMessage());

                    if (apiResponse.isSuccess()) {
                        List<Order> orders = apiResponse.getData();
                        if (orders != null && !orders.isEmpty()) {
                            Log.d(TAG, "📦 Số lượng Orders nhận được từ API: " + orders.size());
                            for (Order order : orders) {
                                Log.d(TAG, "    Order ID: " + order.getId() + ", Final Price: " + order.getFinalPrice() + ", Status: " + order.getStatus());
                                if (order.getItems() != null) { // ✅ Đã đổi từ getOrderItems() sang getItems()
                                    Log.d(TAG, "      Số lượng OrderItems trong Order " + order.getId() + ": " + order.getItems().size());
                                    for (OrderItem item : order.getItems()) {
                                        Log.d(TAG, "        API OrderItem: " + item.toString()); // ✅ LOG: Kiểm tra OrderItem từ API
                                    }
                                } else {
                                    Log.w(TAG, "      Order " + order.getId() + " không có OrderItems.");
                                }
                            }
                            orderAdapter = new OrderAdapter(getContext(), orders);
                            rvOrders.setAdapter(orderAdapter);
                        } else {
                            Log.w(TAG, "⚠ API trả về danh sách Order rỗng.");
                            Toast.makeText(getContext(), "Không có đơn hàng nào.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "❌ API trả về lỗi: " + apiResponse.getMessage());
                        Toast.makeText(getContext(), "Không thể lấy danh sách đơn hàng: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi đọc errorBody: " + e.getMessage());
                    }
                    Log.e(TAG, "❌ Lỗi phản hồi từ server. Code: " + response.code() + ", Message: " + response.message() + ", Error Body: " + errorBody);
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