package com.phoneapp.phonepulse.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.ui.cart.CartActivity;

import org.w3c.dom.Text;

public class ProductDetailActivity extends AppCompatActivity {
    //
    private ViewPager2 viewPagerImages;
    private TextView tvPrice;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_detail);
        // ví dụ
        Button button = findViewById(R.id.btnBuyNow);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProductDetailActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });

    }
}