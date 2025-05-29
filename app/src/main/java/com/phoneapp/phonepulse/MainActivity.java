package com.phoneapp.phonepulse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.phoneapp.phonepulse.ui.product.ProductDetailActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Delay 1 giây rồi chuyển sang ProductDetailActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
            startActivity(intent);
            // Có thể finish MainActivity nếu không muốn quay lại
            finish();
        }, 1000);
    }
}
