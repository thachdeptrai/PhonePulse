package com.phoneapp.phonepulse.FRAGMENT;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.VIEW.ChangePasswordActivity;
import com.phoneapp.phonepulse.VIEW.EditProfileActivity;
import com.phoneapp.phonepulse.VIEW.LoginActivity;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.User;
import com.phoneapp.phonepulse.utils.Constants;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Profile_FRAGMENT extends Fragment {
    private User currentUser;
    private CircleImageView imgAvatar;
    private TextView tvFullName, tvEmail, tvPhone, tvAddress, tvGender, tvBirthday;
    private ImageView btn_settings;
    private LinearLayout history_order_layout;
    private LinearLayout personalInfoHeader, detailsLayout;
    private ImageView ivExpandDetails;

    private static final String TAG = "ProfileFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_fragment, container, false);
        initViews(view);

        // ✅ Load từ SharedPreferences trước (hiển thị ngay)
        loadUserFromPrefs();

        // ✅ Sau đó gọi API để cập nhật dữ liệu mới nhất
        loadUserProfile();

        NextHistory_Oder();
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && getActivity() != null && resultCode == getActivity().RESULT_OK && data != null) {
            loadUserProfile();
        }
    }

    private void initViews(View view) {
        imgAvatar = view.findViewById(R.id.img_avatar);
        tvFullName = view.findViewById(R.id.tv_fullname);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPhone = view.findViewById(R.id.tv_phone);
        tvAddress = view.findViewById(R.id.tv_address);
        tvGender = view.findViewById(R.id.tv_gender);
        tvBirthday = view.findViewById(R.id.tv_birthday);
        btn_settings = view.findViewById(R.id.btn_settings);
        history_order_layout = view.findViewById(R.id.history_order_layout);

        personalInfoHeader = view.findViewById(R.id.personal_info_header);
        detailsLayout = view.findViewById(R.id.details_layout);
        ivExpandDetails = view.findViewById(R.id.iv_expand_details);

        // Edit Profile
        tvFullName.setOnClickListener(v -> goToEditProfile());
        tvPhone.setOnClickListener(v -> goToEditProfile());
        tvAddress.setOnClickListener(v -> goToEditProfile());
        tvGender.setOnClickListener(v -> goToEditProfile());
        tvBirthday.setOnClickListener(v -> goToEditProfile());

        // Đổi mật khẩu
        LinearLayout changePasswordLayout = view.findViewById(R.id.change_password_layout);
        changePasswordLayout.setOnClickListener(v -> {
            if (getContext() == null) return;
            startActivity(new Intent(requireContext(), ChangePasswordActivity.class));
        });

        // Logout
        btn_settings.setOnClickListener(v -> showLogoutConfirmationDialog());

        // Expand/Collapse personal info
        personalInfoHeader.setOnClickListener(v -> {
            if (detailsLayout.getVisibility() == View.GONE) {
                detailsLayout.setVisibility(View.VISIBLE);
            } else {
                detailsLayout.setVisibility(View.GONE);
            }
        });
    }

    private void goToEditProfile() {
        if (getContext() == null) return;
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Chưa có dữ liệu user để chỉnh sửa", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(requireContext(), EditProfileActivity.class);
        intent.putExtra("user_json", new Gson().toJson(currentUser));
        startActivityForResult(intent, 1001);
    }

    private void loadUserFromPrefs() {
        if (getContext() == null) return;
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);

        tvFullName.setText(prefs.getString("fullname", "Chưa có tên"));
        tvEmail.setText(prefs.getString("email", "Không có email"));
        tvPhone.setText(prefs.getString("phone", "Không có số điện thoại"));
        tvAddress.setText(prefs.getString("address", "Chưa có địa chỉ"));
        tvGender.setText(prefs.getString("gender", "Không chia sẻ"));
        tvBirthday.setText(prefs.getString("birthday", "Chưa có ngày sinh"));

        String avatar = prefs.getString("avatar_url", "");
        if (!TextUtils.isEmpty(avatar)) {
            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.avatar_circle)
                    .error(R.drawable.avatar_circle)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.avatar_circle);
        }
    }

    private void loadUserProfile() {
        if (getContext() == null) return;
        String token = Constants.getToken(requireContext());

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để xem thông tin cá nhân.", Toast.LENGTH_LONG).show();
            return;
        }

        ApiService localApiService = RetrofitClient.getApiService(token);
        localApiService.getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        bindUserToUI(apiResponse.getData());
                    } else {
                        Toast.makeText(requireContext(), "❌ " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "❌ Lỗi server: " + response.code(), Toast.LENGTH_SHORT).show();
                    try {
                        if (response.errorBody() != null) {
                            Log.e(TAG, "Error: " + response.errorBody().string());
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Không đọc được errorBody", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(), "❌ Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bindUserToUI(User user) {
        if (getContext() == null) return;
        this.currentUser = user;

        // Lưu lại vào SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fullname", user.getName());
        editor.putString("email", user.getEmail());
        editor.putString("phone", user.getPhone());
        editor.putString("address", user.getAddress());
        editor.putString("gender", user.getGender());
        editor.putString("birthday", user.getBirthday());
        editor.putString("avatar_url", user.getAvatar_url());
        editor.apply();

        tvFullName.setText(nonNull(user.getName(), "Chưa có tên"));
        tvEmail.setText(nonNull(user.getEmail(), "Không có email"));
        tvPhone.setText(nonNull(user.getPhone(), "Không có số điện thoại"));
        tvAddress.setText(nonNull(user.getAddress(), "Chưa có địa chỉ"));
        tvGender.setText(nonNull(user.getGender(), "Không chia sẻ"));

        if (!TextUtils.isEmpty(user.getBirthday())) {
            String rawBirthday = user.getBirthday();
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                java.util.Date date = isoFormat.parse(rawBirthday);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                tvBirthday.setText(outputFormat.format(date));
            } catch (Exception e) {
                tvBirthday.setText(rawBirthday.split("T")[0]);
            }
        } else {
            tvBirthday.setText("Chưa có ngày sinh");
        }

        if (!TextUtils.isEmpty(user.getAvatar_url())) {
            Glide.with(this)
                    .load(user.getAvatar_url())
                    .placeholder(R.drawable.avatar_circle)
                    .error(R.drawable.avatar_circle)
                    .into(imgAvatar);
        } else {
            imgAvatar.setImageResource(R.drawable.avatar_circle);
        }
    }

    private String nonNull(String value, String fallback) {
        return (value != null && !value.trim().isEmpty()) ? value : fallback;
    }

    private void NextHistory_Oder() {
        history_order_layout.setOnClickListener(v -> {
            if (getActivity() == null) return;
            OrderHistory_FRAGMENT fragment = new OrderHistory_FRAGMENT();
            FragmentManager fm = getParentFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

            TextView tvGreeting = getActivity().findViewById(R.id.tv_greeting);
            if (tvGreeting != null) tvGreeting.setText("Lịch sử đơn hàng");

            EditText etSearch = getActivity().findViewById(R.id.et_search_product);
            if (etSearch != null) etSearch.setVisibility(View.GONE);
        });
    }

    private void showLogoutConfirmationDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> performLogout())
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performLogout() {
        if (getContext() == null) return;
        String token = Constants.getToken(requireContext());

        if (token == null || token.isEmpty()) {
            navigateToLogin();
            return;
        }

        ApiService logoutApiService = RetrofitClient.getApiService(token);
        logoutApiService.logout("Bearer " + token).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (!isAdded() || getContext() == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    clearLocalDataAndNavigate();
                } else {
                    Toast.makeText(requireContext(), "Đăng xuất thất bại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearLocalDataAndNavigate() {
        if (getContext() == null) return;

        // Xóa token
        SharedPreferences tokenPrefs = requireContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        tokenPrefs.edit().remove(Constants.TOKEN_KEY).apply();

        // Xóa user_prefs
        SharedPreferences userPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        userPrefs.edit().clear().apply();

        navigateToLogin();
    }

    private void navigateToLogin() {
        if (getActivity() == null) return;
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
