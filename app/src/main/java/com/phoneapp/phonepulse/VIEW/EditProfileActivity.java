package com.phoneapp.phonepulse.VIEW;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.District;
import com.phoneapp.phonepulse.models.Province;
import com.phoneapp.phonepulse.models.User;
import com.phoneapp.phonepulse.models.Ward;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtStreet, edtBirthDate;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private Button btnSave;
    private Toolbar toolbar;

    private User currentUser;
    private Spinner spProvince, spDistrict, spWard;

    // Danh sách dữ liệu
    private List<Province> provinceList;
    private List<District> districtList;
    private List<Ward> wardList;

    private final SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private final SimpleDateFormat sdfBackend = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

    // ===== API cho địa phương =====
    private static final String BASE_URL = "https://provinces.open-api.vn/api/";
    private ApiService locationApi;

    // Biến để theo dõi trạng thái tải địa chỉ
    private boolean isSettingInitialAddress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ view
        toolbar = findViewById(R.id.toolbarEdit);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtStreet = findViewById(R.id.edtStreet);
        edtBirthDate = findViewById(R.id.edtBirthDate);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbOther = findViewById(R.id.rbOther);
        btnSave = findViewById(R.id.btnSave);
        spProvince = findViewById(R.id.spProvince);
        spDistrict = findViewById(R.id.spDistrict);
        spWard = findViewById(R.id.spWard);

        // Toolbar back
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Retrofit cho Location API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        locationApi = retrofit.create(ApiService.class);

        // Nhận user từ Intent
        String userJson = getIntent().getStringExtra("user_json");
        if (!TextUtils.isEmpty(userJson)) {
            currentUser = new Gson().fromJson(userJson, User.class);
            setUserData(currentUser);
        }

        // Chọn ngày sinh
        edtBirthDate.setOnClickListener(v -> showDatePicker());

        // Lưu thay đổi
        btnSave.setOnClickListener(v -> updateUser());

        // Load Provinces từ API
        loadProvinces();
    }

    // ===== Load dữ liệu Location =====
    private void loadProvinces() {
        locationApi.getProvinces().enqueue(new Callback<List<Province>>() {
            @Override
            public void onResponse(Call<List<Province>> call, Response<List<Province>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    provinceList = response.body();
                    List<String> names = new ArrayList<>();
                    for (Province p : provinceList) names.add(p.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfileActivity.this,
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spProvince.setAdapter(adapter);

                    // Thiết lập trạng thái để không gọi API khi người dùng ban đầu được chọn
                    isSettingInitialAddress = true;

                    // Đặt lại vị trí nếu đã có dữ liệu
                    if (currentUser != null && !TextUtils.isEmpty(currentUser.getAddress())) {
                        String[] addressParts = currentUser.getAddress().split(", ");
                        if (addressParts.length >= 4) {
                            String provinceName = addressParts[3];
                            int provincePosition = names.indexOf(provinceName);
                            if (provincePosition != -1) {
                                spProvince.setSelection(provincePosition);
                                loadDistricts(provinceList.get(provincePosition).getCode());
                            }
                        }
                    }

                    spProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            // Chỉ load districts khi người dùng tự thay đổi
                            if (!isSettingInitialAddress) {
                                spDistrict.setAdapter(null);
                                spWard.setAdapter(null);
                                loadDistricts(provinceList.get(pos).getCode());
                            } else {
                                isSettingInitialAddress = false;
                            }
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Province>> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi tải Tỉnh", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDistricts(int provinceCode) {
        locationApi.getProvinceDetail(provinceCode).enqueue(new Callback<Province>() {
            @Override
            public void onResponse(Call<Province> call, Response<Province> response) {
                if (response.isSuccessful() && response.body() != null) {
                    districtList = response.body().getDistricts();
                    List<String> names = new ArrayList<>();
                    for (District d : districtList) names.add(d.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfileActivity.this,
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spDistrict.setAdapter(adapter);

                    if (currentUser != null && !TextUtils.isEmpty(currentUser.getAddress())) {
                        String[] addressParts = currentUser.getAddress().split(", ");
                        if (addressParts.length >= 3) {
                            String districtName = addressParts[2];
                            int districtPosition = names.indexOf(districtName);
                            if (districtPosition != -1) {
                                spDistrict.setSelection(districtPosition);
                                loadWards(districtList.get(districtPosition).getCode());
                            }
                        }
                    }

                    spDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            if (!isSettingInitialAddress) {
                                spWard.setAdapter(null);
                                loadWards(districtList.get(pos).getCode());
                            }
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }
            @Override
            public void onFailure(Call<Province> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi tải Huyện", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadWards(int districtCode) {
        locationApi.getDistrictDetail(districtCode).enqueue(new Callback<District>() {
            @Override
            public void onResponse(Call<District> call, Response<District> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wardList = response.body().getWards();
                    List<String> names = new ArrayList<>();
                    for (Ward w : wardList) names.add(w.getName());

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(EditProfileActivity.this,
                            android.R.layout.simple_spinner_item, names);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spWard.setAdapter(adapter);

                    if (currentUser != null && !TextUtils.isEmpty(currentUser.getAddress())) {
                        String[] addressParts = currentUser.getAddress().split(", ");
                        if (addressParts.length >= 2) {
                            String wardName = addressParts[1];
                            int wardPosition = names.indexOf(wardName);
                            if (wardPosition != -1) {
                                spWard.setSelection(wardPosition);
                            }
                        }
                    }

                    spWard.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                            // Xử lý khi xã/phường được chọn
                        }
                        @Override public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }
            @Override
            public void onFailure(Call<District> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi tải Xã", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ======= User Data =======
    private void setUserData(User user) {
        if (user == null) {
            return;
        }

        edtFullName.setText(user.getName());

        // Logic để thêm số 0 vào đầu số điện thoại nếu cần
        String phone = user.getPhone();
        if (phone != null && phone.length() == 9) {
            edtPhone.setText("0" + phone);
        } else {
            edtPhone.setText(phone);
        }

        // Cố gắng phân tích và hiển thị địa chỉ đã có, nếu không thì để trống
        String[] addressParts = user.getAddress() != null ? user.getAddress().split(", ") : null;
        if (addressParts != null && addressParts.length > 0) {
            edtStreet.setText(addressParts[0]);
        }

        // Thiết lập giới tính
        if ("Nam".equalsIgnoreCase(user.getGender())) {
            rbMale.setChecked(true);
        } else if ("Nữ".equalsIgnoreCase(user.getGender())) {
            rbFemale.setChecked(true);
        } else if ("Khác".equalsIgnoreCase(user.getGender())) {
            rbOther.setChecked(true);
        }

        // Thiết lập ngày sinh
        if (!TextUtils.isEmpty(user.getBirthday())) {
            try {
                Date date = sdfBackend.parse(user.getBirthday());
                if (date != null) {
                    edtBirthDate.setText(sdfInput.format(date));
                }
            } catch (ParseException e) {
                Log.e("EditProfile", "Lỗi phân tích ngày sinh từ server: " + e.getMessage());
            }
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String birthDateStr = edtBirthDate.getText().toString();
        if (!TextUtils.isEmpty(birthDateStr)) {
            try {
                Date date = sdfInput.parse(birthDateStr);
                if (date != null) calendar.setTime(date);
            } catch (ParseException ignored) {}
        }
        new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", d, m + 1, y);
            edtBirthDate.setText(date);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateUser() {
        String name = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String street = edtStreet.getText().toString().trim();
        String birthDateStr = edtBirthDate.getText().toString().trim();

        boolean isValid = true;

        edtFullName.setError(null);
        edtPhone.setError(null);
        edtStreet.setError(null);
        edtBirthDate.setError(null);

        if (TextUtils.isEmpty(name)) {
            edtFullName.setError("Vui lòng nhập họ tên");
            isValid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            edtPhone.setError("Vui lòng nhập số điện thoại");
            isValid = false;
        } else if (!phone.matches("0\\d{9}")) {
            edtPhone.setError("Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)");
            isValid = false;
        }

        if (TextUtils.isEmpty(street)) {
            edtStreet.setError("Vui lòng nhập số nhà/thôn/xóm");
            isValid = false;
        }

        String formattedBirthday = null;
        if (TextUtils.isEmpty(birthDateStr)) {
            edtBirthDate.setError("Vui lòng chọn ngày sinh");
            isValid = false;
        } else {
            try {
                Date date = sdfInput.parse(birthDateStr);
                if (date != null && !date.after(new Date())) {
                    formattedBirthday = sdfOutput.format(date);
                } else {
                    edtBirthDate.setError("Ngày sinh không hợp lệ");
                    isValid = false;
                }
            } catch (ParseException e) {
                edtBirthDate.setError("Ngày sinh không hợp lệ");
                isValid = false;
            }
        }

        String gender = null;
        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show();
            isValid = false;
        } else if (checkedId == R.id.rbMale) {
            gender = "Nam";
        } else if (checkedId == R.id.rbFemale) {
            gender = "Nữ";
        } else if (checkedId == R.id.rbOther) {
            gender = "Khác";
        }

        if (spProvince.getSelectedItem() == null || spDistrict.getSelectedItem() == null || spWard.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng chọn đầy đủ Tỉnh/Thành phố, Quận/Huyện, Xã/Phường", Toast.LENGTH_LONG).show();
            isValid = false;
        }

        if (!isValid) return;

        String province = spProvince.getSelectedItem().toString();
        String district = spDistrict.getSelectedItem().toString();
        String ward = spWard.getSelectedItem().toString();

        StringBuilder fullAddress = new StringBuilder();
        fullAddress.append(street)
                .append(", ").append(ward)
                .append(", ").append(district)
                .append(", ").append(province);

        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(fullAddress.toString());
        user.setGender(gender);
        user.setBirthday(formattedBirthday);

        String token = Constants.getToken(this);
        ApiService apiService = RetrofitClient.getApiService(token);
        apiService.updateProfile(user).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    User updatedUser = response.body().getData();
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    saveUpdatedUserToPrefs(updatedUser);

                    Intent resultIntent = new Intent();
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Lỗi cập nhật", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUpdatedUserToPrefs(User user) {
        if (user == null) return;
        SharedPreferences prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fullname", user.getName());
        editor.putString("phone", user.getPhone());
        editor.putString("address", user.getAddress());
        editor.putString("gender", user.getGender());
        editor.putString("birthday", user.getBirthday());
        editor.apply();
    }
}