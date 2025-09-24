package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.phoneapp.phonepulse.FRAGMENT.CategoryFragment;
import com.phoneapp.phonepulse.FRAGMENT.ChatListFragment;
import com.phoneapp.phonepulse.FRAGMENT.FavouriteFragment;
import com.phoneapp.phonepulse.FRAGMENT.HISTORY_ORDER_FRAGMENT.TatCaDonHang_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.Home_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.OrderHistory_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.Profile_FRAGMENT;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.OrderItem;

import java.util.ArrayList;

public class DashBoar_Activity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar_DashBoar;
    private TextView tv_greeting;
    private ImageView iv_cart_icon;

    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    replaceFragment(new Home_FRAGMENT(), "Trang Chủ", true);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_boar);

        toolbar_DashBoar = findViewById(R.id.toolbar_DashBoar);
        iv_cart_icon = findViewById(R.id.iv_cart_icon);
        tv_greeting = findViewById(R.id.tv_greeting);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        setSupportActionBar(toolbar_DashBoar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        iv_cart_icon.setOnClickListener(view -> {
            startActivity(new Intent(DashBoar_Activity.this, Cart_Activity.class));
        });

        handleIntent(getIntent());

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
            } else if (id == R.id.nav_favorites) {
                replaceFragment(new FavouriteFragment(), "Yêu thích", true);
                return true;
            } else if (id == R.id.nav_message) {
                replaceFragment(new ChatListFragment(), "Tin Nhắn", false);
                return true;
            }
            return false;
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            openDefaultFragment();
            return;
        }

        String fragmentToOpen = intent.getStringExtra("openFragment");
        if ("TatCaDonHang".equals(fragmentToOpen)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            replaceFragment(new TatCaDonHang_FRAGMENT(), "Lịch sử đơn hàng", false);
            return;
        }

        ArrayList<OrderItem> orderItems = intent.getParcelableArrayListExtra("order_items");
        if (orderItems != null && !orderItems.isEmpty()) {
            OrderHistory_FRAGMENT fragment = new OrderHistory_FRAGMENT();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("order_items", orderItems);
            fragment.setArguments(bundle);

            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            replaceFragment(fragment, "Lịch sử đơn hàng", false);
            return;
        }

        if (intent.getBooleanExtra("navigate_to_history", false)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            replaceFragment(new TatCaDonHang_FRAGMENT(), "Lịch sử đơn hàng", false);
            return;
        }

        openDefaultFragment();
    }

    private void openDefaultFragment() {
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
            replaceFragment(new Home_FRAGMENT(), "Trang Chủ", true);
        }
    }

    private void replaceFragment(Fragment fragment, String title, boolean showSearchBar) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);

        // Chỉ addToBackStack nếu không phải Home
        if (!(fragment instanceof Home_FRAGMENT)) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        tv_greeting.setText(title);
    }
}
