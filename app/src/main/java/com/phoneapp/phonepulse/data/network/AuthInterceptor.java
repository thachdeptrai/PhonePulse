package com.phoneapp.phonepulse.data.network;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private final SharedPreferences sharedPreferences;

    @Inject
    public AuthInterceptor(Context context) {
        // Lấy SharedPreferences tên "app_prefs" ở chế độ private
        sharedPreferences = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        // Lấy token đã lưu trong SharedPreferences (nếu có)
        String token = sharedPreferences.getString("auth_token", null);

        // Lấy request hiện tại từ chuỗi xử lý
        Request request = chain.request();

        // Nếu có token, thêm header "Authorization" vào request với định dạng "Bearer <token>"
        if (token != null) {
            request = request.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        }

        // Tiếp tục gửi request (có thể đã được thêm header)
        return chain.proceed(request);
    }

}
