package com.phoneapp.phonepulse.ui.orderhistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Adapter.OrderAdapter;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrder;
    private TextView tvEmpty;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OrderAdapter orderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        // Ánh xạ view
        rvOrder = findViewById(R.id.rv_order_history);
        tvEmpty = findViewById(R.id.tv_empty_order);
        progressBar = findViewById(R.id.pb_loading);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);

        // Setup RecyclerView
        rvOrder.setLayoutManager(new LinearLayoutManager(this));
        rvOrder.setHasFixedSize(true);

        // Kéo để làm mới
        swipeRefreshLayout.setOnRefreshListener(this::fetchUserOrders);

        // Gọi API ban đầu
        fetchUserOrders();
    }

    private void fetchUserOrders() {
        String token = getSavedToken();
        if (token == null) {
            Toast.makeText(this, "Không tìm thấy token. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hiển thị loading
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvOrder.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<List<Order>> call = apiService.getUserOrders(token);

        call.enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orderList = response.body();

                    if (orderList.isEmpty()) {
                        rvOrder.setVisibility(View.GONE);
                        tvEmpty.setVisibility(View.VISIBLE);
                    } else {
                        rvOrder.setVisibility(View.VISIBLE);
                        tvEmpty.setVisibility(View.GONE);
                        orderAdapter = new OrderAdapter(OrderHistoryActivity.this, orderList);
                        rvOrder.setAdapter(orderAdapter);
                    }
                } else {
                    Toast.makeText(OrderHistoryActivity.this, "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(OrderHistoryActivity.this, "Không thể kết nối tới máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSavedToken() {
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(Constants.TOKEN_KEY, null);
    }
}