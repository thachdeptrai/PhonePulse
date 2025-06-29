package com.phoneapp.phonepulse.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.CartAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.ui.checkout.CheckoutActivity;
import com.phoneapp.phonepulse.utils.Constants;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private TextView tvTotalPrice;
    private MaterialButton btnCheckout;
    private CartAdapter cartAdapter;
    private ApiService apiService;
    private double totalPrice = 0;
    private List<CartItem> cartItems = new ArrayList<>(); // Khởi tạo danh sách rỗng

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupToolbar();

        if (Constants.getToken(this) == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService = RetrofitClient.getApiService(Constants.getToken(this));

        setupRecyclerView();
        loadCartItems();

        btnCheckout.setOnClickListener(v -> {
            if (totalPrice <= 0) {
                Toast.makeText(this, "Giỏ hàng trống", Toast.LENGTH_SHORT).show();
            } else {
                startActivity(new Intent(this, CheckoutActivity.class));
            }
        });
    }

    private void initViews() {
        rvCart = findViewById(R.id.rv_cart);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCheckout = findViewById(R.id.btn_checkout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_cart);
        toolbar.setTitle("Giỏ hàng");
        toolbar.setNavigationIcon(R.drawable.tru); // Icon quay lại
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(this, cartItems, this::updateTotalPrice);
        rvCart.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        apiService.getCart(Constants.getToken(this)).enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(Call<List<CartItem>> call, Response<List<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    cartItems.clear();
                    cartItems.addAll(response.body());
                    cartAdapter.notifyDataSetChanged();
                    updateTotalPrice(cartItems);
                } else {
                    Toast.makeText(CartActivity.this, "Không thể tải giỏ hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CartItem>> call, Throwable t) {
                Toast.makeText(CartActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalPrice(List<CartItem> items) {
        totalPrice = 0;
        for (CartItem item : items) {
            if (item.getProduct() != null) {
                double price = item.getProduct().getPrice();
                totalPrice += price * item.getQuantity();
            }
        }

        NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        tvTotalPrice.setText("₫" + numberFormat.format(totalPrice));
    }
}
