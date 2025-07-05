package com.phoneapp.phonepulse.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.ProductGridAdapter;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvSearchResults;
    private ProductGridAdapter adapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> searchResults = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Ánh xạ view
        etSearch = findViewById(R.id.et_search);
        rvSearchResults = findViewById(R.id.rv_search_results);
        ImageView ivBack = findViewById(R.id.iv_back);

        // Xử lý nút quay lại
        ivBack.setOnClickListener(v -> finish());

        // Init API và RecyclerView
        apiService = RetrofitClient.getApiService(null);

        adapter = new ProductGridAdapter(this, searchResults);
        rvSearchResults.setLayoutManager(new GridLayoutManager(this, 2));
        rvSearchResults.setAdapter(adapter);

        // Tải toàn bộ sản phẩm để dùng tìm local khi cần
        loadAllProducts();

        // Lắng nghe sự thay đổi ô tìm kiếm
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = s.toString().trim();
                if (!keyword.isEmpty()) {
                    searchProduct(keyword); // gọi API
                } else {
                    adapter.updateProducts(allProducts); // nếu trống -> show tất cả
                }
            }
        });
    }

    private void loadAllProducts() {
        apiService.getAllProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allProducts.clear();
                    allProducts.addAll(response.body());
                    adapter.updateProducts(allProducts); // hiển thị toàn bộ ban đầu
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(SearchActivity.this, "Không thể tải sản phẩm ban đầu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchProduct(String keyword) {
        apiService.searchProducts(keyword).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResults.clear();
                    searchResults.addAll(response.body());
                    adapter.updateProducts(searchResults);
                } else {
                    Toast.makeText(SearchActivity.this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Log.e("SearchActivity", "API Error: " + t.getMessage());
                Toast.makeText(SearchActivity.this, "Mạng yếu, đang tìm nội bộ...", Toast.LENGTH_SHORT).show();
                filterLocal(keyword); // fallback tìm local
            }
        });
    }

    private void filterLocal(String keyword) {
        List<Product> filtered = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(keyword.toLowerCase())) {
                filtered.add(product);
            }
        }
        adapter.updateProducts(filtered);
    }
}
