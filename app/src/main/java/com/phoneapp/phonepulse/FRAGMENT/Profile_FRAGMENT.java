package com.phoneapp.phonepulse.FRAGMENT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse; // Import ApiResponse
import com.phoneapp.phonepulse.VIEW.ChangePasswordActivity;
import com.phoneapp.phonepulse.VIEW.EditProfileActivity;
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
    private TextView tvFullName, tvEmail, tvPhone, tvAddress, tvGender, tvBirthday, tvRole;
    private Button btnEdit;
    private ImageView btn_settings;
    private LinearLayout history_order_layout;
    private static final String TAG = "ProfileFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        initViews(view);
        loadUserProfile();
        NextHistory_Oder();

        return view;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK && data != null) {
            // Chỉnh sửa thành công → cập nhật giao diện
            loadUserProfile(); // Gọi lại API để lấy dữ liệu mới nhất
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

        // Gắn sự kiện click vào các TextView
        tvFullName.setOnClickListener(v -> goToEditProfile());
        tvPhone.setOnClickListener(v -> goToEditProfile());
        tvAddress.setOnClickListener(v -> goToEditProfile());
        tvGender.setOnClickListener(v -> goToEditProfile());
        tvBirthday.setOnClickListener(v -> goToEditProfile());
        //mmở dialog đổi mật khẩu
        LinearLayout changePasswordLayout = view.findViewById(R.id.change_password_layout);
        changePasswordLayout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

    }

    private void goToEditProfile() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Chưa có dữ liệu user để chỉnh sửa", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(requireContext(), EditProfileActivity.class);
        String userJson = new com.google.gson.Gson().toJson(currentUser);
        intent.putExtra("user_json", userJson);
        startActivityForResult(intent, 1001);
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
        this.currentUser = user;

        Context context = requireContext();
        SharedPreferences preferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("fullname", user.getName());
        editor.putString("phone", String.valueOf(user.getPhone()));
        editor.putString("address", user.getAddress());
        editor.apply();

        Log.d("USER_PREF", "✅ Saved to prefs → Fullname: " + user.getName() +
                ", Phone: " + user.getPhone() +
                ", Address: " + user.getAddress());

        tvFullName.setText(nonNull(user.getName(), "Không có tên"));
        tvEmail.setText(nonNull(user.getEmail(), "Không có email"));
        tvPhone.setText(nonNull(user.getPhone(), "Không có số điện thoại"));
        tvAddress.setText(nonNull(user.getAddress(), "Chưa có địa chỉ"));
        tvGender.setText(nonNull(user.getGender(), "Không chia sẻ"));

//        // ✅ Đoạn sửa lỗi
//        if (!TextUtils.isEmpty(user.getBirthday())) {
//            tvBirthday.setText(user.getBirthday());
//        } else {
//            tvBirthday.setText("Chưa có ngày sinh");
//        }
        if (!TextUtils.isEmpty(user.getBirthday())) {
            String rawBirthday = user.getBirthday();
            String formattedDate;

            try {
                // Parse chuỗi ngày ISO từ backend
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                isoFormat.setLenient(false);  // Giúp phát hiện lỗi định dạng
                java.util.Date date = isoFormat.parse(rawBirthday);

                // Format lại kiểu bạn muốn (ví dụ: "dd/MM/yyyy")
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                formattedDate = outputFormat.format(date);
            } catch (Exception e) {
                formattedDate = rawBirthday.split("T")[0]; // fallback: chỉ lấy phần yyyy-MM-dd nếu lỗi
            }

            tvBirthday.setText(formattedDate);
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




    // Hàm tiện ích tránh lặp null check
    private String nonNull(String value, String fallback) {
        return (value != null && !value.trim().isEmpty()) ? value : fallback;
    }

    private void NextHistory_Oder() {
        history_order_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OrderHistory_FRAGMENT fragment = new OrderHistory_FRAGMENT();
                FragmentManager fm = getSupportFragmentManager();
                if (fm != null) {
                    fm.beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }

                // Cập nhật tiêu đề toolbar
                TextView tvGreeting = requireActivity().findViewById(R.id.tv_greeting);
                if (tvGreeting != null) {
                    tvGreeting.setText("Lịch sử đơn hàng");
                }

                // Ẩn thanh tìm kiếm nếu có
                EditText etSearch = requireActivity().findViewById(R.id.et_search_product);
                if (etSearch != null) {
                    etSearch.setVisibility(View.VISIBLE);
                }
            }
        });
    }





    private FragmentManager getSupportFragmentManager() {
        if (getActivity() != null) {
            return getActivity().getSupportFragmentManager();
        } else {
            Log.e(TAG, "❌ getActivity() is null, cannot get FragmentManager");
            return null;
        }
    }


}