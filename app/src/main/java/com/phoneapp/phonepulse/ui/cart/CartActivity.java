package com.phoneapp.phonepulse.ui.cart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import Toolbar
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.CartAdapter;
import com.phoneapp.phonepulse.Adapter.ProductGridAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.ui.home.HomeActivity; // Ensure this activity exists

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartActivity extends AppCompatActivity {

    private Toolbar toolbarCart; // Toolbar instance (đổi tên để phù hợp với XML)

    private RecyclerView rvCartItems;
    private RecyclerView rvRecommendations;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private CheckBox cbSelectAll;
    private ImageView ivDeleteSelected;
    private LinearLayout emptyCartView;
    private Button btnShopNow;
    private LinearLayout cartActionBar;

    private CartAdapter cartAdapter;
    private ProductGridAdapter recommendationAdapter;
    private List<CartItem> cartItemList;
    private List<Product> recommendationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_cart);


        // --- Khởi tạo Toolbar và thiết lập ---
        toolbarCart = findViewById(R.id.toolbar_cart);
        setupToolbar(); // Gọi phương thức thiết lập Toolbar

        // --- Khởi tạo các View khác ---
        rvCartItems = findViewById(R.id.rv_cart_items);
        rvRecommendations = findViewById(R.id.rv_recommendations);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCheckout = findViewById(R.id.btn_checkout);
        cbSelectAll = findViewById(R.id.cb_select_all);
        ivDeleteSelected = findViewById(R.id.iv_delete_selected);
        emptyCartView = findViewById(R.id.empty_cart_view);
        btnShopNow = findViewById(R.id.btn_shop_now);
        cartActionBar = findViewById(R.id.cart_action_bar);


        // --- Thiết lập RecyclerViews ---
        cartItemList = new ArrayList<>();
        // Ví dụ: Thêm các mặt hàng giả vào giỏ hàn
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);


        recommendationList = new ArrayList<>();
        // Ví dụ: Thêm các sản phẩm đề xuất giả

        recommendationAdapter = new ProductGridAdapter(this, recommendationList);
        rvRecommendations.setLayoutManager(new GridLayoutManager(this, 2));
        rvRecommendations.setAdapter(recommendationAdapter);
        // --- Thiết lập sự kiện cho các nút ---
        btnShopNow.setOnClickListener(v -> {
            Intent homeIntent = new Intent(CartActivity.this, HomeActivity.class);
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            finish();
        });


    }

    /**
     * Thiết lập Toolbar làm ActionBar và cấu hình nút quay lại và tiêu đề.
     */
    private void setupToolbar() {
        setSupportActionBar(toolbarCart); // Sử dụng biến toolbarCart đã khai báo
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            getSupportActionBar().setDisplayShowHomeEnabled(true); // Đảm bảo nút quay lại được hiển thị
            getSupportActionBar().setTitle("Giỏ hàng của bạn"); // Đặt tiêu đề cho Toolbar
        }
        // Gán listener cho nút quay lại trên Toolbar
        toolbarCart.setNavigationOnClickListener(v -> onBackPressed());
    }




    private void checkEmptyCartState() {
        if (cartItemList.isEmpty()) {
            emptyCartView.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            cartActionBar.setVisibility(View.GONE);
            findViewById(R.id.bottom_checkout_bar).setVisibility(View.GONE);
        } else {
            emptyCartView.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            cartActionBar.setVisibility(View.VISIBLE);
            findViewById(R.id.bottom_checkout_bar).setVisibility(View.VISIBLE);
        }
    }
}