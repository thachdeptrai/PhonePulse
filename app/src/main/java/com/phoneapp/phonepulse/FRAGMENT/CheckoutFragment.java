package com.phoneapp.phonepulse.FRAGMENT;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.phoneapp.phonepulse.Adapter.CheckoutProductAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.request.OrderRequest;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutFragment extends Fragment {

    private RecyclerView rvCheckoutProducts;
    private CheckoutProductAdapter adapter;
    private TextView tvShippingAddress, tvSubtotal, tvDiscount, tvFinalPrice;
    private Button btnPlaceOrder, btnChangeAddress;
    private RadioGroup paymentMethodGroup;
    private EditText etOrderNote;
    private ImageButton backButton;

    private ApiService apiService;
    private String token;

    // Giả lập dữ liệu từ giỏ hàng, sử dụng kiểu Order.OrderItem để tương thích với Adapter
    private List<Order.OrderItem> cartItems = new ArrayList<>();
    private double discountAmount = 500000;
    private double finalPrice = 8400000;
    private double subtotal = 8900000;
    private String shippingAddress = "123 Đường Lê Lợi, Quận 1, TP. Hồ Chí Minh";
    private String phoneNumber = "0901234567";

    public CheckoutFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Giả lập token, trong thực tế sẽ lấy từ SharedPreferences
        // Thêm kiểm tra null để tránh lỗi NullPointerException
        if (getContext() != null) {
            token = "Bearer " + Constants.getToken(requireContext());
            apiService = RetrofitClient.getApiService(token);
        }


        // Ánh xạ các view
        rvCheckoutProducts = view.findViewById(R.id.rv_checkout_products);
        tvShippingAddress = view.findViewById(R.id.tv_shipping_address);
        tvSubtotal = view.findViewById(R.id.tv_subtotal);
        tvDiscount = view.findViewById(R.id.tv_discount);
        tvFinalPrice = view.findViewById(R.id.tv_final_price);
        btnPlaceOrder = view.findViewById(R.id.btn_place_order);
        btnChangeAddress = view.findViewById(R.id.btn_change_address);
        paymentMethodGroup = view.findViewById(R.id.payment_method_group);
        etOrderNote = view.findViewById(R.id.et_order_note);
        backButton = view.findViewById(R.id.back_button);

        setupViews();

        // Xử lý sự kiện nhấn nút "Đặt hàng"
        btnPlaceOrder.setOnClickListener(v -> placeOrder());

        // Xử lý sự kiện nhấn nút "Thay đổi" địa chỉ
        btnChangeAddress.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng thay đổi địa chỉ đang được phát triển!", Toast.LENGTH_SHORT).show();
        });

        // Xử lý nút back
        backButton.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().finish();
            }
        });
    }

    private void setupViews() {
        // Giả lập dữ liệu sản phẩm trong giỏ hàng
        Order.OrderItem item1 = new Order.OrderItem();
        item1.setProductId("688b8b5faa03e29fca1a012e");
        item1.setVariantId("688b8b6baa03e29fca1a0130");
        item1.setQuantity(2);
        cartItems.add(item1);

        // Hiển thị danh sách sản phẩm
        adapter = new CheckoutProductAdapter(cartItems);
        rvCheckoutProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCheckoutProducts.setAdapter(adapter);

        // Định dạng tiền tệ
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0);

        // Hiển thị các thông tin khác
        tvShippingAddress.setText(shippingAddress);
        tvSubtotal.setText(currencyFormat.format(subtotal));
        tvDiscount.setText(String.format("- %s", currencyFormat.format(discountAmount)));
        tvFinalPrice.setText(currencyFormat.format(finalPrice));
    }

    private void placeOrder() {
        String selectedPaymentMethod;
        int checkedId = paymentMethodGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_cod) {
            selectedPaymentMethod = "COD";
        } else {
            selectedPaymentMethod = "Momo";
        }

        String orderNote = etOrderNote.getText().toString();

        OrderRequest orderRequest = new OrderRequest(
                shippingAddress,
                phoneNumber,
                null, // Voucher code
                selectedPaymentMethod,
                orderNote,
                finalPrice,
                cartItems
        );

        if (apiService == null) {
            Toast.makeText(getContext(), "Lỗi: API Service chưa được khởi tạo", Toast.LENGTH_SHORT).show();
            return;
        }

        // Gửi yêu cầu lên API
        apiService.createOrder(token, orderRequest).enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();
                    // Chuyển sang màn hình "Đặt hàng thành công" hoặc màn hình chi tiết đơn hàng
                } else {
                    String errorMessage = "Đặt hàng thất bại.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    } else if (response.errorBody() != null) {
                        try {
                            Log.e("CheckoutFragment", "Error Body: " + response.errorBody().string());
                        } catch (Exception e) {
                            Log.e("CheckoutFragment", "Error parsing error body", e);
                        }
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                    Log.e("CheckoutFragment", "API call failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CheckoutFragment", "API call failed", t);
            }
        });
    }
}