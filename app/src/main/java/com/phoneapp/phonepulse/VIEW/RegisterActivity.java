package com.phoneapp.phonepulse.VIEW;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.OtpResponse;
import com.phoneapp.phonepulse.Response.RegisterResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.User;
import com.phoneapp.phonepulse.request.OtpRequest;
import com.phoneapp.phonepulse.request.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edFullName, edEmail, edPhone, edPassword, edConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister, btnLogin;
    private TextInputLayout layoutEmail;
    private ApiService apiService;

    private boolean isOtpVerified = false;
    private String verifiedEmail = "";
    private String currentOtpCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        apiService = RetrofitClient.getApiService(null);
        initViews();

        // 📌 Gửi OTP khi bấm icon trong ô email
        layoutEmail.setEndIconOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edEmail.setError("Email không hợp lệ");
                return;
            }
            sendOtpToEmail(email);
        });

        // 📌 Reset trạng thái OTP khi đổi email
        edEmail.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().equals(verifiedEmail)) {
                    isOtpVerified = false;
                }
            }
        });

        // 📌 Nút Đăng ký
        btnRegister.setOnClickListener(view -> {
            if (!validateInputs()) return;

            String name = edFullName.getText().toString().trim();
            String email = edEmail.getText().toString().trim();
            String phone = edPhone.getText().toString().trim();
            String password = edPassword.getText().toString().trim();

            if (!isOtpVerified || !email.equals(verifiedEmail)) {
                Toast.makeText(this, "Vui lòng xác minh email bằng OTP!", Toast.LENGTH_SHORT).show();
                return;
            }

            // ✅ gọi API đăng ký
            registerUser(name, email, phone, password, currentOtpCode);
        });

        // 📌 Nút quay lại Login
        btnLogin.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void initViews() {
        edFullName = findViewById(R.id.edFullName);
        edEmail = findViewById(R.id.edEmail);
        edPhone = findViewById(R.id.edPhone);
        edPassword = findViewById(R.id.edPassword);
        edConfirmPassword = findViewById(R.id.edConfirmPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        btnLogin = findViewById(R.id.btnLogin);
        layoutEmail = findViewById(R.id.layoutEmail);
    }

    private boolean validateInputs() {
        String name = edFullName.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String password = edPassword.getText().toString().trim();
        String confirmPassword = edConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            edFullName.setError("Vui lòng nhập họ và tên");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("Email không hợp lệ");
            return false;
        }
        if (!TextUtils.isEmpty(phone) && !Patterns.PHONE.matcher(phone).matches()) {
            edPhone.setError("Số điện thoại không hợp lệ");
            return false;
        }
        if (password.length() < 6) {
            edPassword.setError("Mật khẩu phải có ít nhất 6 ký tự");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            edConfirmPassword.setError("Mật khẩu không khớp");
            return false;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Vui lòng chấp nhận Điều khoản & Điều kiện", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // ==============================
    // 📌 OTP
    // ==============================
    private void sendOtpToEmail(String email) {
        apiService.sendOtp(new OtpRequest(email)).enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    OtpResponse otpResponse = response.body();
                    if (otpResponse.isSuccess()) {
                        currentOtpCode = otpResponse.getOtp(); // có thể null nếu server không trả
                        showOtpDialog(email);
                    } else {
                        Toast.makeText(RegisterActivity.this,
                                otpResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "Không thể gửi OTP. Vui lòng thử lại.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showOtpDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.diglog_verification_otp_email, null);
        builder.setView(view);
        builder.setCancelable(false);

        TextView txtTitle = view.findViewById(R.id.tvTitle);
        TextInputEditText edtOtp = view.findViewById(R.id.edtOtp);
        Button btnVerifyOtp = view.findViewById(R.id.btnVerifyOtp);
        TextView txtResendOtp = view.findViewById(R.id.txtResendOtp);
        TextView btnCancel = view.findViewById(R.id.btnCancel);

        txtTitle.setText("Mã OTP đã gửi tới email:\n" + email);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = edtOtp.getText().toString().trim();
            if (TextUtils.isEmpty(otp)) {
                edtOtp.setError("Vui lòng nhập OTP");
                return;
            }
            if (otp.equals(currentOtpCode != null ? currentOtpCode : "")) {
                isOtpVerified = true;
                verifiedEmail = email;
                Toast.makeText(this, "✅ Xác minh OTP thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                edtOtp.setError("❌ Mã OTP không đúng");
            }
        });

        txtResendOtp.setOnClickListener(v -> {
            txtResendOtp.setEnabled(false);
            sendOtpToEmail(email);
            Toast.makeText(this, "Đã gửi lại mã OTP", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(() -> txtResendOtp.setEnabled(true), 10000);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    // ==============================
    // 📌 Register
    // ==============================
    private void registerUser(String name, String email, String phone, String password, String otp) {
        RegisterRequest request = new RegisterRequest(name, email, phone, password, otp);

        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RegisterResponse registerResponse = response.body();

                    if (registerResponse.isSuccess()) {
                        User user = registerResponse.getData();

                        Toast.makeText(RegisterActivity.this,
                                "✅ Đăng ký thành công: " + user.getEmail(),
                                Toast.LENGTH_SHORT).show();

                        // 👉 Gửi email + password sang LoginActivity
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("REGISTERED_EMAIL", email);   // dùng key REGISTERED_EMAIL
                        intent.putExtra("REGISTERED_PASSWORD", password);
                        startActivity(intent);
                        finish();

                    } else {
                        // ❌ Đăng ký thất bại – hiển thị message từ server
                        Toast.makeText(RegisterActivity.this,
                                "❌ " + registerResponse.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this,
                            "❌ Lỗi đăng ký: " + response.message(),
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                Toast.makeText(RegisterActivity.this,
                        "⚠️ Lỗi kết nối: " + t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

}
