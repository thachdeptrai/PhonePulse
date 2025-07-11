package com.phoneapp.phonepulse.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.phoneapp.phonepulse.Adapter.BannerAdapter;
import com.phoneapp.phonepulse.Adapter.FlashSaleAdapter;
import com.phoneapp.phonepulse.Adapter.ProductGridAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.ui.cart.CartActivity;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";

    // UI Components
    private EditText etSearchProduct;
    private ViewPager2 vpBanner;
    private RecyclerView rvFlashSale;
    private RecyclerView rvProductList;
    private ImageView ivCartIcon;

    // Adapters
    private BannerAdapter bannerAdapter;
    private FlashSaleAdapter flashSaleAdapter;
    private ProductGridAdapter productGridAdapter;

    // Data
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> flashSaleProducts = new ArrayList<>();
    private List<String> bannerImages = new ArrayList<>();

    // API Service
    private ApiService apiService;

    // Authentication token
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Load the authentication token as early as possible
        authToken = Constants.getToken(this);
        if (authToken != null) {
            Log.d(TAG, "Auth Token loaded: " + authToken.substring(0, Math.min(authToken.length(), 10)) + "...");
        } else {
            Log.w(TAG, "No auth token found. User might not be logged in or token expired.");
            // Optionally, redirect to login if token is crucial for HomeActivity
        }

        initViews();
        setupRecyclerViews();
        setupViewPager();
        setupSearchFunction();
        initApiService();
        loadData();
        setupCartNavigation();
    }

    private void initViews() {
        etSearchProduct = findViewById(R.id.et_search_product);
        vpBanner = findViewById(R.id.vp_banner);
        rvFlashSale = findViewById(R.id.rv_flash_sale);
        rvProductList = findViewById(R.id.rv_product_list);
        // Đây là dòng đã được sửa: Gán ImageView tìm được vào biến ivCartIcon
        ivCartIcon = findViewById(R.id.iv_cart_icon);
    }

    private void setupRecyclerViews() {
        // Setup Flash Sale RecyclerView (Horizontal)
        flashSaleAdapter = new FlashSaleAdapter(this, flashSaleProducts);
        LinearLayoutManager flashSaleLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFlashSale.setLayoutManager(flashSaleLayoutManager);
        rvFlashSale.setAdapter(flashSaleAdapter);

        // Setup Main Product Grid RecyclerView
        productGridAdapter = new ProductGridAdapter(this, allProducts);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        rvProductList.setLayoutManager(gridLayoutManager);
        rvProductList.setAdapter(productGridAdapter);
    }

    private void setupViewPager() {
        // Sample banner images - replace with actual URLs
        bannerImages = Arrays.asList(
                "https://via.placeholder.com/600x300/FF5733/FFFFFF?text=Banner+1",
                "https://via.placeholder.com/600x300/33FF57/FFFFFF?text=Banner+2",
                "https://via.placeholder.com/600x300/3357FF/FFFFFF?text=Banner+3"
        );

        bannerAdapter = new BannerAdapter(this, bannerImages);
        vpBanner.setAdapter(bannerAdapter);
    }

    private void setupSearchFunction() {
        etSearchProduct.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initApiService() {
        // Pass the loaded token to RetrofitClient when initializing ApiService
        apiService = RetrofitClient.getApiService(authToken);
    }

    private void loadData() {
        loadAllProducts();
    }

    private void loadAllProducts() {
        Call<List<Product>> call = apiService.getAllProducts();
        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allProducts.clear();
                    allProducts.addAll(response.body());

                    // Separate flash sale products (assume discount > 20%)
                    separateFlashSaleProducts();

                    // Update adapters on the UI thread
                    runOnUiThread(() -> {
                        productGridAdapter.notifyDataSetChanged();
                        flashSaleAdapter.notifyDataSetChanged();
                    });

                    Log.d(TAG, "Loaded " + allProducts.size() + " products");
                } else {
                    Log.e(TAG, "Failed to load products: " + response.code() + " - " + response.message());
                    showError("Không thể tải danh sách sản phẩm.");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Network error during product loading: " + t.getMessage(), t);
                showError("Lỗi kết nối mạng. Vui lòng thử lại.");
            }
        });
    }

    private void separateFlashSaleProducts() {
        flashSaleProducts.clear();
        for (Product product : allProducts) {
            if (product.getDiscount() > 20) { // Example Flash sale condition: discount > 20%
                flashSaleProducts.add(product);
            }
        }
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allProducts);
        } else {
            for (Product product : allProducts) {
                // Ensure product.getName() is not null before converting to lower case
                if (product.getName() != null && product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }
        productGridAdapter.updateProducts(filteredList);
    }

    private void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    private void setupCartNavigation() {
        ivCartIcon.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, CartActivity.class);
            startActivity(intent);
        });
    }
}