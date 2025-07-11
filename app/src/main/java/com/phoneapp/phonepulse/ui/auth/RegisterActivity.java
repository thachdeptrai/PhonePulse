package com.phoneapp.phonepulse.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.network.API_UserResponse;
import com.phoneapp.phonepulse.data.network.RegisterRequest;
import com.phoneapp.phonepulse.data.network.RetrofitClient;
import com.phoneapp.phonepulse.data.network.UserAPI_Service;
import com.phoneapp.phonepulse.models.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText rgFullName, rgEmail, rgPass, rgConPass;
    private ImageView showHidePass, showHideConPass;
    private Button btnRegister, btnToLogin;

    private boolean isPassVisible = false;
    private boolean isConPassVisible = false;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        // Ánh xạ view
        rgFullName = findViewById(R.id.rgFullName);
        rgEmail = findViewById(R.id.rgEmail);
        rgPass = findViewById(R.id.rgPass);
        rgConPass = findViewById(R.id.rgConPass);
        btnRegister = findViewById(R.id.btnRg);
        btnToLogin = findViewById(R.id.btnRgLogin);
        showHidePass = findViewById(R.id.showHidePass);
        showHideConPass = findViewById(R.id.showHideConPass);

        // Mắt xem Password
        showHidePass.setOnClickListener(v -> {
            if (isPassVisible) {
                rgPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showHidePass.setImageResource(R.drawable.mat); // Mắt đóng
            } else {
                rgPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showHidePass.setImageResource(R.drawable.mat); // Mắt mở
            }
            isPassVisible = !isPassVisible;
            rgPass.setSelection(rgPass.getText().length());
        });

        // Mắt xem Confirm Password
        showHideConPass.setOnClickListener(v -> {
            if (isConPassVisible) {
                rgConPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showHideConPass.setImageResource(R.drawable.mat);
            } else {
                rgConPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showHideConPass.setImageResource(R.drawable.mat);
            }
            isConPassVisible = !isConPassVisible;
            rgConPass.setSelection(rgConPass.getText().length());
        });

        // Chuyển sang màn hình Login
        btnToLogin.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        // Xử lý đăng ký
        btnRegister.setOnClickListener(v -> {
            String fullName = rgFullName.getText().toString().trim();
            String email = rgEmail.getText().toString().trim();
            String password = rgPass.getText().toString().trim();
            String confirmPassword = rgConPass.getText().toString().trim();

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            RegisterRequest request = new RegisterRequest(fullName, email, password);
            Log.d(TAG, "Dữ liệu JSON gửi đi: " + new Gson().toJson(request));

            UserAPI_Service apiService = RetrofitClient.getUserService();
            Call<API_UserResponse<User>> call = apiService.register(request);

            call.enqueue(new Callback<API_UserResponse<User>>() {
                @Override
                public void onResponse(Call<API_UserResponse<User>> call, Response<API_UserResponse<User>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        API_UserResponse<User> res = response.body();
                        if (res.isSuccess()) {
                            Log.d(TAG, "Đăng ký thành công: " + res.getData().getEmail());
                            Toast.makeText(RegisterActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();

                            // Chuyển sang LoginActivity và truyền email, password
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.putExtra("email", email);
                            intent.putExtra("password", password);
                            startActivity(intent);
                            finish();
                        } else {
                            Log.e(TAG, "Đăng ký thất bại (lỗi logic): " + res.getMessage());
                            Toast.makeText(RegisterActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "Đăng ký thất bại (lỗi phản hồi): " + response.code());
                        Toast.makeText(RegisterActivity.this, "Đăng ký thất bại! Mã lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<API_UserResponse<User>> call, Throwable t) {
                    Log.e(TAG, "Đăng ký lỗi mạng: " + t.getMessage());
                    Toast.makeText(RegisterActivity.this, "Không thể kết nối tới server", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}
