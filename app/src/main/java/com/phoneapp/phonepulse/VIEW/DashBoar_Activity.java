package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.phoneapp.phonepulse.FRAGMENT.CategoryFragment;
import com.phoneapp.phonepulse.FRAGMENT.ChatListFragment;
import com.phoneapp.phonepulse.FRAGMENT.FavouriteFragment;
import com.phoneapp.phonepulse.FRAGMENT.Home_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.OrderHistory_FRAGMENT;
import com.phoneapp.phonepulse.FRAGMENT.Profile_FRAGMENT;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.User;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashBoar_Activity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private Toolbar toolbar_DashBoar;
    private TextView tv_greeting;
    private ImageView iv_cart_icon;
    private EditText et_search_product;
    private CardView card_search_view;

    // ✅ ActivityResultLauncher để nhận kết quả từ EditProfileActivity
    private final ActivityResultLauncher<Intent> editProfileLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Người dùng đã cập nhật profile xong -> load lại Home và không cần kiểm tra lại
                    replaceFragment(new Home_FRAGMENT(), "Trang Chủ", true);
                } else {
                    // Nếu người dùng thoát ra mà chưa cập nhật, tiếp tục kiểm tra để buộc họ cập nhật lại.
                    // Loại bỏ dòng này nếu bạn muốn người dùng tự cập nhật
                }
            });

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

        // Sự kiện giỏ hàng
        iv_cart_icon.setOnClickListener(view -> {
            Intent intent = new Intent(DashBoar_Activity.this, Cart_Activity.class);
            startActivity(intent);
        });

        // Xử lý Intent đến từ Oder_Activity
        handleIntent(getIntent());

        // BottomNavigation xử lý chọn tab
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

        // Theo dõi để ẩn BottomNavigation nếu đang ở Fragment toàn màn hình
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (current != null && "FULLSCREEN".equals(current.getTag())) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                bottomNavigationView.setVisibility(View.VISIBLE);
            }
        });

        // ✅ Bỏ checkUserProfile() tại đây để không tự động chuyển hướng khi khởi động
        // Thay vào đó, bạn có thể gọi nó trong Profile_FRAGMENT để kiểm tra và hiển thị thông báo.
        // Hoặc bạn có thể gọi ở đây nhưng chỉ hiển thị Toast.

        // Ví dụ, gọi checkUserProfile() để chỉ hiển thị thông báo, không chuyển hướng
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
                replaceFragment(new Home_FRAGMENT(), "Trang Chủ", true);
            }
            return;
        }

        String fragmentToOpen = intent.getStringExtra("openFragment");
        if ("TatCaDonHang".equals(fragmentToOpen)) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            replaceFragment(new OrderHistory_FRAGMENT(), "Lịch sử đơn hàng", false);
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
            replaceFragment(new OrderHistory_FRAGMENT(), "Lịch sử đơn hàng", false);
            return;
        }

        if (getSupportFragmentManager().findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(new Home_FRAGMENT(), "Trang Chủ", true);
        }
    }

    private void replaceFragment(Fragment fragment, String title, boolean showSearchBar) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);

        if (!(fragment instanceof Home_FRAGMENT)) {
            transaction.addToBackStack(null);
        }
        transaction.commit();

        if (tv_greeting != null) {
            tv_greeting.setText(title);
        }

    }

    // ✅ Thêm tham số `autoRedirect` để kiểm soát hành vi chuyển hướng



}