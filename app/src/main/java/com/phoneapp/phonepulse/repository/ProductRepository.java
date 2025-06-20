package com.phoneapp.phonepulse.repository;

import com.phoneapp.phonepulse.data.network.Product;
import com.phoneapp.phonepulse.retrofit.ApiService;

import java.util.List;

import retrofit2.Call;

public class ProductRepository {
    private final ApiService apiService;

    public ProductRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<List<Product>> getAllProducts() {
        return apiService.getAllProducts();
    }

    public Call<Product> getProductById(String productId) {
        return apiService.getProductById(productId);
    }
}

