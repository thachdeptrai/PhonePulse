package com.phoneapp.phonepulse.FRAGMENT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse; // Import ApiResponse
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.User;
import com.phoneapp.phonepulse.utils.Constants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile_FRAGMENT extends Fragment {
    private CircleImageView imgAvatar;
    private TextView tvFullName, tvEmail, tvPhone, tvAddress, tvGender, tvBirthday, tvRole;
    private Button btnEdit;

    private static final String TAG = "ProfileFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        initViews(view);
        loadUserProfile();

        return view;
    }

    private void initViews(View view) {
        imgAvatar = view.findViewById(R.id.img_avatar);
        tvFullName = view.findViewById(R.id.tv_fullname);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvAddress = view.findViewById(R.id.tv_address);
        tvGender = view.findViewById(R.id.tv_gender);
        tvBirthday = view.findViewById(R.id.tv_birthday);
        btnEdit = view.findViewById(R.id.btn_edit_profile);
    }

    private void loadUserProfile() {
        String token = Constants.getToken(requireContext());

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để xem thông tin cá nhân.", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "🧪 Token lấy từ SharedPreferences: " + token);

        ApiService apiService = RetrofitClient.getApiService(token);

        // Thay đổi kiểu dữ liệu trong Callback thành ApiResponse<User>
        apiService.getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                Log.d(TAG, "📥 onResponse - code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();

                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        User user = apiResponse.getData();
                        bindUserToUI(user);
                    } else {
                        // Xử lý trường hợp success=false từ server
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi khi lấy dữ liệu profile";
                        Toast.makeText(requireContext(), "❌ " + message, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API Response: " + message);
                    }
                } else {
                    // Xử lý lỗi HTTP (401, 404, 500,...)
                    Toast.makeText(requireContext(), "❌ Lỗi kết nối hoặc phản hồi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "❌ Chi tiết lỗi: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "❌ Không đọc được errorBody", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(requireContext(), "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "❌ onFailure: " + t.getMessage(), t);
            }
        });
    }

    private void bindUserToUI(User user) {
        tvFullName.setText(user.getName() != null ? user.getName() : "Không có tên");
        tvEmail.setText(user.getEmail() != null ? user.getEmail() : "Không có email");
        tvPhone.setText(user.getPhone() != null ? String.valueOf(user.getPhone()) : "Không có số điện thoại");
        tvAddress.setText(user.getAddress() != null ? user.getAddress() : "Chưa có địa chỉ");
        tvGender.setText(user.getGender() != null ? user.getGender() : "Không chia sẻ");

        if (user.getBirthday() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            String formattedDate = sdf.format(user.getBirthday());
            tvBirthday.setText(formattedDate);
        } else {
            tvBirthday.setText("Chưa có ngày sinh");
        }

        if (user.getAvatar_url() != null && !user.getAvatar_url().isEmpty()) {
            Glide.with(this)
                    .load(user.getAvatar_url())
                    .placeholder(R.drawable.avatar_circle)
                    .error(R.drawable.avatar_circle)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.avatar_circle);
        }
    }
}