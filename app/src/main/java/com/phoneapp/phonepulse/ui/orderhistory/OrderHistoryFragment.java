package com.phoneapp.phonepulse.ui.orderhistory;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.phoneapp.phonepulse.Adapter.OrderAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.utils.Constants;
import com.phoneapp.phonepulse.Response.ApiResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryFragment extends Fragment {

    private RecyclerView rvOrderHistory;
    private TextView tvEmptyOrders;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private OrderAdapter orderAdapter;
    private ApiService apiService;
    private String token;

    public OrderHistoryFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrderHistory = view.findViewById(R.id.rv_order_history);
        tvEmptyOrders = view.findViewById(R.id.tv_empty_orders);
        progressBar = view.findViewById(R.id.pb_loading);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        rvOrderHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvOrderHistory.setHasFixedSize(true);

        token = getSavedToken();
        if (token != null) {
            apiService = RetrofitClient.getApiService(token);
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            // Điều hướng người dùng về màn hình đăng nhập
            return;
        }

        swipeRefreshLayout.setOnRefreshListener(this::fetchUserOrders);

        fetchUserOrders();
    }

    private void fetchUserOrders() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyOrders.setVisibility(View.GONE);
        rvOrderHistory.setVisibility(View.GONE);

        // API get all orders for user returns ApiResponse with a list of orders in the data field
        Call<ApiResponse<List<Order>>> call = apiService.getUserOrders(token);

        call.enqueue(new Callback<ApiResponse<List<Order>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Order>>> call, Response<ApiResponse<List<Order>>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Order> orderList = response.body().getData();
                    if (orderList.isEmpty()) {
                        tvEmptyOrders.setVisibility(View.VISIBLE);
                    } else {
                        rvOrderHistory.setVisibility(View.VISIBLE);
                        orderAdapter = new OrderAdapter(getContext(), orderList);
                        rvOrderHistory.setAdapter(orderAdapter);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Order>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(getContext(), "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSavedToken() {
        if (getContext() == null) return null;
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(Constants.TOKEN_KEY, null);
    }
}