package com.phoneapp.phonepulse.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.repository.LoginResponse;
import com.phoneapp.phonepulse.request.LoginRequest;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.ui.home.HomeActivity;
import com.phoneapp.phonepulse.utils.Constants;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText edEmail, edPassword;
    private Button btnLogin, btnRegister;
    private ApiResponse loginResponse;
    private String token ;
    public static final String PREF_NAME = "PhonePulsePrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        apiService = RetrofitClient.getApiService(null); // Khởi tạo ApiService thông qua RetrofitClient
        Log.d("DEBUG", "apiService = " + apiService);
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassWord);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                loginUser();
            }
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private boolean validateInputs() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("Invalid email format");
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            edPassword.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }

    private void loginUser() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString();

        LoginRequest request = new LoginRequest(email, password);
        btnLogin.setEnabled(false);
        Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

        apiService.login(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                btnLogin.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        LoginResponse data = loginResponse.getData();
                        String token = data.getToken();

                        if (token != null) {
                            saveToken(token);
                            apiService = RetrofitClient.getApiService(token);
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Không nhận được token", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.TOKEN_KEY, token).apply();
    }
}
