package com.phoneapp.phonepulse.ui.cart;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.phoneapp.phonepulse.R;

public class Cart_Activity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Thiết lập toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_cart);
        setSupportActionBar(toolbar);

        // Hiện nút quay lại
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // <-- Nút quay lại
            getSupportActionBar().setTitle("Giỏ hàng của bạn");
        }
    }

    // Xử lý khi nhấn vào nút back trên toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Quay về màn hình trước đó
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
