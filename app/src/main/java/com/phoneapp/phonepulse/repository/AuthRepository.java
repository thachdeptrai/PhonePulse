package com.phoneapp.phonepulse.repository;

import com.phoneapp.phonepulse.data.network.User;
import com.phoneapp.phonepulse.request.ApiResponse;
import com.phoneapp.phonepulse.request.LoginRequest;
import com.phoneapp.phonepulse.request.LoginResponse;
import com.phoneapp.phonepulse.request.RegisterRequest;
import com.phoneapp.phonepulse.retrofit.ApiService;

import retrofit2.Call;

public class AuthRepository {
    private final ApiService apiService;

    public AuthRepository(ApiService apiService) {
        this.apiService = apiService;
    }

    public Call<LoginResponse> login(LoginRequest request) {
        return apiService.login(request);
    }

    public Call<ApiResponse<User>> register(RegisterRequest request) {
        return apiService.register(request);
    }
}

