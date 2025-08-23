package com.phoneapp.phonepulse.VIEW;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson; // Import Gson để debug request body
import com.phoneapp.phonepulse.Adapter.CartAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.CartRequest; // Đây là các request body bạn gửi đi
import com.phoneapp.phonepulse.request.OrderItem; // Đây là OrderItem cho màn hình thanh toán
import com.phoneapp.phonepulse.utils.Constants;
import com.phoneapp.phonepulse.utils.CartManager; // Import CartManager

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Cart_Activity extends AppCompatActivity implements CartAdapter.OnCartItemActionListener {

    // Khai báo các thành phần UI
    private RecyclerView rvCartItems;
    private CartAdapter cartAdapter;
    private List<CartItem> currentCartItems; // Sử dụng models.CartItem
    private LinearLayout emptyCartView;
    private LinearLayout bottomCheckoutBar;
    private TextView tvTotalPrice;
    private Button btnCheckout;
    private Button btnShopNow;
    private ProgressBar progressBar;

    private LinearLayout llClearCart;
    private CheckBox cbSelectAll;
    private TextView tvClearCart;

    // Khai báo API Service và dữ liệu
    private ApiService apiService;
    private List<OrderItem> orderItemList = new ArrayList<>();
    private double totalPrice = 0;

    private static final String TAG = "Cart_Activity"; // Tag cho Logcat
    private final Gson gson = new Gson(); // Đối tượng Gson để chuyển đổi object thành JSON string cho log

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Ánh xạ các View từ layout
        Toolbar toolbar = findViewById(R.id.toolbar_cart);
        rvCartItems = findViewById(R.id.rv_cart_items);
        emptyCartView = findViewById(R.id.empty_cart_view);
        bottomCheckoutBar = findViewById(R.id.bottom_checkout_bar);
        tvTotalPrice = findViewById(R.id.tv_total_price);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnShopNow = findViewById(R.id.btn_shop_now);
        progressBar = findViewById(R.id.progressBar);

        llClearCart = findViewById(R.id.ll_clear_cart);
        cbSelectAll = findViewById(R.id.cb_select_all);
        tvClearCart = findViewById(R.id.tv_clear_cart);

        // Thiết lập Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            getSupportActionBar().setTitle("Giỏ hàng của bạn");
        }

        // Khởi tạo RecyclerView và Adapter
        currentCartItems = new ArrayList<>();
        cartAdapter = new CartAdapter(currentCartItems);
        cartAdapter.setOnCartItemActionListener(this); // Đặt listener cho các sự kiện trong adapter
        rvCartItems.setLayoutManager(new LinearLayoutManager(this));
        rvCartItems.setAdapter(cartAdapter);

        // Xử lý sự kiện khi nhấn nút "Mua sắm ngay" (khi giỏ hàng trống)
        btnShopNow.setOnClickListener(v -> {
            Log.d(TAG, "Nút 'Mua sắm ngay' được nhấn.");
            finish(); // Đóng Activity hiện tại
        });

        // Xử lý sự kiện khi nhấn nút "Thanh toán"
        btnCheckout.setOnClickListener(v -> {
            Log.d(TAG, "Nút 'Thanh toán' được nhấn.");
            if (orderItemList.isEmpty()) { // Kiểm tra danh sách OrderItem đã chuẩn bị
                Toast.makeText(Cart_Activity.this, "Giỏ hàng của bạn đang trống, không thể thanh toán.", Toast.LENGTH_SHORT).show();
                return;
            }
            // Chuyển sang màn hình đặt hàng (Oder_Activity)
            Intent intent = new Intent(Cart_Activity.this, Oder_Activity.class);
            intent.putParcelableArrayListExtra("order_items", new ArrayList<>(orderItemList)); // Gửi danh sách sản phẩm
            intent.putExtra("total_price", totalPrice); // Gửi tổng tiền
            Log.d(TAG, "Chuyển sang Order_Activity với " + orderItemList.size() + " sản phẩm và tổng tiền " + totalPrice);
            startActivityForResult(intent, 1001); // Bắt đầu activity và chờ kết quả
        });

        // Xử lý sự kiện khi nhấn vào LinearLayout "Xóa giỏ hàng"
        llClearCart.setOnClickListener(v -> {
            Log.d(TAG, "LinearLayout 'Xóa giỏ hàng' được nhấn.");
            if (currentCartItems.isEmpty()) {
                Toast.makeText(this, "Giỏ hàng của bạn đã trống.", Toast.LENGTH_SHORT).show();
                cbSelectAll.setChecked(false); // Đảm bảo checkbox bỏ chọn
                return;
            }
            // Hiển thị hộp thoại xác nhận xóa tất cả
            showClearCartConfirmationDialog();
        });

        // Tải dữ liệu giỏ hàng khi Activity được tạo lần đầu
        Log.d(TAG, "onCreate: Bắt đầu tải dữ liệu giỏ hàng.");
        fetchCartData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Tải lại dữ liệu giỏ hàng mỗi khi Activity trở lại foreground
        Log.d(TAG, "onResume: Tải lại dữ liệu giỏ hàng.");
        fetchCartData();
    }

    /**
     * Tải dữ liệu giỏ hàng từ API.
     */
    private void fetchCartData() {
        String token = Constants.getToken(this); // Lấy token xác thực

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để xem giỏ hàng.", Toast.LENGTH_LONG).show();
            showEmptyCartView(true); // Hiển thị giao diện giỏ hàng trống
            Log.e(TAG, "fetchCartData: Token rỗng hoặc không tồn tại.");
            return;
        }

        apiService = RetrofitClient.getApiService(token); // Khởi tạo ApiService với token
        progressBar.setVisibility(View.VISIBLE); // Hiển thị ProgressBar
        Log.d(TAG, "fetchCartData: Đang gọi API getCart.");

        Call<ApiResponse<Cart>> call = apiService.getCart();
        call.enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                progressBar.setVisibility(View.GONE); // Ẩn ProgressBar
                Log.d(TAG, "getCart API Response Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Cart> apiResponse = response.body();
                    Log.d(TAG, "getCart API Response Success: " + apiResponse.isSuccess() + ", Message: " + apiResponse.getMessage());

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        Cart cart = apiResponse.getData();
                        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
                            currentCartItems.clear();
                            currentCartItems.addAll(cart.getItems());
                            cartAdapter.setCartItemList(currentCartItems); // Cập nhật adapter
                            CartManager.getInstance().setCartItems(currentCartItems); // Cập nhật CartManager

                            // Chuyển đổi CartItem sang OrderItem cho màn hình thanh toán
                            orderItemList.clear();
                            for (CartItem cartItem : currentCartItems) {
                                // Đảm bảo dữ liệu sản phẩm và biến thể không null
                                if (cartItem.getProduct() != null && cartItem.getVariant() != null) {
                                    int itemPrice = (int) cartItem.getVariant().getPrice(); // Lấy giá từ Variant
                                    // Xây dựng tên biến thể từ Color và Size
                                    String variantName = "";
                                    if (cartItem.getVariant().getColor() != null && cartItem.getVariant().getSize() != null) {
                                        variantName = cartItem.getVariant().getColor().getColorName() + ", " + cartItem.getVariant().getSize().getSizeName();
                                    } else if (cartItem.getVariant().getColor() != null) {
                                        variantName = cartItem.getVariant().getColor().getColorName();
                                    } else if (cartItem.getVariant().getSize() != null) {
                                        variantName = cartItem.getVariant().getSize().getSizeName();
                                    }

                                    orderItemList.add(new OrderItem(
                                            null, // ID của OrderItem (thường backend sẽ tự tạo)
                                            cartItem.getProduct().getProductName(),
                                            cartItem.getProductImage(),
                                            itemPrice,
                                            cartItem.getQuantity(),
                                            variantName, // Tên biến thể đã được xây dựng
                                            cartItem.getProduct().getId(),
                                            cartItem.getVariant().getId()
                                    ));
                                    Log.d(TAG, "Đã thêm OrderItem: " + cartItem.getProduct().getProductName() + " - " + variantName + " - Giá: " + itemPrice + " - SL: " + cartItem.getQuantity());
                                } else {
                                    Log.w(TAG, "CartItem thiếu thông tin Product hoặc Variant, bỏ qua: " + gson.toJson(cartItem));
                                }
                            }
                            showEmptyCartView(false); // Hiển thị giỏ hàng có sản phẩm
                            updateTotalPrice(); // Cập nhật tổng tiền
                        } else {
                            Log.d(TAG, "Giỏ hàng trống từ API.");
                            showEmptyCartView(true); // Hiển thị giao diện giỏ hàng trống
                        }
                    } else {
                        String errorMessage = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi không xác định";
                        Log.e(TAG, "Lỗi API khi lấy giỏ hàng: " + response.code() + " - " + errorMessage);
                        Toast.makeText(Cart_Activity.this, "Lỗi khi lấy giỏ hàng: " + errorMessage, Toast.LENGTH_SHORT).show();
                        showEmptyCartView(true);
                    }
                } else {
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi đọc errorBody: " + e.getMessage());
                    }
                    Log.e(TAG, "Phản hồi API không thành công: Code: " + response.code() + ", Message: " + response.message() + ", Error Body: " + errorBody);
                    Toast.makeText(Cart_Activity.this, "Lỗi kết nối server khi lấy giỏ hàng. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    showEmptyCartView(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e(TAG, "Lỗi mạng khi lấy giỏ hàng: ", t);
                Toast.makeText(Cart_Activity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyCartView(true);
            }
        });
    }

    /**
     * Cập nhật tổng số tiền và tổng số lượng sản phẩm trong giỏ hàng.
     */
    private void updateTotalPrice() {
        totalPrice = 0;
        int totalItems = 0;
        for (CartItem item : currentCartItems) {
            // Đảm bảo Variant và Product không null trước khi truy cập
            if (item.getVariant() != null && item.getProduct() != null) {
                totalPrice += item.getVariant().getPrice() * item.getQuantity();
                totalItems += item.getQuantity();
            } else {
                Log.w(TAG, "updateTotalPrice: CartItem thiếu thông tin Variant hoặc Product, không thể tính giá. " + gson.toJson(item));
            }
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        currencyFormat.setMaximumFractionDigits(0); // Không hiển thị số lẻ
        tvTotalPrice.setText(currencyFormat.format(totalPrice)); // Hiển thị tổng tiền
        btnCheckout.setText("Thanh toán (" + totalItems + ")"); // Cập nhật nút thanh toán
        Log.d(TAG, "updateTotalPrice: Tổng tiền: " + totalPrice + ", Tổng số lượng: " + totalItems);
    }

    /**
     * Hiển thị hoặc ẩn giao diện giỏ hàng trống.
     * @param isEmpty True nếu giỏ hàng trống, False nếu có sản phẩm.
     */
    private void showEmptyCartView(boolean isEmpty) {
        if (isEmpty) {
            emptyCartView.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            llClearCart.setVisibility(View.GONE);
            bottomCheckoutBar.setVisibility(View.GONE);
            cbSelectAll.setChecked(false); // Bỏ chọn checkbox
            Log.d(TAG, "Hiển thị giao diện giỏ hàng trống.");
        } else {
            emptyCartView.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            llClearCart.setVisibility(View.VISIBLE);
            bottomCheckoutBar.setVisibility(View.VISIBLE);
            Log.d(TAG, "Hiển thị giao diện giỏ hàng có sản phẩm.");
        }
    }

    /**
     * Hiển thị hộp thoại xác nhận khi người dùng muốn xóa toàn bộ giỏ hàng.
     */
    private void showClearCartConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa giỏ hàng")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng không?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    Log.d(TAG, "Dialog xóa tất cả: Người dùng chọn Xóa.");
                    removeAllItemsFromCart(); // Gọi hàm xóa tất cả sản phẩm
                })
                .setNegativeButton("Hủy", (dialog, which) -> {
                    Log.d(TAG, "Dialog xóa tất cả: Người dùng chọn Hủy.");
                    cbSelectAll.setChecked(false); // Nếu hủy, bỏ chọn checkbox
                })
                .show();
    }

    /**
     * Xóa tất cả các sản phẩm khỏi giỏ hàng bằng cách gọi API cho từng sản phẩm.
     * (Lưu ý: Nếu backend có API xóa toàn bộ giỏ hàng, nên dùng API đó thay vì xóa từng cái).
     */
    private void removeAllItemsFromCart() {
        String token = Constants.getToken(this);
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để xóa giỏ hàng.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "removeAllItemsFromCart: Token rỗng.");
            return;
        }

        if (currentCartItems.isEmpty()) {
            Toast.makeText(this, "Giỏ hàng của bạn đã trống.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            Log.d(TAG, "removeAllItemsFromCart: Giỏ hàng đã trống, không cần thực hiện xóa API.");
            return;
        }

        apiService = RetrofitClient.getApiService(token);
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "removeAllItemsFromCart: Bắt đầu xóa " + currentCartItems.size() + " sản phẩm.");

        // Tạo một bản sao của danh sách để tránh lỗi ConcurrentModificationException
        // khi các item bị xóa khỏi danh sách gốc trong quá trình API response.
        List<CartItem> itemsToRemove = new ArrayList<>(currentCartItems);
        // Bỏ chọn checkbox "Chọn tất cả" ngay sau khi bắt đầu xóa
        cbSelectAll.setChecked(false);

        // Sử dụng bộ đếm để theo dõi số lượng yêu cầu API đã hoàn thành
        final int[] successCount = {0};
        final int[] failureCount = {0};
        final int totalItemsToAttemptRemove = itemsToRemove.size();

        if (totalItemsToAttemptRemove == 0) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(Cart_Activity.this, "Giỏ hàng đã trống.", Toast.LENGTH_SHORT).show();
            fetchCartData();
            return;
        }

        // Vòng lặp để gửi yêu cầu xóa cho từng item một
        for (CartItem item : itemsToRemove) {
            // Kiểm tra tính hợp lệ của dữ liệu trước khi gửi yêu cầu
            if (item.getProduct() != null && item.getVariant() != null &&
                    item.getProduct().getId() != null && !item.getProduct().getId().isEmpty() &&
                    item.getVariant().getId() != null && !item.getVariant().getId().isEmpty()) {

                CartRequest.RemoveCartItem request = new CartRequest.RemoveCartItem(
                        item.getProduct().getId(),
                        item.getVariant().getId()
                );

                Log.d(TAG, "Gửi yêu cầu xóa item: " + gson.toJson(request)); // Log request body để debug
                apiService.removeFromCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            successCount[0]++;
                            Log.d(TAG, "Xóa item thành công: ProductId=" + item.getProduct().getId() + ", VariantId=" + item.getVariant().getId());
                        } else {
                            failureCount[0]++;
                            String errorMsg = response.body() != null ? response.body().getMessage() : "Lỗi không xác định từ server";
                            Log.e(TAG, "Xóa item thất bại: ProductId=" + item.getProduct().getId() + ", VariantId=" + item.getVariant().getId() +
                                    ". Code: " + response.code() + ", Lỗi: " + errorMsg);
                        }
                        // Kiểm tra xem tất cả các yêu cầu đã hoàn tất chưa
                        if ((successCount[0] + failureCount[0]) == totalItemsToAttemptRemove) {
                            Log.d(TAG, "Đã hoàn tất tất cả yêu cầu xóa. Thành công: " + successCount[0] + ", Thất bại: " + failureCount[0]);
                            progressBar.setVisibility(View.GONE);
                            if (failureCount[0] == 0) {
                                Toast.makeText(Cart_Activity.this, "Đã xóa toàn bộ sản phẩm khỏi giỏ hàng.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Cart_Activity.this, "Đã xóa " + successCount[0] + " sản phẩm. " + failureCount[0] + " sản phẩm xóa thất bại.", Toast.LENGTH_LONG).show();
                            }
                            fetchCartData(); // Tải lại giỏ hàng để cập nhật UI sau khi xóa xong
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                        failureCount[0]++;
                        Log.e(TAG, "Lỗi mạng khi xóa item: ProductId=" + item.getProduct().getId() + ", VariantId=" + item.getVariant().getId() + ": " + t.getMessage(), t);
                        // Vẫn kiểm tra hoàn tất để đảm bảo ProgressBar ẩn đi
                        if ((successCount[0] + failureCount[0]) == totalItemsToAttemptRemove) {
                            Log.d(TAG, "Đã hoàn tất tất cả yêu cầu xóa (có lỗi mạng). Thành công: " + successCount[0] + ", Thất bại: " + failureCount[0]);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Cart_Activity.this, "Đã xóa " + successCount[0] + " sản phẩm. " + failureCount[0] + " sản phẩm xóa thất bại do lỗi mạng.", Toast.LENGTH_LONG).show();
                            fetchCartData(); // Tải lại giỏ hàng để cập nhật UI
                        }
                    }
                });
            } else {
                failureCount[0]++; // Tăng số lượng lỗi nếu dữ liệu item không hợp lệ
                Log.e(TAG, "removeAllItemsFromCart: Dữ liệu CartItem không hợp lệ (Product/Variant ID null/empty), bỏ qua xóa: " + gson.toJson(item));
                // Vẫn cần kiểm tra hoàn tất để tránh kẹt ProgressBar
                if ((successCount[0] + failureCount[0]) == totalItemsToAttemptRemove) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Cart_Activity.this, "Đã hoàn tất xóa. Có một số lỗi do dữ liệu không hợp lệ.", Toast.LENGTH_LONG).show();
                    fetchCartData();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Đóng Activity khi nhấn nút quay lại trên Toolbar
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Xử lý sự kiện khi số lượng của một sản phẩm trong giỏ hàng thay đổi.
     * @param item Sản phẩm trong giỏ hàng được thay đổi.
     * @param newQuantity Số lượng mới.
     */
    @Override
    public void onQuantityChange(CartItem item, int newQuantity) {
        Log.d(TAG, "onQuantityChange: Sản phẩm: " + item.getProduct().getProductName() + ", Số lượng mới: " + newQuantity);

        if (newQuantity <= 0) {
            // Nếu số lượng về 0 hoặc nhỏ hơn, hỏi người dùng có muốn xóa sản phẩm không
            showRemoveItemConfirmationDialog(item);
            return;
        }

        // Kiểm tra dữ liệu sản phẩm và biến thể hợp lệ
        if (item.getProduct() == null || item.getVariant() == null ||
                item.getProduct().getId() == null || item.getProduct().getId().isEmpty() ||
                item.getVariant().getId() == null || item.getVariant().getId().isEmpty()) {
            Toast.makeText(this, "Không thể cập nhật: Dữ liệu sản phẩm bị thiếu.", Toast.LENGTH_SHORT).show();
            cartAdapter.notifyDataSetChanged(); // Cập nhật lại UI để số lượng về đúng
            Log.e(TAG, "onQuantityChange: Dữ liệu CartItem thiếu Product ID hoặc Variant ID.");
            return;
        }

        String productId = item.getProduct().getId();
        String variantId = item.getVariant().getId();
        String token = Constants.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để cập nhật giỏ hàng.", Toast.LENGTH_LONG).show();
            cartAdapter.notifyDataSetChanged();
            Log.e(TAG, "onQuantityChange: Token rỗng.");
            return;
        }

        CartRequest.UpdateCartItem request = new CartRequest.UpdateCartItem(productId, variantId, newQuantity);

        apiService = RetrofitClient.getApiService(token);
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Gửi yêu cầu cập nhật số lượng: " + gson.toJson(request)); // Log request body

        apiService.updateCartItem(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Cart_Activity.this, "Cập nhật số lượng thành công!", Toast.LENGTH_SHORT).show();
                    fetchCartData(); // Tải lại dữ liệu để đảm bảo UI khớp với backend
                    Log.d(TAG, "Cập nhật số lượng thành công cho ProductId: " + productId + ", VariantId: " + variantId);
                } else {
                    String errorMsg = "Lỗi khi cập nhật số lượng.";
                    String serverMessage = "";
                    if (response.body() != null && response.body().getMessage() != null) {
                        serverMessage = response.body().getMessage();
                        errorMsg = serverMessage;
                    } else if (response.errorBody() != null) {
                        try {
                            serverMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc errorBody khi cập nhật số lượng: " + e.getMessage());
                        }
                    }
                    Toast.makeText(Cart_Activity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Update quantity API failed: " + response.code() + " - " + serverMessage);
                    fetchCartData(); // Tải lại giỏ hàng để hiển thị trạng thái hiện tại
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Cart_Activity.this, "Lỗi mạng khi cập nhật số lượng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Update quantity network failure: ", t);
                fetchCartData(); // Tải lại giỏ hàng
            }
        });
    }

    /**
     * Hiển thị hộp thoại xác nhận khi người dùng muốn xóa một sản phẩm cụ thể.
     * @param item Sản phẩm cần xác nhận xóa.
     */
    private void showRemoveItemConfirmationDialog(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có muốn xóa sản phẩm " + item.getProduct().getProductName() + " khỏi giỏ hàng không?")
                .setPositiveButton("Xóa", (dialog, which) -> onRemoveItem(item)) // Gọi hàm xóa item
                .setNegativeButton("Hủy", (dialog, which) -> cartAdapter.notifyDataSetChanged()) // Cập nhật lại UI nếu hủy
                .show();
    }

    /**
     * Xóa một sản phẩm cụ thể khỏi giỏ hàng.
     * @param item Sản phẩm CartItem cần xóa.
     */
    @Override
    public void onRemoveItem(CartItem item) {
        Log.d(TAG, "onRemoveItem: Yêu cầu xóa item: " + item.getProduct().getProductName());

        // Kiểm tra dữ liệu sản phẩm và biến thể hợp lệ trước khi gửi request
        if (item.getProduct() == null || item.getVariant() == null ||
                item.getProduct().getId() == null || item.getProduct().getId().isEmpty() ||
                item.getVariant().getId() == null || item.getVariant().getId().isEmpty()) {
            Log.e(TAG, "onRemoveItem: Dữ liệu Product hoặc Variant ID bị thiếu cho item: " + gson.toJson(item));
            Toast.makeText(this, "Không thể xóa: Dữ liệu sản phẩm bị thiếu.", Toast.LENGTH_SHORT).show();
            return;
        }

        String productId = item.getProduct().getId();
        String variantId = item.getVariant().getId();
        String token = Constants.getToken(this);

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để xóa sản phẩm khỏi giỏ hàng.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "onRemoveItem: Token rỗng.");
            return;
        }

        CartRequest.RemoveCartItem request = new CartRequest.RemoveCartItem(productId, variantId);
        apiService = RetrofitClient.getApiService(token);
        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Gửi yêu cầu xóa item: " + gson.toJson(request)); // Log request body để debug

        apiService.removeFromCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(Cart_Activity.this, "Xóa sản phẩm khỏi giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                    // Cập nhật CartManager và tải lại dữ liệu để cập nhật UI
                    CartManager.getInstance().removeItem(variantId); // Xóa khỏi bộ nhớ cục bộ
                    fetchCartData(); // Tải lại dữ liệu từ API để đồng bộ hóa
                    Log.d(TAG, "Xóa item thành công cho ProductId: " + productId + ", VariantId: " + variantId);
                } else {
                    String errorMsg = "Lỗi khi xóa sản phẩm khỏi giỏ hàng.";
                    String serverMessage = "";
                    if (response.body() != null && response.body().getMessage() != null) {
                        serverMessage = response.body().getMessage();
                        errorMsg = serverMessage;
                    } else if (response.errorBody() != null) {
                        try {
                            serverMessage = response.errorBody().string();
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc errorBody khi xóa item: " + e.getMessage());
                        }
                    }
                    Toast.makeText(Cart_Activity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Remove item API failed: " + response.code() + " - " + serverMessage);
                    fetchCartData(); // Tải lại giỏ hàng để hiển thị trạng thái hiện tại
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Cart_Activity.this, "Lỗi mạng khi xóa sản phẩm: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Remove item network failure: ", t);
                fetchCartData(); // Tải lại giỏ hàng
            }
        });
    }
    // Hàm format tiền chuẩn VNPay

    @Override
    public void onItemSelected(CartItem item, boolean isSelected) {
        // Logic cho checkbox chọn item (nếu được triển khai)
        Log.d(TAG, "onItemSelected: Sản phẩm: " + item.getProduct().getProductName() + ", Đã chọn: " + isSelected);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) { // Kiểm tra requestCode cho Order_Activity
            if (resultCode == RESULT_OK && data != null) {
                boolean orderSuccess = data.getBooleanExtra("order_success", false);
                if (orderSuccess) {
                    Log.d(TAG, "Đặt hàng thành công, tải lại giỏ hàng.");
                    fetchCartData(); // Tải lại giỏ hàng sau khi đặt hàng thành công để đảm bảo cập nhật UI
                } else {
                    Log.d(TAG, "Đặt hàng không thành công.");
                }
            } else {
                Log.d(TAG, "onActivityResult: Kết quả từ Order_Activity không thành công hoặc không có dữ liệu.");
            }
        }
    }
}