package com.phoneapp.phonepulse.ui.auth;

import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.request.RegisterRequest;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private EditText edFullName, edEmail, edPhone, edPassword, edConfirmPassword;
    private CheckBox cbTerms;
    private Button btnRegister;
    private ProgressBar progressBar; // Note: This ProgressBar is not actually used in the layout/shown to the user. Consider adding it to your layout if you intend to show progress.
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize ApiService via RetrofitClient
        apiService = RetrofitClient.getApiService(null); // null because registration doesn't require a token
        Log.d("DEBUG", "apiService = " + apiService);
        Log.d("DEBUG", "BASE_URL = " + com.phoneapp.phonepulse.utils.Constants.BASE_URL);

        // Map views
        initViews();

        // Register button click event
        btnRegister.setOnClickListener(view -> {
            if (validateInputs()) {
                registerUser();
            }
        });
    }

    private void initViews() {
        edFullName = findViewById(R.id.edFullName);
        edEmail = findViewById(R.id.edEmail);
        edPhone = findViewById(R.id.edPhone);
        edPassword = findViewById(R.id.edPassWord); // Corrected ID assuming it matches activity_login, ensure it's correct for activity_register
        edConfirmPassword = findViewById(R.id.edConfirmPassword);
        cbTerms = findViewById(R.id.cbTerms);
        btnRegister = findViewById(R.id.btnRegister);
        // The ProgressBar instance is created here but not added to a layout, so it won't be visible.
        // If you want to show a ProgressBar, you should define it in your XML layout (activity_register.xml)
        // and then find it using findViewById(). For now, it's just an instance.
        progressBar = new ProgressBar(this);
    }

    private boolean validateInputs() {
        String name = edFullName.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String password = edPassword.getText().toString();
        String confirmPassword = edConfirmPassword.getText().toString();

        if (TextUtils.isEmpty(name)) {
            edFullName.setError("Full Name is required");
            return false;
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edEmail.setError("Invalid Email");
            return false;
        }

        // Phone number is optional, but if entered, it must be valid
        if (!TextUtils.isEmpty(phone) && !Patterns.PHONE.matcher(phone).matches()) {
            edPhone.setError("Invalid Phone Number");
            return false;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            edPassword.setError("Password must be at least 6 characters");
            return false;
        }
        if (!password.matches(".*[A-Z].*")) {
            edPassword.setError("Password must contain at least 1 uppercase letter");
            return false;
        }

        if (!password.matches(".*[0-9].*")) {
            edPassword.setError("Password must contain at least 1 digit");
            return false;
        }

        if (!password.matches(".*[!@#$%^&*()_+=|<>?{}\\[\\]~-].*")) {
            edPassword.setError("Password must contain at least 1 special character");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            edConfirmPassword.setError("Passwords do not match");
            return false;
        }

        if (!cbTerms.isChecked()) {
            Toast.makeText(this, "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void registerUser() {
        String name = edFullName.getText().toString().trim();
        String email = edEmail.getText().toString().trim();
        String phone = edPhone.getText().toString().trim();
        String password = edPassword.getText().toString();

        RegisterRequest request = new RegisterRequest(name, email, phone, password);

        // Log request info
        Log.d("RegisterActivity", "Starting registration...");
        Log.d("RegisterActivity", "Request data: " + request.toString()); // Make sure RegisterRequest has a toString() method

        btnRegister.setEnabled(false);
        Toast.makeText(this, "Registering...", Toast.LENGTH_SHORT).show();

        apiService.register(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(RegisterActivity.this, "Register Successful!", Toast.LENGTH_LONG).show();

                        // --- MODIFICATION START ---
                        // Create an Intent to go back to LoginActivity
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        // Put email and password as extras
                        intent.putExtra("REGISTERED_EMAIL", email);
                        intent.putExtra("REGISTERED_PASSWORD", password);
                        // Add flags to clear the activity stack and bring LoginActivity to the top
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Finish RegisterActivity
                        // --- MODIFICATION END ---

                    } else {
                        Toast.makeText(RegisterActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed. Try again.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                Log.e("RegisterActivity", "Network Error", t);
                String errorMsg = "Network Error: ";
                if (t instanceof java.net.SocketException) {
                    errorMsg += "Connection failed. Check server URL and network.";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMsg += "Cannot resolve server address.";
                } else if (t instanceof java.net.ConnectException) {
                    errorMsg += "Connection refused. Check if server is running.";
                } else {
                    errorMsg += t.getMessage();
                }
                Toast.makeText(RegisterActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }
}