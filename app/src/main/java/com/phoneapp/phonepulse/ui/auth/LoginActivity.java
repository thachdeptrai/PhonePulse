package com.phoneapp.phonepulse.ui.auth;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.*;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
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

import java.util.Arrays;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private ApiService apiService;
    private EditText edEmail, edPassword;
    private Button btnLogin, btnRegister;
    private ImageButton btnGoogle, btnFacebook;
    private static final int RC_SIGN_IN = 1000;
    public static final String PREF_NAME = "PhonePulsePrefs";

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager callbackManager;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiService = RetrofitClient.getApiService(null); // Chưa có token
        edEmail = findViewById(R.id.edEmail);
        edPassword = findViewById(R.id.edPassWord);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        btnFacebook.setOnClickListener(v -> {
            // Đăng xuất trước để không tự động đăng nhập lại
            LoginManager.getInstance().logOut();
            //  Xóa session trình duyệt WebView/Chrome (rất quan trọng!)
            CookieSyncManager.createInstance(getApplicationContext());
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookies(null);
            cookieManager.flush();
            // Gọi đăng nhập Facebook và yêu cầu quyền truy cập email + hồ sơ công khai
            LoginManager.getInstance().logInWithReadPermissions(
                    LoginActivity.this,
                    Arrays.asList("email", "public_profile")
            );
        });

            callbackManager = CallbackManager.Factory.create();

            LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    String accessToken = loginResult.getAccessToken().getToken();
                    sendTokenToBackendFacebook(accessToken); // Gửi token lên server
                }

                @Override
                public void onCancel() {
                    Toast.makeText(LoginActivity.this, "Đăng nhập Facebook bị hủy", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(LoginActivity.this, "Lỗi Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });



        // Khởi tạo Google Sign-In Client
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1017838440430-9nc0771j67mroe29lhgdhi6b87aaame3.apps.googleusercontent.com")
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                loginUser();
            }
        });

        btnGoogle.setOnClickListener(v -> {
            mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            });
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
                    LoginResponse data = response.body().getData();
                    if (data != null && data.getToken() != null) {
                        String token = data.getToken();
                        User user = data.getUser();

                        PrefUtils.saveToken(LoginActivity.this, token);
                        PrefUtils.saveUser(LoginActivity.this, user);

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

    // Phải nằm ở cấp lớp, không được nằm trong onCreate
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (callbackManager != null) {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();
                sendTokenToBackend(idToken);
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

    private void sendTokenToBackendFacebook(String accessToken) {
        SocialLoginRequest request = new SocialLoginRequest("facebook", accessToken);

        apiService.socialLogin(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                handleSocialLoginResponse(response);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Lỗi Facebook: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void handleSocialLoginResponse(Response<LoginResponse> response) {
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
            Toast.makeText(LoginActivity.this, "Đăng nhập thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.TOKEN_KEY, token).apply();
    }
}
