package com.phoneapp.phonepulse.ui.auth;

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
import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.repository.LoginResponse;
import com.phoneapp.phonepulse.request.LoginRequest;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.ui.home.HomeActivity;
import com.phoneapp.phonepulse.utils.Constants;

import dagger.hilt.android.AndroidEntryPoint; // Keep if using Dagger Hilt
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint // Keep if using Dagger Hilt
public class LoginActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText edEmail, edPassword;
    private Button btnLogin, btnRegister;
    private CheckBox cbRememberMe; // Added CheckBox
    private TextView tvForgotPassword; // Added TextView for Forgot Password

    // SharedPreferences keys
    public static final String PREF_NAME = "PhonePulsePrefs";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_EMAIL = "saved_email";
    private static final String KEY_PASSWORD = "saved_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize ApiService via RetrofitClient
        apiService = RetrofitClient.getApiService(null);
        Log.d("DEBUG", "apiService = " + apiService);

        // Map views
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassWord);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        cbRememberMe = findViewById(R.id.cbRememberMe); // Map CheckBox
        tvForgotPassword = findViewById(R.id.tvForgotPassword); // Map TextView

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
            // Optionally, show a toast message
            Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_LONG).show();
            // Clear the extras to prevent re-filling on subsequent onCreate calls (e.g., rotation)
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
            // Handle Forgot Password click - e.g., navigate to ForgotPasswordActivity
            Toast.makeText(LoginActivity.this, "Forgot Password clicked!", Toast.LENGTH_SHORT).show();
            // Example: startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
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

        // Save credentials if "Remember Me" is checked
        if (cbRememberMe.isChecked()) {
            saveCredentials(email, password, true);
        } else {
            // Clear saved credentials if "Remember Me" is unchecked
            saveCredentials("", "", false);
        }

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
                            // Re-initialize apiService with the token for subsequent authenticated requests
                            apiService = RetrofitClient.getApiService(token);
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Token not received", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.TOKEN_KEY, token).apply();
    }

    /**
     * Saves or clears login credentials based on the rememberMe flag.
     * @param email The email to save.
     * @param password The password to save.
     * @param rememberMe If true, saves the credentials; if false, clears them.
     */
    private void saveCredentials(String email, String password, boolean rememberMe) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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
     * Loads saved login credentials and pre-fills the EditText fields.
     */
    private void loadSavedCredentials() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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