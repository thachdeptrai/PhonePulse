package com.phoneapp.phonepulse.FRAGMENT;

import android.app.AlertDialog; // THÊM MỚI
import android.content.Context;
import android.content.DialogInterface; // THÊM MỚI
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// import android.widget.Button; // btnEdit không được sử dụng, có thể xóa import
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
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.VIEW.ChangePasswordActivity;
import com.phoneapp.phonepulse.VIEW.EditProfileActivity;
import com.phoneapp.phonepulse.VIEW.LoginActivity; // THÊM MỚI
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
    private ImageView btn_settings; // SẼ LÀ NÚT ĐĂNG XUẤT
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
        // SỬA ĐỔI: Thêm kiểm tra getActivity() != null
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
        btn_settings = view.findViewById(R.id.btn_settings); // Đây là ImageView
        history_order_layout = view.findViewById(R.id.history_order_layout);

        tvFullName.setOnClickListener(v -> goToEditProfile());
        tvPhone.setOnClickListener(v -> goToEditProfile());
        tvAddress.setOnClickListener(v -> goToEditProfile());
        tvGender.setOnClickListener(v -> goToEditProfile());
        tvBirthday.setOnClickListener(v -> goToEditProfile());

        LinearLayout changePasswordLayout = view.findViewById(R.id.change_password_layout);
        changePasswordLayout.setOnClickListener(v -> {
            if (getContext() == null) return;
            Intent intent = new Intent(requireContext(), ChangePasswordActivity.class);
            startActivity(intent);
        });

        // THÊM MỚI: Gắn sự kiện click cho btn_settings để đăng xuất
        btn_settings.setOnClickListener(v -> {
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
            return;
        }
        String token = Constants.getToken(requireContext());

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn cần đăng nhập để xem thông tin cá nhân.", Toast.LENGTH_LONG).show();
            // Cân nhắc gọi navigateToLogin() nếu bạn muốn chuyển người dùng đi ngay
            // navigateToLogin();
            return;
        }

        // Tạo instance ApiService CỤC BỘ với token hiện tại
        ApiService localApiService = RetrofitClient.getApiService(token);

        localApiService.getProfile().enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (!isAdded() || getContext() == null) {
                    return;
                }
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<User> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        User user = apiResponse.getData();
                        bindUserToUI(user);
                    } else {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi khi lấy dữ liệu profile";
                        Toast.makeText(requireContext(), "❌ " + message, Toast.LENGTH_SHORT).show();
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
                    return;
                }
                Toast.makeText(requireContext(), "❌ Lỗi kết nối (profile): " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
        editor.putString("phone", String.valueOf(user.getPhone())); // Chuyển sang String nếu getPhone() trả về số
        editor.putString("address", user.getAddress());
        editor.apply();

        Log.d("USER_PREF", "✅ Saved to prefs → Fullname: " + user.getName() +
                ", Phone: " + user.getPhone() +
                ", Address: " + user.getAddress());

        tvFullName.setText(nonNull(user.getName(), "Chưa có tên"));
        tvEmail.setText(nonNull(user.getEmail(), "Không có email"));
        // SỬA ĐỔI: Đảm bảo user.getPhone() được chuyển thành String trước khi gọi nonNull
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
            // SỬA ĐỔI: Kiểm tra this.isAdded() trước khi dùng Glide
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
                // SỬA ĐỔI: Nên dùng getParentFragmentManager() trong Fragment
                FragmentManager fm = getParentFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.fragment_container, fragment) // Đảm bảo R.id.fragment_container là đúng
                        .addToBackStack(null)
                        .commit();

                TextView tvGreeting = getActivity().findViewById(R.id.tv_greeting);
                if (tvGreeting != null) {
                    tvGreeting.setText("Lịch sử đơn hàng");
                }
                EditText etSearch = getActivity().findViewById(R.id.et_search_product);
                if (etSearch != null) {
                    etSearch.setVisibility(View.GONE); // Sửa: Ẩn thanh tìm kiếm khi xem lịch sử
                }
            }
        });
    }

    // --- CÁC PHƯƠNG THỨC ĐĂNG XUẤT ---
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
            return;
        }
        String token = Constants.getToken(requireContext());

        if (token == null || token.isEmpty()) {
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập.", Toast.LENGTH_SHORT).show();
            navigateToLogin();
            return;
        }

        // Tạo instance ApiService CỤC BỘ với token hiện tại
        ApiService logoutApiService = RetrofitClient.getApiService(token);

        // Gọi API logout, truyền "Bearer " + token vào làm giá trị cho header Authorization
        // vì phương thức logout trong ApiService của bạn được định nghĩa với @Header
        Call<ApiResponse> call = logoutApiService.logout("Bearer " + token);

        // (Tùy chọn) Hiển thị ProgressBar
        // ProgressBar progressBarLogout = (getView() != null) ? getView().findViewById(R.id.your_progressbar_id_logout) : null;
        // if(progressBarLogout!=null) progressBarLogout.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                // if(progressBarLogout!=null) progressBarLogout.setVisibility(View.GONE);
                if (!isAdded() || getContext() == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    // Giả sử ApiResponse có trường isSuccess() hoặc một cách để kiểm tra thành công logic
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(requireContext(), "Đăng xuất thành công!", Toast.LENGTH_SHORT).show();
                        clearLocalDataAndNavigate();
                    } else {
                        String message = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Đăng xuất không thành công.";
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                        // Cân nhắc có nên clearLocalDataAndNavigate() ở đây không
                    }
                } else {
                    String errorMsg = "Lỗi khi đăng xuất. Mã lỗi: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Không đọc được errorBody khi logout", e);
                    }
                    Log.e(TAG, "API đăng xuất thất bại (HTTP error): " + errorMsg);
                    Toast.makeText(requireContext(), "Lỗi khi đăng xuất, vui lòng thử lại.", Toast.LENGTH_LONG).show();
                    // Cân nhắc có nên clearLocalDataAndNavigate() ở đây không
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // if(progressBarLogout!=null) progressBarLogout.setVisibility(View.GONE);
                if (!isAdded() || getContext() == null) {
                    return;
                }
                Toast.makeText(requireContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_LONG).show();
                // Cân nhắc có nên clearLocalDataAndNavigate() ở đây không
            }
        });
    }

    private void clearLocalDataAndNavigate() {
        if (getContext() == null) {
            return;
        }
        SharedPreferences tokenPrefs = requireContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor tokenEditor = tokenPrefs.edit();
        tokenEditor.remove(Constants.TOKEN_KEY); // SỬ DỤNG ĐÚNG HẰNG SỐ CỦA BẠN
        tokenEditor.apply();

        SharedPreferences userPrefs = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor userEditor = userPrefs.edit();
        userEditor.clear(); // Xóa tất cả dữ liệu trong "user_prefs"
        userEditor.apply();


        // 2. (Tùy chọn) Xóa dữ liệu trong CartManager hoặc các Singleton khác
        // ví dụ: CartManager.getInstance().clearCartData();

        // 3. Điều hướng về màn hình Đăng nhập
        navigateToLogin();
    }

    private void navigateToLogin() {
        if (getActivity() == null) {
            return;
        }
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish(); // Kết thúc Activity chứa Fragment này
    }



}

