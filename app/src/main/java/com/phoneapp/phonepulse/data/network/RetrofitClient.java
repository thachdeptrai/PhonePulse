package com.phoneapp.phonepulse.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    // ✅ Sử dụng 10.0.2.2 để kết nối từ emulator đến localhost của máy thật
    private static final String BASE_URL = "http://10.0.2.2:5000/";
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static UserAPI_Service getUserService() {
        return getClient().create(UserAPI_Service.class);
    }
}
