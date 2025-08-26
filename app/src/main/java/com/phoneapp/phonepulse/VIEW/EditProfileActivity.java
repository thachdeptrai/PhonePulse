package com.phoneapp.phonepulse.VIEW;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.phoneapp.phonepulse.R;
import com.phoneapp.phonepulse.Response.ApiResponse;
import com.phoneapp.phonepulse.data.api.ApiService;
import com.phoneapp.phonepulse.data.api.RetrofitClient;
import com.phoneapp.phonepulse.models.User;
import com.phoneapp.phonepulse.utils.Constants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {


    private EditText edtFullName, edtPhone, edtAddress, edtBirthDate;
    private RadioGroup rgGender;
    private RadioButton rbMale, rbFemale, rbOther;
    private Button btnSave;
    private Toolbar toolbar;

    private User currentUser; // Dữ liệu user hiện tại

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);

        // Ánh xạ view
        toolbar = findViewById(R.id.toolbarEdit);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtAddress = findViewById(R.id.edtAddress);
        edtBirthDate = findViewById(R.id.edtBirthDate);
        rgGender = findViewById(R.id.rgGender);
        rbMale = findViewById(R.id.rbMale);
        rbFemale = findViewById(R.id.rbFemale);
        rbOther = findViewById(R.id.rbOther);
        btnSave = findViewById(R.id.btnSave);

        // Toolbar back
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // ✅ Lấy dữ liệu user từ Intent (dưới dạng JSON)
        String userJson = getIntent().getStringExtra("user_json");
        if (userJson != null) {
            currentUser = new com.google.gson.Gson().fromJson(userJson, User.class);
            setUserData(currentUser); // Hiển thị lên form
        }

        // Mở DatePicker khi bấm chọn ngày sinh
        edtBirthDate.setOnClickListener(v -> showDatePicker());

        // Xử lý lưu thông tin khi nhấn nút Lưu
        btnSave.setOnClickListener(v -> updateUser());
    }


    private void setUserData(User user) {
        edtFullName.setText(user.getName());
        edtPhone.setText(user.getPhone());
        edtAddress.setText(user.getAddress());

        if (user.getBirthday() != null && !user.getBirthday().isEmpty()) {
            String rawBirthday = user.getBirthday();
            Date parsedDate = null;

            // Thử parse ISO 8601 trước
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            isoFormat.setLenient(false);

            try {
                parsedDate = isoFormat.parse(rawBirthday);
            } catch (ParseException e) {
                // fallback parse yyyy-MM-dd
                try {
                    SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    parsedDate = simpleFormat.parse(rawBirthday.split("T")[0]);
                } catch (ParseException e2) {
                    e2.printStackTrace();
                }
            }

            if (parsedDate != null) {
                SimpleDateFormat sdfOutput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                edtBirthDate.setText(sdfOutput.format(parsedDate));
            } else {
                edtBirthDate.setText("");
            }
        } else {
            edtBirthDate.setText("");
        }

        switch (user.getGender()) {
            case "Nam":
                rbMale.setChecked(true);
                break;
            case "Nữ":
                rbFemale.setChecked(true);
                break;
            default:
                rbOther.setChecked(true);
                break;
        }
    }


    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        // Lấy ngày sinh từ edtBirthDate (dd/MM/yyyy)
        String birthDateStr = edtBirthDate.getText().toString();
        if (!birthDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Date date = sdf.parse(birthDateStr);
                calendar.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month1 + 1, year1);
                    edtBirthDate.setText(date);
                },
                year, month, day
        );
        dialog.show();
    }

    private void updateUser() {
        String name = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String birthDateStr = edtBirthDate.getText().toString().trim(); // dd/MM/yyyy

        // Giới tính
        String gender;
        int checkedId = rgGender.getCheckedRadioButtonId();
        if (checkedId == R.id.rbMale) gender = "Nam";
        else if (checkedId == R.id.rbFemale) gender = "Nữ";
        else gender = "Khác";

        // Convert dd/MM/yyyy -> yyyy-MM-dd
        String formattedBirthday = null;
        if (!birthDateStr.isEmpty()) {
            try {
                SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                SimpleDateFormat sdfOutput = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date date = sdfInput.parse(birthDateStr);
                formattedBirthday = sdfOutput.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // ✅ Tạo object User
        User user = new User();
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        user.setGender(gender);
        user.setBirthday(formattedBirthday);

        String token = Constants.getToken(this);
        ApiService apiService = RetrofitClient.getApiService(token);

        apiService.updateProfile(user).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                    // Trả data mới về cho ProfileActivity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("updatedUser", response.body().getData());
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
}