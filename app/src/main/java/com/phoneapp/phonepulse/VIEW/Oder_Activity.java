package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.phoneapp.phonepulse.Adapter.AddressAdapter;
import com.phoneapp.phonepulse.Adapter.OrderItemAdapter;
import com.phoneapp.phonepulse.MainActivity;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Address;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.models.District;
import com.phoneapp.phonepulse.models.Order;
import com.phoneapp.phonepulse.models.Province;
import com.phoneapp.phonepulse.models.Variant;
import com.phoneapp.phonepulse.models.Voucher;
import com.phoneapp.phonepulse.models.Ward;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.request.MomoData;
import com.phoneapp.phonepulse.request.OrderItem;
import com.phoneapp.phonepulse.request.OrderRequest;
import com.phoneapp.phonepulse.ui.voucher.VoucherBottomSheet;
import com.phoneapp.phonepulse.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Oder_Activity extends AppCompatActivity {

    private static final String TAG = "Oder_Activity";

    private MaterialToolbar toolbar;
    private TextView tvFullName, tvPhoneNumber, tvShippingAddress;
    private Button btnChangeAddress, btnPlaceOrder;
    private RecyclerView rvCheckoutProducts;
    private EditText etOrderNote;
    private RadioGroup paymentMethodGroup;
    private RadioButton radioCod, radioMomo;
    private TextView tvSubtotal, tvDiscount, tvFinalPrice, tvTotalAmount;
    private TextView tvAddCoupon, tvSelectedCoupon; // ✅ THÊM tvSelectedCoupon
    private RecyclerView recyclerView;
    private List<Address> addressList;
    private AddressAdapter addressAdapter;

    // Dữ liệu
    public  ArrayList<OrderItem> orderItemList = new ArrayList<>();
    private ApiService apiService;
    private List<Variant> variantsInCart = new ArrayList<>();
    private Voucher selectedVoucher; // ✅ THÊM biến voucher đã chọn
    private int subtotal = 0; // ✅ THÊM biến subtotal để sử dụng trong các hàm khác

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_oder);

        // Load danh sách đã lưu từ SharedPreferences
        addressList = loadAddressesFromPrefs();
        if (addressList == null) addressList = new ArrayList<>();
        // ✅ Tách biệt luồng logic xử lý Intent
        Intent intent = getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Luồng 2: Xử lý deep link từ MoMo
            handleMomoReturnIntent(intent);
        } else {
            // Luồng 1: Xử lý khi đến từ giỏ hàng
            handleInitialOrderIntent(intent);
        }
    }
    // ✅ Phương thức mới để xử lý Intent ban đầu (khi đến từ giỏ hàng)
    private void handleInitialOrderIntent(Intent intent) {
        getIntentData(); // ✅ lấy dữ liệu orderItemList
        if (orderItemList == null || orderItemList.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào để đặt hàng.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Khởi tạo ApiService sớm
        apiService = RetrofitClient.getApiService(Constants.getToken(this));

        initViews();
        setupToolbar();
        bindUserToUI();
        setupListeners();
        openBottomSheetDialog();

        loadVariantsInCart(); // ✅ chỉ gọi 1 lần
    }
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
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
        tvSelectedCoupon = findViewById(R.id.tv_selected_coupon);

    }
    /**
     * Thiết lập Toolbar với tiêu đề và nút quay lại.
     */

    private void setupToolbar() {
        toolbar.setTitle("Thanh toán đơn hàng");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    private void openBottomSheetDialog() {
        btnChangeAddress.setOnClickListener(v -> {
            BottomSheetDialog dialog = new BottomSheetDialog(this);
            View view = getLayoutInflater().inflate(R.layout.layout_change_address, null);
            dialog.setContentView(view);

            EditText edtName = view.findViewById(R.id.edtName);
            EditText edtPhone = view.findViewById(R.id.edtPhone);
            EditText edtStreet = view.findViewById(R.id.edtStreet);
            Spinner spProvince = view.findViewById(R.id.spProvince);
            Spinner spDistrict = view.findViewById(R.id.spDistrict);
            Spinner spWard = view.findViewById(R.id.spWard);
            Button btnUpdate = view.findViewById(R.id.btnUpdate);
            ImageView btnClose = view.findViewById(R.id.btnClose);

            // RecyclerView hiển thị danh sách địa chỉ
            RecyclerView rvAddresses = view.findViewById(R.id.recyclerViewAddresses);
            addressAdapter = new AddressAdapter(addressList, selectedAddress -> {
                Log.d("SelectedAddress",
                        "Họ tên: " + selectedAddress.getFullName() +
                                " | SĐT: " + selectedAddress.getPhoneNumber() +
                                " | Địa chỉ: " + selectedAddress.getFullAddress()
                );

                tvFullName.setText(selectedAddress.getFullName());
                tvPhoneNumber.setText(selectedAddress.getPhoneNumber());
                tvShippingAddress.setText(selectedAddress.getFullAddress());

                // 🔥 Lưu list + địa chỉ đang chọn
                saveAddressesToPrefs(addressList);
                saveSelectedAddress(selectedAddress);

                dialog.dismiss();
            });

            rvAddresses.setLayoutManager(new LinearLayoutManager(this));
            rvAddresses.setAdapter(addressAdapter);

            btnClose.setOnClickListener(v1 -> dialog.dismiss());

            // ==== Retrofit API ====
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://provinces.open-api.vn/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            ApiService locationApi = retrofit.create(ApiService.class);

            // Load provinces
            locationApi.getProvinces().enqueue(new Callback<List<Province>>() {
                @Override
                public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Province> provinceList = response.body();
                        List<String> provinceNames = new ArrayList<>();
                        for (Province p : provinceList) provinceNames.add(p.getName());

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                Oder_Activity.this,
                                android.R.layout.simple_spinner_item,
                                provinceNames
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spProvince.setAdapter(adapter);

                        spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                                Province selectedProvince = provinceList.get(pos);
                                loadDistricts(locationApi, selectedProvince.getCode(), spDistrict, spWard);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                    } else {
                        Toast.makeText(Oder_Activity.this, "Không tải được danh sách tỉnh", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<Province>> call, Throwable t) {
                    Toast.makeText(Oder_Activity.this, "Lỗi tải tỉnh: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Nút cập nhật -> thêm địa chỉ mới
            btnUpdate.setOnClickListener(v1 -> {
                String name = edtName.getText().toString().trim();
                String phone = edtPhone.getText().toString().trim();
                String street = edtStreet.getText().toString().trim();

                if (name.isEmpty() || phone.isEmpty() || street.isEmpty() ||
                        spProvince.getSelectedItem() == null ||
                        spDistrict.getSelectedItem() == null ||
                        spWard.getSelectedItem() == null) {
                    Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                    return;
                }

                String province = spProvince.getSelectedItem().toString();
                String district = spDistrict.getSelectedItem().toString();
                String ward = spWard.getSelectedItem().toString();

                String fullAddress = street + ", " + ward + ", " + district + ", " + province;

                Address newAddress = new Address(name, phone, fullAddress);
                addressList.add(newAddress);

                addressAdapter.notifyItemInserted(addressList.size() - 1);

                // Lưu SharedPreferences
                saveAddressesToPrefs(addressList);

                // ✅ Cập nhật UI chính luôn với địa chỉ mới
                tvFullName.setText(name);
                tvPhoneNumber.setText(phone);
                tvShippingAddress.setText(fullAddress);

                Toast.makeText(this, "Thêm địa chỉ thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });

            dialog.show();
        });
    }


    // Hàm load Districts theo provinceCode
    private void loadDistricts(ApiService api, int provinceCode, Spinner spDistrict, Spinner spWard) {
        api.getProvinceDetail(provinceCode).enqueue(new Callback<Province>() {
            @Override
            public void onResponse(Call<Province> call, Response<Province> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<District> districtList = response.body().getDistricts();
                    List<String> names = new ArrayList<>();
                    for (District d : districtList) names.add(d.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            Oder_Activity.this,
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDistrict.setAdapter(adapter);

                    spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            District selected = districtList.get(pos);
                            loadWards(api, selected.getCode(), spWard);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } else {
                    Toast.makeText(Oder_Activity.this, "Không tải được danh sách huyện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Province> call, Throwable t) {
                Toast.makeText(Oder_Activity.this, "Lỗi tải huyện: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm load Wards theo districtCode
    private void loadWards(ApiService api, int districtCode, Spinner spWard) {
        api.getDistrictDetail(districtCode).enqueue(new Callback<District>() {
            @Override
            public void onResponse(Call<District> call, Response<District> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Ward> wardList = response.body().getWards();
                    List<String> names = new ArrayList<>();
                    for (Ward w : wardList) names.add(w.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            Oder_Activity.this,
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spWard.setAdapter(adapter);
                } else {
                    Toast.makeText(Oder_Activity.this, "Không tải được danh sách xã", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<District> call, Throwable t) {
                Toast.makeText(Oder_Activity.this, "Lỗi tải xã: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm lưu danh sách Address vào SharedPreferences
    private void saveAddressesToPrefs(List<Address> addresses) {
        SharedPreferences prefs = getSharedPreferences("address_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(addresses);
        editor.putString("address_list", json);
        editor.apply();
    }

    // Hàm load danh sách Address từ SharedPreferences
    private List<Address> loadAddressesFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("address_prefs", MODE_PRIVATE);
        String json = prefs.getString("address_list", null);

        if (json != null) {
            Type type = new TypeToken<List<Address>>() {}.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }
    // Hàm lưu địa chỉ đã chọn
    private void saveSelectedAddress(Address address) {
        SharedPreferences prefs = getSharedPreferences("address_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("selected_name", address.getFullName());
        editor.putString("selected_phone", address.getPhoneNumber());
        editor.putString("selected_address", address.getFullAddress());
        editor.apply();
    }

    // Hàm load địa chỉ đã chọn
    private Address loadSelectedAddress() {
        SharedPreferences prefs = getSharedPreferences("address_prefs", MODE_PRIVATE);
        String name = prefs.getString("selected_name", "");
        String phone = prefs.getString("selected_phone", "");
        String fullAddress = prefs.getString("selected_address", "");

        return new Address(name, phone, fullAddress);
    }


    /**
     * Khởi tạo tất cả các thành phần UI bằng cách tìm ID tương ứng của chúng.
     */


    /**
     * Lấy danh sách các sản phẩm trong đơn hàng từ Intent và kiểm tra giá.
     */

    private void getIntentData() {
        orderItemList = getIntent().getParcelableArrayListExtra("order_items");
        if (orderItemList != null && !orderItemList.isEmpty()) {
            Log.d(TAG, "Kiểm tra OrderItems từ Intent:");
            for (int i = 0; i < orderItemList.size(); i++) {
                OrderItem item = orderItemList.get(i);
                Log.d(TAG, String.format(Locale.getDefault(),
                        "  Item %d: Name=%s, Price=%d, Quantity=%d, ProductId=%s, VariantId=%s",
                        i, item.getName(), item.getPrice(), item.getQuantity(), item.getProductId(), item.getVariantId()));
                if (item.getPrice() <= 0) {
                    Log.e(TAG, "❌ CẢNH BÁO: OrderItem '" + item.getName() + "' có giá <= 0 từ Intent! Tổng giá có thể sai.");
                }
            }
        } else {
            Log.w(TAG, "orderItemList rỗng hoặc null từ Intent.");
        }
    }

    /**
     * Thiết lập các lắng nghe sự kiện cho các nút và các thành phần UI khác.
     */

    private void setupListeners() {
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        // btnChangeAddress.setOnClickListener(v -> handleChangeAddress());

        // ✅ THÊM: Listener cho nút "Thêm mã giảm giá"
        if (tvAddCoupon != null) {
            tvAddCoupon.setOnClickListener(v -> fetchVouchers());
        }
    }

    /**
     * Tải thông tin chi tiết của các biến thể (variant) có trong giỏ hàng từ API.
     * Sử dụng CountDownLatch để đợi tất cả các yêu cầu API hoàn thành trước khi cập nhật UI.
     */
    private void loadVariantsInCart() {
        apiService = RetrofitClient.getApiService(Constants.getToken(this));
        final CountDownLatch latch = new CountDownLatch(orderItemList.size());
        variantsInCart.clear();

        for (OrderItem item : orderItemList) {
            final OrderItem currentItem = item;

            if (currentItem.getProductId() == null || currentItem.getVariantId() == null ||
                    currentItem.getProductId().isEmpty() || currentItem.getVariantId().isEmpty()) {
                Log.w(TAG, "Bỏ qua item '" + currentItem.getName() + "' do thiếu productId hoặc variantId. Giảm bộ đếm.");
                latch.countDown();
                continue;
            }

            apiService.getVariantForProductById(currentItem.getProductId(), currentItem.getVariantId())
                    .enqueue(new Callback<Variant>() {
                        @Override
                        public void onResponse(Call<Variant> call, Response<Variant> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Variant variant = response.body();
                                if (variant != null) {
                                    variantsInCart.add(variant);
                                    Log.d(TAG, "Đã tải biến thể: " + variant.getId() + " - Tồn kho: " + variant.getQuantity());
                                } else {
                                    Log.e(TAG, "Phản hồi API cho biến thể " + currentItem.getVariantId() + " là null.");
                                }
                            } else {
                                Log.e(TAG, "Lỗi khi lấy biến thể " + currentItem.getVariantId() + ". Mã lỗi: " + response.code() + ", Thông báo: " + response.message());
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onFailure(Call<Variant> call, Throwable t) {
                            Log.e(TAG, "Lỗi mạng khi lấy biến thể " + currentItem.getVariantId() + ": " + t.getMessage(), t);
                            latch.countDown();
                        }
                    });
        }

        new Thread(() -> {
            try {
                latch.await();
                runOnUiThread(this::updateUIWithCartItems);
                Log.d(TAG, "Tất cả các biến thể đã được tải. Cập nhật UI.");
            } catch (InterruptedException e) {
                Log.e(TAG, "Luồng bị gián đoạn khi chờ tải biến thể.", e);
                runOnUiThread(this::updateUIWithCartItems); // Vẫn cố gắng cập nhật UI
            }
        }).start();
    }

    /**
     * Cập nhật giao diện người dùng với các sản phẩm trong giỏ hàng và tính toán tổng số tiền.
     */
    private void updateUIWithCartItems() {
        OrderItemAdapter adapter = new OrderItemAdapter(orderItemList);
        rvCheckoutProducts.setLayoutManager(new LinearLayoutManager(this));
        rvCheckoutProducts.setAdapter(adapter);

        // ✅ ĐÃ SỬA: Tính và lưu subtotal
        subtotal = 0;
        for (OrderItem item : orderItemList) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        String formattedTotal = formatCurrency(subtotal);
        tvTotalAmount.setText(formattedTotal);
        tvSubtotal.setText(formattedTotal);
        tvFinalPrice.setText(formattedTotal);
        tvDiscount.setText(formatCurrency(0));
        Log.d(TAG, "UI đã được cập nhật. Tổng tiền hiển thị: " + formattedTotal);
    }

    /**
     * Xử lý quá trình đặt hàng.
     * Kiểm tra token, tồn kho, sau đó gửi yêu cầu tạo đơn hàng đến API.
     */



    private void placeOrder() {
        String token = Constants.getToken(Oder_Activity.this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để đặt hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (orderItemList == null || orderItemList.isEmpty()) {
            Toast.makeText(this, "Không có sản phẩm nào để đặt hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkStockBeforeOrder()) {
            Toast.makeText(this, "Một số sản phẩm không đủ tồn kho.", Toast.LENGTH_LONG).show();
            return;
        }

        // ✅ Kiểm tra địa chỉ
        Address selectedAddress = null;
        if (addressAdapter != null) {
            selectedAddress = addressAdapter.getSelectedAddress();
        }

        String shippingAddress = (selectedAddress != null)
                ? selectedAddress.getFullAddress()
                : (tvShippingAddress != null ? tvShippingAddress.getText().toString().trim() : "");

        if (shippingAddress.isEmpty() || shippingAddress.equals("Chưa có địa chỉ")) {
            Toast.makeText(this, "Vui lòng cập nhật địa chỉ giao hàng.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidAddress(shippingAddress)) {
            Toast.makeText(this,
                    "⚠️ Vui lòng nhập địa chỉ hợp lệ trước khi đặt hàng.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String paymentMethod = radioCod.isChecked() ? "COD" : "MOMO";
        String note = etOrderNote.getText().toString().trim();
        int discount = calculateDiscount(subtotal, selectedVoucher);
        int finalPrice = subtotal - discount;

        if (finalPrice <= 0) {
            Toast.makeText(this, "Tổng giá đơn hàng không hợp lệ.", Toast.LENGTH_LONG).show();
            return;
        }

        OrderRequest request = new OrderRequest(orderItemList, discount, finalPrice,
                shippingAddress, paymentMethod, note);

        apiService = RetrofitClient.getApiService(token);

        if (paymentMethod.equals("COD")) {
            // ================= COD =================
            apiService.createOrder(request).enqueue(new Callback<ApiResponse<Order>>() {
                @Override
                public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Toast.makeText(Oder_Activity.this, "Đặt hàng COD thành công!", Toast.LENGTH_SHORT).show();

                        // ✅ Lưu thông tin vào SharedPreferences
                        saveUserInfoForHistory(
                                tvFullName.getText().toString().trim(),
                                tvPhoneNumber.getText().toString().trim(),
                                shippingAddress
                        );

                        updateVariantStockOnServer(orderItemList);
                    } else {
                        Toast.makeText(Oder_Activity.this, "Đặt hàng COD thất bại.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                    Toast.makeText(Oder_Activity.this, "Lỗi mạng COD: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // ================= MOMO =================
            apiService.createMomoOrder("Bearer " + token, request).enqueue(new Callback<ApiResponse<MomoData>>() {
                @Override
                public void onResponse(Call<ApiResponse<MomoData>> call, Response<ApiResponse<MomoData>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        String payUrl = response.body().getData().getMomoPayUrl();
                        String qrUrl = response.body().getData().getQrCodeUrl();

                        // ✅ Lưu thông tin vào SharedPreferences trước khi mở MoMo
                        saveUserInfoForHistory(
                                tvFullName.getText().toString().trim(),
                                tvPhoneNumber.getText().toString().trim(),
                                shippingAddress
                        );

                        // 👉 Mở màn thanh toán MoMo
                        Intent intent = new Intent(Oder_Activity.this, MomoPaymentWebViewActivity.class);
                        intent.putExtra(MomoPaymentWebViewActivity.EXTRA_URL, payUrl);
                        intent.putExtra(MomoPaymentWebViewActivity.EXTRA_QR, qrUrl);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Oder_Activity.this, "Không tạo được link MoMo.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<MomoData>> call, Throwable t) {
                    Toast.makeText(Oder_Activity.this, "Lỗi mạng khi gọi MoMo: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    private void saveUserInfoForHistory(String name, String phone, String address) {
        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("customer_name", name);
        editor.putString("customer_phone", phone);
        editor.putString("customer_address", address);
        editor.apply();
    }

    private void handleMomoReturnIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && "momo_return".equals(data.getScheme())) {
            String resultCodeStr = data.getQueryParameter("resultCode");
            String orderId = data.getQueryParameter("orderId");
            String message = data.getQueryParameter("message");
            String extraData = data.getQueryParameter("extraData");

            int resultCode = resultCodeStr != null ? Integer.parseInt(resultCodeStr) : -1;
            Log.i(TAG, "MoMo Return Params: resultCode=" + resultCode +
                    ", orderId=" + orderId + ", message=" + message);

            confirmMomoPayment(resultCode, orderId, message, extraData);
        }
    }
    private void confirmMomoPayment(int resultCode, String orderId, String message, String extraData) {

        String token = Constants.getToken(Oder_Activity.this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xác nhận thanh toán.", Toast.LENGTH_SHORT).show();
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<ApiResponse<Order>> call = apiService.handleMomoReturn(resultCode, orderId, message, extraData);

        call.enqueue(new Callback<ApiResponse<Order>>() {
            @Override
            public void onResponse(Call<ApiResponse<Order>> call, Response<ApiResponse<Order>> response) {
                Log.d(TAG, "onResponse: API call successful? " + response.isSuccessful() + ", HTTP Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Order> apiResponse = response.body();
                    Log.d(TAG, "API Response: isSuccess=" + apiResponse.isSuccess() + ", message=" + apiResponse.getMessage());

                    if (apiResponse.isSuccess()) {
                        Order order = apiResponse.getData();
                        Log.i(TAG, "Thanh toán thành công! Order ID: " + (order != null ? order.getId() : "null"));

                        Toast.makeText(Oder_Activity.this, "Thanh toán thành công!", Toast.LENGTH_SHORT).show();


                        // ✅ Xóa giỏ hàng
                        clearCartOnServer();
                        // ✅ BỔ SUNG: Gọi phương thức để cập nhật tồn kho và xóa giỏ hàng.
                        // Luồng chuyển hướng sẽ được gọi từ phương thức này sau khi hoàn tất.
                        updateVariantStockOnServer(orderItemList);

                    } else {
                        Toast.makeText(Oder_Activity.this,
                                "Thanh toán thất bại: " + apiResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(Oder_Activity.this,
                            "Lỗi khi xác nhận thanh toán! (HTTP " + response.code() + ")",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Order>> call, Throwable t) {
                Toast.makeText(Oder_Activity.this,
                        "API lỗi: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
    // ✅ THÊM: Phương thức cập nhật giá cuối cùng sau khi áp dụng voucher
    private void updateFinalPrice() {
        int discount = calculateDiscount(subtotal, selectedVoucher);
        int finalPrice = subtotal - discount;
        if (finalPrice < 0) finalPrice = 0;

        // Nếu có giảm giá thì hiển thị "- xxx đ", còn không thì "0 đ"
        if (discount > 0) {
            tvDiscount.setText("- " + formatCurrency(discount));
        } else {
            tvDiscount.setText(formatCurrency(0));
        }

        tvFinalPrice.setText(formatCurrency(finalPrice));
        tvTotalAmount.setText(formatCurrency(finalPrice));
    }


    // ✅ THÊM: Phương thức tính toán giảm giá
    private int calculateDiscount(int subtotal, Voucher voucher) {
        if (voucher == null) {
            return 0; // Không có voucher thì không giảm
        }

        // Kiểm tra điều kiện đơn hàng tối thiểu
        if (subtotal < voucher.getMinOrderValue()) {
            Toast.makeText(this,
                    "Đơn hàng cần tối thiểu " + formatCurrency((int) voucher.getMinOrderValue())
                            + " để áp dụng voucher này",
                    Toast.LENGTH_SHORT).show();
            return 0;
        }

        int discount = 0;
        switch (voucher.getDiscountType()) {
            case "percent":
                discount = (int) (subtotal * voucher.getDiscountValue() / 100);
                if (voucher.getMaxDiscount() > 0 && discount > voucher.getMaxDiscount()) {
                    discount = (int) voucher.getMaxDiscount();
                }
                break;

            case "amount":
                discount = (int) voucher.getDiscountValue();
                break;
        }

        return Math.max(discount, 0);
    }


    // ✅ THÊM: Phương thức định dạng tiền tệ
    /**
     * Định dạng một số nguyên thành chuỗi tiền tệ tiếng Việt (ví dụ: "11.700.000 đ").
     * Đảm bảo sử dụng Locale Việt Nam để có định dạng dấu chấm phân cách hàng nghìn.
     *
     * @param amount Giá trị tiền tệ cần định dạng.
     * @return Chuỗi tiền tệ đã định dạng.
     */
    private String formatCurrency(int amount) {
        // Sử dụng Locale Việt Nam để đảm bảo định dạng số nhất quán (dấu chấm cho hàng nghìn)
        // và thêm ký hiệu tiền tệ 'đ' vào cuối.
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Mặc định, NumberFormat.getCurrencyInstance cho Locale "vi", "VN" sẽ thêm ký hiệu "₫"
        // và có thể có dấu thập phân ".00".
        // Chúng ta cần điều chỉnh để nó chỉ hiển thị số nguyên và thêm " đ" thủ công.
        DecimalFormat decimalFormatter = (DecimalFormat) formatter;
        // Loại bỏ phần thập phân
        decimalFormatter.applyPattern("#,###"); // Sử dụng dấu phẩy tạm thời để trình bày ở đây,
        // nhưng thực tế với Locale "vi", "VN" nó sẽ dùng dấu chấm.
        // Hoặc bạn có thể dùng "#.###" nếu muốn tường minh.

        // Bạn có thể thiết lập Symbol nếu muốn kiểm soát ký hiệu tiền tệ
        // decimalFormatter.setCurrencySymbol(" đ"); // Điều này có thể không hoạt động như mong đợi với mọi Locale

        // Cách tốt nhất là định dạng số, sau đó nối thêm ký hiệu " đ"
        String formattedNumber = decimalFormatter.format(amount);

        // Sau khi định dạng, thay thế dấu phẩy (nếu có do pattern) bằng dấu chấm theo chuẩn VN
        // và đảm bảo định dạng cuối cùng là "số.số.số đ"
        formattedNumber = formattedNumber.replace(",", "."); // Thay dấu phẩy bằng dấu chấm cho định dạng VN
        // (nếu DecimalFormat mặc định dùng dấu phẩy cho grouping)


        return formattedNumber + " đ";
    }

    // ✅ THÊM: Phương thức gọi API để lấy danh sách voucher
    private void fetchVouchers() {
        String token = Constants.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem mã giảm giá.", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getVouchers("Bearer " + token).enqueue(new Callback<ApiResponse<List<Voucher>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Voucher>>> call, Response<ApiResponse<List<Voucher>>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Voucher> vouchers = response.body().getData();
                    showVoucherBottomSheet(vouchers); // ✅ CHUYỂN DANH SÁCH VOUCHER VÀO BOTTOM SHEET
                } else {
                    String errorMsg = response.body() != null ? response.body().getMessage() : "Không thể tải mã giảm giá. Vui lòng thử lại.";
                    Toast.makeText(Oder_Activity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Voucher>>> call, Throwable t) {
                Toast.makeText(Oder_Activity.this, "Lỗi kết nối khi tải mã giảm giá.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ THÊM: Phương thức hiển thị BottomSheet
    private void showVoucherBottomSheet(List<Voucher> vouchers) {
        if (vouchers == null || vouchers.isEmpty()) {
            Toast.makeText(this, "Không có mã giảm giá nào hiện có.", Toast.LENGTH_SHORT).show();
            return;
        }

        VoucherBottomSheet bottomSheet = new VoucherBottomSheet(vouchers, selected -> {
            if (selected != null && subtotal < selected.getMinOrderValue()) {
                Toast.makeText(this, "Đơn hàng cần tối thiểu "
                        + formatCurrency((int) selected.getMinOrderValue())
                        + " để dùng voucher này", Toast.LENGTH_SHORT).show();
                return; // 🚫 Giữ bottomsheet mở, không set voucher
            }

            this.selectedVoucher = selected;
            if (tvSelectedCoupon != null) {
                tvSelectedCoupon.setText(selected != null ? selected.getCode() : "Không dùng mã");
            }
            updateFinalPrice();
        });
        bottomSheet.show(getSupportFragmentManager(), "VoucherBottomSheet");
    }

    /**
     * Kiểm tra xem có đủ tồn kho cho tất cả các sản phẩm trong đơn hàng hay không.
     *
     * @return true nếu đủ tồn kho, false nếu không.
     */
    private boolean checkStockBeforeOrder() {
        boolean allInStock = true;
        StringBuilder stockErrorMsg = new StringBuilder("Các sản phẩm sau không đủ tồn kho:\n");
        for (OrderItem item : orderItemList) {
            Variant variant = findVariantById(item.getVariantId(), variantsInCart);
            if (variant == null) {
                stockErrorMsg.append("- ").append(item.getName()).append(" (biến thể không tìm thấy)\n");
                allInStock = false;
            } else if (variant.getQuantity() < item.getQuantity()) {
                stockErrorMsg.append("- ").append(item.getName())
                        .append(" (Yêu cầu: ").append(item.getQuantity())
                        .append(", Tồn kho: ").append(variant.getQuantity()).append(")\n");
                allInStock = false;
            }
        }
        if (!allInStock) {
            Log.e(TAG, "Kiểm tra tồn kho thất bại: \n" + stockErrorMsg.toString());
        }
        return allInStock;
    }

    /**
     * Cập nhật số lượng tồn kho của các biến thể trên máy chủ sau khi đặt hàng thành công.
     * Phương thức này đảm bảo rằng giá sản phẩm không bị thay đổi.
     *
     * @param orderedItems Danh sách các sản phẩm đã được đặt.
     */
    public void updateVariantStockOnServer(ArrayList<OrderItem> orderedItems) {
        if (orderedItems == null || orderedItems.isEmpty()) {
            Log.w(TAG, "Không có OrderedItems để cập nhật tồn kho. Chuyển hướng.");
            navigateToOrderHistory();
            return;
        }

        final CountDownLatch stockUpdateLatch = new CountDownLatch(orderedItems.size());
        Log.d(TAG, "Bắt đầu cập nhật tồn kho cho " + orderedItems.size() + " sản phẩm.");

        for (OrderItem item : orderedItems) {
            final OrderItem finalOrderItem = item;

            String variantId = finalOrderItem.getVariantId();
            String productId = finalOrderItem.getProductId();

            if (variantId == null || variantId.trim().isEmpty() || productId == null || productId.trim().isEmpty()) {
                Log.w(TAG, "❌ Bỏ qua cập nhật tồn kho: Thiếu variantId hoặc productId cho sản phẩm: " + finalOrderItem.getName());
                stockUpdateLatch.countDown();
                continue;
            }

            Variant foundVariant = findVariantById(variantId, variantsInCart);
            if (foundVariant != null) {
                int currentQuantity = foundVariant.getQuantity();
                int quantityOrdered = finalOrderItem.getQuantity();
                int newQuantity = Math.max(currentQuantity - quantityOrdered, 0); // Đảm bảo số lượng không âm

                Log.d(TAG, String.format(Locale.getDefault(),
                        "📦 Chuẩn bị cập nhật tồn kho cho Variant ID: %s | Tồn kho cũ: %d | Số lượng đặt: %d | Tồn kho mới dự kiến: %d",
                        variantId, currentQuantity, quantityOrdered, newQuantity
                ));

                Variant updatedVariant = new Variant();
                updatedVariant.setId(variantId);
                updatedVariant.setQuantity(newQuantity);
                // CHÚ Ý: KHÔNG GÁN GIÁ (PRICE) VÀO updatedVariant. Giá không thay đổi!

                apiService.updateVariantForProductById(productId, variantId, updatedVariant)
                        .enqueue(new Callback<ApiResponse<Variant>>() {
                            @Override
                            public void onResponse(Call<ApiResponse<Variant>> call, Response<ApiResponse<Variant>> response) {
                                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                    Log.i(TAG, "✅ Tồn kho đã cập nhật thành công cho variant ID: " + variantId + ". Tồn kho mới trên server: " + response.body().getData().getQuantity());
                                } else {
                                    String errorDetail = (response.body() != null ? response.body().getMessage() : "Không rõ lỗi.");
                                    Log.e(TAG, "⚠️ Lỗi cập nhật tồn kho cho variant ID: " + variantId +
                                            ". Mã lỗi: " + response.code() + ". Chi tiết: " + errorDetail + ". Payload: " + call.request().body());
                                }
                                stockUpdateLatch.countDown();
                            }

                            @Override
                            public void onFailure(Call<ApiResponse<Variant>> call, Throwable t) {
                                Log.e(TAG, "🌐 Lỗi mạng/API khi cập nhật tồn kho cho variant ID: " + variantId + ": " + t.getMessage(), t);
                                stockUpdateLatch.countDown();
                            }
                        });
            } else {
                Log.e(TAG, "❌ Không tìm thấy biến thể ID: " + variantId + " trong danh sách biến thể đã tải cục bộ. Không thể cập nhật tồn kho.");
                stockUpdateLatch.countDown();
            }
        }

        new Thread(() -> {
            try {
                stockUpdateLatch.await();
                runOnUiThread(() -> {
                    Log.d(TAG, "✅ Tất cả cập nhật tồn kho đã hoàn tất. Bắt đầu xóa giỏ hàng.");
                    clearCartOnServer(); // Gọi phương thức xóa giỏ hàng
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "⚠️ Luồng bị gián đoạn khi chờ cập nhật tồn kho.", e);
                runOnUiThread(this::navigateToOrderHistory);
            }
        }).start();
    }

    public void clearCartOnServer() {
        if (orderItemList == null || orderItemList.isEmpty()) {
            Log.d(TAG, "Giỏ hàng đã rỗng. Chuyển hướng.");
            navigateToOrderHistory();
            return;
        }

        // Sử dụng CountDownLatch để đợi tất cả các yêu cầu xóa hoàn tất
        final CountDownLatch cartRemovalLatch = new CountDownLatch(orderItemList.size());
        Log.d(TAG, "Bắt đầu xóa " + orderItemList.size() + " sản phẩm khỏi giỏ hàng.");

        for (OrderItem item : orderItemList) {
            String productId = item.getProductId();   // ✅ Lấy productId từ OrderItem
            String variantId = item.getVariantId();

            // Kiểm tra tính hợp lệ của cả productId và variantId
            if (productId == null || productId.trim().isEmpty() ||
                    variantId == null || variantId.trim().isEmpty()) {
                Log.w(TAG, "❌ Bỏ qua xóa giỏ hàng: Thiếu productId hoặc variantId cho sản phẩm: " + item.getName());
                cartRemovalLatch.countDown(); // Giảm bộ đếm ngay lập tức nếu dữ liệu không hợp lệ
                continue;
            }

            // ✅ Tạo request với cả productId và variantId
            CartRequest.RemoveCartItem request = new CartRequest.RemoveCartItem(productId, variantId);

            apiService.removeFromCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
                @Override
                public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        Log.i(TAG, "✅ Đã xóa thành công sản phẩm với productId: " + productId + ", variantId: " + variantId + " khỏi giỏ hàng.");
                    } else {
                        String errorDetail = (response.body() != null ? response.body().getMessage() : "Không rõ lỗi.");
                        Log.e(TAG, "⚠️ Lỗi xóa sản phẩm khỏi giỏ hàng cho productId: " + productId + ", variantId: " + variantId +
                                ". Mã lỗi: " + response.code() + ". Chi tiết: " + errorDetail);
                    }
                    cartRemovalLatch.countDown(); // Giảm bộ đếm sau mỗi phản hồi API
                }

                @Override
                public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                    Log.e(TAG, "🌐 Lỗi mạng/API khi xóa sản phẩm khỏi giỏ hàng cho productId: " + productId + ", variantId: " + variantId + ": " + t.getMessage(), t);
                    cartRemovalLatch.countDown(); // Giảm bộ đếm ngay cả khi lỗi mạng
                }
            });
        }

        // Luồng chờ tất cả các yêu cầu xóa hoàn tất
        new Thread(() -> {
            try {
                cartRemovalLatch.await(); // Chờ cho đến khi tất cả các countDown() được gọi
                runOnUiThread(() -> {
                    Log.d(TAG, "✅ Tất cả các sản phẩm đã được xử lý xong. Chuyển hướng đến lịch sử đơn hàng.");
                    // Có thể cần tải lại giỏ hàng một lần nữa để đảm bảo UI trống
                    // fetchCartData();
                    navigateToOrderHistory();
                });
            } catch (InterruptedException e) {
                Log.e(TAG, "⚠️ Luồng bị gián đoạn khi chờ xóa giỏ hàng.", e);
                // Nếu luồng bị gián đoạn, vẫn cố gắng chuyển hướng
                runOnUiThread(this::navigateToOrderHistory);
            }
        }).start();
    }


    /**
     * Tìm một biến thể (Variant) trong danh sách dựa trên ID của nó.
     *
     * @param variantId ID của biến thể cần tìm.
     * @param variants  Danh sách các biến thể để tìm kiếm.
     * @return Đối tượng Variant nếu tìm thấy, ngược lại trả về null.
     */
    private Variant findVariantById(String variantId, List<Variant> variants) {
        if (variants == null || variantId == null || variantId.isEmpty()) {
            return null;
        }
        for (Variant variant : variants) {
            if (variant != null && variant.getId() != null && variant.getId().equals(variantId)) {
                return variant;
            }
        }
        return null;
    }

    /**
     * Chuyển hướng người dùng đến màn hình lịch sử đơn hàng (DashBoar_Activity).
     * Đặt cờ Intent để xóa các activity trên stack và tạo một task mới.
     */
    private void navigateToOrderHistory() {
        Intent intent = new Intent(this, DashBoar_Activity.class);
        intent.putExtra("navigate_to_history", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Hiển thị thông tin người dùng (tên, số điện thoại, địa chỉ giao hàng) lên giao diện.
     * Lấy dữ liệu từ SharedPreferences.
     */
    private void bindUserToUI() {
        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String fullname = preferences.getString("fullname", "");
        String phone = preferences.getString("phone", "");
        String address = preferences.getString("address", "");

        tvFullName.setText(!fullname.isEmpty() ? fullname : "Chưa có tên");
        tvPhoneNumber.setText(!phone.isEmpty() ? phone : "Chưa có số điện thoại");
        tvShippingAddress.setText(!address.isEmpty() ? address : "Chưa có địa chỉ");
        Log.d(TAG, "Thông tin người dùng: Tên=" + fullname + ", SĐT=" + phone + ", Địa chỉ=" + address);
    }

    // Hàm kiểm tra địa chỉ
    private boolean isValidAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return false; // Địa chỉ rỗng
        }

        // Địa chỉ phải ít nhất 10 ký tự
        if (address.length() < 10) {
            return false;
        }

        // Bắt buộc phải có cả chữ và số
        boolean hasLetter = address.matches(".*[a-zA-ZÀ-ỹ].*");
        boolean hasNumber = address.matches(".*\\d.*");

        return hasLetter && hasNumber;
    }

}