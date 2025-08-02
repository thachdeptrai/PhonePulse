package com.phoneapp.phonepulse.VIEW;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.phoneapp.phonepulse.Adapter.OrderItemAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.request.OrderItem;

import java.util.ArrayList;

public class Oder_Activity extends AppCompatActivity {

    // Khai báo các View
    private MaterialToolbar toolbar;
    private TextView tvFullName, tvPhoneNumber, tvShippingAddress;
    private Button btnChangeAddress, btnPlaceOrder;
    private RecyclerView rvCheckoutProducts;
    private EditText etOrderNote;
    private RadioGroup paymentMethodGroup;
    private RadioButton radioCod, radioMomo;
    private TextView tvSubtotal, tvDiscount, tvFinalPrice, tvTotalAmount;
    private TextView tvAddCoupon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_oder);

        // Ánh xạ Toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Thanh toán đơn hàng");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish()); // Nút quay lại

        // Ánh xạ các View
        tvFullName = findViewById(R.id.tv_full_name);
        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        tvShippingAddress = findViewById(R.id.tv_shipping_address);
        btnChangeAddress = findViewById(R.id.btn_change_address);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        rvCheckoutProducts = findViewById(R.id.rv_checkout_products);
        etOrderNote = findViewById(R.id.et_order_note);
        paymentMethodGroup = findViewById(R.id.payment_method_group);
        radioCod = findViewById(R.id.radio_cod);
        radioMomo = findViewById(R.id.radio_momo);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvDiscount = findViewById(R.id.tv_discount);
        tvFinalPrice = findViewById(R.id.tv_final_price);
        tvTotalAmount = findViewById(R.id.tv_total_amount);
        tvAddCoupon = findViewById(R.id.tv_add_coupon);


        // Gán dữ liệu người dùng vào giao diện
        bindUserToUI();
        // Nhận dữ liệu truyền từ Intent
        ArrayList<OrderItem> orderItemList = (ArrayList<OrderItem>) getIntent().getSerializableExtra("order_items");

        if (orderItemList != null && !orderItemList.isEmpty()) {
            OrderItemAdapter adapter = new OrderItemAdapter(orderItemList);
            rvCheckoutProducts.setLayoutManager(new LinearLayoutManager(this));
            rvCheckoutProducts.setAdapter(adapter);

            // Tính tổng tiền
            int total = 0;
            for (OrderItem item : orderItemList) {
                total += item.getPrice() * item.getQuantity();
            }
            tvTotalAmount.setText(String.format("%,d đ", total).replace(",", "."));
            tvSubtotal.setText(tvTotalAmount.getText());
            tvFinalPrice.setText(tvTotalAmount.getText());
        } else {
            Log.w("OderActivity", "Không có sản phẩm trong đơn hàng.");
        }
    }

    private void bindUserToUI() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String fullname = preferences.getString("fullname", "");
        String phone = preferences.getString("phone", "");
        String address = preferences.getString("address", "");

        // Debug log
        Log.d("USER_PREF", "Fullname: " + fullname + ", Phone: " + phone + ", Address: " + address);

        tvFullName.setText(!fullname.isEmpty() ? fullname : "Chưa có tên");
        tvPhoneNumber.setText(!phone.isEmpty() ? phone : "Chưa có số điện thoại");
        tvShippingAddress.setText(!address.isEmpty() ? address : "Chưa có địa chỉ");
    }

}
