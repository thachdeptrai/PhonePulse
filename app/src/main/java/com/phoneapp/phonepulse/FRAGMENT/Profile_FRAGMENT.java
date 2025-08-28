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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView; // THÊM MỚI
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
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
    private Button btnEdit; // Không thấy sử dụng
    private ImageView btn_settings; // Nút đăng xuất
    private CardView history_order_layout; // SỬA: Từ LinearLayout thành CardView
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
        history_order_layout = view.findViewById(R.id.history_order_layout); // SỬA: Ép kiểu thành CardView

        tvFullName.setOnClickListener(v -> goToEditProfile());
        tvPhone.setOnClickListener(v -> goToEditProfile());
        tvAddress.setOnClickListener(v -> goToEditProfile());
        tvGender.setOnClickListener(v -> goToEditProfile());
        tvBirthday.setOnClickListener(v -> goToEditProfile());

        CardView changePasswordLayout = view.findViewById(R.id.change_password_layout); // SỬA: Từ LinearLayout thành CardView
        changePasswordLayout.setOnClickListener(v -> {
            if (getContext() == null) return;
            Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        btn_settings.setOnClickListener(v -> {
            Log.d(TAG, "Nút cài đặt (đăng xuất) được nhấn.");
            showLogoutConfirmationDialog();
        });
    }

    private void goToEditProfile() {
        if (getContext() == null) return;
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
        if (getContext() == null) {
            Log.w(TAG, "loadUserProfile: Context is null, cannot proceed.");
            return;
        }
        String token = Constants.getToken(requireContext());

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để xem thông tin cá nhân.", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(TAG, "🧪 Token lấy từ SharedPreferences cho getProfile: " + token);

        ApiService localApiService = RetrofitClient.getApiService(token);

        localApiService.getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "loadUserProfile onResponse: Fragment not added or context is null.");
                    return;
                }
                Log.d(TAG, "📥 getProfile onResponse - code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        User user = apiResponse.getData();
                        bindUserToUI(user);
                    } else {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi khi lấy dữ liệu profile";
                        Toast.makeText(requireContext(), "❌ " + message, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API Response (getProfile): " + message);
                    }
                } else {
                    Toast.makeText(requireContext(), "❌ Lỗi kết nối hoặc phản hồi server (profile): " + response.code(), Toast.LENGTH_SHORT).show();
                    if (response.errorBody() != null) {
                        try {
                            Log.e(TAG, "❌ Chi tiết lỗi (profile): " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e(TAG, "❌ Không đọc được errorBody (profile)", e);
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "loadUserProfile onFailure: Fragment not added or context is null.");
                    return;
                }
                Toast.makeText(requireContext(), "❌ Lỗi kết nối (profile): " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "❌ onFailure (profile): " + t.getMessage(), t);
            }
        });
    }

    private void bindUserToUI(User user) {
        if (getContext() == null) return;
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

        tvFullName.setText(nonNull(user.getName(), "Chưa có tên"));
        tvEmail.setText(nonNull(user.getEmail(), "Không có email"));
        tvPhone.setText(nonNull(user.getPhone() != null ? String.valueOf(user.getPhone()) : null, "Không có số điện thoại"));
        tvAddress.setText(nonNull(user.getAddress(), "Chưa có địa chỉ"));
        tvGender.setText(nonNull(user.getGender(), "Không chia sẻ"));

        if (!TextUtils.isEmpty(user.getBirthday())) {
            String rawBirthday = user.getBirthday();
            String formattedDate;
            try {
                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
                isoFormat.setLenient(false);
                java.util.Date date = isoFormat.parse(rawBirthday);
                SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                formattedDate = outputFormat.format(date);
            } catch (Exception e) {
                Log.w(TAG, "Lỗi định dạng ngày sinh: " + rawBirthday, e);
                formattedDate = rawBirthday.split("T")[0];
            }
            tvBirthday.setText(formattedDate);
        } else {
            tvBirthday.setText("Chưa có ngày sinh");
        }

        if (!TextUtils.isEmpty(user.getAvatar_url())) {
            if (this.isAdded()) {
                Glide.with(this)
                        .load(user.getAvatar_url())
                        .placeholder(R.drawable.avatar_circle)
                        .error(R.drawable.avatar_circle)
                        .into(imgAvatar);
            }
        } else {
            imgAvatar.setImageResource(R.drawable.avatar_circle);
        }
    }

    private String nonNull(String value, String fallback) {
        return (value != null && !value.trim().isEmpty()) ? value : fallback;
    }

    private void NextHistory_Oder() {
        history_order_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() == null) return;
                OrderHistory_FRAGMENT fragment = new OrderHistory_FRAGMENT();
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();

                TextView tvGreeting = getActivity().findViewById(R.id.tv_greeting);
                if (tvGreeting != null) {
                    tvGreeting.setText("Lịch sử đơn hàng");
                }
                EditText etSearch = getActivity().findViewById(R.id.et_search_product);
                if (etSearch != null) {
                    etSearch.setVisibility(View.GONE);
                }
            }
        });
    }

    private void showLogoutConfirmationDialog() {
        if (getContext() == null) {
            Log.w(TAG, "showLogoutConfirmationDialog: Context is null.");
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
                .setPositiveButton("Đăng xuất", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        performLogout();
                    }
                })
                .setNegativeButton("Hủy", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void performLogout() {
        if (getContext() == null) {
            Log.w(TAG, "performLogout: Context is null, cannot proceed.");
            return;
        }
        String token = Constants.getToken(requireContext());

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }
        Log.d(TAG, "🧪 Token sẽ được dùng cho performLogout: " + token);

        ApiService logoutApiService = RetrofitClient.getApiService(token);

        Log.d(TAG, "Đang thực hiện gọi API đăng xuất...");
        Call<ApiResponse> call = logoutApiService.logout("Bearer " + token);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "performLogout onResponse: Fragment not added or context is null.");
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.i(TAG, "Đăng xuất thành công từ server.");
                        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                        clearLocalDataAndNavigate();
                    } else {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đăng xuất không thành công.";
                        Log.w(TAG, "Đăng xuất không thành công từ server (logic error): " + message);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = "Lỗi khi đăng xuất. Mã lỗi: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi đọc errorBody khi đăng xuất", e);
                    }
                    Log.e(TAG, "API đăng xuất thất bại (HTTP error): " + errorMsg);
                    Toast.makeText(requireContext(), "Lỗi khi đăng xuất, vui lòng thử lại.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "performLogout onFailure: Fragment not added or context is null.");
                    return;
                }
                Log.e(TAG, "Lỗi mạng khi đăng xuất: ", t);
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void clearLocalDataAndNavigate() {
        if (getContext() == null) {
            Log.w(TAG, "clearLocalDataAndNavigate: Context is null.");
            return;
        }
        SharedPreferences tokenPrefs = requireContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor tokenEditor = tokenPrefs.edit();
        tokenEditor.remove(Constants.TOKEN_KEY);
        tokenEditor.apply();
        Log.i(TAG, "Token đã được xóa khỏi SharedPreferences: " + Constants.SHARED_PREFS);

        SharedPreferences userPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor userEditor = userPrefs.edit();
        userEditor.clear();
        userEditor.apply();

        Log.i(TAG, "Dữ liệu người dùng cục bộ (token và user_prefs) đã được xóa.");

        navigateToLogin();
    }

    private void navigateToLogin() {
        if (getActivity() == null) {
            Log.w(TAG, "navigateToLogin: Activity is null.");
            return;
        }
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
        Log.i(TAG, "Đã điều hướng đến LoginActivity.");
    }
}