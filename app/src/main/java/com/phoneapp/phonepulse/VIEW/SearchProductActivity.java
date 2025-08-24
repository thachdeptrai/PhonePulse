package com.phoneapp.phonepulse.VIEW;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.ProgressBar; // THÊM MỚI (Tùy chọn, nếu bạn có ProgressBar)
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson; // THÊM MỚI - Để log request body
import com.phoneapp.phonepulse.Adapter.SearchProductAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse; // THÊM MỚI
import com.phoneapp.phonepulse.data.api.ApiService; // THÊM MỚI
import com.phoneapp.phonepulse.data.api.RetrofitClient; // THÊM MỚI
import com.phoneapp.phonepulse.models.Cart; // THÊM MỚI
import com.phoneapp.phonepulse.models.Variant; // THÊM MỚI - Dùng cho checkStock
import com.phoneapp.phonepulse.request.CartRequest; // THÊM MỚI
import com.phoneapp.phonepulse.request.ProductGirdItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call; // THÊM MỚI
import retrofit2.Callback; // THÊM MỚI
import retrofit2.Response; // THÊM MỚI

public class SearchProductActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity"; // THÊM MỚI - Tag cho log

    private TextInputEditText et_search;
    private TextInputLayout til_search;
    private MaterialButton btn_sort_asc, btn_sort_desc;
    private TextView tv_no_results, tv_suggestions_title;
    private RecyclerView rv_products, rv_suggestions;
    private SearchProductAdapter productAdapter, suggestionAdapter;
    private List<ProductGirdItem> productList, filteredProductList, suggestionList;

    private ApiService apiService; // THÊM MỚI
    private ProgressBar progressBarSearch; // THÊM MỚI (Tùy chọn - ID của ProgressBar trong layout của bạn)
    private String authToken; // THÊM MỚI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_product);

        // 1. Ánh xạ tất cả các View trước
        til_search = findViewById(R.id.til_search);
        et_search = findViewById(R.id.et_search);
        btn_sort_asc = findViewById(R.id.btn_sort_asc);
        btn_sort_desc = findViewById(R.id.btn_sort_desc);
        tv_no_results = findViewById(R.id.tv_no_results);
        tv_suggestions_title = findViewById(R.id.tv_suggestions_title);
        rv_products = findViewById(R.id.rv_products);
        rv_suggestions = findViewById(R.id.rv_suggestions);
        ImageButton backButton = findViewById(R.id.back_button);
        // progressBarSearch = findViewById(R.id.progressBar_search_activity); // THÊM MỚI - Thay R.id.progressBar_search_activity bằng ID thực tế

        // THÊM MỚI - Khởi tạo ApiService và authToken
        authToken = Constants.getToken(this);
        if (authToken != null && !authToken.isEmpty()) {
            apiService = RetrofitClient.getApiService(authToken);
        } else {
            Log.w(TAG, "AuthToken is null or empty. API calls might fail.");
            // Có thể hiển thị thông báo hoặc chuyển hướng đăng nhập nếu cần
        }


        // 2. Khởi tạo danh sách dữ liệu chính
        productList = (ArrayList<ProductGirdItem>) getIntent().getSerializableExtra("product_list");
        if (productList == null) {
            productList = new ArrayList<>();
        }

        // 3. Khởi tạo các danh sách phụ
        filteredProductList = new ArrayList<>();
        suggestionList = new ArrayList<>();

        // 4. Thiết lập RecyclerView và Adapters
        // Adapter cho sản phẩm tìm kiếm
        rv_products.setLayoutManager(new LinearLayoutManager(this));
        productAdapter = new SearchProductAdapter(this, filteredProductList);
        productAdapter.setOnItemClickListener(new SearchProductAdapter.OnItemClickListener() {
            @Override
            public void onAddToCartClick(ProductGirdItem item) {
                // SỬA ĐỔI - Gọi hàm xử lý chung
                checkStockAndAddToCart(item.get_id(), item.getVariant_id(), 1, item.getProduct_name());
            }

            @Override
            public void onItemClick(ProductGirdItem item) {
                Log.d(TAG, "onItemClick: ProductId=" + item.get_id() + ", VariantId=" + item.getVariant_id());
                Intent intent = new Intent(SearchProductActivity.this, ProductDetailActivity.class);
                intent.putExtra(Constants.PRODUCT_ID, item.get_id());
                intent.putExtra(Constants.VARIANT_ID, item.getVariant_id());
                startActivity(intent);
            }
        });
        rv_products.setAdapter(productAdapter);

        // Adapter cho gợi ý
        rv_suggestions.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new SearchProductAdapter(this, suggestionList);
        suggestionAdapter.setOnItemClickListener(new SearchProductAdapter.OnItemClickListener() {
            @Override
            public void onAddToCartClick(ProductGirdItem item) {
                // SỬA ĐỔI - Gọi hàm xử lý chung
                checkStockAndAddToCart(item.get_id(), item.getVariant_id(), 1, item.getProduct_name());
            }

            @Override
            public void onItemClick(ProductGirdItem item) {
                et_search.setText(item.getProduct_name());
                filterProducts(item.getProduct_name());
                hideSuggestions();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null && et_search != null) {
                    imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                }
            }
        });
        rv_suggestions.setAdapter(suggestionAdapter);

        // 5. Nạp dữ liệu
        loadProducts();

        // 6. Thiết lập listeners còn lại
        // ... (các listener khác của bạn giữ nguyên) ...
        backButton.setOnClickListener(v -> onBackPressed());
        til_search.setStartIconOnClickListener(v -> finish());

        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = et_search.getText().toString().trim();
                filterProducts(query);
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null && v != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        btn_sort_asc.setOnClickListener(v -> {
            Collections.sort(filteredProductList, (p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
            productAdapter.setData(filteredProductList);
        });

        btn_sort_desc.setOnClickListener(v -> {
            Collections.sort(filteredProductList, (p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
            productAdapter.setData(filteredProductList);
        });

        et_search.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(et_search, InputMethodManager.SHOW_IMPLICIT);
        }
    }


    // THÊM MỚI - Hàm kiểm tra tồn kho và thêm vào giỏ hàng (tương tự ProductDetailActivity)
    private void checkStockAndAddToCart(final String productId, final String variantId, final int addedQuantity, final String productNameForToast) {
        if (apiService == null) {
            Toast.makeText(this, "Lỗi dịch vụ, không thể thêm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ApiService is null in checkStockAndAddToCart");
            return;
        }
        if (productId == null || productId.isEmpty() || variantId == null || variantId.isEmpty()) {
            Toast.makeText(this, "Thông tin sản phẩm không hợp lệ.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ProductId or VariantId is null/empty in checkStockAndAddToCart.");
            return;
        }

        if (progressBarSearch != null) progressBarSearch.setVisibility(View.VISIBLE);

        // Bước 1: Lấy giỏ hàng hiện tại (để biết số lượng đã có của variant này)
        apiService.getCart().enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                int existingQuantity = 0;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Cart cart = response.body().getData();
                    if (cart != null && cart.getItems() != null) {
                        for (com.phoneapp.phonepulse.request.CartItem itemInCart : cart.getItems()) { // Sử dụng đúng CartItem từ package request
                            if (itemInCart.getVariant() != null && variantId.equals(itemInCart.getVariant().getId())) {
                                existingQuantity = itemInCart.getQuantity();
                                break;
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "Không thể lấy giỏ hàng hiện tại, hoặc giỏ hàng trống. Code: " + response.code());
                    // Tiếp tục với existingQuantity = 0
                }

                // Bước 2: Lấy thông tin tồn kho của variant
                final int finalExistingQuantity = existingQuantity;
                apiService.getVariantForProductById(productId, variantId).enqueue(new Callback<Variant>() {
                    @Override
                    public void onResponse(Call<Variant> call, Response<Variant> response) {
                        if (progressBarSearch != null) progressBarSearch.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            Variant variant = response.body();
                            int stockQuantity = variant.getQuantity();
                            int totalRequested = finalExistingQuantity + addedQuantity;

                            Log.d(TAG, "Kiểm tra tồn kho cho VariantID: " + variantId +
                                    ". Tồn kho: " + stockQuantity +
                                    ". Đã có trong giỏ: " + finalExistingQuantity +
                                    ". Muốn thêm: " + addedQuantity +
                                    ". Tổng yêu cầu: " + totalRequested);

                            if (totalRequested > stockQuantity) {
                                Toast.makeText(SearchProductActivity.this,
                                        "Không đủ hàng! Tồn kho: " + stockQuantity + ". Trong giỏ: " + finalExistingQuantity,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                callAddToCartApi(productId, variantId, addedQuantity, productNameForToast);
                            }
                        } else {
                            Toast.makeText(SearchProductActivity.this, "Không thể lấy thông tin tồn kho.", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Lỗi lấy thông tin variant. Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Variant> call, Throwable t) {
                        if (progressBarSearch != null) progressBarSearch.setVisibility(View.GONE);
                        Toast.makeText(SearchProductActivity.this, "Lỗi mạng khi kiểm tra tồn kho: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        Log.e(TAG, "Lỗi mạng lấy thông tin variant: ", t);
                    }
                });
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                // Nếu không lấy được giỏ hàng, vẫn thử kiểm tra tồn kho và thêm (với giả định existingQuantity = 0)
                Log.w(TAG, "Lỗi mạng khi lấy giỏ hàng. Tiếp tục kiểm tra tồn kho và thêm.", t);
                apiService.getVariantForProductById(productId, variantId).enqueue(new Callback<Variant>() {
                    @Override
                    public void onResponse(Call<Variant> call, Response<Variant> response) {
                        if (progressBarSearch != null) progressBarSearch.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null) {
                            Variant variant = response.body();
                            int stockQuantity = variant.getQuantity();
                            if (addedQuantity > stockQuantity) {
                                Toast.makeText(SearchProductActivity.this,
                                        "Không đủ hàng! Tồn kho: " + stockQuantity,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                callAddToCartApi(productId, variantId, addedQuantity, productNameForToast);
                            }
                        } else {
                            Toast.makeText(SearchProductActivity.this, "Không thể lấy thông tin tồn kho.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<Variant> call, Throwable t_inner) {
                        if (progressBarSearch != null) progressBarSearch.setVisibility(View.GONE);
                        Toast.makeText(SearchProductActivity.this, "Lỗi mạng khi kiểm tra tồn kho: " + t_inner.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    // THÊM MỚI - Hàm gọi API thêm vào giỏ hàng
    private void callAddToCartApi(String productId, String variantId, int quantity, final String productNameForToast) {
        if (apiService == null) {
            Toast.makeText(this, "Lỗi dịch vụ, không thể thêm vào giỏ hàng.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "ApiService is null in callAddToCartApi");
            return;
        }

        CartRequest.AddToCart request = new CartRequest.AddToCart(productId, variantId, quantity);
        // Toast.makeText(SearchProductActivity.this, "Đang thêm " + productNameForToast + "...", Toast.LENGTH_SHORT).show();
        if (progressBarSearch != null) progressBarSearch.setVisibility(View.VISIBLE);
        Log.d(TAG, "Gửi yêu cầu AddToCart: " + new Gson().toJson(request));

        apiService.addToCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(Call<ApiResponse<Cart>> call, Response<ApiResponse<Cart>> response) {
                if (progressBarSearch != null) progressBarSearch.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SearchProductActivity.this, "Đã thêm " + productNameForToast + " vào giỏ hàng!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Thêm vào giỏ hàng thành công. Cart: " + new Gson().toJson(response.body().getData()));

                    // (Tùy chọn) Cập nhật CartManager nếu bạn có
                    // Cart updatedCart = response.body().getData();
                    // if (updatedCart != null && updatedCart.getItems() != null) {
                    //    CartManager.getInstance().setCartItems(updatedCart.getItems());
                    // }

                    // Chuyển sang Cart_Activity sau khi thêm thành công
                    Intent intent = new Intent(SearchProductActivity.this, Cart_Activity.class);
                    startActivity(intent);

                } else {
                    String errorMsg = "Lỗi khi thêm vào giỏ hàng.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    } else {
                        try {
                            if (response.errorBody() != null) {
                                errorMsg = errorMsg + " " + response.errorBody().string();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Lỗi đọc errorBody khi thêm vào giỏ hàng", e);
                        }
                    }
                    Toast.makeText(SearchProductActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Thêm vào giỏ hàng thất bại: Code " + response.code() + " - " + errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Cart>> call, Throwable t) {
                if (progressBarSearch != null) progressBarSearch.setVisibility(View.GONE);
                Toast.makeText(SearchProductActivity.this, "Lỗi mạng khi thêm vào giỏ hàng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e(TAG, "Lỗi mạng khi thêm vào giỏ hàng: ", t);
            }
        });
    }


    // ... (Các phương thức loadProducts, filterProducts, showSuggestions, hideSuggestions giữ nguyên) ...
    private void loadProducts() {
        // Kiểm tra an toàn, mặc dù với logic onCreate đúng, chúng không nên null
        if (filteredProductList == null || suggestionList == null || productList == null ||
                productAdapter == null || suggestionAdapter == null) {
            Log.e(TAG, "Lỗi khởi tạo trong loadProducts. Một thành phần là null.");
            if (productList == null) productList = new ArrayList<>();
            if (filteredProductList == null) filteredProductList = new ArrayList<>();
            if (suggestionList == null) suggestionList = new ArrayList<>();
            if (productAdapter == null || suggestionAdapter == null) {
                Log.e(TAG, "Adapter chưa được khởi tạo trước khi gọi loadProducts.");
                return;
            }
        }

        filteredProductList.clear();
        suggestionList.clear();

        if (!productList.isEmpty()) {
            filteredProductList.addAll(productList);
        }

        productAdapter.setData(filteredProductList);
        suggestionAdapter.setData(suggestionList);

        if (filteredProductList.isEmpty() && !et_search.getText().toString().trim().isEmpty()) {
            showSuggestions();
        } else {
            hideSuggestions();
        }
    }



    private void filterProducts(String query) {
        filteredProductList.clear();
        if (query.isEmpty()) {
            filteredProductList.addAll(productList);
            hideSuggestions();
        } else {
            String lowerQuery = query.toLowerCase();
            for (ProductGirdItem product : productList) {
                // Giả sử ProductGirdItem có getProduct_name() và getCategory_id() (hoặc tương đương)
                boolean nameMatches = product.getProduct_name() != null && product.getProduct_name().toLowerCase().contains(lowerQuery);
                boolean categoryMatches = product.getCategory_id() != null && product.getCategory_id().toLowerCase().contains(lowerQuery); // Sửa: Giả sử category là String ID

                if (nameMatches || categoryMatches) {
                    filteredProductList.add(product);
                }
            }

            if (filteredProductList.isEmpty()) {
                showSuggestions();
            } else {
                hideSuggestions();
            }
        }
        productAdapter.setData(filteredProductList);
    }
    private void showSuggestions() {
        tv_no_results.setVisibility(View.VISIBLE);
        tv_suggestions_title.setVisibility(View.VISIBLE);
        rv_suggestions.setVisibility(View.VISIBLE);

        if (productList == null || productList.isEmpty()) {
            suggestionList.clear(); // Xóa gợi ý cũ nếu productList rỗng
            suggestionAdapter.setData(suggestionList);
            return;
        }

        List<ProductGirdItem> top = new ArrayList<>(productList);
        // Sắp xếp theo sold_count nếu có, nếu không thì không cần sắp xếp hoặc theo logic khác
        // Collections.sort(top, (p1, p2) -> Integer.compare(p2.getSold_count(), p1.getSold_count()));


        suggestionList.clear();
        // Hiển thị tối đa 5 gợi ý hoặc ít hơn nếu danh sách sản phẩm gốc nhỏ hơn
        suggestionList.addAll(top.subList(0, Math.min(5, top.size())));
        suggestionAdapter.setData(suggestionList);
    }

    private void hideSuggestions() {
        tv_no_results.setVisibility(View.GONE);
        tv_suggestions_title.setVisibility(View.GONE);
        rv_suggestions.setVisibility(View.GONE);
    }
}
