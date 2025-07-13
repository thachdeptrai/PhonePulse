package com.phoneapp.phonepulse.repository;

import com.phoneapp.phonepulse.Response.LoginResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.request.LoginRequest;
import com.phoneapp.phonepulse.request.RegisterRequest;

import retrofit2.Call;

public class AuthRepository {
    private final ApiService apiService;

    public AuthRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<ApiResponse<LoginResponse>> login(LoginRequest request) {
        return apiService.login(request);
    }

    public Call<ApiResponse> register(RegisterRequest request) {
        return apiService.register(request);
    }

}

