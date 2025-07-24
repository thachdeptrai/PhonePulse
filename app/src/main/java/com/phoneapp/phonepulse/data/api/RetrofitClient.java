package com.phoneapp.phonepulse.data.api;

import com.phoneapp.phonepulse.utils.Constants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // Không cần biến static 'retrofit' ở đây nếu bạn muốn token luôn được cập nhật.
    // Mỗi lần gọi getApiService, chúng ta sẽ xây dựng một Retrofit instance mới
    // với OkHttpClient được cấu hình lại theo token truyền vào.

    /**
     * Cung cấp một instance của ApiService đã được cấu hình sẵn với token được truyền vào.
     * Phương thức này sẽ tạo một OkHttpClient mới mỗi lần được gọi, đảm bảo header Authorization
     * được cập nhật với token hiện tại.
     *
     * @param token Chuỗi token xác thực. Có thể là null hoặc rỗng nếu không cần xác thực.
     * @return Một instance của ApiService.
     */
    public static ApiService getApiService(String token) { // Nhận String token trực tiếp
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

        // Cấu hình timeout từ Constants
        httpClient.connectTimeout(Constants.TIMEOUT, TimeUnit.SECONDS);
        httpClient.readTimeout(Constants.TIMEOUT, TimeUnit.SECONDS);
        httpClient.writeTimeout(Constants.TIMEOUT, TimeUnit.SECONDS);

        // Thêm logging interceptor để debug
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // BODY để xem request/response body
        httpClient.addInterceptor(loggingInterceptor);

        // Thêm interceptor để gắn header Authorization và Content-Type
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            // Chỉ gắn token nếu nó tồn tại và không rỗng
            if (token != null && !token.isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }

            Request request = requestBuilder.method(original.method(), original.body()).build();
            return chain.proceed(request);
        });

        // Xây dựng Retrofit instance.
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        return retrofit.create(ApiService.class);
    }
}