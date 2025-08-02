package com.phoneapp.phonepulse.FRAGMENT;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.phoneapp.phonepulse.Adapter.CategoryAdapter;
import com.phoneapp.phonepulse.Adapter.ItemProduct_ADAPTER;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.Category;
import com.phoneapp.phonepulse.request.ProductGirdItem;
import com.phoneapp.phonepulse.utils.Constants;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private RecyclerView rvCategories;
    private ProgressBar progressBar;
    private TextView tvError;
    private CategoryAdapter categoryAdapter;

    // TODO: Định nghĩa một mảng các FrameLayout ID để chứa các ProductListFragment
    private int[] fragmentContainerIds = {
            R.id.fragment_container_category_1,
            R.id.fragment_container_category_2,
            R.id.fragment_container_category_3
    };

    public CategoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_category, container, false);
        rvCategories = view.findViewById(R.id.rv_categories);
        progressBar = view.findViewById(R.id.pb_loading);
        tvError = view.findViewById(R.id.tv_error);

        // Cấu hình RecyclerView cho danh mục kéo ngang
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Tải danh sách danh mục
        loadCategories();

        return view;
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.GONE);

        // Lấy token từ SharedPreferences
        String token = getSavedToken();
        if (token == null) {
            tvError.setText("Lỗi: Không tìm thấy token xác thực.");
            tvError.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(token);
        Call<List<Category>> call = apiService.getAllCategories();
        call.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categoryList = response.body();
                    if (!categoryList.isEmpty()) {
                        categoryAdapter = new CategoryAdapter(getContext(), categoryList);
                        categoryAdapter.setOnItemClickListener(category -> {
                            // Xử lý sự kiện click vào danh mục (ví dụ: chuyển sang màn hình danh sách sản phẩm)
                            Toast.makeText(getContext(), "Bạn đã click vào: " + category.getName(), Toast.LENGTH_SHORT).show();
                        });
                        rvCategories.setAdapter(categoryAdapter);

                        // Load 3 danh mục nổi bật vào các FrameLayout
                        int count = Math.min(categoryList.size(), fragmentContainerIds.length);
                        for (int i = 0; i < count; i++) {
                            Category category = categoryList.get(i);
                            loadProductList(fragmentContainerIds[i], category.getId(), category.getName());
                        }
                    } else {
                        tvError.setText("Không có danh mục nào.");
                        tvError.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvError.setText("Lỗi khi tải danh mục.");
                    tvError.setVisibility(View.VISIBLE);
                    Log.e("CategoryFragment", "API call failed with code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                tvError.setText("Lỗi kết nối: " + t.getMessage());
                tvError.setVisibility(View.VISIBLE);
                Log.e("CategoryFragment", "API call failed", t);
            }
        });
    }

    // Phương thức để tải ProductListFragment vào một FrameLayout cụ thể
    private void loadProductList(int containerId, String categoryId, String categoryName) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        ProductListFragment productListFragment = ProductListFragment.newInstance(categoryId, categoryName);

        fragmentTransaction.replace(containerId, productListFragment);
        fragmentTransaction.commit();
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
