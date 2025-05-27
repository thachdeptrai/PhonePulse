package com.phoneapp.phonepulse.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.phoneapp.phonepulse.R;

public class ProfileActivity extends AppCompatActivity {
    LinearLayout itemOrderHistory, itemChangePassword, itemLogout;
    Button btnEditProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        // Gán view
        itemOrderHistory = findViewById(R.id.itemOrderHistory);
        itemChangePassword = findViewById(R.id.itemChangePassword);
        itemLogout = findViewById(R.id.itemLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);

        // Gán nội dung từng mục
        setupItem(itemOrderHistory, "Lịch sử đơn hàng", R.drawable.ic_order);
        setupItem(itemChangePassword, "Đổi mật khẩu", R.drawable.ic_password);
        setupItem(itemLogout, "Đăng xuất", R.drawable.ic_logout);

//        // Click: chỉnh sửa hồ sơ
//        btnEditProfile.setOnClickListener(v -> {
//            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
//            startActivity(intent);
//        });
//
//        // Click: các chức năng
//        itemOrderHistory.setOnClickListener(v -> {
//            Intent intent = new Intent(ProfileActivity.this, OrdersActivity.class);
//            startActivity(intent);
//        });
//
//        itemChangePassword.setOnClickListener(v -> {
//            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
//            startActivity(intent);
//        });

        itemLogout.setOnClickListener(v -> {
            // TODO: Xóa SharedPreferences, quay về LoginActivity
            finish();
        });
    }

    private void setupItem(View item, String title, int iconResId) {
        ImageView icon = item.findViewById(R.id.optionIcon);
        TextView text = item.findViewById(R.id.optionText);
        icon.setImageResource(iconResId);
        text.setText(title);
    }
}