package com.phoneapp.phonepulse.VIEW;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.request.OtpRequest;
import com.phoneapp.phonepulse.request.RegisterRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private TextInputEditText edFullName, edEmail, edPhone, edPassword, edConfirmPassword, edtOtp;
    private CheckBox cbTerms;
    private Button btnRegister, btnLogin;
    private TextInputLayout layoutEmail;
    private ApiService apiService;

    // Lưu OTP do user nhập
    private String enteredOtp = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        apiService = RetrofitClient.getApiService(null);
        Log.d(TAG, "ApiService initialized. Base URL: " + com.phoneapp.phonepulse.utils.Constants.BASE_URL);

        initViews();

        btnRegister.setOnClickListener(view -> {
            if (validateInputs()) {
                registerUser();
            }
        });

        btnLogin.setOnClickListener(view -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        layoutEmail.setEndIconOnClickListener(v -> {
            String email = edEmail.getText().toString().trim();
            if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edEmail.setError("Please enter a valid email to send OTP");
                return;
            }
            sendOtpToEmail(email);
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
        String password = edPassword.getText().toString();
        String confirmPassword = edConfirmPassword.getText().toString();
        String otp = enteredOtp;

        if (TextUtils.isEmpty(name)) {
            edFullName.setError("Full Name is required");
            return false;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("Invalid Email");
            return false;
        }
        if (!TextUtils.isEmpty(phone) && !Patterns.PHONE.matcher(phone).matches()) {
            edPhone.setError("Invalid Phone Number");
            return false;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            edPassword.setError("Password must be at least 6 characters long");
            return false;
        }
        Pattern pattern = Pattern.compile("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+=|<>?{}\\[\\]~-]).{6,}$");
        Matcher matcher = pattern.matcher(password);
        if (!matcher.matches()) {
            edPassword.setError("Password must contain at least 1 uppercase letter, 1 digit, and 1 special character");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            edConfirmPassword.setError("Passwords do not match");
            return false;
        }
        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept the Terms & Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(otp) || otp.length() != 6) {
            Toast.makeText(this, "Please enter a valid 6-digit OTP.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void sendOtpToEmail(String email) {
        Toast.makeText(this, "Sending OTP...", Toast.LENGTH_SHORT).show();
        apiService.sendOtp(new OtpRequest(email)).enqueue(new Callback<com.phoneapp.phonepulse.Response.OtpResponse>() {
            @Override
            public void onResponse(Call<com.phoneapp.phonepulse.Response.OtpResponse> call, Response<com.phoneapp.phonepulse.Response.OtpResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.phoneapp.phonepulse.Response.OtpResponse otpResponse = response.body();
                    if (otpResponse.isSuccess()) {
                        Toast.makeText(RegisterActivity.this, "OTP sent! Please check your email.", Toast.LENGTH_LONG).show();
                        showOtpDialog(email);
                    } else {
                        Toast.makeText(RegisterActivity.this, otpResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e(TAG, "OTP send failed. Response code: " + response.code());
                    Toast.makeText(RegisterActivity.this, "Failed to send OTP. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<com.phoneapp.phonepulse.Response.OtpResponse> call, Throwable t) {
                Log.e(TAG, "Network error during OTP send.", t);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showOtpDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.diglog_verification_otp_email, null);
        builder.setView(view);
        builder.setCancelable(false);

        TextView txtTitle = view.findViewById(R.id.tvTitle);
        edtOtp = view.findViewById(R.id.edtOtp);
        Button btnConfirmOtp = view.findViewById(R.id.btnVerifyOtp);
        TextView txtResendOtp = view.findViewById(R.id.txtResendOtp);
        TextView btnCancel = view.findViewById(R.id.btnCancel);

        txtTitle.setText("A verification code has been sent to:\n" + email);
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();

        btnConfirmOtp.setOnClickListener(v -> {
            String otp = edtOtp.getText().toString().trim();
            if (TextUtils.isEmpty(otp) || otp.length() != 6) {
                edtOtp.setError("Please enter a valid 6-digit OTP.");
                return;
            }
            enteredOtp = otp;
            Toast.makeText(RegisterActivity.this, "OTP accepted.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        txtResendOtp.setOnClickListener(v -> sendOtpToEmail(email));
    }

    private void registerUser() {
        String name = edFullName.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String password = edPassword.getText().toString();

        RegisterRequest request = new RegisterRequest(name, email, phone, password, enteredOtp);

        Log.d(TAG, "Starting registration for: " + email + " with OTP: " + enteredOtp);
        btnRegister.setEnabled(false);
        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show();

        apiService.register(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        // ✅ Lấy dữ liệu user từ server
                        String userData = apiResponse.getData().toString();
                        // Nếu bạn đã có model UserResponse thì parse sang object để lấy từng trường

                        // ✅ Lưu vào SharedPreferences (ví dụ)
                        getSharedPreferences("user_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("user_data", userData)
                                .apply();

                        // ✅ Hiển thị Toast thành công với dữ liệu
                        Toast.makeText(RegisterActivity.this,
                                "Registration Successful!\n" + userData,
                                Toast.LENGTH_LONG).show();

                        // Điều hướng về LoginActivity
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        intent.putExtra("REGISTERED_EMAIL", email);
                        intent.putExtra("REGISTERED_PASSWORD", password);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed. Please try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }
}
