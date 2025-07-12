package com.phoneapp.phonepulse.repository;

import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.request.CartItem;
import com.phoneapp.phonepulse.request.CartRequest;

import retrofit2.Call;
import java.util.List;

public class CartRepository {

    private final ApiService apiService;

    public CartRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    /**
     * Lấy giỏ hàng
     * @param token Bearer token
     * @return List<CartItem>
     */
    public Call<List<CartItem>> getCart(String token) {
        return apiService.getCart("Bearer " + token);
    }

    /**
     * Thêm sản phẩm vào giỏ
     * @param token Bearer token
     * @param request thông tin sản phẩm cần thêm
     * @return ApiResponse
     */
    public Call<ApiResponse> addToCart(String token, CartRequest request) {
        return apiService.addToCart("Bearer " + token, request);
    }

    /**
     * Cập nhật số lượng sản phẩm
     * @param token Bearer token
     * @param request thông tin sản phẩm cần cập nhật
     * @return ApiResponse
     */
    public Call<ApiResponse> updateCart(String token, CartRequest request) {
        return apiService.updateCart("Bearer " + token, request);
    }

    /**
     * Xóa sản phẩm khỏi giỏ
     * @param token Bearer token
     * @param request thông tin sản phẩm cần xóa
     * @return ApiResponse
     */
    public Call<ApiResponse> removeFromCart(String token, CartRequest request) {
        return apiService.removeFromCart("Bearer " + token, request);
    }
}
