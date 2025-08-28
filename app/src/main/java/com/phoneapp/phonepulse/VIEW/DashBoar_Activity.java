package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.phoneapp.phonepulse.FRAGMENT.CategoryFragment;
import com.phoneapp.phonepulse.FRAGMENT.ChatListFragment;
import com.phoneapp.phonepulse.FRAGMENT.FavouriteFragment;
import com.phoneapp.phonepulse.FRAGMENT.Home_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.Profile_FRAGMENT;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.FRAGMENT.OrderHistory_FRAGMENT; // Thêm import cho OrderHistory_FRAGMENT
import com.phoneapp.phonepulse.request.OrderItem;

import java.util.ArrayList;

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

        // Xử lý Intent đến từ Oder_Activity
        handleIntent(getIntent());

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
            } else if (id == R.id.nav_favorites) {
                replaceFragment(new FavouriteFragment(), "Yêu thích", true);
                return true;
            } else if (id == R.id.nav_message) { // THÊM BLOCK NÀY
                // Xử lý khi chọn mục "Tin nhắn"
                // Tùy chọn 1: Nếu bạn muốn hiển thị danh sách chat bằng một Fragment
                // Giả sử bạn sẽ tạo một Fragment tên là ChatListFragment
                // Tham số thứ 3 (showSearchBar) có thể cần điều chỉnh tùy theo thiết kế của bạn
                replaceFragment(new ChatListFragment(), "Tin Nhắn", false); // Hoặc true nếu muốn thanh tìm kiếm

                // Tùy chọn 2: Nếu bạn muốn mở một Activity mới cho danh sách chat
                // Intent intent = new Intent(DashBoar_Activity.this, ChatListActivity.class); // Tạo Activity này
                // startActivity(intent);

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Xử lý Intent nếu Activity đã tồn tại
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            // Load Home mặc định nếu không có Intent nào
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                replaceFragment(new Home_FRAGMENT(), "Trang Chủ", true);
            }
            return;
        }

        // ✅ Tách riêng việc xử lý deep link/redirect từ MoMo hoặc các nguồn khác
        String fragmentToOpen = intent.getStringExtra("openFragment");
        if ("TatCaDonHang".equals(fragmentToOpen)) {
            Log.d("DashBoar_Activity", "Nhận yêu cầu mở fragment TatCaDonHang từ MoMo redirect.");
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            replaceFragment(new OrderHistory_FRAGMENT(), "Lịch sử đơn hàng", false);
            return; // Rất quan trọng để kết thúc xử lý tại đây
        }

        // ✅ Xử lý các Intent truyền dữ liệu khác (ví dụ: từ Cart_Activity)
        ArrayList<OrderItem> orderItems = intent.getParcelableArrayListExtra("order_items");
        if (orderItems != null && !orderItems.isEmpty()) {
            Log.d("DashBoar_Activity", "Nhận được " + orderItems.size() + " item từ Intent.");
            OrderHistory_FRAGMENT fragment = new OrderHistory_FRAGMENT();
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("order_items", orderItems);
            fragment.setArguments(bundle);

            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            replaceFragment(fragment, "Lịch sử đơn hàng", false);
            return;
        }

        // ✅ Xử lý các cờ Intent đơn giản (ví dụ: chỉ điều hướng)
        if (intent.getBooleanExtra("navigate_to_history", false)) {
            Log.d("DashBoar_Activity", "Nhận yêu cầu navigate_to_history.");
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            replaceFragment(new OrderHistory_FRAGMENT(), "Lịch sử đơn hàng", false);
            return;
        }

        // Load Home mặc định nếu không có điều kiện nào ở trên được thỏa mãn
        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(new Home_FRAGMENT(), "Trang Chủ", true);
        }
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
        if (card_search_view != null) {
            card_search_view.setVisibility(showSearchBar ? View.VISIBLE : View.GONE);
        }
    }
}