package com.phoneapp.phonepulse.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        setupRecyclerViews();
        setupViewPager();
        setupSearchFunction();
        initApiService();
        loadData();
    }

    private void initViews() {
        etSearchProduct = findViewById(R.id.et_search_product);
        vpBanner = findViewById(R.id.vp_banner);
        rvFlashSale = findViewById(R.id.rv_flash_sale);
        rvProductList = findViewById(R.id.rv_product_list);
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
        // Sample banner images - thay thế bằng URL thực tế
        bannerImages = Arrays.asList(
                "https://via.placeholder.com/150",
                "https://via.placeholder.com/150",
                "https://via.placeholder.com/150"
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
        apiService = RetrofitClient.getApiService(null);
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

                    // Tách flash sale products (giả sử discount > 20%)
                    separateFlashSaleProducts();

                    // Update adapters
                    runOnUiThread(() -> {
                        productGridAdapter.notifyDataSetChanged();
                        flashSaleAdapter.notifyDataSetChanged();
                    });

                    Log.d(TAG, "Loaded " + allProducts.size() + " products");
                } else {
                    Log.e(TAG, "Failed to load products: " + response.code());
                    showError("Không thể tải danh sách sản phẩm");
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                showError("Lỗi kết nối mạng");
            }
        });
    }

    private void separateFlashSaleProducts() {
        flashSaleProducts.clear();
        for (Product product : allProducts) {
            if (product.getDiscount() > 20) { // Flash sale condition
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
                if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(product);
                }
            }
        }

        productGridAdapter.updateProducts(filteredList);
    }

    private void showError(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }
}