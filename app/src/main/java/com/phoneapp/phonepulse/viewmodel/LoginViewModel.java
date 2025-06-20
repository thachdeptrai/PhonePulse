package com.phoneapp.phonepulse.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.phoneapp.phonepulse.repository.AuthRepository;
import com.phoneapp.phonepulse.request.LoginRequest;
import com.phoneapp.phonepulse.request.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public LoginViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void login(LoginRequest request) {
        authRepository.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful()) {
                    loginResponse.setValue(response.body());
                } else {
                    error.setValue("Đăng nhập thất bại");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                error.setValue(t.getMessage());
            }
        });
    }
}
