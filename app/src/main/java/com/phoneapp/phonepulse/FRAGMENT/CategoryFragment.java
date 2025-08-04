package com.phoneapp.phonepulse.FRAGMENT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.phoneapp.phonepulse.Adapter.CategoryAdapter;
import com.phoneapp.phonepulse.Adapter.ItemProduct_ADAPTER;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Cart;
import com.phoneapp.phonepulse.models.Category;
import com.phoneapp.phonepulse.models.Product;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.request.DataConverter;
import com.phoneapp.phonepulse.request.ProductGirdItem;
import com.phoneapp.phonepulse.VIEW.ProductDetailActivity;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment implements ItemProduct_ADAPTER.OnItemClickListener {

    private static final String TAG = "CategoryFragment";

    private RecyclerView rvCategories, rv_products_by_category;
    private ProgressBar progressBar;
    private TextView tvError;
    private CategoryAdapter categoryAdapter;
    private ItemProduct_ADAPTER productAdapter;

    private List<ProductGirdItem> fullProductList = new ArrayList<>();
    private List<ProductGirdItem> filteredProductList = new ArrayList<>();

    private ApiService apiService;
    private String authToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        rvCategories = view.findViewById(R.id.rv_categories);
        rv_products_by_category = view.findViewById(R.id.rv_products_by_category);
        progressBar = view.findViewById(R.id.pb_loading);
        tvError = view.findViewById(R.id.tv_error);

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rv_products_by_category.setLayoutManager(new GridLayoutManager(getContext(), 2));

        productAdapter = new ItemProduct_ADAPTER(getContext(), filteredProductList);
        productAdapter.setOnItemClickListener(this); // Set the listener here
        rv_products_by_category.setAdapter(productAdapter);

        authToken = getSavedToken();
        if (authToken != null) {
            apiService = RetrofitClient.getApiService(authToken);
            loadCategories();
            loadAllProducts();
        } else {
            tvError.setText("Lỗi: Không tìm thấy token xác thực. Vui lòng đăng nhập lại.");
            tvError.setVisibility(View.VISIBLE);
        }

        return view;
    }

    private void loadCategories() {
        if (apiService == null) {
            tvError.setText("Lỗi: Dịch vụ API chưa được khởi tạo.");
            tvError.setVisibility(View.VISIBLE);
            return;
        }

        apiService.getAllCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categories = response.body();
                    categoryAdapter = new CategoryAdapter(getContext(), categories);
                    categoryAdapter.setOnItemClickListener(category -> {
                        filterProductsByCategory(category.getId());
                        Toast.makeText(getContext(), "Đã chọn: " + category.getName(), Toast.LENGTH_SHORT).show();
                    });
                    rvCategories.setAdapter(categoryAdapter);
                } else {
                    tvError.setText("Không thể tải danh mục. Mã lỗi: " + response.code());
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                tvError.setText("Lỗi kết nối: " + t.getMessage());
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void loadAllProducts() {
        progressBar.setVisibility(View.VISIBLE);
        if (apiService == null) {
            progressBar.setVisibility(View.GONE);
            return;
        }

        apiService.getAllProductsRaw().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    fullProductList = DataConverter.convertProductsToGridItems(response.body());
                    filteredProductList.clear();
                    filteredProductList.addAll(fullProductList);
                    productAdapter.notifyDataSetChanged();
                } else {
                    tvError.setText("Không tải được sản phẩm. Mã lỗi: " + response.code());
                    tvError.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvError.setText("Lỗi kết nối: " + t.getMessage());
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    private void filterProductsByCategory(String categoryId) {
        filteredProductList.clear();
        for (ProductGirdItem item : fullProductList) {
            if (item.getCategory_id() != null && item.getCategory_id().equals(categoryId)) {
                filteredProductList.add(item);
            }
        }
        productAdapter.notifyDataSetChanged();
    }

    private String getSavedToken() {
        if (getContext() == null) return null;
        SharedPreferences prefs = requireContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(Constants.TOKEN_KEY, null);
    }

    @Override
    public void onAddToCartClick(ProductGirdItem item) {
        if (isAdded() && getContext() != null) {
            if (item.get_id() == null || item.getVariant_id() == null) {
                Toast.makeText(requireContext(), "Không thể thêm sản phẩm này vào giỏ hàng (thiếu ID).", Toast.LENGTH_SHORT).show();
                return;
            }

            callAddToCartApi(item.get_id(), item.getVariant_id(), 1);
        }
    }

    private void callAddToCartApi(String productId, String variantId, int quantity) {
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng.", Toast.LENGTH_LONG).show();
            return;
        }

        CartRequest.AddToCart request = new CartRequest.AddToCart(productId, variantId, quantity);

        apiService.addToCart(request).enqueue(new Callback<ApiResponse<Cart>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<com.phoneapp.phonepulse.models.Cart>> call, @NonNull Response<ApiResponse<com.phoneapp.phonepulse.models.Cart>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Thêm vào giỏ hàng thành công!", Toast.LENGTH_SHORT).show();
                } else {
                    String errorMsg = "Lỗi khi thêm vào giỏ hàng.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMsg = response.body().getMessage();
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<com.phoneapp.phonepulse.models.Cart>> call, @NonNull Throwable t) {
                Toast.makeText(requireContext(), "Lỗi mạng khi thêm vào giỏ hàng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onItemClick(ProductGirdItem item) {
        if (isAdded() && getContext() != null) {
            Toast.makeText(requireContext(), "Đang mở chi tiết: " + item.getProduct_name(), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireContext(), ProductDetailActivity.class);
            if (item.get_id() != null) {
                intent.putExtra(Constants.PRODUCT_ID, item.get_id());
            }
            if (item.getVariant_id() != null) {
                intent.putExtra(Constants.VARIANT_ID, item.getVariant_id());
            }
            startActivity(intent);
        }
    }
}