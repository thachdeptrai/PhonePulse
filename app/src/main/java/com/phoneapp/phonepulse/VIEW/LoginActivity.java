package com.phoneapp.phonepulse.VIEW;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.Response.LoginResponse;
import com.phoneapp.phonepulse.request.LoginRequest;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.utils.Constants; // Đảm bảo đã import Constants

import dagger.hilt.android.AndroidEntryPoint; // Keep if using Dagger Hilt
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint // Keep if using Dagger Hilt
public class LoginActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText edEmail, edPassword;
    private Button btnLogin, btnRegister;
    private CheckBox cbRememberMe;
    private TextView tvForgotPassword;

    // Sử dụng Constants.SHARED_PREFS cho SharedPreferences name để nhất quán
    // public static final String PREF_NAME = "PhonePulsePrefs"; // Dòng này có thể xóa
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_EMAIL = "saved_email";
    private static final String KEY_PASSWORD = "saved_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo ApiService lần đầu (chưa có token)
        apiService = RetrofitClient.getApiService(null);
        Log.d("DEBUG", "apiService = " + apiService);

        // Map views
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassWord);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Load saved credentials if "Remember Me" was checked previously
        loadSavedCredentials();

        // Check for extras from RegisterActivity
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("REGISTERED_EMAIL")) {
            String registeredEmail = intent.getStringExtra("REGISTERED_EMAIL");
            String registeredPassword = intent.getStringExtra("REGISTERED_PASSWORD");

            if (registeredEmail != null) {
                edEmail.setText(registeredEmail);
            }
            if (registeredPassword != null) {
                edPassword.setText(registeredPassword);
            }
            Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
            // Xóa extras để tránh hiển thị lại khi xoay màn hình hoặc khởi tạo lại Activity
            intent.removeExtra("REGISTERED_EMAIL");
            intent.removeExtra("REGISTERED_PASSWORD");
        }

        // Set click listeners
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                loginUser();
            }
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });

        tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Chức năng quên mật khẩu đang phát triển!", Toast.LENGTH_SHORT).show();
            // Example: startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });
    }

    private boolean validateInputs() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("Email không hợp lệ.");
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            edPassword.setError("Mật khẩu phải có ít nhất 6 ký tự.");
            return false;
        }

        return true;
    }

    private void loginUser() {
        String email = edEmail.getText().toString().trim();
        String password = edPassword.getText().toString();

        // Lưu hoặc xóa credentials dựa trên trạng thái "Remember Me"
        if (cbRememberMe.isChecked()) {
            saveCredentials(email, password, true);
        } else {
            saveCredentials("", "", false); // Xóa credentials
        }

        LoginRequest request = new LoginRequest(email, password);
        btnLogin.setEnabled(false); // Vô hiệu hóa nút để tránh spam click
        Toast.makeText(this, "Đang đăng nhập...", Toast.LENGTH_SHORT).show();

        apiService.login(request).enqueue(new Callback<ApiResponse<LoginResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<LoginResponse>> call, Response<ApiResponse<LoginResponse>> response) {
                btnLogin.setEnabled(true); // Kích hoạt lại nút

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<LoginResponse> loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        LoginResponse data = loginResponse.getData();
                        String token = data.getToken();

                        if (token != null) {
                            // Sử dụng Constants.saveToken để lưu token một cách nhất quán
                            Constants.saveToken(LoginActivity.this, token);

                            // Khởi tạo lại apiService với token để sử dụng cho các request cần xác thực
                            // Lưu ý: Nếu RetrofitClient đã có logic Interceptor để tự động thêm token
                            // thì bước này có thể không cần thiết, tùy thuộc vào cách bạn thiết kế RetrofitClient.
                            // Tuy nhiên, việc truyền null/token rõ ràng là một cách an toàn.
                            apiService = RetrofitClient.getApiService(token);

                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashBoar_Activity.class));
                            finish(); // Kết thúc LoginActivity để không quay lại
                        } else {
                            Toast.makeText(LoginActivity.this, "Không nhận được token từ máy chủ.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Hiển thị thông báo lỗi từ API
                        Toast.makeText(LoginActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Xử lý lỗi HTTP (ví dụ: 404, 500) hoặc phản hồi không thành công
                    Toast.makeText(LoginActivity.this, "Đăng nhập thất bại. Vui lòng kiểm tra lại thông tin.", Toast.LENGTH_SHORT).show();
                    Log.e("LoginActivity", "Login failed: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                btnLogin.setEnabled(true); // Kích hoạt lại nút
                Toast.makeText(LoginActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LoginActivity", "Network error during login", t);
            }
        });
    }

    // Phương thức này không còn cần thiết vì đã chuyển việc lưu token vào Constants
    // private void saveToken(String token) {
    //     SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    //     prefs.edit().putString(Constants.TOKEN_KEY, token).apply();
    // }

    /**
     * Lưu hoặc xóa thông tin đăng nhập (email, mật khẩu) và trạng thái "Nhớ tôi" vào SharedPreferences.
     * @param email Email của người dùng.
     * @param password Mật khẩu của người dùng.
     * @param rememberMe True nếu muốn lưu thông tin, false nếu muốn xóa.
     */
    private void saveCredentials(String email, String password, boolean rememberMe) {
        // Sử dụng Constants.SHARED_PREFS để lưu credentials nhất quán
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(KEY_REMEMBER_ME, rememberMe);
        if (rememberMe) {
            editor.putString(KEY_EMAIL, email);
            editor.putString(KEY_PASSWORD, password);
        } else {
            editor.remove(KEY_EMAIL);
            editor.remove(KEY_PASSWORD);
        }
        editor.apply();
    }

    /**
     * Tải thông tin đăng nhập đã lưu từ SharedPreferences và điền vào các trường EditText.
     */
    private void loadSavedCredentials() {
        // Sử dụng Constants.SHARED_PREFS để đọc credentials nhất quán
        SharedPreferences prefs = getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        boolean rememberMe = prefs.getBoolean(KEY_REMEMBER_ME, false);
        cbRememberMe.setChecked(rememberMe);

        if (rememberMe) {
            String savedEmail = prefs.getString(KEY_EMAIL, "");
            String savedPassword = prefs.getString(KEY_PASSWORD, "");
            edEmail.setText(savedEmail);
            edPassword.setText(savedPassword);
        }
    }
}