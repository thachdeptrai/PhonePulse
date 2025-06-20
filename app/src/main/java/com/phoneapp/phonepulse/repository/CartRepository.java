package com.phoneapp.phonepulse.repository;

import com.phoneapp.phonepulse.request.ApiResponse;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.CartRequest;
import com.phoneapp.phonepulse.retrofit.ApiService;

import java.util.List;

import retrofit2.Call;

public class CartRepository {
    private final ApiService apiService;

    public CartRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<List<CartItem>> getCart(String token) {
        return apiService.getCart(token);
    }

    public Call<ApiResponse> addToCart(String token, CartRequest request) {
        return apiService.addToCart(token, request);
    }

    public Call<ApiResponse> updateCart(String token, CartRequest request) {
        return apiService.updateCart(token, request);
    }

    public Call<ApiResponse> removeFromCart(String token, CartRequest request) {
        return apiService.removeFromCart(token, request);
    }
}
