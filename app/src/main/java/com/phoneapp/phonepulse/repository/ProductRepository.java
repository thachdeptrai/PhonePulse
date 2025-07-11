package com.phoneapp.phonepulse.repository;

import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.Product;

import java.util.List;

import retrofit2.Call;

public class ProductRepository {

    private final ApiService apiService;

    public ProductRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    // Gọi API lấy tất cả sản phẩm
    public Call<List<Product>> getAllProducts() {
        return apiService.getAllProducts();
    }

    // Gọi API lấy chi tiết sản phẩm theo ID
    public Call<Product> getProductById(String productId) {
        return apiService.getProductById(productId);
    }
}
