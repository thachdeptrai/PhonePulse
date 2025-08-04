package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.phoneapp.phonepulse.FRAGMENT.CategoryFragment;
import com.phoneapp.phonepulse.FRAGMENT.Home_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.Profile_FRAGMENT;
import com.phoneapp.phonepulse.R;

public class DashBoar_Activity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar_DashBoar;
    private TextView tv_greeting;
    private ImageView iv_cart_icon;
    private EditText et_search_product;
    private CardView card_search_view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_boar);

        // Ánh xạ view
        toolbar_DashBoar = findViewById(R.id.toolbar_DashBoar);
        iv_cart_icon = findViewById(R.id.iv_cart_icon);
        tv_greeting = findViewById(R.id.tv_greeting);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        et_search_product = findViewById(R.id.et_search_product);
        card_search_view = findViewById(R.id.card_search_view);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar_DashBoar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Xử lý sự kiện nhấn vào giỏ hàng
        iv_cart_icon.setOnClickListener(view -> {
            Log.d("CartClick", "Bạn đã nhấn vào giỏ hàng");
            Toast.makeText(DashBoar_Activity.this, "Đã nhấn giỏ hàng", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DashBoar_Activity.this, Cart_Activity.class);
            startActivity(intent);
        });

        // Load Fragment mặc định khi lần đầu vào
        if (savedInstanceState == null) {
            replaceFragment(new Home_FRAGMENT(), "Trang Chủ", true);
        }
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("open_order_history", false)) {
            // Nếu có cờ mở Order History, load fragment tương ứng
            replaceFragment(new com.phoneapp.phonepulse.FRAGMENT.OrderHistory_FRAGMENT(), "Lịch sử đơn hàng", false);
            // Đặt chọn bottom nav về profile nếu cần (nếu fragment nằm trong tab đó)
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            return; // không load Home_FRAGMENT nữa
        }
        // Xử lý chọn BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new Home_FRAGMENT(), "Trang Chủ", true);
                return true;
            } else if (id == R.id.nav_categories) {
                replaceFragment(new CategoryFragment(), "Thể Loại", true);
                return true;
            } else if (id == R.id.nav_profile) {
                replaceFragment(new Profile_FRAGMENT(), "Tài khoản", false);
                return true;
            }
            return false;
        });

        // Theo dõi để ẩn BottomNavigation nếu đang ở Fragment toàn màn hình
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (current != null && "FULLSCREEN".equals(current.getTag())) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Thay thế Fragment và cập nhật trạng thái UI tương ứng.
     * @param fragment Fragment mới sẽ được hiển thị.
     * @param title Tiêu đề của Toolbar cho Fragment đó.
     * @param showSearchBar True nếu muốn hiển thị thanh tìm kiếm, False nếu muốn ẩn.
     */
    private void replaceFragment(Fragment fragment, String title, boolean showSearchBar) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);

        if (!(fragment instanceof Home_FRAGMENT)) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        // Cập nhật tiêu đề Toolbar
        if (tv_greeting != null) {
            tv_greeting.setText(title);
        }

        // Ẩn hoặc hiện thanh tìm kiếm
        if (et_search_product != null) {
            et_search_product.setVisibility(showSearchBar ? View.VISIBLE : View.GONE);
        }
    }

}