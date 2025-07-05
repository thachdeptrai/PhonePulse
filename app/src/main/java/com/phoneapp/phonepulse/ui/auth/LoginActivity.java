package com.phoneapp.phonepulse.ui.auth;

import android.annotation.SuppressLint;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.data.api.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.models.User;
import com.phoneapp.phonepulse.repository.LoginResponse;
import com.phoneapp.phonepulse.request.LoginRequest;
import com.phoneapp.phonepulse.request.SocialLoginRequest;
import com.phoneapp.phonepulse.retrofit.RetrofitClient;
import com.phoneapp.phonepulse.ui.home.HomeActivity;
import com.phoneapp.phonepulse.utils.Constants;
import com.phoneapp.phonepulse.utils.PrefUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText edEmail, edPassword;
    private Button btnLogin, btnRegister;
    private ImageButton btnGoogle, btnFacebook;
    private ApiResponse loginResponse;
    private String token ;
    public static final String PREF_NAME = "PhonePulsePrefs";
    private static final int RC_SIGN_IN = 1000;

    private GoogleSignInClient mGoogleSignInClient;
    private GoogleSignInOptions gso;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo Google Sign-In Client sau khi có context
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1017838440430-9nc0771j67mroe29lhgdhi6b87aaame3.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        apiService = RetrofitClient.getApiService(null); // Chưa có token
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassWord);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);

        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                loginUser();
            }
        });

        btnGoogle.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
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

                    LoginResponse data = response.body().getData();
                    if (data != null && data.getToken() != null) {
                        String token = data.getToken();
                        User user = data.getUser();

                        PrefUtils.saveToken(LoginActivity.this, token);
                        PrefUtils.saveUser(LoginActivity.this, user); // hoặc user tùy logic

                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Token null", Toast.LENGTH_SHORT).show();
                    }

                }
            }
            @Override
            public void onFailure(Call<ApiResponse<LoginResponse>> call, Throwable t) {
                btnLogin.setEnabled(true);
                Toast.makeText(LoginActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();  // ✅ access_token từ Google

                sendTokenToBackend(idToken); // Gửi về API backend
            } catch (ApiException e) {
                e.printStackTrace();
                Toast.makeText(this, "Google Sign In failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendTokenToBackend(String idToken) {
        SocialLoginRequest request = new SocialLoginRequest("google", idToken);

        apiService.socialLogin(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginData = response.body();

                    if (loginData.getToken() != null && loginData.getUser() != null) {
                        String jwtToken = loginData.getToken();
                        User user = loginData.getUser();

                        PrefUtils.saveToken(LoginActivity.this, jwtToken);
                        PrefUtils.saveUser(LoginActivity.this, user);

                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Thiếu dữ liệu người dùng hoặc token", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.TOKEN_KEY, token).apply();
    }
}