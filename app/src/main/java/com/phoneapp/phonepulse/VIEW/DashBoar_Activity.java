package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.phoneapp.phonepulse.FRAGMENT.Home_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.Profile_FRAGMENT;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.ui.cart.Cart_Activity;

public class DashBoar_Activity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar_DashBoar;
    private TextView tv_greeting;
    private ImageView iv_cart_icon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_boar);

        // Ánh xạ view
        toolbar_DashBoar = findViewById(R.id.toolbar_DashBoar);
        iv_cart_icon = findViewById(R.id.iv_cart_icon);
        tv_greeting = findViewById(R.id.tv_greeting);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        iv_cart_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CartClick", "Bạn đã nhấn vào giỏ hàng");
                Toast.makeText(DashBoar_Activity.this, "Đã nhấn giỏ hàng", Toast.LENGTH_SHORT).show();
                Intent intent =  new Intent(DashBoar_Activity.this, Cart_Activity.class);
                startActivity(intent);
            }
        });

        // Setup toolbar
        setSupportActionBar(toolbar_DashBoar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Load Fragment mặc định khi lần đầu vào
        if (savedInstanceState == null) {
            loadFragment(new Home_FRAGMENT());
        }

        // Xử lý chọn BottomNavigationView
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                replaceFragment(new Home_FRAGMENT(), "Trang Chủ");
                return true;
            } else if (id == R.id.nav_categories) {
                // TODO: Thêm Fragment categories nếu cần
                return true;
            } else if (id == R.id.nav_profile) {
                replaceFragment(new Profile_FRAGMENT(), "Tài khoản");
                return true;
            }

            return false;
        });


        // Theo dõi để ẩn BottomNavigation nếu đang ở Cart_FRAGMENT
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (current != null && current.getTag() != null && current.getTag().contains("FULLSCREEN")) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

    }

    // Hàm thay Fragment kèm cập nhật tiêu đề
    private void replaceFragment(Fragment fragment, String title) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);

        // Cho phép quay lại nếu không phải Home
        if (!(fragment instanceof Home_FRAGMENT)) {
            transaction.addToBackStack(null);
        }

        transaction.commit();

        // Cập nhật tiêu đề toolbar
        if (tv_greeting != null) {
            tv_greeting.setText(title);
        }
    }

    // Load Fragment Home mặc định
    private void loadFragment(Home_FRAGMENT fragment) {
        replaceFragment(fragment, "Trang Chủ");
    }
}
