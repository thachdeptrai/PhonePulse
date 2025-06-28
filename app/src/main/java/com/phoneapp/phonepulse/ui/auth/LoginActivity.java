package com.phoneapp.phonepulse.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.network.API_UserResponse;
import com.phoneapp.phonepulse.data.network.LoginRequest;
import com.phoneapp.phonepulse.data.network.RetrofitClient;
import com.phoneapp.phonepulse.data.network.UserAPI_Service;
import com.phoneapp.phonepulse.models.User;
import com.phoneapp.phonepulse.ui.home.HomeActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText edEmail, edPass;
    private Button btnLogin, btnRegister;
    private CheckBox cbRemember;
    private ImageView imgShowHidePass;

    private boolean isPassVisible = false;
    private static final String TAG = "LoginActivity";
    private static final String PREFS_NAME = "MyPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        edEmail = findViewById(R.id.edEmail);
        edPass = findViewById(R.id.edPassWord);
        cbRemember = findViewById(R.id.cbRemember);
        imgShowHidePass = findViewById(R.id.imgShowHidePass);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        String savedEmail = preferences.getString("email", "");
        String savedPassword = preferences.getString("password", "");
        boolean isRemember = preferences.getBoolean("remember", false);

        if (isRemember) {
            edEmail.setText(savedEmail);
            edPass.setText(savedPassword);
            cbRemember.setChecked(true);
        }

        // Nhận dữ liệu từ RegisterActivity nếu có
        Intent intent = getIntent();
        String emailFromRegister = intent.getStringExtra("email");
        String passFromRegister = intent.getStringExtra("password");
        if (emailFromRegister != null) edEmail.setText(emailFromRegister);
        if (passFromRegister != null) edPass.setText(passFromRegister);

        // Xử lý hiện/ẩn mật khẩu
        imgShowHidePass.setOnClickListener(v -> {
            if (isPassVisible) {
                edPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                imgShowHidePass.setImageResource(R.drawable.mat); // icon mắt đóng
            } else {
                edPass.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                imgShowHidePass.setImageResource(R.drawable.mat); // icon mắt mở
            }
            isPassVisible = !isPassVisible;
            edPass.setSelection(edPass.getText().length());
        });

        btnLogin.setOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            String password = edPass.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            LoginRequest request = new LoginRequest(email, password);
            UserAPI_Service apiService = RetrofitClient.getUserService();
            Call<API_UserResponse<User>> call = apiService.login(request);

            call.enqueue(new Callback<API_UserResponse<User>>() {
                @Override
                public void onResponse(Call<API_UserResponse<User>> call, Response<API_UserResponse<User>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        API_UserResponse<User> res = response.body();
                        if (res.isSuccess()) {
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

                            SharedPreferences.Editor editor = preferences.edit();
                            if (cbRemember.isChecked()) {
                                editor.putString("email", email);
                                editor.putString("password", password);
                                editor.putBoolean("remember", true);
                            } else {
                                editor.clear();
                            }
                            editor.apply();

                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, res.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại! Mã lỗi: " + response.code(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<API_UserResponse<User>> call, Throwable t) {
                    Toast.makeText(LoginActivity.this, "Không thể kết nối tới server", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Lỗi mạng: " + t.getMessage());
                }
            });
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
