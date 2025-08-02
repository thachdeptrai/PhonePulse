package com.phoneapp.phonepulse.FRAGMENT;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.phoneapp.phonepulse.Adapter.ItemProduct_ADAPTER;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.request.ProductGirdItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_CATEGORY_NAME = "category_name";
    private static final String TAG = "ProductListFragment";

    private String categoryId;
    private String categoryName;

    private TextView tvCategoryName;
    private TextView tvEmptyProducts;
    private RecyclerView rvProducts;
    private ProgressBar pbLoadingProducts;
    private ItemProduct_ADAPTER productAdapter;

    // Phương thức factory để tạo một instance mới và truyền cả ID và Tên danh mục
    public static ProductListFragment newInstance(String categoryId, String categoryName) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);
        tvCategoryName = view.findViewById(R.id.tv_category_name);
        rvProducts = view.findViewById(R.id.rv_products);
        pbLoadingProducts = view.findViewById(R.id.pb_loading_products);
        tvEmptyProducts = view.findViewById(R.id.tv_empty_products);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (categoryName != null) {
            tvCategoryName.setText(categoryName);
        }

        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setHasFixedSize(true);

        fetchProductsByCategoryId(categoryId);
    }

    /**
     * Fetches the products for a given category ID using the Retrofit API client.
     * @param id The category ID.
     */
    private void fetchProductsByCategoryId(String id) {
        pbLoadingProducts.setVisibility(View.VISIBLE);
        tvEmptyProducts.setVisibility(View.GONE);
        rvProducts.setVisibility(View.GONE);

        String token = getSavedToken();
        if (token == null) {
            Toast.makeText(getContext(), "Lỗi: Không tìm thấy token xác thực.", Toast.LENGTH_SHORT).show();
            pbLoadingProducts.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        // This is the call that was causing the error.
        // It now correctly references the getProductsByCategory method defined in the new ApiService interface.
        Call<List<ProductGirdItem>> call = apiService.getProductsByCategory(id);

        call.enqueue(new Callback<List<ProductGirdItem>>() {
            @Override
            public void onResponse(Call<List<ProductGirdItem>> call, Response<List<ProductGirdItem>> response) {
                pbLoadingProducts.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductGirdItem> productList = response.body();
                    if (productList.isEmpty()) {
                        tvEmptyProducts.setVisibility(View.VISIBLE);
                    } else {
                        rvProducts.setVisibility(View.VISIBLE);
                        productAdapter = new ItemProduct_ADAPTER(getContext(), productList);
                        // TODO: Xử lý sự kiện click vào sản phẩm
                        // productAdapter.setOnItemClickListener(...);
                        rvProducts.setAdapter(productAdapter);
                    }
                } else {
                    tvEmptyProducts.setVisibility(View.VISIBLE);
                    tvEmptyProducts.setText("Lỗi khi tải sản phẩm: " + response.code());
                    Log.e(TAG, "API call failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<ProductGirdItem>> call, Throwable t) {
                pbLoadingProducts.setVisibility(View.GONE);
                tvEmptyProducts.setVisibility(View.VISIBLE);
                tvEmptyProducts.setText("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "API call failed", t);
            }
        });
    }

    /**
     * Lấy token xác thực đã lưu từ SharedPreferences.
     * @return Token đã lưu, hoặc null nếu không tìm thấy.
     */
    private String getSavedToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        return prefs.getString(Constants.TOKEN_KEY, null);
    }
}
